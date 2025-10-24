# Event Schema 모듈

AWS Glue Schema Registry를 사용한 Avro 기반 이벤트 스키마 정의 모듈

## 개요

이 모듈은 Kafka 이벤트의 Avro 스키마를 정의하고 관리합니다. 
향후 멀티 레포 전환 시 독립 repository로 분리될 예정입니다.

### 주요 특징
- ✅ **환경 독립적**: 물리적 토픽 이름은 각 서비스에서 관리
- ✅ **타입 안전**: Avro SpecificRecord 기반
- ✅ **AWS Glue 통합**: Schema Registry 자동 등록 및 버전 관리
- ✅ **멀티 레포 대비**: 독립적인 스키마 모듈로 설계

## 디렉토리 구조

```
src/main/
├── avro/                                    # Avro 스키마 정의
│   ├── common/
│   │   └── KafkaEvent.avsc
│   ├── order/
│   │   └── OrderCreatedEventPayload.avsc
│   ├── delivery/
│   │   ├── DeliveryStartedEventPayload.avsc
│   │   ├── DeliveryPickedUpEventPayload.avsc
│   │   └── DeliveryDeliveredEventPayload.avsc
│   └── dispatch/
│       └── DispatchDispatchedEventPayload.avsc
│
└── java/vroong/laas/common/event/          # Java 클래스
    ├── KafkaEventType.java                 # 이벤트 타입 정의
    ├── KafkaEventSource.java               # 이벤트 소스 정의
    ├── TopicResolver.java                  # 토픽 매핑 인터페이스
    └── util/
        └── AvroSerializer.java             # Avro 직렬화 유틸리티
```

## 빌드

### Avro Java 클래스 생성

```bash
./gradlew :common:event-schema:generateAvroJava
```

생성된 클래스 위치: `build/generated-main-avro-java/`

### 전체 빌드

```bash
./gradlew :common:event-schema:build
```

## 사용법

### 의존성 추가

다른 모듈에서 사용하려면 `build.gradle`에 추가:

```groovy
dependencies {
    implementation project(':common:event-schema')
}
```

### Producer 사용 예시

```java
import vroong.laas.common.event.OrderEventType;  // 도메인별 EventType
import vroong.laas.common.event.KafkaEventSource;
import vroong.laas.common.event.TopicResolver;
import vroong.laas.common.event.avro.KafkaEvent;
import vroong.laas.common.event.avro.payload.order.OrderCreatedEventPayload;
import vroong.laas.common.event.util.AvroSerializer;

// 1. Payload 생성
OrderCreatedEventPayload payload = OrderCreatedEventPayload.newBuilder()
    .setOrderId(12345L)
    .setOrderNumber("ORD-2024-001")
    .setOrderStatus("CREATED")
    // ...
    .build();

// 2. KafkaEvent 생성
KafkaEvent event = KafkaEvent.newBuilder()
    .setEventId(UUID.randomUUID().toString())
    .setType(OrderEventType.ORDER_CREATED.getValue())  // 도메인별 타입
    .setSource(KafkaEventSource.ORDER.getValue())
    .setTimestamp(System.currentTimeMillis())
    .setSchemaVersion("1.0")
    .setPayload(ByteBuffer.wrap(AvroSerializer.serialize(payload)))
    .build();

// 3. 토픽 결정
String topic = topicResolver.resolveTopicName(OrderEventType.ORDER_CREATED);

// 4. 전송
kafkaTemplate.send(topic, event);
```

### Consumer에서 이벤트 타입 파싱

```java
// 문자열에서 EventType 조회
EventType eventType = EventTypes.from(kafkaEvent.getType());

// 도메인별로 처리
if (eventType instanceof OrderEventType orderEvent) {
    switch(orderEvent) {
        case ORDER_CREATED -> handleOrderCreated(payload);
        // ...
    }
} else if (eventType instanceof DeliveryEventType deliveryEvent) {
    switch(deliveryEvent) {
        case DELIVERY_STARTED -> handleDeliveryStarted(payload);
        // ...
    }
}
```

### TopicResolver 구현 예시

```java
@Component
public class KafkaTopicResolver implements TopicResolver {
    
    @Value("${kafka.topics.order-main}")
    private String orderMainTopic;
    
    @Value("${kafka.topics.order-payment}")
    private String orderPaymentTopic;
    
    @Value("${kafka.topics.delivery-main}")
    private String deliveryMainTopic;
    
    @Value("${kafka.topics.delivery-tracking}")
    private String deliveryTrackingTopic;
    
    @Value("${kafka.topics.dispatch-main}")
    private String dispatchMainTopic;
    
    @Override
    public String resolveTopicName(TopicKey topicKey) {
        return switch(topicKey) {
            case ORDER_MAIN -> orderMainTopic;
            case ORDER_PAYMENT -> orderPaymentTopic;
            case DELIVERY_MAIN -> deliveryMainTopic;
            case DELIVERY_TRACKING -> deliveryTrackingTopic;
            case DISPATCH_MAIN -> dispatchMainTopic;
        };
    }
    
    // resolveTopicName(EventType)은 default 메서드로 자동 제공됨
}
```

**장점**:
- ✅ 1개 도메인에서 여러 토픽 지원 (order-main, order-payment 등)
- ✅ 컴파일 타임에 모든 토픽 매핑 강제
- ✅ IDE 자동완성 및 타입 안전
- ✅ TopicKey enum에 새 토픽 추가 시 컴파일 에러로 누락 방지

### application.yml 설정

```yaml
# Production
kafka:
  topics:
    order-main: "order-event"
    order-payment: "order-payment-event"
    delivery-main: "delivery-event"
    delivery-tracking: "delivery-tracking-event"
    dispatch-main: "dispatch-event"

# Development
kafka:
  topics:
    order-main: "dev-order-event"
    order-payment: "dev-order-payment-event"
    delivery-main: "dev-delivery-event"
    delivery-tracking: "dev-delivery-tracking-event"
    dispatch-main: "dev-dispatch-event"

# 기본값
kafka:
  topics:
    order: order-event
    dispatch: dispatch-event
    delivery: delivery-event

# dev 환경 (application-dev.yml)
kafka:
  topics:
    order: dev-order-event
    dispatch: dev-dispatch-event
    delivery: dev-delivery-event

# prod 환경 (application-prod.yml)
kafka:
  topics:
    order: prod-order-event
    dispatch: prod-dispatch-event
    delivery: prod-delivery-event
```

## 스키마 변경 가이드

### 1. 호환성 유지 원칙

- **BACKWARD 호환성** (권장): 새 Consumer가 이전 Producer 메시지를 읽을 수 있음
  - ✅ 새 필드 추가 시 **반드시 default 값 지정**
  - ✅ 기존 필드 삭제 가능
  - ❌ 필드 타입 변경 불가

### 2. 스키마 변경 예시

#### 필드 추가 (BACKWARD 호환)

```json
{
  "name": "newField",
  "type": "string",
  "default": "",
  "doc": "새로 추가된 필드"
}
```

#### Optional 필드 추가

```json
{
  "name": "optionalField",
  "type": ["null", "string"],
  "default": null,
  "doc": "선택적 필드"
}
```

### 3. 스키마 변경 프로세스

1. `.avsc` 파일 수정
2. `./gradlew :common:event-schema:build` 실행
3. 컴파일 에러 확인 및 수정
4. 테스트
5. Commit & Push

## Avro 타입 매핑

| Java 타입 | Avro 타입 | 설명 |
|-----------|-----------|------|
| String | string | 문자열 |
| Long | long | 64비트 정수 |
| Integer | int | 32비트 정수 |
| Boolean | boolean | 불린 |
| BigDecimal | bytes (decimal) | 고정소수점 |
| Instant | long (timestamp-millis) | 타임스탬프 |
| List<T> | array | 배열 |
| Optional<T> | union [null, T] | 선택적 값 |

## 멀티 레포 전환 준비

이 모듈은 향후 독립 repository로 분리될 예정입니다:

1. **현재 (멀티 모듈)**
   - `common/event-schema` 모듈
   - 다른 모듈은 `project(':common:event-schema')` 의존

2. **향후 (멀티 레포)**
   - 독립 `schema-definitions` repository
   - CI/CD로 AWS Glue Schema Registry에 자동 등록
   - 각 서비스는 Schema Registry에서 스키마 조회

## 참고 자료

- [Apache Avro Documentation](https://avro.apache.org/docs/current/)
- [AWS Glue Schema Registry](https://docs.aws.amazon.com/glue/latest/dg/schema-registry.html)
- [Gradle Avro Plugin](https://github.com/davidmc24/gradle-avro-plugin)

