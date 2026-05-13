package com.uson.myapplication.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.text.NumberFormat
import java.time.YearMonth
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val yearMonth: YearMonth = YearMonth.now(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val hasLoadedData: Boolean = false,
    val expense: Long = 0L,
    val income: Long = 0L,
    val previousExpense: Long? = null,
    val previousIncome: Long? = null,
    val expenseDifferenceRate: Double? = null,
    val incomeDifferenceRate: Double? = null,
    val totalCategoryExpense: Long = 0L,
    val categories: List<CategoryExpenseItem> = emptyList(),
    val expenses: List<TransactionItem> = emptyList(),
    val incomes: List<TransactionItem> = emptyList(),
) {
    val recentTransactions: List<TransactionItem>
        get() = (expenses + incomes).take(5)

    val expenseLabel: String
        get() = expense.formatWon()

    val incomeLabel: String
        get() = income.formatWon()

    val balanceLabel: String
        get() = (income - expense).formatSignedWon()

    val previousMonth: YearMonth
        get() = yearMonth.minusMonths(1)

    val topCategory: CategoryExpenseItem?
        get() = categories.maxByOrNull(CategoryExpenseItem::amount)

    val topCategoryInsight: String
        get() = topCategory
            ?.let { "${it.categoryName.ifBlank { it.category }}에 가장 많이\n쓰고 있어요" }
            ?: "이번 달 지출을\n확인해보세요"

    val expenseRatioItems: List<StatBar>
        get() = categories.map {
            StatBar(
                label = it.categoryName.ifBlank { it.category },
                value = it.ratio,
                color = it.barColor,
            )
        }

    val expenseComparisonLabel: String
        get() = expenseDifferenceRate.toComparisonLabel()

    val shouldShowSkeleton: Boolean
        get() = isLoading || !hasLoadedData
}

data class TransactionItem(
    val icon: String,
    val merchant: String,
    val time: String,
    val category: String,
    val amount: Long,
    val positive: Boolean = false,
) {
    val amountLabel: String
        get() = if (positive) amount.formatSignedWon() else "-${amount.formatWon()}"
}

data class CategoryExpenseItem(
    val category: String,
    val categoryName: String,
    val amount: Long,
    val ratio: Float,
    val spentMoreThanPreviousMonth: Boolean,
) {
    val barColor: androidx.compose.ui.graphics.Color
        get() = when (category) {
            "FOOD", "CAFE_SNACK", "CONVENIENCE_MART_MISC" -> com.uson.myapplication.ui.theme.BrandBlue
            "SHOPPING", "BEAUTY" -> androidx.compose.ui.graphics.Color(0xFF85B4FF)
            "TRANSPORT_CAR", "TRAVEL_STAY" -> androidx.compose.ui.graphics.Color(0xFFC5DDFF)
            else -> androidx.compose.ui.graphics.Color(0xFFCBD5E1)
        }
}

data class StatBar(
    val label: String,
    val value: Float,
    val color: androidx.compose.ui.graphics.Color,
)

class HomeViewModel(
    private val repository: HomeApiRepository = HomeApiRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh(yearMonth: YearMonth = _uiState.value.yearMonth) {
        viewModelScope.launch {
            _uiState.update { it.copy(yearMonth = yearMonth, isLoading = true, errorMessage = null) }
            runCatching { repository.loadMonth(yearMonth) }
                .onSuccess { snapshot ->
                    _uiState.update {
                        it.copy(
                            yearMonth = snapshot.yearMonth,
                            isLoading = false,
                            errorMessage = null,
                            hasLoadedData = true,
                            expense = snapshot.expense,
                            income = snapshot.income,
                            previousExpense = snapshot.previousExpense,
                            previousIncome = snapshot.previousIncome,
                            expenseDifferenceRate = snapshot.expenseDifferenceRate,
                            incomeDifferenceRate = snapshot.incomeDifferenceRate,
                            totalCategoryExpense = snapshot.totalCategoryExpense,
                            categories = snapshot.categories,
                            expenses = snapshot.expenses,
                            incomes = snapshot.incomes,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            yearMonth = yearMonth,
                            isLoading = false,
                            hasLoadedData = false,
                            errorMessage = throwable.message ?: "API 데이터를 불러오지 못했어요",
                        )
                    }
                }
        }
    }

    fun showPreviousMonth() {
        refresh(_uiState.value.yearMonth.minusMonths(1))
    }

    fun showNextMonth() {
        refresh(_uiState.value.yearMonth.plusMonths(1))
    }
}

private fun Long.formatWon(): String = "${NumberFormat.getNumberInstance(Locale.KOREA).format(this)}원"

private fun Long.formatSignedWon(): String {
    val sign = if (this >= 0) "+" else "-"
    return "$sign${kotlin.math.abs(this).formatWon()}"
}

private fun Double?.toComparisonLabel(): String = when {
    this == null -> "지난달 데이터 없음"
    this < 0.0 -> "지난달보다 ${kotlin.math.abs(this).toInt()}% 감소"
    this > 0.0 -> "지난달보다 ${this.toInt()}% 증가"
    else -> "지난달과 동일"
}
