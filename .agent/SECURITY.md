# SECURITY.md

> 금융 데이터를 다루는 앱이므로 보안은 최우선입니다.
> Codex는 이 규칙을 반드시 따르세요.

---

## 1. 민감 데이터 저장 규칙

| 데이터 | 저장 위치 | 암호화 |
|--------|-----------|--------|
| 인증 토큰 | `EncryptedSharedPreferences` | ✅ AES256 |
| 거래 내역 | Room DB | ✅ SQLCipher (v2 도입 예정) |
| 사용자 설정 | `SharedPreferences` | ❌ (민감 정보 아님) |

```kotlin
// 토큰 저장 — 반드시 EncryptedSharedPreferences 사용
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val prefs = EncryptedSharedPreferences.create(
    context, "secure_prefs", masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

---

## 2. 네트워크 보안

- HTTPS 전용 — HTTP 통신 절대 금지
- `network_security_config.xml`에서 cleartext 명시적 차단
- API Key, Secret은 `local.properties`에만 저장, Git에 절대 커밋 금지
- `.gitignore`에 `local.properties` 반드시 포함

---

## 3. 알림 데이터 처리

- `NotificationListenerService`에서 수집한 데이터는 결제 관련 정보만 파싱
- 파싱 후 원본 알림 텍스트는 즉시 메모리에서 해제 (보관 금지)
- 수집 범위를 온보딩에서 사용자에게 명확히 고지

---

## 4. 금지 사항

- ❌ `Log.d()`에 금액, 가맹점명 등 거래 정보 출력 금지 (Timber + debug only)
- ❌ 외부 분석 SDK에 거래 데이터 전송 금지
- ❌ 백업 허용 시 거래 DB 포함 금지 (`android:allowBackup="false"` 또는 backup rules 설정)

---

## 5. 스크린 보안

```xml
<!-- 금융 화면은 스크린샷/화면 녹화 차단 -->
window.setFlags(
    WindowManager.LayoutParams.FLAG_SECURE,
    WindowManager.LayoutParams.FLAG_SECURE
)
```

거래 내역, 통계 화면에 적용.
