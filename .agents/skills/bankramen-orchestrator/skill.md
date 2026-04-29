---
name: bankramen-orchestrator
description: "뱅크라면 Android 앱 작업을 팀 단위로 조율하는 오케스트레이터. Figma 화면 구현, NotificationListenerService 기능 추가, Swagger/OpenAPI 자동 생성, MVVM 구조 점검, QA 검수가 필요한 다단계 작업이면 반드시 이 스킬을 사용한다."
---

# Bankramen Orchestrator

뱅크라면 Android 앱 작업을 여러 전문 에이전트로 분배하고 통합한다.

## 실행 모드: 에이전트 팀

## 에이전트 구성

| 팀원 | 역할 | 권장 스킬 | 출력 |
|------|------|-----------|------|
| bankramen-screen-agent | Figma 기반 화면 구현 | implement-screen, bankramen-figma-compose, bankramen-fast-mvvm | `_workspace/01_screen.md` |
| bankramen-openapi-agent | Swagger/OpenAPI 자동 생성 | bankramen-openapi-client | `_workspace/02_openapi.md` |
| bankramen-notification-agent | 알림 수집/파싱 구현 | bankramen-notification-parsing, bankramen-fast-mvvm | `_workspace/03_notification.md` |
| bankramen-qa-agent | 구조/품질 검증 | requesting-code-review, review-template | `_workspace/04_qa.md` |

## 워크플로우

### Phase 1: 준비
1. 사용자 입력에서 작업 유형을 분류한다.
2. 작업 디렉토리에 `_workspace/`를 만든다.
3. 입력 자료를 `_workspace/00_input/`에 저장한다.
4. 기존 `.agent/*.md` 문서와 사용자 최신 지시가 충돌하면 사용자 지시를 우선한다.

### Phase 2: 팀 구성
1. 화면/알림/API/QA 에이전트를 팀으로 구성한다.
2. 작업을 다음 기준으로 배정한다.
   - Figma 링크 포함: screen agent 선행
   - Swagger/OpenAPI 포함: openapi agent 병렬 또는 선행
   - NotificationListenerService 포함: notification agent 병렬
   - 구현 완료 후: QA agent 검증

### Phase 3: 병렬 실행
- 화면, API, 알림은 가능한 범위에서 병렬로 진행한다.
- 서로 필요한 데이터 shape, 권한 플로우, navigation 요구사항은 메시지로 공유한다.
- generated code 경계와 UI 상태 모델은 QA가 중간 점검한다.

### Phase 4: 통합
1. 각 팀원의 `_workspace/*.md` 결과를 수집한다.
2. 충돌 지점을 정리한다.
3. 실제 파일 변경 또는 후속 작업 순서를 확정한다.

### Phase 5: 정리
1. `_workspace/`는 보존한다.
2. 사용자에게 구현 결과와 다음 입력 포맷을 알려준다.

## 입력 라우팅 규칙
- **Figma 링크**: 화면명 + 링크 + 필요한 인터랙션을 screen agent에 전달한다.
- **Swagger 링크/파일**: spec 위치 + 인증 방식 + baseUrl 정책을 openapi agent에 전달한다.
- **알림 파싱 요청**: 대상 앱 패키지 + 샘플 문구 + 저장 필드를 notification agent에 전달한다.

## 에러 핸들링

| 상황 | 전략 |
|------|------|
| Figma 정보 부족 | 누락 레이어/상태를 적고 최소 구현 범위만 진행 |
| OpenAPI 스펙 품질 문제 | 생성기 옵션으로 흡수 가능한 문제와 수동 보정 포인트를 분리 보고 |
| 알림 포맷 불안정 | 공통 parser + 패키지별 parser로 격리 |
| QA 반려 | 반려 사유를 구현 에이전트에 재배정 |

## 테스트 시나리오

### 정상 흐름
1. 사용자가 Figma 링크와 Swagger 링크를 준다.
2. 화면/API/알림 작업이 병렬로 진행된다.
3. QA가 Route/Screen 분리, generated code 경계, 권한 플로우를 확인한다.
4. 예상 결과: 구현 파일과 `_workspace` 산출물이 생성된다.

### 에러 흐름
1. OpenAPI 스펙이 불완전하다.
2. openapi agent가 생성 실패 원인과 우회 설정을 기록한다.
3. 화면/알림 작업은 계속 진행한다.
4. 최종 보고서에 API 자동화 보류 지점을 명시한다.
