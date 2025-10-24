# BFF 설정 가이드

## 설정 구조

### ServiceProperties 클래스

```java
@ConfigurationProperties(prefix = "gateway")
public class ServiceProperties {
  private ServiceConfig order;
  private ServiceConfig delivery;
  private ServiceConfig dispatch;
  private ServiceConfig readModel;
  
  public static class ServiceConfig {
    private String uri;        // 서비스 URI
    private String routeId;    // 라우트 ID
    private String path;       // 매칭 경로 패턴
  }
}
```

## 기본 설정 (application.yml)

```yaml
gateway:
  order:
    route-id: order-commands
    path: /api/orders
    uri: http://localhost:8080
  
  delivery:
    route-id: delivery-commands
    path: /api/deliveries/**
    uri: http://localhost:8081
  
  dispatch:
    route-id: dispatch-commands
    path: /api/dispatches/**
    uri: http://localhost:8082
  
  read-model:
    route-id: read-model-queries
    path: /api/**
    uri: http://localhost:8083
```

## 설정 외부화의 장점

### 1. 코드 변경 없이 설정 변경 가능

**시나리오: API 버전 변경**

```yaml
# application-v2.yml
gateway:
  order:
    path: /api/v2/orders    # 경로만 변경
    uri: http://localhost:8080
```

### 2. 환경별 다른 경로 사용

**시나리오: 프로덕션에서 다른 경로 패턴 사용**

```yaml
# application-prod.yml
gateway:
  order:
    route-id: prod-order-commands
    path: /v1/orders          # 프로덕션용 경로
    uri: http://order-service.prod.svc.cluster.local:8080
```

### 3. 라우트 ID를 환경별로 관리

**시나리오: 모니터링 시스템에서 환경 구분**

```yaml
# application-dev.yml
gateway:
  order:
    route-id: dev-order-commands

# application-prod.yml
gateway:
  order:
    route-id: prod-order-commands
```

## 고급 사용 사례

### 1. Blue-Green 배포

```yaml
# Blue 환경
gateway:
  order:
    uri: http://order-service-blue:8080

# Green 환경
gateway:
  order:
    uri: http://order-service-green:8080
```

### 2. A/B 테스트

```yaml
# 새 버전 테스트
gateway:
  order:
    path: /api/orders/beta/**
    uri: http://order-service-v2:8080
```

### 3. 지역별 라우팅

```yaml
# 아시아 리전
gateway:
  order:
    uri: http://order-service.asia.svc.cluster.local:8080

# 유럽 리전
gateway:
  order:
    uri: http://order-service.eu.svc.cluster.local:8080
```

### 4. 개발 환경에서 Mock 서버 사용

```yaml
# application-dev.yml
gateway:
  order:
    uri: http://localhost:9999  # Mock 서버
```

## 환경별 설정 오버라이드

Spring Boot의 Profile 시스템을 활용하여 환경별 설정을 오버라이드합니다.

### 우선순위 (높음 → 낮음)

1. `application-{profile}.yml`
2. `application.yml`
3. 코드의 기본값

### 예시: Staging 환경

**application.yml** (기본)
```yaml
gateway:
  order:
    route-id: order-commands
    path: /api/orders
    uri: http://localhost:8080
```

**application-staging.yml** (오버라이드)
```yaml
gateway:
  order:
    uri: http://order-service.staging.svc.cluster.local:8080
    # route-id, path는 기본값 사용
```

**최종 결과:**
- route-id: `order-commands` (기본값)
- path: `/api/orders` (기본값)
- uri: `http://order-service.staging.svc.cluster.local:8080` (오버라이드)

## 설정 검증

### 필수 값 검증

```java
@ConfigurationProperties(prefix = "gateway")
@Validated
public class ServiceProperties {
  
  @NotNull
  private ServiceConfig order;
  
  @Getter
  @Setter
  public static class ServiceConfig {
    @NotBlank
    private String uri;
    
    @NotBlank
    private String routeId;
    
    @NotBlank
    private String path;
  }
}
```

### 시작 시 설정 확인

```java
@Component
@RequiredArgsConstructor
public class GatewayConfigValidator {
  
  private final ServiceProperties properties;
  
  @PostConstruct
  public void validate() {
    log.info("Gateway Configuration:");
    log.info("  Order: {} -> {}", 
        properties.getOrder().getPath(), 
        properties.getOrder().getUri());
    // ...
  }
}
```

## 런타임 설정 변경

### Spring Cloud Config 사용

```yaml
# config-server의 application.yml
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/org/config-repo
```

### ConfigMap 사용 (Kubernetes)

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: bff-config
data:
  application.yml: |
    gateway:
      order:
        uri: http://order-service:8080
```

## 모범 사례

### 1. 기본 설정은 application.yml에

개발 환경에서 가장 많이 사용하는 설정을 기본값으로 설정합니다.

### 2. 민감한 정보는 환경 변수로

```yaml
gateway:
  order:
    uri: ${ORDER_SERVICE_URI:http://localhost:8080}
```

### 3. 문서화

각 설정의 의미와 사용 예시를 주석으로 작성합니다.

```yaml
gateway:
  order:
    # 라우트 식별자 (모니터링/로깅용)
    route-id: order-commands
    
    # 매칭 경로 패턴 (Ant-style)
    path: /api/orders
    
    # Order 서비스 URI
    uri: http://localhost:8080
```

## 트러블슈팅

### 설정이 적용되지 않을 때

1. **Profile 확인**
   ```bash
   # 로그에서 확인
   The following 1 profile is active: "dev"
   ```

2. **설정 파일 위치 확인**
   ```
   src/main/resources/
   ├── application.yml
   └── application-dev.yml
   ```

3. **YAML 구조 확인**
   - 들여쓰기는 스페이스 2개
   - 탭 문자 사용 금지

### 설정 디버깅

```bash
# 현재 적용된 설정 확인
java -jar bff.jar --debug --spring.profiles.active=dev
```

## 설정 마이그레이션 가이드

### 기존 하드코딩된 값 → 설정 파일

**Before:**
```java
.route("order-commands", r -> r
    .path("/api/orders")
    .uri("http://localhost:8080"))
```

**After:**
```java
var config = serviceProperties.getOrder();
.route(config.getRouteId(), r -> r
    .path(config.getPath())
    .uri(config.getUri()))
```

```yaml
gateway:
  order:
    route-id: order-commands
    path: /api/orders
    uri: http://localhost:8080
```

## 참고 자료

- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html)
- [Spring Cloud Gateway Documentation](https://docs.spring.io/spring-cloud-gateway/reference/)
- [@ConfigurationProperties](https://docs.spring.io/spring-boot/api/java/org/springframework/boot/context/properties/ConfigurationProperties.html)


