# 실행 계획 — v1 MVP

> 현재 진행 중인 구현 계획입니다.
> 완료된 항목은 `completed/` 폴더로 이동하세요.

---

## 목표

**v1 MVP** — 알림 자동 파싱 + 거래 내역 확인이 가능한 최소 기능 앱

---

## 마일스톤

### Phase 1: 프로젝트 셋업 (1주)
- [ ] Android 프로젝트 생성 (Kotlin, Compose)
- [ ] Hilt, Retrofit, Room, Navigation 의존성 세팅
- [ ] 패키지 구조 생성 (`presentation/domain/data/service/di`)
- [ ] `local.properties` 기반 환경변수 설정
- [ ] GitHub Actions CI 기본 세팅 (빌드 체크)

### Phase 2: 알림 파싱 (2주)
- [ ] `TransactionNotificationService` 구현
- [ ] `NotificationParser` — 카카오페이 파싱
- [ ] `NotificationParser` — 토스 파싱
- [ ] `NotificationParser` — 네이버페이 파싱
- [ ] `NotificationParser` — 주요 카드사 SMS/알림 파싱
- [ ] Room `TransactionEntity` + DAO 구현
- [ ] 파싱 결과 저장 + 로컬 확인

### Phase 3: API 연동 (1주)
- [ ] Swagger 문서 기반 Retrofit interface 생성
- [ ] `AuthInterceptor` 구현 (토큰 헤더 자동 주입)
- [ ] `TransactionRepository` 서버 동기화 구현
- [ ] `WorkManager` 기반 백그라운드 동기화

### Phase 4: UI 구현 (2주)
- [ ] 온보딩 화면 (권한 요청 포함)
- [ ] 홈 화면 (오늘 소비 요약)
- [ ] 거래 내역 목록 화면
- [ ] 거래 내역 수정 화면
- [ ] 월별 소비 통계 화면

### Phase 5: QA + 출시 준비 (1주)
- [ ] 파싱 정확도 테스트 (실제 알림 기반)
- [ ] 엣지 케이스 처리 (중복 감지, 파싱 실패 등)
- [ ] Play Store 출시 준비 (스크린샷, 설명문)

---

## 현재 진행 중

> 여기에 지금 작업 중인 항목을 업데이트하세요.

- [ ] Phase 1 진행 중
