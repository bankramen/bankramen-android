# RELIABILITY.md

> 앱의 안정성을 보장하기 위한 규칙과 전략입니다.

---

## 1. 알림 파싱 안정성

알림 파싱은 앱의 핵심 기능이므로 절대 크래시를 발생시키면 안 된다.

```kotlin
// 파싱 실패는 항상 조용히 처리
fun parse(sbn: StatusBarNotification): ParsedTransaction? {
    return try {
        // 파싱 로직
    } catch (e: Exception) {
        Timber.e(e, "파싱 실패: ${sbn.packageName}")
        null  // null 반환, 크래시 없음
    }
}
```

---

## 2. 오프라인 우선 전략

- 모든 거래 내역은 로컬 Room에 먼저 저장
- 서버 동기화 실패 시 `isSynced = false` 로 마킹
- 네트워크 복구 시 `WorkManager`가 자동으로 재시도
- 사용자는 오프라인에서도 내역 조회 가능

---

## 3. WorkManager 재시도 전략

```kotlin
val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
    .setBackoffCriteria(
        BackoffPolicy.EXPONENTIAL,
        WorkRequest.MIN_BACKOFF_MILLIS,
        TimeUnit.MILLISECONDS
    )
    .setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    )
    .build()
```

---

## 4. 중복 거래 감지

같은 결제가 중복으로 파싱되는 것을 방지한다.

- `timestamp` + `amount` + `merchant` 조합으로 중복 체크
- 5분 이내 동일 조합이 있으면 무시
- Room UNIQUE constraint 활용

---

## 5. 모니터링

- **Firebase Crashlytics**: 프로덕션 크래시 실시간 모니터링
- **Timber**: 개발 중 파싱 실패 로그
- **ANR 방지**: NotificationListenerService 콜백에서 무거운 작업은 반드시 코루틴으로 처리
