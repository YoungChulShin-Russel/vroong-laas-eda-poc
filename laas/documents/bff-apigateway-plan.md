# API Gateway 도입 및 CQRS 아키텍처 재설계 계획

## 🎯 프로젝트 개요

### 목적
- **현재 문제**: Command API가 100개 이상 추가 예정, Custom Code 방식은 유지보수 비용 과다
- **해결 방안**: API Gateway로 Command 자동 라우팅, Read Model 분리로 CQRS 명확화
- **기대 효과**: 신규 API 추가 시 코드 수정 불필요, 독립적 스케일링, 명확한 역할 분리

---

## 🏗️ 아키텍처 변경

### AS-IS (현재)
```
서버 구성 (5개):
1. Order Service (8080) - Write Model
2. Dispatch Service (8081) - Write Model  
3. Delivery Service (8082) - Write Model
4. Projection Service (8083) - Event Consumer만
5. BFF (8084) - Command Controller + Query Controller

문제점:
- BFF에서 Command를 수동 라우팅 (Controller/Service/Client 코드 필요)
- 신규 Command API마다 BFF 코드 추가 (30분/개 × 100개 = 50시간)
- Projection과 Query가 분리되어 있어 추가 통신 필요
- BFF 역할이 모호 (Gateway인가? BFF인가?)
```

### TO-BE (목표)
```
서버 구성 (5개):
1. API Gateway (8080) - Command 전용 자동 라우팅
2. Order Service (8081) - Write Model + Query API (Fallback용)
3. Dispatch Service (8082) - Write Model + Query API (Fallback용)
4. Delivery Service (8083) - Write Model + Query API (Fallback용)
5. Read Model Service (8084) - Event Consumer + Projection + Query API

특징:
✅ Command: API Gateway가 설정 기반 자동 라우팅 (코드 불필요)
✅ Query: Read Model Service가 완전 책임 (Event 수신 → Projection → API)
✅ CQRS 명확한 분리 (Write Side vs Read Side)
✅ 독립적 스케일링 준비 (Profile 기반)
```

---

## 📋 주요 설계 결정

### 1. API Gateway 도입 (Command 전용)

**역할**:
- Command 요청(POST, PUT, PATCH, DELETE)만 처리
- MSA 서비스로 자동 라우팅
- Circuit Breaker, Retry, 로깅

**기술 스택**:
- Spring Cloud Gateway
- Resilience4j Circuit Breaker
- Netty

**장점**:
- 신규 API 추가 시 BFF 코드 수정 불필요
- 설정 기반 (선언적)
- 검증된 프레임워크

**라우팅 규칙**:
```
/api/v1/orders/** + POST/PUT/PATCH/DELETE → Order Service
/api/v1/dispatches/** + POST/PUT/PATCH/DELETE → Dispatch Service  
/api/v1/deliveries/** + POST/PUT/PATCH/DELETE → Delivery Service
```

---

### 2. BFF 제거 → API Gateway로 대체

**결정 이유**:
- 현재 BFF의 Command 로직은 단순 프록시만 수행
- 복잡한 비즈니스 로직, Request/Response 변환 없음
- API Gateway가 더 적합한 역할
- 코드 유지보수 부담 제거

**기존 BFF의 역할 이관**:
- Command 라우팅 → API Gateway
- Query 처리 → Read Model Service

---

### 3. Projection + Query Service 통합 → Read Model Service

**통합 대상**:
- Projection Service (Event Consumer)
- Query API (새로 구축)

**새 이름**: Read Model Service

**역할**:
1. Kafka Event 구독
2. Projection 생성 및 저장 (Redis + MongoDB)
3. Query REST API 제공
4. Fallback 로직 (Write Service 호출)

**통합 이유**:
- 응집도 높음 (Event → Projection → API가 하나의 흐름)
- 같은 데이터 다룸 (Read Model)
- 서버 수 최소화 (6개 → 5개)
- 초기 단계에 적합
- 필요 시 분리 가능하게 설계

**독립 스케일링 대비**:
- 패키지 명확히 분리 (consumer, handler, controller)
- Profile 지원 (consumer, api, full)
- 향후 Event 처리량 증가 시 분리 가능

---

### 4. Query Fallback 전략

**문제**: Read Model Service 장애 시 조회 불가

**해결**:
- Primary: Read Model Service (Redis/MongoDB)
- Fallback: Write Service의 Query API
- 구현: Read Model Service가 Write Service 직접 호출

**Fallback 적용 대상**:
- Critical Query (주문 상태, 배송 위치, 배차 상태)
- Non-Critical Query는 503 에러 반환

**장점**:
- 가용성 향상
- 사용자 경험 개선
- Netflix, Uber 등 대형 플랫폼도 사용

**주의사항**:
- Write DB 부하 모니터링
- Circuit Breaker로 보호
- Fallback 사용 메트릭 추적

---

### 5. 데이터 접근 규약

**현재 이슈**: Read Model Service가 Redis/MongoDB 직접 조회

**규약**:
1. Redis/MongoDB 소유자: Read Model Service
2. 쓰기 권한: Read Model Service만 (Event Consumer)
3. 읽기 권한: Read Model Service만
4. 스키마 변경: Read Model 팀 주도

**향후 옵션** (필요 시):
- Read Model Service에 REST API 추가
- 다른 서비스는 API 통해서만 접근
- 더 느슨한 결합

---

## 🗓️ 작업 단계

### Phase 0: 준비 및 검토 (1일)

**목표**: 아키텍처 검토 및 기술 검증

**작업**:
- [ ] Spring Cloud Gateway 학습 (공식 문서)
- [ ] Gateway와 WebFlux 혼합 가능성 확인
- [ ] 간단한 PoC 테스트
- [ ] 팀 리뷰 및 승인

**산출물**:
- 기술 검증 보고서
- 팀 승인

---

### Phase 1: API Gateway 구축 (2일)

**목표**: Command 라우팅용 API Gateway 서버 생성

**작업**:
1. **프로젝트 생성**
   - Spring Cloud Gateway 기반 프로젝트
   - 포트: 8080
   - Java 21, Spring Boot 3.x

2. **라우팅 설정**
   - Order/Dispatch/Delivery 라우팅 규칙
   - HTTP Method 필터 (POST/PUT/PATCH/DELETE)
   - Circuit Breaker 설정
   - Retry 정책

3. **전역 필터 구현**
   - Request ID 생성/전달
   - 로깅 (요청/응답)
   - 모니터링 메트릭

4. **Fallback Handler**
   - Circuit Breaker 열릴 때 에러 응답
   - 503 Service Unavailable

5. **설정 외부화**
   - MSA 서비스 URL (application.yml)
   - Circuit Breaker 설정
   - Timeout 설정

**테스트**:
- [ ] 각 MSA로 라우팅 검증
- [ ] Circuit Breaker 동작 확인
- [ ] Timeout 동작 확인
- [ ] 부하 테스트

**산출물**:
- api-gateway 프로젝트
- 설정 문서
- 테스트 결과

---

### Phase 2: Read Model Service 구축 (3일)

**목표**: Event Consumer + Query API 통합 서버 생성

**작업**:
1. **프로젝트 구조 재구성**
   - 기존 Projection 프로젝트 확장
   - 또는 새 프로젝트 생성
   - 포트: 8084

2. **패키지 구조 설계**
   ```
   read-model-service/
   ├── consumer/       # Event Consumer
   ├── handler/        # Projection Handler  
   ├── controller/     # Query REST API
   ├── service/        # 공통 로직 + Fallback
   ├── repository/     # Redis/MongoDB
   └── config/         # 설정
   ```

3. **Event Consumer 구현**
   - Kafka 구독 (order.event, dispatch.event, delivery.event)
   - Event → Projection 변환
   - Redis + MongoDB 저장

4. **Query REST API 구현**
   - GET /api/v1/orders/{orderId}
   - GET /api/v1/dispatches/{dispatchId}
   - GET /api/v1/deliveries/{deliveryId}
   - Redis → MongoDB Fallback

5. **Fallback 로직 구현**
   - Write Service Client 추가
   - Cache Miss 시 Write Service 호출
   - Circuit Breaker 적용

6. **Profile 기반 활성화**
   ```yaml
   # Profile: full (기본)
   kafka.enabled: true
   api.enabled: true
   
   # Profile: consumer (Event 처리만)
   kafka.enabled: true
   api.enabled: false
   
   # Profile: api (Query API만)
   kafka.enabled: false
   api.enabled: true
   ```

**테스트**:
- [ ] Event 수신 및 Projection 생성 확인
- [ ] Query API 동작 확인
- [ ] Redis/MongoDB Fallback 확인
- [ ] Write Service Fallback 확인
- [ ] Profile별 실행 확인

**산출물**:
- read-model-service 프로젝트
- API 문서
- Profile 가이드

---

### Phase 3: 기존 BFF 제거 (1일)

**목표**: BFF 프로젝트 제거 및 정리

**작업**:
1. **Command 관련 코드 삭제**
   - Controller (OrderCommandController 등)
   - Service (OrderCommandService 등)
   - Client (OrderServiceClient 등)
   - Config (WebClientConfig 등)

2. **Query 관련 코드 이관**
   - BFF의 Query Controller → Read Model Service로 이동
   - BFF의 Query Service → Read Model Service로 이동
   - Repository 코드 → Read Model Service로 이동

3. **BFF 프로젝트 제거**
   - bff/ 디렉토리 전체 제거
   - 또는 아카이브

**테스트**:
- [ ] API Gateway로 Command 요청 성공
- [ ] Read Model Service로 Query 요청 성공
- [ ] 전체 플로우 E2E 테스트

**산출물**:
- 정리된 프로젝트 구조
- 삭제 내역 문서

---

### Phase 4: 통합 테스트 및 검증 (2일)

**목표**: 전체 시스템 통합 테스트

**작업**:
1. **기능 테스트**
   - Command 플로우 (API Gateway → MSA)
   - Query 플로우 (Read Model Service → Redis/MongoDB)
   - Event 플로우 (MSA → Kafka → Read Model)
   - Fallback 플로우 (Read Model → Write Service)

2. **장애 시나리오 테스트**
   - MSA 서비스 다운 → API Gateway Circuit Breaker
   - Read Model Service 다운 → Fallback 동작
   - Kafka 다운 → Event Consumer 재시도
   - Redis 다운 → MongoDB Fallback

3. **성능 테스트**
   - Command 처리량 (API Gateway)
   - Query 응답 시간 (Read Model)
   - Event 처리 지연 (Lag)

4. **모니터링 설정**
   - Prometheus 메트릭
   - Grafana 대시보드
   - Alert 설정

**테스트 케이스**:
- [ ] 주문 생성 → 이벤트 발행 → Projection 생성 → 조회 성공
- [ ] 배차 수락 → 이벤트 발행 → Projection 업데이트 → 조회 성공
- [ ] 배송 완료 → 이벤트 발행 → Projection 업데이트 → 조회 성공
- [ ] Circuit Breaker 동작 확인
- [ ] Fallback 동작 확인

**산출물**:
- 테스트 결과 보고서
- 성능 측정 결과
- 모니터링 대시보드

---

### Phase 5: 문서화 및 배포 (1일)

**목표**: 운영 문서 작성 및 배포 준비

**작업**:
1. **API 문서 작성**
   - Command API (API Gateway)
   - Query API (Read Model Service)
   - 신규 API 추가 가이드

2. **아키텍처 문서**
   - 서버 구성도
   - 데이터 플로우
   - 장애 시나리오별 대응

3. **운영 가이드**
   - 배포 방법
   - 모니터링 지표
   - 트러블슈팅
   - Fallback 정책

4. **팀 교육**
   - 아키텍처 변경 사항
   - 신규 API 추가 방법
   - 모니터링 방법

**산출물**:
- API 문서
- 아키텍처 문서
- 운영 가이드
- 교육 자료

---

## 📊 최종 서버 구성

### Write Side (Command)
```
1. API Gateway (8080)
   - 역할: Command 라우팅
   - 기술: Spring Cloud Gateway
   - 스케일: 1-2 Pod (가볍움)

2. Order Service (8081)
   - 역할: 주문 비즈니스 로직
   - 기술: Spring Boot (Traditional)
   - DB: MySQL
   - 스케일: 트랜잭션에 따라

3. Dispatch Service (8082)
   - 역할: 배차 비즈니스 로직
   - 기술: Spring Boot (Traditional)
   - DB: MySQL
   - 스케일: 트랜잭션에 따라

4. Delivery Service (8083)
   - 역할: 배송 비즈니스 로직
   - 기술: Spring Boot (Traditional)
   - DB: MySQL
   - 스케일: 트랜잭션에 따라
```

### Read Side (Query)
```
5. Read Model Service (8084)
   - 역할: Event Consumer + Query API
   - 기술: Spring Boot WebFlux (Reactive)
   - 저장소: Redis (Cache) + MongoDB (Persistent)
   - 스케일: Read 트래픽에 따라 (독립적)
   
   구성 요소:
   a. Event Consumer
      - Kafka 구독
      - Projection 생성
      
   b. Query API
      - Redis → MongoDB Fallback
      - Write Service Fallback (Critical Query)
      
   향후 분리 가능:
   - Profile 기반 (consumer, api)
   - 독립 배포
   - 필요 시 완전 분리
```

---

## ✅ 체크리스트

### 설계 검토
- [ ] 아키텍처 다이어그램 작성
- [ ] 기술 스택 검증
- [ ] 데이터 플로우 확인
- [ ] 장애 시나리오 분석
- [ ] 팀 리뷰 완료

### Phase 1: API Gateway
- [ ] 프로젝트 생성
- [ ] 라우팅 설정
- [ ] Circuit Breaker 설정
- [ ] 전역 필터 구현
- [ ] 테스트 완료

### Phase 2: Read Model Service
- [ ] 프로젝트 구조 설계
- [ ] Event Consumer 구현
- [ ] Query API 구현
- [ ] Fallback 로직 구현
- [ ] Profile 설정
- [ ] 테스트 완료

### Phase 3: BFF 제거
- [ ] Command 코드 삭제
- [ ] Query 코드 이관
- [ ] BFF 프로젝트 제거
- [ ] 테스트 완료

### Phase 4: 통합 테스트
- [ ] 기능 테스트
- [ ] 장애 시나리오 테스트
- [ ] 성능 테스트
- [ ] 모니터링 설정

### Phase 5: 문서화
- [ ] API 문서
- [ ] 아키텍처 문서
- [ ] 운영 가이드
- [ ] 팀 교육

---

## 📈 예상 효과

### 개발 생산성
- 신규 Command API 추가: 30분 → 0분 (무한대 개선)
- 100개 API 기준 시간 절감: 50시간
- 코드량: ~5,000줄 → ~50줄 (100배 감소)
- 유지보수 파일: ~300개 → ~3개 (100배 감소)

### 시스템 성능
- Command 응답 시간: 동일 (Gateway 오버헤드 미미)
- Query 응답 시간: 동일 또는 개선
- 독립 스케일링: Event 처리와 Query API 분리 가능

### 아키텍처 품질
- 역할 명확성: 매우 높음 (CQRS 명확)
- 확장성: 매우 높음 (독립 스케일링)
- 유지보수성: 매우 높음 (코드량 감소)
- 장애 격리: 높음 (Circuit Breaker, Fallback)

---

## 🚨 위험 요소 및 대응

### 위험 1: Read Model Service 역할 과중
**증상**: Event 처리 지연 + API 응답 느림
**대응**: 
- Profile 기반으로 분리 (consumer, api)
- 독립 배포
- 리소스 모니터링

### 위험 2: Fallback으로 Write DB 부하
**증상**: Write DB 과부하
**대응**:
- Circuit Breaker로 보호
- Critical Query만 Fallback 적용
- Fallback 사용률 모니터링

### 위험 3: API Gateway SPOF
**증상**: Gateway 다운 시 전체 Command 불가
**대응**:
- 다중 Pod 배포
- Health Check 설정
- Auto Scaling

### 위험 4: 데이터 일관성
**증상**: Event 지연으로 조회 시 최신 데이터 아님
**대응**:
- Eventual Consistency 명시
- 클라이언트에 안내
- Critical 데이터는 Fallback 활용

---

## 📚 참고 자료

### 공식 문서
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [CQRS Pattern](https://learn.microsoft.com/en-us/azure/architecture/patterns/cqrs)
- [Resilience4j](https://resilience4j.readme.io/)

### 산업계 사례
- Netflix: Zuul/API Gateway
- Uber: CQRS + Event Sourcing
- Amazon: Read Replica Fallback

---

## 🎯 마일스톤

| Phase | 기간 | 완료 목표일 | 담당자 |
|-------|------|------------|--------|
| Phase 0 | 1일 | TBD | Backend Team |
| Phase 1 | 2일 | TBD | Backend Team |
| Phase 2 | 3일 | TBD | Backend Team |
| Phase 3 | 1일 | TBD | Backend Team |
| Phase 4 | 2일 | TBD | Backend + QA |
| Phase 5 | 1일 | TBD | Backend Team |
| **총계** | **10일** | | |

---

**작성일**: 2025-01-23  
**작성자**: Backend Team  
**최종 검토**: Architecture Review  
**승인**: Tech Lead
