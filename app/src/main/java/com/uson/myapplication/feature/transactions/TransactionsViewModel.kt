package com.uson.myapplication.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uson.myapplication.feature.home.HomeApiRepository
import com.uson.myapplication.feature.home.HomeApiSnapshot
import com.uson.myapplication.feature.home.TransactionItem
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TransactionKind { Expense, Income }

data class TransactionsUiState(
    val month: YearMonth = YearMonth.now(),
    val kind: TransactionKind = TransactionKind.Expense,
    val loading: Boolean = true,
    val error: String? = null,
    val expense: Long = 0,
    val income: Long = 0,
    val differenceRate: Double? = null,
    val expenses: List<TransactionItem> = emptyList(),
    val incomes: List<TransactionItem> = emptyList(),
) {
    val visibleItems: List<TransactionItem>
        get() = if (kind == TransactionKind.Expense) expenses else incomes
}

class TransactionsViewModel(
    private val repository: HomeApiRepository = HomeApiRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState = _uiState.asStateFlow()
    private var allowMockData = false
    private var loadJob: Job? = null

    fun load(allowMockData: Boolean) {
        this.allowMockData = allowMockData
        refresh(_uiState.value.month)
    }

    fun selectKind(kind: TransactionKind) = _uiState.update { it.copy(kind = kind) }
    fun previousMonth() = refresh(_uiState.value.month.minusMonths(1))
    fun nextMonth() = refresh(_uiState.value.month.plusMonths(1))

    private fun refresh(month: YearMonth) {
        if (allowMockData) {
            applyMockData(month)
            return
        }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(month = month, loading = true, error = null) }
            try {
                applySnapshot(repository.loadMonth(month))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Throwable) {
                _uiState.update { it.copy(loading = false, error = failure.message ?: "거래를 불러오지 못했어요") }
            }
        }
    }

    private fun applySnapshot(snapshot: HomeApiSnapshot) = _uiState.update {
        it.copy(
            month = snapshot.yearMonth,
            loading = false,
            error = null,
            expense = snapshot.expense,
            income = snapshot.income,
            differenceRate = snapshot.expenseDifferenceRate,
            expenses = snapshot.expenses,
            incomes = snapshot.incomes,
        )
    }

    private fun applyMockData(month: YearMonth) = _uiState.update {
        val referenceMonth = month == YearMonth.of(2026, 9)
        it.copy(
            month = month,
            loading = false,
            error = null,
            expense = if (referenceMonth) 1_160_200 else 0,
            income = if (referenceMonth) 3_892_300 else 0,
            differenceRate = if (referenceMonth) -4.0 else null,
            expenses = if (referenceMonth) mockTransactions else emptyList(),
            incomes = if (referenceMonth) mockIncomes else emptyList(),
        )
    }
}

internal fun categoryLabel(code: String): String = when (code) {
    "CAFE_SNACK" -> "카페 · 간식"
    "FOOD" -> "식비"
    "CONVENIENCE_MART_MISC" -> "마트 · 편의점"
    "TRANSPORT_CAR" -> "교통"
    "SHOPPING" -> "온라인 쇼핑"
    "LIVING" -> "생활 · 주거"
    "HEALTH_FITNESS" -> "의료 · 건강"
    "HOBBY_LEISURE" -> "구독 · 정기결제"
    else -> "분류 중"
}

private fun mock(id: String, merchant: String, time: String, category: String, amount: Long, day: Int, manual: Boolean = false) = TransactionItem(
    id = UUID.nameUUIDFromBytes(id.toByteArray()), icon = "", merchant = merchant, time = time,
    categoryCode = category, category = categoryLabel(category), amount = amount,
    date = LocalDate.of(2026, 9, day), manual = manual,
)

private val mockTransactions = listOf(
    mock("t-01", "어니언 성수", "09:41", "UNCATEGORIZED", 6_800, 15),
    mock("t-02", "스타벅스 강남R점", "08:12", "CAFE_SNACK", 5_600, 15),
    mock("t-03", "이마트24 뚝섬점", "22:07", "CONVENIENCE_MART_MISC", 4_300, 14),
    mock("t-04", "쿠팡", "19:32", "UNCATEGORIZED", 32_900, 14),
    mock("t-05", "서울교통공사", "08:24", "TRANSPORT_CAR", 1_400, 14, true),
    mock("t-06", "배달의민족", "20:15", "FOOD", 23_500, 13),
    mock("t-07", "올리브영 강남본점", "15:48", "SHOPPING", 18_400, 13),
    mock("t-08", "넷플릭스", "11:02", "HOBBY_LEISURE", 17_000, 13),
)

private val mockIncomes = listOf(
    TransactionItem(UUID.nameUUIDFromBytes("income-1".toByteArray()), "", "월급", "09:00", "SALARY", "급여", 3_800_000, true, LocalDate.of(2026, 9, 10)),
)
