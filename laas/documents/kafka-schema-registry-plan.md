# Kafka Schema Registry 적용 계획

## 개요
현재 JSON 기반 직렬화 방식을 사용하는 Kafka 메시지 시스템에 AWS Glue Schema Registry를 적용하여 스키마 관리, 버전 제어, 타입 안정성을 확보하는 작업 계획

## 현재 상황 분석

### 현재 아키텍처
- **직렬화 방식**: JSON (Jackson ObjectMapper 기반)
- **Producer**: `StringSerializer` → JSON 문자열 전송
- **Consumer**: `StringDeserializer` → 수동 JSON 파싱
- **메시지 구조**: `KafkaEvent<T extends KafkaEventPayload>` 래퍼 클래스

### 현재 구현 위치
```
Producer (메시지 발행):
- order/core-service/.../MessageRelayKafkaConfig.java
- delivery/infrastructure/.../MessageRelayKafkaConfig.java
- dispatch/core/.../MessageRelayKafkaConfig.java

Consumer (메시지 수신):
- read-model/.../KafkaConfig.java
- delivery/api/.../KafkaConsumerConfig.java
- dispatch/api/.../KafkaConsumerConfig.java

이벤트 모델:
- common/event/src/.../KafkaEvent.java
- common/event/src/.../KafkaEventPayload.java
- common/event/src/.../payload/* (각 도메인별 이벤트)
```

### 현재 방식의 한계
- ❌ 스키마 버전 관리 부재 (코드 레벨에서만 관리)
- ❌ 런타임 에러 가능성 (타입 불일치 시)
- ❌ Producer-Consumer 간 스키마 불일치 가능
- ❌ 스키마 변경 시 호환성 검증 없음
- ❌ 문서화 및 거버넌스 부족

## 아키텍처 변경사항

### Before (현재)
```
Producer → JSON Serialization → Kafka → JSON Deserialization → Consumer
              (StringSerializer)              (StringDeserializer)
                    ↓                                  ↓
           KafkaEvent.toJson()                KafkaEvent.fromJson()
```

### After (목표)
```
Producer → Avro Serialization → Kafka → Avro Deserialization → Consumer
         (GlueSchemaRegistry)        (GlueSchemaRegistry)
              ↓                              ↓
        Schema Validation              Schema Retrieval
              ↓                              ↓
        Schema ID 포함 전송            Schema ID로 스키마 조회
```

### 스키마 형식 선택: Avro 권장
- **Avro 장점**:
  - 바이너리 포맷으로 메시지 크기 감소 (30-50% 절약)
  - 스키마 진화 지원 우수
  - 빠른 직렬화/역직렬화 성능
  - AWS Glue와 호환성 좋음

- **대안**: JSON Schema (현재 구조 유지 원할 시)
  - 기존 JSON 구조 유지 가능
  - 마이그레이션 비용 낮음
  - 하지만 성능 개선 효과 제한적

## 작업 단계

### Phase 1: 환경 준비 및 설정 (1주)

#### 1.1 AWS Glue Schema Registry 생성
```bash
# AWS CLI로 Registry 생성
aws glue create-registry \
  --registry-name laas-event-registry \
  --description "LaaS Event Schema Registry" \
  --region ap-northeast-2
```

#### 1.2 IAM 권한 설정
- 필요한 권한:
  - `glue:GetSchemaVersion`
  - `glue:RegisterSchemaVersion`
  - `glue:GetSchema`
  - `glue:CreateSchema`

#### 1.3 Gradle 의존성 추가
```groovy
// common/event/build.gradle에 추가
dependencies {
    // AWS Glue Schema Registry
    implementation 'software.amazon.glue:schema-registry-serde:1.1.18'
    implementation 'software.amazon.glue:schema-registry-kafkaconnect-converter:1.1.18'
    
    // Avro
    implementation 'org.apache.avro:avro:1.11.3'
    
    // Avro 플러그인 (코드 생성)
    id 'com.github.davidmc24.gradle.plugin.avro' version '1.9.1'
    
    // AWS SDK v2
    implementation platform('software.amazon.awssdk:bom:2.20.0')
    implementation 'software.amazon.awssdk:glue'
}
```

#### 1.4 설정 파일 추가
```yaml
# application.yml에 추가
aws:
  region: ap-northeast-2
  glue:
    registry:
      name: laas-event-registry
      auto-registration: true
    schema:
      compatibility: BACKWARD
      compression: ZLIB
```

### Phase 2: Avro 스키마 정의 (2주)

#### 2.1 공통 스키마 정의
`common/event/src/main/avro/KafkaEvent.avsc`:
```json
{
  "type": "record",
  "name": "KafkaEvent",
  "namespace": "vroong.laas.common.event.avro",
  "fields": [
    {"name": "eventId", "type": "string"},
    {"name": "type", "type": "string"},
    {"name": "source", "type": "string"},
    {"name": "timestamp", "type": "long"},
    {"name": "schemaVersion", "type": "string"},
    {"name": "payload", "type": ["null", "bytes"], "default": null}
  ]
}
```

#### 2.2 도메인별 Payload 스키마 정의
예시: `common/event/src/main/avro/OrderCreatedEventPayload.avsc`
```json
{
  "type": "record",
  "name": "OrderCreatedEventPayload",
  "namespace": "vroong.laas.common.event.avro.payload.order",
  "fields": [
    {"name": "orderId", "type": "long"},
    {"name": "userId", "type": "long"},
    {"name": "merchantId", "type": "long"},
    {"name": "pickupAddress", "type": "string"},
    {"name": "deliveryAddress", "type": "string"},
    {"name": "totalPrice", "type": "long"},
    {"name": "orderStatus", "type": "string"},
    {"name": "createdAt", "type": "long"}
  ]
}
```

#### 2.3 스키마 파일 작성 체크리스트
- [ ] KafkaEvent 공통 스키마
- [ ] OrderCreatedEventPayload
- [ ] DispatchDispatchedEventPayload
- [ ] DeliveryStartedEventPayload
- [ ] DeliveryPickedUpEventPayload
- [ ] DeliveryDeliveredEventPayload

#### 2.4 Avro 코드 생성 설정
```groovy
// build.gradle
avro {
    createSetters = true
    fieldVisibility = "PRIVATE"
    outputCharacterEncoding = "UTF-8"
    stringType = "String"
}

generateAvroJava {
    source = file("src/main/avro")
}
```

### Phase 3: Producer 설정 변경 (1주)

#### 3.1 MessageRelayKafkaConfig 수정
각 모듈의 Kafka Config 파일 수정:
- `order/core-service/.../MessageRelayKafkaConfig.java`
- `delivery/infrastructure/.../MessageRelayKafkaConfig.java`
- `dispatch/core/.../MessageRelayKafkaConfig.java`

**변경 내용**:
```java
// Before
configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

// After
configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, 
           GlueSchemaRegistryKafkaSerializer.class);
           
// AWS Glue 설정 추가
configs.put(AWSSchemaRegistryConstants.AWS_REGION, awsRegion);
configs.put(AWSSchemaRegistryConstants.REGISTRY_NAME, registryName);
configs.put(AWSSchemaRegistryConstants.SCHEMA_AUTO_REGISTRATION_SETTING, true);
configs.put(AWSSchemaRegistryConstants.DATA_FORMAT, DataFormat.AVRO.name());
configs.put(AWSSchemaRegistryConstants.AVRO_RECORD_TYPE, 
           AvroRecordType.SPECIFIC_RECORD.getName());
configs.put(AWSSchemaRegistryConstants.COMPATIBILITY_SETTING, 
           Compatibility.BACKWARD);
```

#### 3.2 OutboxEventPayloadGenerator 수정
JSON 문자열 생성 대신 Avro 객체 생성:
```java
// Before
private String generateDeliveryStartedPayload(Delivery delivery) {
    var payload = DeliveryStartedEventPayload.builder()...build();
    var kafkaEvent = getKafkaEvent(DELIVERY_DELIVERY_STARTED, payload);
    return kafkaEvent.toJson(); // String 반환
}

// After
private vroong.laas.common.event.avro.KafkaEvent generateDeliveryStartedPayload(Delivery delivery) {
    var payload = vroong.laas.common.event.avro.payload.delivery.DeliveryStartedEventPayload
        .newBuilder()
        .setDeliveryId(delivery.getId())
        .setOrderId(delivery.getOrderId())
        ...
        .build();
    
    return vroong.laas.common.event.avro.KafkaEvent
        .newBuilder()
        .setEventId(UUID.randomUUID().toString())
        .setType(DELIVERY_DELIVERY_STARTED.getValue())
        .setTimestamp(System.currentTimeMillis())
        .setPayload(ByteBuffer.wrap(payload.toByteBuffer().array()))
        .build();
}
```

#### 3.3 KafkaTemplate 타입 변경
```java
// Before
KafkaTemplate<String, String> messageRelaykafkaTemplate()

// After  
KafkaTemplate<String, SpecificRecord> messageRelaykafkaTemplate()
```

### Phase 4: Consumer 설정 변경 (1주)

#### 4.1 KafkaConsumerConfig 수정
각 모듈의 Consumer Config 파일 수정:
- `read-model/.../KafkaConfig.java`
- `delivery/api/.../KafkaConsumerConfig.java`
- `dispatch/api/.../KafkaConsumerConfig.java`

**변경 내용**:
```java
// Before
configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, 
               StringDeserializer.class);

// After
configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, 
               GlueSchemaRegistryKafkaDeserializer.class);
               
// AWS Glue 설정 추가
configProps.put(AWSSchemaRegistryConstants.AWS_REGION, awsRegion);
configProps.put(AWSSchemaRegistryConstants.REGISTRY_NAME, registryName);
configProps.put(AWSSchemaRegistryConstants.AVRO_RECORD_TYPE, 
               AvroRecordType.SPECIFIC_RECORD.getName());
```

#### 4.2 Consumer 클래스 수정
```java
// Before
@KafkaListener(topics = "${readmodel.topics.delivery}")
public void handleDeliveryEvent(@Payload String message, ...) {
    KafkaEvent<? extends KafkaEventPayload> kafkaEvent = KafkaEvent.fromJson(message);
    ...
}

// After
@KafkaListener(topics = "${readmodel.topics.delivery}")
public void handleDeliveryEvent(
        @Payload vroong.laas.common.event.avro.KafkaEvent message, ...) {
    // 직접 Avro 객체로 수신
    String eventType = message.getType();
    ByteBuffer payloadBuffer = message.getPayload();
    
    // Payload 역직렬화
    DeliveryStartedEventPayload payload = 
        DeliveryStartedEventPayload.fromByteBuffer(payloadBuffer);
    ...
}
```

### Phase 5: 공통 유틸리티 개발 (3일)

#### 5.1 Avro 변환 유틸리티
```java
public class AvroEventConverter {
    
    public static <T extends SpecificRecordBase> T deserializePayload(
            ByteBuffer buffer, Class<T> clazz) {
        // Avro SpecificDatumReader를 사용한 역직렬화
    }
    
    public static ByteBuffer serializePayload(SpecificRecordBase payload) {
        // Avro SpecificDatumWriter를 사용한 직렬화
    }
}
```

#### 5.2 스키마 관리 유틸리티
```java
public class SchemaRegistryClient {
    
    public SchemaMetadata getLatestSchema(String schemaName) {
        // AWS Glue에서 최신 스키마 조회
    }
    
    public void registerSchema(String schemaName, String schemaDefinition) {
        // 스키마 등록
    }
    
    public boolean checkCompatibility(String schemaName, String newSchema) {
        // 호환성 체크
    }
}
```

### Phase 6: 기존 시스템과 병행 운영 (2-3주)

#### 6.1 듀얼 모드 구현
Producer가 JSON과 Avro 모두 발행:
```java
@Configuration
public class DualModeKafkaConfig {
    
    @Bean
    public KafkaTemplate<String, String> jsonKafkaTemplate() {
        // 기존 JSON 방식 (레거시 Consumer용)
    }
    
    @Bean  
    public KafkaTemplate<String, SpecificRecord> avroKafkaTemplate() {
        // 새로운 Avro 방식
    }
}
```

Consumer가 JSON과 Avro 모두 처리:
```java
@KafkaListener(topics = "${readmodel.topics.delivery}")
public void handleDeliveryEvent(@Payload Object message, ...) {
    if (message instanceof String) {
        // 레거시 JSON 처리
        handleJsonMessage((String) message);
    } else if (message instanceof vroong.laas.common.event.avro.KafkaEvent) {
        // 새로운 Avro 처리
        handleAvroMessage((KafkaEvent) message);
    }
}
```

#### 6.2 모니터링 및 점진적 전환
1. Avro 토픽 별도 생성 (예: `delivery-events-avro`)
2. 트래픽 일부만 Avro로 전환 (Feature Flag)
3. 모니터링 후 문제 없으면 점진적 확대
4. 최종적으로 JSON 토픽 폐기

### Phase 7: 테스트 및 검증 (2주)

#### 7.1 단위 테스트
```java
@Test
void testAvroSerialization() {
    // Avro 직렬화/역직렬화 테스트
    var original = OrderCreatedEventPayload.newBuilder()
        .setOrderId(1L)
        ...
        .build();
    
    byte[] serialized = serializeToBytes(original);
    var deserialized = deserializeFromBytes(serialized, OrderCreatedEventPayload.class);
    
    assertEquals(original, deserialized);
}
```

#### 7.2 통합 테스트
```java
@SpringBootTest
@EmbeddedKafka
class SchemaRegistryIntegrationTest {
    
    @Test
    void testEndToEndWithSchemaRegistry() {
        // Producer → Kafka → Consumer 전체 플로우 테스트
        // Schema Registry 연동 확인
    }
}
```

#### 7.3 성능 테스트
- 메시지 크기 비교 (JSON vs Avro)
- 처리 속도 비교
- Consumer Lag 모니터링

#### 7.4 호환성 테스트
- BACKWARD 호환성 테스트 (필드 추가)
- FORWARD 호환성 테스트 (필드 삭제)
- 버전 간 메시지 교환 테스트

### Phase 8: 문서화 및 운영 가이드 (1주)

#### 8.1 문서 작성
- [ ] 스키마 설계 가이드
- [ ] 스키마 변경 프로세스
- [ ] 트러블슈팅 가이드
- [ ] 모니터링 대시보드 구성

#### 8.2 팀 교육
- Schema Registry 개념 교육
- Avro 스키마 작성법
- 스키마 진화 전략

## 주요 고려사항

### 1. 스키마 진화 (Schema Evolution)

#### 호환성 모드 선택
- **BACKWARD** (권장): 새 Consumer가 이전 Producer 메시지 읽기 가능
  - 필드 추가 시 기본값 필수
  - 필드 삭제 가능
  - 가장 일반적인 선택

- **FORWARD**: 이전 Consumer가 새 Producer 메시지 읽기 가능
  - 필드 삭제 가능
  - 필드 추가 시 이전 Consumer는 무시

- **FULL**: BACKWARD + FORWARD 모두 만족
  - 가장 엄격하지만 유연성 높음

#### 스키마 변경 시나리오
```json
// Version 1
{
  "type": "record",
  "name": "OrderEvent",
  "fields": [
    {"name": "orderId", "type": "long"},
    {"name": "userId", "type": "long"}
  ]
}

// Version 2 (BACKWARD 호환)
{
  "type": "record",
  "name": "OrderEvent",
  "fields": [
    {"name": "orderId", "type": "long"},
    {"name": "userId", "type": "long"},
    {"name": "merchantId", "type": "long", "default": 0}  // 기본값 필수
  ]
}
```

### 2. 성능 최적화

#### 스키마 캐싱
```java
// AWS Glue Schema Registry는 자동 캐싱 지원
configs.put(AWSSchemaRegistryConstants.CACHE_TIME_TO_LIVE_MILLIS, "86400000"); // 24시간
configs.put(AWSSchemaRegistryConstants.CACHE_SIZE, "1000");
```

#### 압축 설정
```java
configs.put(AWSSchemaRegistryConstants.COMPRESSION_TYPE, Compression.ZLIB.name());
```

#### 메시지 크기 비교
- JSON: ~1-2KB (텍스트 기반)
- Avro: ~500B-1KB (바이너리, 30-50% 절감)

### 3. 운영 고려사항

#### 스키마 네이밍 규칙
```
{domain}.{aggregate}.{event-name}.{version}
예: order.order.created.v1
    delivery.delivery.started.v1
```

#### 스키마 버전 관리
- 자동 등록 vs 수동 등록
  - 개발 환경: 자동 등록 활성화
  - 운영 환경: 수동 등록 (승인 프로세스)

#### 모니터링 지표
- 스키마 등록 실패 횟수
- 직렬화/역직렬화 에러율
- 스키마 캐시 히트율
- 호환성 체크 실패 횟수

### 4. 에러 처리

#### 스키마 관련 에러
```java
try {
    // Avro 직렬화
} catch (SchemaNotFoundException e) {
    // 스키마를 찾을 수 없음 - 등록 필요
} catch (IncompatibleSchemaException e) {
    // 호환되지 않는 스키마 - 스키마 수정 필요
} catch (SerializationException e) {
    // 직렬화 실패 - 데이터 검증
}
```

#### Fallback 전략
```java
public void sendEvent(Event event) {
    try {
        // Avro로 전송 시도
        avroKafkaTemplate.send(topic, event);
    } catch (SchemaRegistryException e) {
        log.error("Schema Registry error, falling back to JSON", e);
        // JSON으로 Fallback
        jsonKafkaTemplate.send(topic, event.toJson());
    }
}
```

### 5. 보안

#### IAM 역할 설정
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "glue:GetSchemaVersion",
        "glue:RegisterSchemaVersion",
        "glue:GetSchema"
      ],
      "Resource": "arn:aws:glue:ap-northeast-2:*:registry/laas-event-registry"
    }
  ]
}
```

#### 스키마 접근 제어
- Producer: 스키마 등록 권한
- Consumer: 스키마 조회 권한만
- 운영자: 스키마 삭제 권한

## 마이그레이션 전략

### Option 1: Big Bang (일괄 전환)
**장점**:
- 빠른 전환
- 중간 복잡도 없음

**단점**:
- 리스크 높음
- 롤백 어려움

**적용 조건**:
- 트래픽이 낮은 시스템
- 충분한 테스트 완료

### Option 2: Strangler Pattern (점진적 전환) - 권장
**단계**:
1. 새 Avro 토픽 생성
2. Producer 듀얼 모드 (JSON + Avro 동시 발행)
3. Consumer 순차 전환 (하나씩 Avro로)
4. 모든 Consumer 전환 후 JSON 발행 중단
5. 레거시 JSON 토픽 폐기

**장점**:
- 리스크 최소화
- 단계별 검증 가능
- 롤백 용이

**단점**:
- 전환 기간 길어짐
- 중간 복잡도 증가

### Option 3: Feature Toggle (기능 스위치)
```java
@Value("${feature.schema-registry.enabled:false}")
private boolean schemaRegistryEnabled;

public void sendEvent(Event event) {
    if (schemaRegistryEnabled) {
        avroKafkaTemplate.send(topic, convertToAvro(event));
    } else {
        jsonKafkaTemplate.send(topic, event.toJson());
    }
}
```

**장점**:
- 즉시 롤백 가능
- A/B 테스트 가능

**단점**:
- 코드 복잡도 증가
- 기술 부채 발생

### 권장 마이그레이션 타임라인
```
Week 1-2: Phase 1-2 (환경 준비, 스키마 정의)
Week 3-4: Phase 3-4 (Producer/Consumer 개발)
Week 5: Phase 5 (유틸리티 개발)
Week 6-8: Phase 6 (병행 운영, 점진적 전환)
  - Week 6: read-model Consumer만 Avro 전환
  - Week 7: 다른 Consumer 순차 전환
  - Week 8: Producer JSON 발행 중단
Week 9-10: Phase 7 (테스트 및 검증)
Week 11: Phase 8 (문서화)
```

## 테스트 계획

### 단위 테스트
- [ ] Avro 스키마 직렬화/역직렬화
- [ ] 스키마 호환성 검증
- [ ] 변환 유틸리티 테스트

### 통합 테스트
- [ ] Producer → Kafka → Consumer 전체 플로우
- [ ] Schema Registry 연동
- [ ] 에러 시나리오 (스키마 미등록, 호환성 오류)

### 성능 테스트
- [ ] 메시지 크기 비교 (JSON vs Avro)
- [ ] 처리 시간 비교
- [ ] 대용량 부하 테스트 (10,000 msg/sec)

### 호환성 테스트
- [ ] 스키마 버전 업 시나리오
- [ ] 필드 추가/삭제 호환성
- [ ] 이전 버전 Consumer와 통신

## 체크리스트

### 개발 완료 기준
- [ ] AWS Glue Schema Registry 생성 및 IAM 권한 설정
- [ ] 모든 이벤트에 대한 Avro 스키마 정의 완료
- [ ] Producer 설정 변경 및 테스트 완료
- [ ] Consumer 설정 변경 및 테스트 완료
- [ ] 공통 유틸리티 개발 완료
- [ ] 단위/통합 테스트 커버리지 80% 이상
- [ ] 성능 테스트 결과 요구사항 충족
- [ ] 스키마 진화 시나리오 검증 완료

### 운영 준비 기준
- [ ] 병행 운영 기간 (최소 2주) 완료
- [ ] 모니터링 대시보드 구축
- [ ] 장애 대응 매뉴얼 작성
- [ ] 스키마 변경 프로세스 수립
- [ ] 팀 교육 완료
- [ ] 롤백 계획 수립

### 마이그레이션 완료 기준
- [ ] 모든 Producer Avro 전환
- [ ] 모든 Consumer Avro 전환
- [ ] 레거시 JSON 코드 제거
- [ ] 스키마 버전 관리 프로세스 정착
- [ ] 문서화 완료

## 참고 자료

### AWS 공식 문서
- [AWS Glue Schema Registry](https://docs.aws.amazon.com/glue/latest/dg/schema-registry.html)
- [Schema Registry with MSK](https://docs.aws.amazon.com/msk/latest/developerguide/schema-registry.html)
- [Glue Schema Registry Serializers/Deserializers](https://github.com/awslabs/aws-glue-schema-registry)

### Apache Avro
- [Avro 공식 문서](https://avro.apache.org/docs/current/)
- [Avro Schema Evolution](https://docs.confluent.io/platform/current/schema-registry/avro.html)
- [Gradle Avro Plugin](https://github.com/davidmc24/gradle-avro-plugin)

### 모범 사례
- [Schema Evolution Best Practices](https://docs.confluent.io/platform/current/schema-registry/schema-evolution.html)
- [Kafka Schema Registry Patterns](https://www.confluent.io/blog/schemas-contracts-compatibility/)
- [AWS Glue Schema Registry Integrations](https://aws.amazon.com/blogs/big-data/use-the-aws-glue-schema-registry-for-data-streaming-and-storage/)

### 예제 코드
- [AWS Glue Schema Registry Examples](https://github.com/aws-samples/aws-glue-schema-registry-examples)
- [Spring Kafka with Schema Registry](https://docs.spring.io/spring-kafka/reference/html/#with-schema-registry)

## 롤백 계획

### 긴급 롤백 시나리오
1. **Feature Flag 비활성화**
   ```yaml
   feature.schema-registry.enabled: false
   ```

2. **이전 버전 배포**
   - 기존 JSON 기반 코드로 롤백
   - Kafka 메시지는 유지 (Consumer만 변경)

3. **데이터 복구**
   - Avro 메시지를 JSON으로 변환하는 브릿지 애플리케이션 실행
   - 새 JSON 토픽으로 재발행

### 부분 롤백
- 특정 Consumer만 JSON 모드로 복구
- Producer는 듀얼 모드 유지

