# Read Model Service (Projection Service)

## 개요

Read Model Service는 CQRS 패턴의 Read Side를 담당하는 서비스입니다.
Event-Driven Architecture를 기반으로 MSA에서 발생한 이벤트를 구독하여 조회 최적화된 Projection을 생성하고, Query REST API를 제공합니다.

## 주요 기능

### 1. Event Consumer (이벤트 구독)
- **Kafka Consumer**: Order, Dispatch, Delivery 이벤트 구독
- **Projection 생성**: 이벤트 기반 Read Model 생성
- **데이터 저장**: Redis (캐시) + MongoDB (영구 저장)

### 2. Query REST API (조회 API)
- **GET /api/v1/orders/{orderId}**: Order Projection 조회
- **캐싱 전략**: Redis → MongoDB → Write Service Fallback
- **고가용성**: Write Service Fallback으로 장애 대응

## 아키텍처

### 데이터 플로우
```
Write Service (MSA)
    ↓ (Kafka Event)
Event Consumer
    ↓ (Projection 생성)
Redis + MongoDB
    ↓ (Query API)
Client
```

### Fallback 전략
```
Client Request
    ↓
1. Redis 캐시 조회 (HIT → 응답)
    ↓ (MISS)
2. MongoDB 조회 (FOUND → Redis 캐싱 → 응답)
    ↓ (NOT FOUND)
3. Write Service API 호출 (SUCCESS → MongoDB 저장 → 응답)
    ↓ (FAIL)
4. 404 Not Found
```

## Profile 기반 실행

### Full Mode (기본)
Event Consumer + Query API 모두 활성화

```bash
./gradlew :projection:bootRun
# 또는
java -jar projection.jar
```

### Consumer Mode
Event Consumer만 활성화 (Query API 비활성화)

```bash
./gradlew :projection:bootRun --args='--spring.profiles.active=consumer'
# 또는
java -jar projection.jar --spring.profiles.active=consumer
```

**사용 사례**:
- Event 처리량이 많아 독립 스케일링 필요
- Kafka Lag이 발생하여 Consumer 성능 향상 필요

### API Mode
Query API만 활성화 (Event Consumer 비활성화)

```bash
./gradlew :projection:bootRun --args='--spring.profiles.active=api'
# 또는
java -jar projection.jar --spring.profiles.active=api
```

**사용 사례**:
- Query 트래픽이 많아 API 서버 스케일 아웃 필요
- Event 처리는 안정적이지만 조회 성능 향상 필요

## 설정

### application.yml (기본 설정)
```yaml
projection:
  features:
    consumer:
      enabled: true  # Event Consumer 활성화
    api:
      enabled: true  # Query API 활성화
  fallback:
    enabled: true    # Write Service Fallback 활성화
```

### application-consumer.yml (Consumer 전용)
```yaml
projection:
  features:
    consumer:
      enabled: true
    api:
      enabled: false
```

### application-api.yml (API 전용)
```yaml
projection:
  features:
    consumer:
      enabled: false
    api:
      enabled: true
```

## 기술 스택

- **Language**: Java 21
- **Framework**: Spring Boot 3.x
- **Event Streaming**: Apache Kafka
- **Cache**: Redis
- **Database**: MongoDB
- **Build Tool**: Gradle

## 개발 가이드

### 신규 Projection 추가

1. **Projection Model 생성**
```java
// model/projection/NewProjection.java
@Getter
@Builder
public class NewProjection {
    private Long id;
    // ... fields
}
```

2. **Event Consumer 생성**
```java
// consumer/NewEventConsumer.java
@Component
@ConditionalOnProperty(
    name = "projection.features.consumer.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class NewEventConsumer {
    @KafkaListener(topics = "${projection.topics.new}")
    public void handleEvent(...) {
        // Event 처리 로직
    }
}
```

3. **Query Controller 생성**
```java
// controller/NewQueryController.java
@RestController
@RequestMapping("/api/v1/new")
@ConditionalOnProperty(
    name = "projection.features.api.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class NewQueryController {
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NewProjection>> get(@PathVariable Long id) {
        // Query 로직
    }
}
```

4. **Query Service 생성**
```java
// service/NewQueryService.java
@Service
public class NewQueryService {
    public NewProjection getProjection(Long id) {
        // Redis → MongoDB → Fallback 로직
    }
}
```

### 테스트

#### 단위 테스트
```bash
./gradlew :projection:test
```

#### 통합 테스트 (Testcontainers)
```bash
./gradlew :projection:integrationTest
```

#### E2E 테스트
```bash
# 1. 인프라 실행
cd scripts/
docker-compose up -d

# 2. Projection Service 실행
./gradlew :projection:bootRun

# 3. API 테스트
curl http://localhost:8083/api/v1/orders/1
```

## 모니터링

### Health Check
```bash
curl http://localhost:8083/actuator/health
```

### Metrics
```bash
curl http://localhost:8083/actuator/metrics
```

### 주요 지표
- **kafka_consumer_lag**: Kafka Consumer Lag (이벤트 처리 지연)
- **redis_cache_hit_rate**: Redis 캐시 히트율
- **fallback_count**: Fallback 발생 횟수

## 트러블슈팅

### Kafka Consumer Lag 증가
**원인**: Event 처리 속도 < Event 발생 속도

**해결**:
1. Consumer Profile로 독립 실행
2. Consumer Pod 스케일 아웃
3. Partition 수 증가

### Query API 응답 느림
**원인**: Query 트래픽 과다

**해결**:
1. API Profile로 독립 실행
2. API Pod 스케일 아웃
3. Redis TTL 조정

### Fallback 빈번 발생
**원인**: Event 처리 지연 또는 MongoDB 데이터 누락

**해결**:
1. Consumer Lag 확인
2. MongoDB 데이터 정합성 확인
3. Fallback 사용률 모니터링

## 배포

### Docker
```dockerfile
FROM openjdk:21-slim
COPY build/libs/projection.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes
```yaml
# deployment-consumer.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: projection-consumer
spec:
  replicas: 2
  template:
    spec:
      containers:
      - name: projection
        image: projection:latest
        args: ["--spring.profiles.active=consumer"]
---
# deployment-api.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: projection-api
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: projection
        image: projection:latest
        args: ["--spring.profiles.active=api"]
```

## 참고 자료

- [CQRS Pattern](https://martinfowler.com/bliki/CQRS.html)
- [Event Sourcing](https://martinfowler.com/eaaDev/EventSourcing.html)
- [Spring Kafka Documentation](https://docs.spring.io/spring-kafka/reference/)
- [Redis Documentation](https://redis.io/documentation)
- [MongoDB Documentation](https://www.mongodb.com/docs/)

