# BFF 아키텍처

## 파일 구조

```
bff/
├── build.gradle
├── README.md
├── ARCHITECTURE.md (이 파일)
│
└── src/
    └── main/
        ├── java/vroong/laas/bff/
        │   ├── BffApplication.java                    # 메인 애플리케이션
        │   │
        │   └── config/                                # 설정 클래스
        │       ├── ServiceProperties.java             # 서비스 URI 바인딩
        │       ├── OrderGatewayConfig.java            # Order 서비스 라우팅
        │       ├── DeliveryGatewayConfig.java         # Delivery 서비스 라우팅
        │       ├── DispatchGatewayConfig.java         # Dispatch 서비스 라우팅
        │       └── ReadModelGatewayConfig.java        # Read-model 라우팅
        │
        └── resources/
            ├── application.yml                        # 기본 설정
            ├── application-dev.yml                    # 개발 환경
            ├── application-staging.yml                # 스테이징 환경
            └── application-prod.yml                   # 프로덕션 환경
```

## 컴포넌트 설명

### 1. BffApplication.java
Spring Boot 메인 애플리케이션 클래스

```java
@SpringBootApplication
public class BffApplication {
  public static void main(String[] args) {
    SpringApplication.run(BffApplication.class, args);
  }
}
```

### 2. ServiceProperties.java
서비스 URI를 외부 설정에서 주입받는 Properties 클래스

```java
@Component
@ConfigurationProperties(prefix = "services")
public class ServiceProperties {
  private String orderService;
  private String deliveryService;
  private String dispatchService;
  private String readModelService;
}
```

**역할:**
- YAML 파일의 `services.*` 속성을 Java 객체로 바인딩
- 환경별로 다른 URI 설정 가능
- 타입 안전한 설정 관리

### 3. Gateway Config 클래스들

#### OrderGatewayConfig.java
Order 서비스에 대한 라우팅 규칙 정의

**라우팅:**
- `POST /api/orders` → Order Service

**책임:**
- 주문 생성 Command 라우팅만 담당

#### DeliveryGatewayConfig.java
Delivery 서비스에 대한 라우팅 규칙 정의

**라우팅:**
- `POST/PUT/PATCH/DELETE /api/deliveries/**` → Delivery Service

**책임:**
- 배송 관련 모든 Command 라우팅 담당

#### DispatchGatewayConfig.java
Dispatch 서비스에 대한 라우팅 규칙 정의

**라우팅:**
- `POST/PUT/PATCH/DELETE /api/dispatches/**` → Dispatch Service

**책임:**
- 배차 관련 모든 Command 라우팅 담당

#### ReadModelGatewayConfig.java
Read-model 서비스에 대한 라우팅 규칙 정의

**라우팅:**
- `GET /api/**` → Read-model Service

**책임:**
- 모든 Query(조회) 요청 라우팅 담당
- CQRS 패턴의 Query 측 진입점

## 설계 원칙

### 단일 책임 원칙 (SRP)
각 Gateway Config 클래스는 **하나의 백엔드 서비스**에 대한 라우팅만 담당

**장점:**
- 코드 이해가 쉬움
- 수정 범위가 명확함
- 테스트 작성이 용이함

### 개방-폐쇄 원칙 (OCP)
새로운 서비스 추가 시 **기존 코드 수정 없이** 새 Config만 추가

**예시:**
```java
// 새로운 Payment 서비스 추가
@Configuration
@RequiredArgsConstructor
public class PaymentGatewayConfig {
  private final ServiceProperties serviceProperties;
  
  @Bean
  public RouteLocator paymentRoutes(RouteLocatorBuilder builder) {
    return builder.routes()
        .route("payment-commands", r -> r
            .path("/api/payments/**")
            .uri(serviceProperties.getPaymentService()))
        .build();
  }
}
```

### 의존성 역전 원칙 (DIP)
Gateway Config는 **구체적인 URI에 의존하지 않고** ServiceProperties 인터페이스에 의존

**장점:**
- 설정 변경이 코드에 영향 없음
- 환경별 설정이 독립적
- 테스트 시 Mock 객체 주입 용이

## Spring Bean 통합 원리

Spring Cloud Gateway는 애플리케이션 컨텍스트에서 모든 `RouteLocator` Bean을 자동으로 수집하여 하나의 라우팅 테이블로 통합합니다.

```
OrderGatewayConfig.orderRoutes()
  ↓
DeliveryGatewayConfig.deliveryRoutes()
  ↓
DispatchGatewayConfig.dispatchRoutes()     → Spring Gateway가 자동 통합
  ↓
ReadModelGatewayConfig.readModelRoutes()
  ↓
[통합된 라우팅 테이블]
```

**동작 방식:**
1. Spring이 모든 `@Configuration` 클래스 스캔
2. 각 Config의 `@Bean RouteLocator` 메서드 실행
3. 반환된 모든 RouteLocator를 수집
4. 단일 라우팅 테이블로 통합
5. 요청 시 우선순위에 따라 매칭

## 라우트 우선순위

Gateway는 **먼저 등록된 라우트가 높은 우선순위**를 가집니다.

**현재 우선순위:**
1. `order-commands`: `POST /api/orders`
2. `delivery-commands`: `POST/PUT/PATCH/DELETE /api/deliveries/**`
3. `dispatch-commands`: `POST/PUT/PATCH/DELETE /api/dispatches/**`
4. `read-model-queries`: `GET /api/**` (가장 넓은 매칭 범위)

**중요:**
- 구체적인 경로가 일반적인 경로보다 먼저 등록되어야 함
- `read-model-queries`는 `GET /api/**`로 가장 넓은 범위이므로 마지막에 위치

## 환경별 설정

### application.yml (기본)
```yaml
services:
  order-service: http://localhost:8080
  delivery-service: http://localhost:8081
  dispatch-service: http://localhost:8082
  read-model-service: http://localhost:8083
```

### application-prod.yml (프로덕션)
```yaml
services:
  order-service: http://order-service.prod.svc.cluster.local:8080
  delivery-service: http://delivery-service.prod.svc.cluster.local:8081
  dispatch-service: http://dispatch-service.prod.svc.cluster.local:8082
  read-model-service: http://read-model-service.prod.svc.cluster.local:8083
```

## 확장 시나리오

### 새로운 서비스 추가
1. `ServiceProperties`에 필드 추가
2. 환경별 YAML에 URI 설정 추가
3. 새 Gateway Config 클래스 생성
4. 라우팅 규칙 정의

### 필터 추가 (예: 인증)
```java
@Bean
public RouteLocator orderRoutes(RouteLocatorBuilder builder) {
  return builder.routes()
      .route("order-commands", r -> r
          .path("/api/orders")
          .filters(f -> f
              .addRequestHeader("X-Gateway", "BFF")
              .circuitBreaker(config -> config.setName("orderCB")))
          .uri(serviceProperties.getOrderService()))
      .build();
}
```

### 로드 밸런싱
```yaml
services:
  order-service: lb://order-service  # Eureka/Consul 사용 시
```

## 테스트 전략

### 단위 테스트
각 Gateway Config의 라우트 설정 검증

### 통합 테스트
실제 라우팅 동작 검증 (WebTestClient 사용)

### E2E 테스트
모든 백엔드 서비스와 함께 전체 플로우 테스트

