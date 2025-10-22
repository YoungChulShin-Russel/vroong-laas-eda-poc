# Projection 모듈 작업 계획

## 개요
Order, Delivery, Dispatch 서버가 발행하는 Kafka 이벤트를 받아서 조회 모델을 생성하고 저장하는 모듈

## 기술 스택
- Java, Spring Boot
- Kafka Consumer
- Redis (1일 TTL, key/value)
- MongoDB (1일 이후 데이터, 복잡한 쿼리 지원)

## 프로젝트 구조
```
projection/
├── src/main/java/vroong/laas/projection/
│   ├── ProjectionApplication.java
│   ├── config/
│   │   ├── KafkaConfig.java
│   │   ├── RedisConfig.java
│   │   └── MongoConfig.java
│   ├── consumer/
│   │   ├── OrderEventConsumer.java
│   │   ├── DeliveryEventConsumer.java
│   │   └── DispatchEventConsumer.java
│   ├── model/
│   │   ├── event/
│   │   │   ├── OrderEvent.java
│   │   │   ├── DeliveryEvent.java
│   │   │   └── DispatchEvent.java
│   │   ├── projection/
│   │   │   └── OrderProjection.java
│   │   └── document/
│   │       └── OrderDocument.java
│   ├── projection/
│   │   ├── OrderProjectionHandler.java
│   │   ├── DeliveryProjectionHandler.java
│   │   └── DispatchProjectionHandler.java
│   ├── repository/
│   │   ├── redis/
│   │   │   └── OrderProjectionRedisRepository.java
│   │   └── mongo/
│   │       └── OrderProjectionMongoRepository.java
│   └── service/
│       └── ProjectionService.java
```

## 작업 단계

### Phase 1: 기본 설정 및 인프라
1. **의존성 설정** - build.gradle 구성
   - Spring Boot Starter
   - Spring Kafka
   - Spring Data Redis
   - Spring Data MongoDB
   - Common Event 모듈 의존성

2. **설정 클래스 구현**
   - KafkaConfig: Consumer 설정
   - RedisConfig: Redis 연결 및 TTL 설정
   - MongoConfig: MongoDB 연결 설정

3. **Application 구성**
   - application.yml 설정
   - Main Application 클래스

### Phase 2: 이벤트 모델 정의
1. **이벤트 모델 구현**
   - 기존 common/event 모듈의 이벤트 페이로드 활용
   - OrderEvent, DeliveryEvent, DispatchEvent 래퍼 클래스

2. **Projection 모델 설계**
   - OrderProjection: 통합된 주문 상태 정보
   - Redis용 간단한 구조 (key-value)
   - MongoDB용 복잡한 구조 (다양한 쿼리 지원)

### Phase 3: Kafka Consumer 구현
1. **Consumer 클래스 구현**
   - OrderEventConsumer
   - DeliveryEventConsumer  
   - DispatchEventConsumer

2. **에러 처리**
   - DLQ (Dead Letter Queue) 설정
   - 재시도 로직
   - 로깅

### Phase 4: Projection Handler 구현
1. **핸들러 클래스 구현**
   - 이벤트를 받아 projection 모델로 변환
   - 비즈니스 로직 처리
   - 상태 집계 및 계산

2. **이벤트 타입별 처리 로직**
   - Order 생성/수정
   - Delivery 상태 변경
   - Dispatch 상태 변경

### Phase 5: 저장소 구현
1. **Redis Repository**
   - 1일 TTL 설정
   - Key 설계 (orderId 기반)
   - 단순 CRUD 연산

2. **MongoDB Repository**
   - 인덱스 설계
   - 복잡한 쿼리 지원
   - 날짜 기반 파티셔닝 고려

### Phase 6: 서비스 레이어 구현
1. **ProjectionService**
   - Redis/MongoDB 저장 로직 조율
   - TTL 만료 후 MongoDB 이관 로직
   - 트랜잭션 처리

### Phase 7: 모니터링 및 운영
1. **메트릭 수집**
   - Consumer lag 모니터링
   - 처리 성능 지표
   - 에러율 추적

2. **헬스체크**
   - Kafka 연결 상태
   - Redis/MongoDB 연결 상태

## 주요 고려사항

### 데이터 일관성
- 이벤트 순서 보장 (Kafka partition key 활용)
- 중복 처리 방지 (idempotent 처리)
- 장애 복구 시 데이터 정합성

### 성능 최적화
- 배치 처리 고려
- Redis 파이프라이닝
- MongoDB 벌크 연산

### 확장성
- Consumer 그룹 활용한 수평 확장
- 파티션 증가 대응
- 샤딩 고려

### 운영 효율성
- 설정 외부화
- 로그 구조화
- 장애 대응 매뉴얼

## 개발 순서
1. Phase 1 (기본 설정) → 2. Phase 2 (모델 정의) → 3. Phase 3 (Consumer) → 4. Phase 4 (Handler) → 5. Phase 5 (Repository) → 6. Phase 6 (Service) → 7. Phase 7 (모니터링)

각 Phase별로 단위 테스트와 통합 테스트를 병행하여 진행