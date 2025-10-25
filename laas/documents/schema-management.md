# Schema Registry 운영 및 관리 가이드

## 개요

이 문서는 AWS Glue Schema Registry를 활용한 Avro 스키마 관리 방법과 대규모 조직에서의 운영 프로세스를 다룹니다.

## 목차

1. [Schema Registry 구조](#schema-registry-구조)
2. [스키마 호환성](#스키마-호환성)
3. [운영 프로세스](#운영-프로세스)
4. [Backend 코드 관리](#backend-코드-관리)
5. [모범 사례](#모범-사례)

## Schema Registry 구조

### Registry 계층 구조

AWS Glue Schema Registry는 **Registry → Schema → Version** 3단계 구조로 구성됩니다.

```
laas-event-registry/
├── order.order.created
│   ├── Version 1 (ID: abc123) - 기본 필드
│   ├── Version 2 (ID: def456) - merchantId 추가
│   └── Version 3 (ID: ghi789) - items 배열 추가
├── delivery.delivery.started
│   ├── Version 1 (ID: jkl012)
│   └── Version 2 (ID: mno345)
└── dispatch.dispatch.dispatched
    └── Version 1 (ID: pqr678)
```

### 주요 특징

- **Registry**: 논리적 그룹 (도메인/서비스별)
- **Schema**: 개별 이벤트 타입 
- **Version**: 스키마 변경 이력 (누적 관리)
- **Version ID**: 각 버전의 고유 식별자 (메시지에 포함)

## 스키마 호환성

### 호환성 모드

| 모드 | 설명 | 사용 시나리오 |
|------|------|---------------|
| `BACKWARD` | 새 Consumer가 이전 Producer 메시지 읽기 가능 | 일반적인 필드 추가 |
| `FORWARD` | 이전 Consumer가 새 Producer 메시지 읽기 가능 | 필드 제거 |
| `FULL` | BACKWARD + FORWARD 모두 | 엄격한 호환성 요구 |
| `NONE` | 호환성 체크 안함 | Breaking Change (주의 필요) |

### 호환성 확인 시점

#### 1. 스키마 설계 시점 (가장 빠름)
```bash
# Avro 도구로 호환성 체크
java -jar avro-tools-1.11.3.jar compatible old_schema.avsc new_schema.avsc
```

#### 2. Registry 등록 시점 (가장 확실함)
```bash
# 등록 전 호환성 체크
aws glue check-schema-version-validity \
  --schema-id arn:aws:glue:region:account:schema/registry/schema-name \
  --schema-definition file://new_schema.avsc

# 등록 시 자동 호환성 체크 (실패 시 등록 거부)
aws glue register-schema-version \
  --schema-id arn:aws:glue:region:account:schema/registry/schema-name \
  --schema-definition file://new_schema.avsc
```

#### 3. CI/CD 파이프라인 시점
- 자동화된 호환성 테스트
- Pull Request 시 자동 검증
- 배포 전 최종 확인

### 안전한 스키마 진화 예시

#### ✅ BACKWARD 호환 가능한 변경
```json
// 기본값이 있는 필드 추가
{
  "name": "newField",
  "type": "string", 
  "default": ""
}

// Optional 필드를 Union 타입으로 추가
{
  "name": "optionalField",
  "type": ["null", "string"],
  "default": null
}
```

#### ❌ BACKWARD 호환 불가능한 변경
```json
// 기본값 없는 필수 필드 추가
{
  "name": "requiredField",
  "type": "string"  // default 없음
}

// 기존 필드 타입 변경
{
  "name": "orderId",
  "type": "string"  // 기존 long에서 string으로 변경
}
```

## 운영 프로세스

### 1. 스키마 변경 요청 및 설계

#### Data/Platform Team 역할
- 새로운 이벤트나 기존 스키마 변경 요청 접수
- 도메인 전문가와 함께 스키마 설계 검토
- 호환성 영향도 분석 (BACKWARD/FORWARD 호환성)
- 변경 사유와 영향 범위 문서화

#### 거버넌스 위원회 검토
- 스키마 변경이 다른 팀/서비스에 미치는 영향 검토
- Breaking Change 여부 판단
- 마이그레이션 계획 승인

### 2. 스키마 개발 및 검증

#### 개발 환경에서 검증
- 개발자가 로컬/개발 환경에서 스키마 작성
- Auto-registration으로 개발 Registry에 등록
- 호환성 테스트 및 integration 테스트

#### 코드 리뷰
- 스키마 파일을 Git에서 코드 리뷰
- 필드명, 타입, 기본값 등 검토
- 네이밍 컨벤션 준수 여부 확인

### 3. 스키마 배포 프로세스

#### 수동 등록 금지 원칙
- **Console 직접 등록 금지** (사람이 직접 등록하지 않음)
- **Infrastructure as Code** 사용 (Terraform, CloudFormation)
- **CI/CD Pipeline**을 통한 자동 배포

#### 배포 단계
1. **Staging 환경**에 스키마 등록
2. **호환성 자동 테스트** 실행
3. **승인 후 Production** 배포
4. **배포 결과 모니터링**

### 4. Producer/Consumer 업데이트

#### Producer 업데이트
- **점진적 배포**: Blue-Green 또는 Canary 배포
- **새 스키마로 메시지 발행** 시작
- **기존 스키마와 병행 운영** (필요시)
- **메시지 발행 성공률 모니터링**

#### Consumer 업데이트
- **호환성 기반 순서 결정**:
  - BACKWARD 호환: Consumer 먼저 업데이트
  - FORWARD 호환: Producer 먼저 업데이트
  - FULL 호환: 순서 무관
- **독립적 배포**: 각 Consumer 팀이 자신의 일정에 맞춰 업데이트
- **업데이트 전후 메시지 모두 처리** 가능

### 실제 업무 플로우 예시

#### 새로운 이벤트 추가 (order.payment.completed)

**1주차: 설계 및 승인**
- Product Team: 결제 완료 이벤트 필요성 제기
- Data Team: 스키마 초안 작성
- Architecture Review: 설계 검토 및 승인

**2주차: 개발**
- Backend Team: Producer 코드 개발
- Consumer Team들: Consumer 코드 개발 (병렬)
- 개발 환경에서 E2E 테스트

**3주차: 배포**
- Staging 배포 및 통합 테스트
- Production 스키마 등록 (CI/CD)
- Producer 배포
- Consumer 순차 배포

#### 기존 스키마 변경 (order.order.created에 필드 추가)

**설계 단계**
- 영향도 분석: 몇 개 팀의 Consumer가 영향 받는지
- 호환성 검토: BACKWARD 호환 가능한지
- 마이그레이션 계획: 필요 시 점진적 전환 계획

**커뮤니케이션**
- 관련 팀들에게 변경 사항 공지
- 업데이트 일정 협의
- 문서 업데이트

**배포 및 모니터링**
- 스키마 새 버전 등록
- Producer 업데이트 배포
- Consumer 팀별 자율적 업데이트
- 메트릭 모니터링 및 이슈 대응

## Backend 코드 관리

### 1. 공유 라이브러리 방식 (권장)

#### 중앙 Schema 저장소 구조
```
event-schemas/
├── src/main/avro/
│   ├── order/
│   │   ├── OrderCreatedEvent.avsc
│   │   └── OrderUpdatedEvent.avsc
│   ├── delivery/
│   │   └── DeliveryStartedEvent.avsc
│   └── common/
│       └── EventMetadata.avsc
├── build.gradle
└── settings.gradle
```

#### 의존성 관리
- Schema 라이브러리를 Maven/Gradle 의존성으로 추가
- 버전 관리를 통해 안정적인 스키마 참조
- 팀별로 필요한 스키마 버전 선택 가능

#### Backend 서비스에서 사용
```gradle
// build.gradle
dependencies {
    implementation 'com.vroong.laas:event-schemas:1.2.0'
}
```

#### 장점/단점
- **장점**: 중앙 집중식 관리, 일관성 보장, 재사용성, 타입 안전성
- **단점**: 스키마 변경 시 라이브러리 업데이트 필요

### 2. Schema Registry 기반 동적 생성

#### Runtime Schema 다운로드
- 애플리케이션 시작 시 Schema Registry에서 스키마 다운로드
- 런타임에 동적으로 Java 클래스 생성
- 최신 스키마 자동 반영

#### 코드 생성 도구
- Schema Registry의 REST API를 통해 스키마 조회
- Gradle/Maven 플러그인으로 빌드 시 클래스 생성
- CI/CD에서 자동 업데이트

#### 장점/단점
- **장점**: 항상 최신 스키마 사용, 별도 라이브러리 불필요
- **단점**: 네트워크 의존성, 런타임 오버헤드

### 3. Git Submodule 방식

#### 스키마 저장소 연결
- 공통 스키마 저장소를 Git Submodule로 참조
- 각 서비스별로 필요한 스키마 버전 고정
- 로컬 빌드 시 Avro 클래스 생성

#### 버전 관리
- Submodule을 특정 커밋/태그로 고정
- 스키마 업데이트 시 Submodule 포인터 변경
- 팀별 독립적인 업데이트 일정

#### 장점/단점
- **장점**: 버전 명확성, 오프라인 작업 가능
- **단점**: Submodule 관리 복잡성

## 팀별 역할 분담

### Platform/Data Team
- **스키마 설계 가이드라인** 제공
- **중앙 Schema 저장소** 운영
- **빌드 도구 및 플러그인** 개발/유지보수
- **Schema Registry** 운영

### Backend Team (Producer)
- **비즈니스 로직에 따른 스키마 요구사항** 정의
- **Producer 코드** 구현
- **스키마 라이브러리 의존성** 관리
- **메시지 발행 로직** 구현

### Consumer Team
- **필요한 스키마 버전** 선택
- **Consumer 로직** 구현
- **호환성 테스트** 수행
- **독립적인 배포 일정** 관리

## 거버넌스 및 정책

### Schema Registry 접근 권한
- **읽기**: 모든 개발자
- **개발 환경 쓰기**: 개발자 (auto-registration)
- **운영 환경 쓰기**: Platform Team만 (CI/CD via)
- **스키마 삭제**: 아키텍트/Platform Lead만

### 정책 및 규칙
- **네이밍 컨벤션**: `{domain}.{entity}.{action}` (예: order.order.created)
- **호환성 정책**: 기본적으로 BACKWARD 호환성 요구
- **Breaking Change**: 별도 승인 프로세스 필요
- **스키마 문서화**: 필드별 설명 필수

## 모범 사례

### 1. 의존성 관리
- **정확한 버전 명시**: `1.2.0` (SNAPSHOT 사용 금지)
- **호환성 매트릭스**: 어떤 Producer/Consumer 버전이 호환되는지 문서화
- **점진적 업그레이드**: 한 번에 여러 버전 점프 금지

### 2. 코드 품질
- **Generated 코드 Git 제외**: `.gitignore`에 생성된 Java 클래스 제외
- **Schema 파일만 버전 관리**: `.avsc` 파일만 Git으로 관리
- **빌드 시 재생성**: 항상 최신 스키마로 클래스 생성

### 3. 테스트
- **Schema Evolution 테스트**: 이전 버전과의 호환성 자동 테스트
- **Round-trip 테스트**: 직렬화/역직렬화 검증
- **Integration 테스트**: 실제 Schema Registry와 연동 테스트

### 4. 모니터링 및 알림
- **스키마 등록 실패 알림**
- **호환성 위반 감지**
- **메시지 처리 지연 알림**
- **Consumer Lag 모니터링**

### 5. 문서화
- **스키마 카탈로그**: 모든 스키마 중앙 관리
- **API 문서 자동 생성**
- **변경 이력 추적**
- **Consumer 의존성 맵**

## 핵심 원칙

1. **수동 Console 작업 금지** - 모든 변경은 코드/파이프라인을 통해
2. **호환성 우선** - Breaking Change 최소화
3. **독립적 배포** - 팀 간 강한 의존성 제거
4. **투명성** - 모든 변경 사항 추적 및 공유
5. **자동화** - 사람의 실수 방지

## 결론

Schema Registry를 활용한 체계적인 스키마 관리는 마이크로서비스 아키텍처에서 **안전하고 확장 가능한 이벤트 기반 통신**을 가능하게 합니다. 

- **중앙 집중식 스키마 관리**로 일관성 보장
- **호환성 기반 진화**로 안전한 변경
- **팀별 독립적 배포**로 개발 생산성 향상
- **자동화된 프로세스**로 운영 효율성 증대

이러한 접근 방식을 통해 대규모 조직에서도 효과적인 스키마 거버넌스를 달성할 수 있습니다.