# Event Schema 모듈

AWS Glue Schema Registry를 사용한 Avro 기반 이벤트 스키마 정의 모듈

## 개요

이 모듈은 Kafka 이벤트의 Avro 스키마를 정의하고 관리합니다. 향후 멀티 레포 전환 시 독립 repository로 분리될 예정입니다.

## 디렉토리 구조

```
src/main/avro/
├── common/              # 공통 스키마
│   └── KafkaEvent.avsc
├── order/               # Order 도메인 스키마
│   └── OrderCreatedEventPayload.avsc
├── delivery/            # Delivery 도메인 스키마
│   ├── DeliveryStartedEventPayload.avsc
│   ├── DeliveryPickedUpEventPayload.avsc
│   └── DeliveryDeliveredEventPayload.avsc
└── dispatch/            # Dispatch 도메인 스키마
    └── DispatchDispatchedEventPayload.avsc
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

### Avro 클래스 사용 예시

```java
import vroong.laas.common.event.avro.KafkaEvent;
import vroong.laas.common.event.avro.payload.order.OrderCreatedEventPayload;
import vroong.laas.common.event.avro.payload.order.OrderLocation;
import vroong.laas.common.event.avro.payload.order.OrderItem;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

// OrderLocation 생성
OrderLocation origin = OrderLocation.newBuilder()
    .setContactName("홍길동")
    .setContactPhoneNumber("010-1234-5678")
    .setLatitude(ByteBuffer.wrap(new BigDecimal("37.5665").unscaledValue().toByteArray()))
    .setLongitude(ByteBuffer.wrap(new BigDecimal("126.9780").unscaledValue().toByteArray()))
    .setJibunAddress("서울시 중구")
    .setRoadAddress("서울시 중구 세종대로")
    .setDetailAddress("101동 101호")
    .build();

// OrderItem 생성
OrderItem item = OrderItem.newBuilder()
    .setItemName("치킨")
    .setQuantity(2)
    .setPrice(ByteBuffer.wrap(new BigDecimal("20000").unscaledValue().toByteArray()))
    .build();

// OrderCreatedEventPayload 생성
OrderCreatedEventPayload payload = OrderCreatedEventPayload.newBuilder()
    .setOrderId(12345L)
    .setOrderNumber("ORD-2024-001")
    .setOrderStatus("CREATED")
    .setOriginLocation(origin)
    .setDestinationLocation(destination)
    .setItems(List.of(item))
    .setOrderedAt(Instant.now().toEpochMilli())
    .build();

// KafkaEvent 생성
KafkaEvent event = KafkaEvent.newBuilder()
    .setEventId(UUID.randomUUID().toString())
    .setType("order.order.created")
    .setSource("ORDER")
    .setTimestamp(System.currentTimeMillis())
    .setSchemaVersion("1.0")
    .setPayload(ByteBuffer.wrap(serializePayload(payload)))
    .build();
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

