# BFF (Backend for Frontend)

Spring Cloud Gateway를 사용한 API Gateway 구현

## 개요

BFF는 Command/Query 분리(CQRS) 패턴을 적용하여 클라이언트 요청을 적절한 백엔드 서비스로 라우팅합니다.

## 아키텍처

```
Client
  ↓
BFF (Gateway) :8085
  ↓
  ├── Command (POST/PUT/PATCH/DELETE)
  │   ├── Order Service      :8080
  │   ├── Delivery Service   :8081
  │   └── Dispatch Service   :8082
  │
  └── Query (GET)
      └── Read-model Service :8083
```

## 라우팅 규칙

### Command 라우팅 (쓰기 작업)
- `POST /api/orders` → Order Service (8080)
- `POST/PUT/PATCH/DELETE /api/deliveries/**` → Delivery Service (8081)
- `POST/PUT/PATCH/DELETE /api/dispatches/**` → Dispatch Service (8082)

### Query 라우팅 (읽기 작업)
- `GET /api/**` → Read-model Service (8083)

## 환경별 설정

### 개발 환경 (dev)
```bash
./gradlew :bff:bootRun --args='--spring.profiles.active=dev'
# 또는
SPRING_PROFILES_ACTIVE=dev ./gradlew :bff:bootRun
```

설정: `application-dev.yml`
- Order Service: http://localhost:8080
- Delivery Service: http://localhost:8081
- Dispatch Service: http://localhost:8082
- Read-model Service: http://localhost:8083

### 스테이징 환경 (staging)
```bash
./gradlew :bff:bootRun --args='--spring.profiles.active=staging'
```

설정: `application-staging.yml`
- Kubernetes 클러스터 내부 서비스 주소 사용
- 예: `http://order-service.staging.svc.cluster.local:8080`

### 프로덕션 환경 (prod)
```bash
./gradlew :bff:bootRun --args='--spring.profiles.active=prod'
```

설정: `application-prod.yml`
- Kubernetes 클러스터 내부 서비스 주소 사용
- 예: `http://order-service.prod.svc.cluster.local:8080`

## 설정 파일 구조

```
bff/src/main/resources/
├── application.yml           # 기본 설정
├── application-dev.yml       # 개발 환경
├── application-staging.yml   # 스테이징 환경
└── application-prod.yml      # 프로덕션 환경
```

### 서비스 URI 설정 방법

`application.yml` 또는 환경별 파일에서 설정:

```yaml
services:
  order-service: http://localhost:8080
  delivery-service: http://localhost:8081
  dispatch-service: http://localhost:8082
  read-model-service: http://localhost:8083
```

## 구현 상세

### 의존성
- Spring Boot 4.0.0-RC1
- Spring Cloud Gateway 5.0.0-M4 (2025.1.0-M4)
- WebFlux (Reactive)

### 주요 클래스
- `BffApplication.java`: 메인 애플리케이션
- `ServiceProperties.java`: 서비스 URI 설정 바인딩
- **Gateway Config (서비스별 분리)**:
  - `OrderGatewayConfig.java`: Order 서비스 라우팅
  - `DeliveryGatewayConfig.java`: Delivery 서비스 라우팅
  - `DispatchGatewayConfig.java`: Dispatch 서비스 라우팅
  - `ReadModelGatewayConfig.java`: Read-model 서비스 라우팅

## 테스트

### 빌드 및 테스트
```bash
./gradlew :bff:build
```

### 실행
```bash
./gradlew :bff:bootRun
```

### 라우팅 테스트

#### GET 요청 (Read-model로 라우팅)
```bash
curl -i http://localhost:8085/api/orders
curl -i http://localhost:8085/api/deliveries/123
```

#### POST 요청 (Command 서비스로 라우팅)
```bash
curl -i -X POST http://localhost:8085/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId": 1, "items": []}'
```

## 기대 효과

1. **단일 진입점**: 클라이언트는 BFF 하나만 알면 됨
2. **Command/Query 분리**: CQRS 패턴 적용으로 성능 최적화
3. **서비스 추상화**: 백엔드 서비스 변경 시 클라이언트 영향 최소화
4. **환경별 설정 관리**: Profile을 통한 유연한 배포
5. **타입 안전성**: Java Config로 컴파일 타임 검증
6. **관심사 분리**: 서비스별 Config 분리로 독립적인 관리 가능
7. **유지보수성**: 각 서비스의 라우팅 규칙을 독립적으로 수정 가능

## 아키텍처 설계 원칙

### 서비스별 Config 분리
각 백엔드 서비스마다 독립적인 Gateway Config 클래스를 생성하여:
- **단일 책임 원칙(SRP)**: 각 Config는 하나의 서비스에만 집중
- **개방-폐쇄 원칙(OCP)**: 새 서비스 추가 시 기존 코드 수정 불필요
- **의존성 역전(DIP)**: ServiceProperties를 통한 설정 주입

### Spring Bean 통합
Spring Cloud Gateway는 모든 `RouteLocator` Bean을 자동으로 수집하여 통합:
```java
@Bean
public RouteLocator orderRoutes(RouteLocatorBuilder builder) { ... }

@Bean
public RouteLocator deliveryRoutes(RouteLocatorBuilder builder) { ... }

// Spring이 자동으로 모든 RouteLocator를 통합
```

## 확장 가능한 기능

- [ ] Rate Limiting (요청 제한)
- [ ] Circuit Breaker (장애 격리)
- [ ] 인증/인가 필터
- [ ] 요청/응답 로깅
- [ ] CORS 설정
- [ ] 메트릭 수집 (Actuator)
- [ ] 분산 추적 (Spring Cloud Sleuth)

