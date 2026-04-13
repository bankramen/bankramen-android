# DB Schema

> Room 데이터베이스 스키마 정의입니다.
> Entity 변경 시 이 문서도 반드시 업데이트하세요.

---

## TransactionEntity

결제 내역 (알림 파싱 결과 + 수동 입력 통합)

```kotlin
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Long,               // 금액 (원 단위)
    val merchant: String,           // 가맹점명
    val category: String?,          // 카테고리 (null = 미분류)
    val paymentMethod: String,      // 결제 수단 (카카오페이, 토스, 카드명 등)
    val memo: String?,              // 사용자 메모
    val timestamp: Long,            // 결제 시각 (epoch ms)
    val isManual: Boolean,          // 수동 입력 여부
    val isSynced: Boolean = false,  // 서버 동기화 여부
    val serverId: String?           // 서버에서 내려온 ID (동기화 후)
)
```

---

## CategoryEntity

카테고리 목록

```kotlin
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,     // ex) "food", "transport"
    val name: String,               // ex) "식비", "교통"
    val iconRes: String,            // 아이콘 리소스명
    val isDefault: Boolean          // 기본 제공 카테고리 여부
)
```

---

## BudgetEntity

월별 예산 설정

```kotlin
@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val yearMonth: String,  // ex) "2025-01"
    val totalBudget: Long,              // 총 예산 (원 단위)
    val categoryBudgets: String?        // JSON: { "food": 200000, "transport": 50000 }
)
```

---

## 관계

```
TransactionEntity.category → CategoryEntity.id (FK, nullable)
```

---

## 마이그레이션 전략

- 스키마 변경 시 반드시 `Migration` 클래스 작성
- `fallbackToDestructiveMigration()` 은 개발 중에만 허용, 프로덕션 절대 금지
- 마이그레이션 이력은 `data/local/database/migrations/` 에 버전별로 관리
