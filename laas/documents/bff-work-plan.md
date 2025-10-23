# BFF 개발 작업 가이드 (WebFlux)

## 프로젝트 개요
- **목적**: MSA 시스템의 Backend for Frontend (BFF) 서버 개발
- **기술 스택**: Spring Boot 4.0, WebFlux, Java 25
- **역할**: Command/Query 라우팅, 조회 모델 최적화 (Redis/MongoDB)

## 1. 환경 설정 및 프로젝트 초기화

### 1.1 프로젝트 생성
```bash
# Spring Initializr 설정
- Spring Boot: 4.0.0-M3
- Java: 25
- Dependencies: WebFlux, Spring Data Redis Reactive, Spring Data MongoDB Reactive
```

### 1.2 핵심 의존성
```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis-reactive'
    implementation 'org.springframework.boot:spring-boot-starter-data-mongodb-reactive'
    implementation 'org.springframework.cloud:spring-cloud-starter-config'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'io.micrometer:micrometer-registry-prometheus'
}
```

### 1.3 기본 설정
```yaml
spring:
  webflux:
    base-path: /api/v1
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2s
    mongodb:
      uri: mongodb://localhost:27017/projection
  cloud:
    config:
      enabled: false # 개발 초기 단계
```

## 2. 팀 학습 계획 (2-3주)

### 2.1 Week 1: Reactive 기초
- **목표**: Mono, Flux 이해
- **학습 내용**:
  - Reactive Streams 개념
  - Mono/Flux 기본 연산자 (map, flatMap, filter)
  - 에러 처리 (onErrorResume, onErrorReturn)
- **실습**: 간단한 WebFlux Controller 작성

### 2.2 Week 2: WebFlux 심화
- **목표**: 실제 BFF 패턴 구현
- **학습 내용**:
  - WebClient 사용법
  - Reactive Database 접근
  - 비동기 조합 패턴
- **실습**: Redis/MongoDB 연동 예제

### 2.3 Week 3: 고급 패턴
- **목표**: 운영 고려사항
- **학습 내용**:
  - 백프레셰어 처리
  - 모니터링 및 디버깅
  - 테스트 작성법
- **실습**: 통합 시나리오 구현

## 3. 아키텍처 설계

### 3.1 패키지 구조
```
src/main/java/com/vroong/bff/
├── config/          # 설정 클래스
├── controller/      # REST 컨트롤러
├── service/         # 비즈니스 로직
├── client/          # MSA 호출 클라이언트
├── repository/      # 데이터 접근 계층
├── model/           # 도메인 모델
├── dto/             # 데이터 전송 객체
└── common/          # 공통 유틸리티
```

### 3.2 핵심 컴포넌트

#### RouterConfig
```java
@Configuration
public class RouterConfig {
    
    @Bean
    public RouterFunction<ServerResponse> routerFunction(
            CommandHandler commandHandler,
            QueryHandler queryHandler) {
        return route()
            .POST("/commands/**", commandHandler::handleCommand)
            .GET("/queries/**", queryHandler::handleQuery)
            .build();
    }
}
```

#### QueryHandler (Redis → MongoDB Fallback)
```java
@Component
public class QueryHandler {
    
    public Mono<ServerResponse> handleQuery(ServerRequest request) {
        return extractQueryParams(request)
            .flatMap(this::queryWithFallback)
            .flatMap(result -> ServerResponse.ok().bodyValue(result))
            .onErrorResume(this::handleError);
    }
    
    private Mono<Object> queryWithFallback(QueryParams params) {
        return redisRepository.findByKey(params.getKey())
            .switchIfEmpty(mongoRepository.findByCondition(params))
            .doOnNext(result -> cacheToRedis(params.getKey(), result));
    }
}
```

## 4. 개발 단계별 계획

### 4.1 Phase 1: 기반 구조 (1주)
- [ ] 프로젝트 초기화 및 의존성 설정
- [ ] 기본 설정 파일 작성
- [ ] Health Check 엔드포인트
- [ ] 로깅 및 모니터링 설정

### 4.2 Phase 2: Query 기능 (2주)
- [ ] Redis 연동 설정
- [ ] MongoDB 연동 설정
- [ ] Query Router 구현
- [ ] Redis → MongoDB Fallback 로직
- [ ] TTL 관리 로직

### 4.3 Phase 3: Command 기능 (1주)
- [ ] MSA 클라이언트 구현 (WebClient)
- [ ] Command Router 구현
- [ ] 로드 밸런싱 및 서킷 브레이커
- [ ] 에러 처리 및 재시도 로직

### 4.4 Phase 4: 통합 및 최적화 (2주)
- [ ] 통합 테스트 작성
- [ ] 성능 테스트 및 튜닝
- [ ] 모니터링 대시보드 설정
- [ ] 운영 문서 작성

## 5. 공통 패턴 및 유틸리티

### 5.1 에러 처리 패턴
```java
public class ErrorHandler {
    
    public static <T> Mono<T> handleWithFallback(
            Mono<T> primary, 
            Supplier<Mono<T>> fallback) {
        return primary
            .onErrorResume(TimeoutException.class, e -> fallback.get())
            .onErrorResume(ConnectException.class, e -> fallback.get())
            .onErrorMap(Exception.class, BusinessException::new);
    }
}
```

### 5.2 캐시 관리 유틸리티
```java
public class CacheUtil {
    
    public static <T> Mono<T> getWithTTL(
            ReactiveRedisTemplate<String, T> redisTemplate,
            String key,
            Duration ttl,
            Supplier<Mono<T>> dataSupplier) {
        
        return redisTemplate.opsForValue().get(key)
            .switchIfEmpty(dataSupplier.get()
                .flatMap(data -> redisTemplate.opsForValue()
                    .set(key, data, ttl)
                    .thenReturn(data)));
    }
}
```

## 6. 테스트 전략

### 6.1 단위 테스트
- `StepVerifier`를 사용한 Reactive 테스트
- `@WebFluxTest` 어노테이션 활용
- Mock 기반 의존성 격리

### 6.2 통합 테스트
- `@SpringBootTest(webEnvironment = RANDOM_PORT)`
- TestContainers를 활용한 Redis/MongoDB 테스트
- WebTestClient를 사용한 E2E 테스트

### 6.3 성능 테스트
- JMeter 또는 K6를 활용한 부하 테스트
- 메모리 및 스레드 사용량 모니터링
- 지연시간 및 처리량 측정

## 7. 모니터링 및 운영

### 7.1 메트릭 수집
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### 7.2 핵심 지표
- 요청 처리량 (RPS)
- 응답 시간 (P50, P95, P99)
- 에러율
- Redis/MongoDB 연결 상태
- MSA 호출 성공률

### 7.3 로깅 전략
- 구조화된 로깅 (JSON 형태)
- 요청별 Trace ID 관리
- MDC(Mapped Diagnostic Context) 활용

## 8. 배포 및 운영 고려사항

### 8.1 배포 전략
- Blue-Green 배포
- Rolling Update
- 무중단 배포를 위한 Graceful Shutdown

### 8.2 설정 관리
- 환경별 설정 분리
- Spring Cloud Config 활용
- 민감 정보 암호화

### 8.3 확장성 고려
- 수평적 확장 (Pod 증설)
- 연결 풀 튜닝
- JVM 메모리 최적화

## 9. 체크리스트

### 개발 완료 기준
- [ ] 모든 API 엔드포인트 정상 동작
- [ ] 단위/통합 테스트 커버리지 80% 이상
- [ ] 성능 요구사항 충족 (목표 RPS 달성)
- [ ] 모니터링 대시보드 구축
- [ ] 운영 문서 완성

### 운영 준비 기준
- [ ] 장애 시나리오 대응 방안 수립
- [ ] 백업 및 복구 절차 마련
- [ ] 보안 검토 완료
- [ ] 팀 교육 및 인수인계 완료

## 10. 참고 자료

- [Spring WebFlux 공식 문서](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
- [Reactive Redis 사용법](https://docs.spring.io/spring-data/redis/docs/current/reference/html/#redis:reactive)
- [Reactive MongoDB 가이드](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mongo.reactive)
- [WebFlux 테스트 가이드](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#webtestclient)