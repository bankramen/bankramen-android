# Bankramen × Hermes Agent 연동 요약

## 목표
Bankramen이 결제 알림을 정제·저장하고, Hermes는 선택적으로 지출 분석·예산 알림·자연어 질의를 제공한다.

## 현재 확인된 구현
- Android 알림 리스너: `TransactionNotificationService`
- 지원 결제 앱 알림을 파싱: 카카오페이, 케이뱅크, 토스, 네이버페이
- 파싱된 거래를 로컬 저장 후 Bankramen API로 업로드
- `POST /transactions/payment-notifications`는 제목·금액을 받고 서버에서 카테고리를 추천해 거래를 저장

## 권장 아키텍처
```text
Android 결제 알림
  → Bankramen Backend / DB (원본·권한 관리)
  → Hermes Agent (선택적 분석/알림)
```

### 기본: Pull 조회
Hermes가 매 거래마다 수신하지 않고, 사용자 질문 또는 일/주/월 정기 실행 때 Bankramen의 **읽기 전용 Agent API/MCP 도구**로 최신 데이터를 조회한다.

예시 API:
- `GET /agent/v1/spending-summary?month=YYYY-MM`
- `GET /agent/v1/category-comparison?from=YYYY-MM&to=YYYY-MM`
- `GET /agent/v1/transactions?...`
- `GET /agent/v1/recurring-payments`
- `GET /agent/v1/budget-status`

### 예외: Push 이벤트
아래처럼 즉시 알림 가치가 있는 경우에만 서버가 Hermes webhook으로 전달한다.
- 예산 90% 도달/초과
- 평소보다 큰 결제
- 새 정기결제 후보
- 소비 패턴 급변

## 다중 사용자 원칙
- Hermes 연동은 **사용자별 opt-in 기능**이다.
- Hermes 미사용자는 기존 Bankramen 기능만 사용하며 데이터가 외부 Agent로 전달되지 않는다.
- 거래 저장 성공은 Hermes 상태와 무관해야 한다.
- 사용자별 연동 상태, 대상 이벤트, 연결 ID/비밀값을 서버가 관리한다.

## 보안 원칙
- Android 앱에서 Hermes로 직접 전송하지 않고 **Bankramen 서버를 신뢰 경계**로 둔다.
- Hermes에 DB master/raw SQL 권한을 주지 않는다.
- Agent API는 최소 권한·읽기 전용·사용자 범위 제한·페이지/기간 제한을 적용한다.
- Hermes에는 모바일 사용자 JWT를 저장하지 않는다. 별도 짧은 수명 서비스 인증 또는 HMAC/mTLS를 사용한다.
- Hermes webhook은 HMAC 서명, 이벤트 ID 기반 멱등성, 재시도 outbox/queue를 적용한다.
- 전송 데이터는 금액·가맹점·카테고리·시각 등 최소 거래 레코드만 포함한다.
- 알림 원문, 카드/계좌번호, 잔액, 승인번호, 인증정보, OAuth token은 전송하지 않는다.

## 릴리스 전 점검
1. `NotificationDebugRepository`의 알림 원문 로그를 릴리스에서 비활성화하거나 마스킹한다.
2. 일반 `SharedPreferences`에 저장된 access/refresh token을 Android Keystore 기반 암호화 저장소로 이전한다.
3. 실제 `API_BASE_URL`이 HTTPS인지 확인한다. 평문 HTTP는 금지한다.
4. 알림 재발행에 대비해 서버에서 거래 중복 방지를 구현한다.
5. GitHub에는 코드와 `.env.example`만 올리고 URL, HMAC secret, DB/API 토큰은 커밋하지 않는다.

## 단계적 적용
1. 기존 거래 저장 흐름 보안 보완
2. Bankramen 읽기 전용 Agent API 구현
3. Hermes MCP/custom tool로 API 연결
4. 개인 계정 기준으로 Pull 조회 검증
5. 예외 이벤트 webhook 추가
6. 다중 사용자 opt-in 베타 확장
