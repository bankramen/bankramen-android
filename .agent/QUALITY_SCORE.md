# QUALITY_SCORE.md

> 코드 품질 기준 및 측정 방법입니다.

---

## 품질 지표

### 파싱 정확도
- **목표**: 지원 앱 알림 파싱 정확도 > 95%
- **측정**: 실제 알림 샘플 기반 단위 테스트
- **기준 앱**: 카카오페이, 토스, 네이버페이, 삼성카드, 신한카드

### 앱 안정성
- **목표**: Crash-free rate > 99.5%
- **측정**: Firebase Crashlytics
- **기준**: NotificationListenerService 파싱 실패는 크래시로 이어지면 안 됨

### 코드 품질
- **ktlint**: 경고 0개 (CI에서 강제)
- **detekt**: 주요 규칙 위반 0개
- **테스트**: NotificationParser 단위 테스트 커버리지 > 80%

---

## CI 체크 항목

```yaml
# GitHub Actions에서 자동으로 검사
- ktlint
- detekt
- Unit Test (NotificationParser)
- Build 성공 여부
```

---

## 코드 리뷰 기준

- PR 당 변경 파일 10개 이하 권장
- 새로운 파싱 패턴 추가 시 반드시 테스트 케이스 포함
- ViewModel 로직 변경 시 UiState 변경 여부 확인
