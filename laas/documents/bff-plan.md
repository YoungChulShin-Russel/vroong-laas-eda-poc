# BFF (Backend for Frontend) 구현 계획

## 개요
Spring Cloud Gateway를 사용하여 BFF를 구현합니다. Command 요청은 각 서비스로, Query 요청은 read-model로 라우팅합니다.

## 프로젝트 구조 분석
- **Order 서비스**: `/order/api` (포트: 8080)
- **Delivery 서비스**: `/delivery/api` (포트: 8081) 
- **Dispatch 서비스**: `/dispatch/api` (포트: 8082)
- **Read-model 서비스**: `/read-model` (포트: 8083)
- **BFF 모듈**: `/bff` (포트: 8090)

## 구현 계획

### 1. BFF 모듈 설정
- Spring Cloud Gateway 의존성 추가
- Spring Boot Starter Web 및 기본 의존성 구성
- Gradle 빌드 설정

### 2. 라우팅 규칙 설계

#### Command 요청 라우팅
- **Order Commands**: `POST /api/orders` → Order 서비스
- **Delivery Commands**: `POST /api/deliveries/*`, `PUT /api/deliveries/*` → Delivery 서비스  
- **Dispatch Commands**: `POST /api/dispatches/*`, `PUT /api/dispatches/*` → Dispatch 서비스

#### Query 요청 라우팅
- **모든 GET 요청**: `GET /api/**` → Read-model 서비스
- **Order Queries**: `GET /api/orders/*` → Read-model 서비스
- **Delivery Queries**: `GET /api/deliveries/*` → Read-model 서비스
- **Dispatch Queries**: `GET /api/dispatches/*` → Read-model 서비스

### 3. 애플리케이션 설정
```yaml
server:
  port: 8090

spring:
  cloud:
    gateway:
      routes:
        # Command 라우팅
        - id: order-commands
          uri: http://localhost:8080
          predicates:
            - Path=/api/orders
            - Method=POST
        
        - id: delivery-commands
          uri: http://localhost:8081
          predicates:
            - Path=/api/deliveries/**
            - Method=POST,PUT,PATCH,DELETE
        
        - id: dispatch-commands
          uri: http://localhost:8082
          predicates:
            - Path=/api/dispatches/**
            - Method=POST,PUT,PATCH,DELETE
        
        # Query 라우팅 (Read-model)
        - id: read-model-queries
          uri: http://localhost:8083
          predicates:
            - Path=/api/**
            - Method=GET
```

### 4. 주요 구현 파일
- `BffApplication.java`: 메인 애플리케이션 클래스
- `application.yml`: Gateway 라우팅 설정
- `build.gradle`: 의존성 및 빌드 설정

### 5. 테스트 계획
- 각 라우팅 규칙 검증
- Command/Query 분리 동작 확인
- 서비스별 라우팅 정확성 테스트

## 기대 효과
1. **단일 진입점**: 클라이언트는 BFF 하나만 알면 됨
2. **Command/Query 분리**: CQRS 패턴 적용으로 성능 최적화
3. **서비스 추상화**: 백엔드 서비스 변경 시 클라이언트 영향 최소화
4. **로드 밸런싱**: 필요 시 각 서비스별 로드 밸런싱 적용 가능

## 구현 순서
1. ✅ 프로젝트 구조 분석 완료
2. 🔄 BFF 모듈 Spring Cloud Gateway 설정
3. ⏳ Command 요청 라우팅 설정
4. ⏳ Query 요청 라우팅 설정  
5. ⏳ 애플리케이션 설정 파일 구성
6. ⏳ 메인 애플리케이션 클래스 생성
7. ⏳ 라우팅 기능 테스트