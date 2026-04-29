---
name: bankramen-notification-parsing
description: "NotificationListenerService 기반 알림 수집/파싱 기능을 뱅크라면 Android 앱에 구현할 때 쓰는 프로젝트 스킬. 서비스 선언, 권한 화면, onListenerConnected 제약, StatusBarNotification extras 파싱, 패키지별 parser strategy, Room 저장 흐름이 필요하면 반드시 이 스킬을 사용한다."
---

# Bankramen Notification Parsing

## 기본 구조

```text
NotificationListenerService
  -> NotificationParser
  -> ParsedTransaction
  -> local repository / Room
  -> ViewModel StateFlow
```

## 구현 원칙
- Service는 수집과 위임만 담당한다.
- `onListenerConnected()` 이후 동작을 기준으로 설계한다.
- 파싱 로직은 패키지명 기준 strategy 또는 parser 분기로 분리한다.
- 기본 필드는 `amount`, `merchant`, `timestamp`, `paymentMethod`다.
- 중복 저장 방지를 위해 notification key 또는 정규화된 fingerprint를 고려한다.

## 권한 플로우
- 앱 내부에서 직접 권한을 줄 수 없으므로 설정 화면 유도가 필요하다.
- 온보딩 또는 권한 화면에서 현재 활성화 상태를 보여준다.
- 미승인 상태는 오류가 아니라 제한 상태로 취급한다.

## 파싱 규칙
- `Notification.EXTRA_TITLE`, `EXTRA_TEXT`, `EXTRA_BIG_TEXT`, `EXTRA_SUB_TEXT`를 우선 사용한다.
- 패키지별 문구 차이를 parser 단위에 격리한다.
- 파싱 실패 시 앱을 죽이지 말고 무시 또는 디버그 로그 처리한다.

## 저장 규칙
- 서비스에서 무거운 처리 로직을 오래 붙잡지 말고 repository 쪽으로 넘긴다.
- MVP 범위에서는 local 우선 저장 흐름이면 충분하다.
- 서버 동기화는 나중 단계로 분리해도 된다.

## 함께 쓰면 좋은 스킬
- `bankramen-fast-mvvm`
- `requesting-code-review`
