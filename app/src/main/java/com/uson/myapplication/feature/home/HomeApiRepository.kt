package com.uson.myapplication.feature.home

import com.uson.myapplication.core.api.BankramenApiFactory
import com.uson.myapplication.generated.api.CategoryApi
import com.uson.myapplication.generated.api.MonthlyReportApi
import com.uson.myapplication.generated.api.TransactionApi
import com.uson.myapplication.generated.model.BankramenLocalTime
import com.uson.myapplication.generated.model.CategoryExpense
import com.uson.myapplication.generated.model.CategoryListResponse
import com.uson.myapplication.generated.model.TransactionHistoryResponse
import java.time.YearMonth
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import retrofit2.Response

class HomeApiRepository(
    private val transactionApi: TransactionApi = BankramenApiFactory.createTransactionApi(),
    private val monthlyReportApi: MonthlyReportApi = BankramenApiFactory.createMonthlyReportApi(),
    private val categoryApi: CategoryApi = BankramenApiFactory.createCategoryApi(),
) {
    suspend fun loadMonth(yearMonth: YearMonth): HomeApiSnapshot = coroutineScope {
        val year = yearMonth.year
        val month = yearMonth.monthValue
        val summary = async { monthlyReportApi.getMonthlyAmountSummary(year = year, month = month).requireBody() }
        val categories = async { monthlyReportApi.getMonthlyCategoryExpenses(year = year, month = month).requireBody() }
        val expenses = async { transactionApi.getMonthlyExpenseTransactions(year = year, month = month).requireBody() }
        val incomes = async { transactionApi.getMonthlyIncomeTransactions(year = year, month = month).requireBody() }
        val categoryNames = async {
            runCatching { categoryApi.getCategories().requireBody().toDisplayNameMap() }
                .getOrDefault(emptyMap())
        }
        val summaryBody = summary.await()
        val categoryBody = categories.await()
        val defaultCategoryNames = categoryNames.await()

        HomeApiSnapshot(
            yearMonth = yearMonth,
            expense = summaryBody.expense?.currentAmount ?: 0L,
            income = summaryBody.income?.currentAmount ?: 0L,
            previousExpense = summaryBody.expense?.previousAmount,
            previousIncome = summaryBody.income?.previousAmount,
            expenseDifferenceRate = summaryBody.expense?.differenceRate?.toDouble(),
            incomeDifferenceRate = summaryBody.income?.differenceRate?.toDouble(),
            totalCategoryExpense = categoryBody.totalExpense ?: 0L,
            categories = categoryBody.categories.orEmpty().map { it.toUiModel(defaultCategoryNames) },
            expenses = expenses.await().expenses.orEmpty().map {
                it.toUiModel(positive = false, categoryNames = defaultCategoryNames)
            },
            incomes = incomes.await().incomes.orEmpty().map {
                it.toUiModel(positive = true, categoryNames = defaultCategoryNames)
            },
        )
    }
}

data class HomeApiSnapshot(
    val yearMonth: YearMonth,
    val expense: Long,
    val income: Long,
    val previousExpense: Long?,
    val previousIncome: Long?,
    val expenseDifferenceRate: Double?,
    val incomeDifferenceRate: Double?,
    val totalCategoryExpense: Long,
    val categories: List<CategoryExpenseItem>,
    val expenses: List<TransactionItem>,
    val incomes: List<TransactionItem>,
)

private fun CategoryListResponse.toDisplayNameMap(): Map<String, String> =
    categories.orEmpty()
        .mapNotNull { category ->
            val code = category.code?.takeIf(String::isNotBlank) ?: return@mapNotNull null
            code to (category.displayName?.takeIf(String::isNotBlank) ?: code)
        }
        .toMap()

private fun CategoryExpense.toUiModel(categoryNames: Map<String, String>): CategoryExpenseItem = CategoryExpenseItem(
    category = category?.value.orEmpty(),
    categoryName = categoryName ?: categoryNames[category?.value].orEmpty().ifBlank { category?.value.orEmpty() },
    amount = expenseAmount ?: 0L,
    ratio = ((expenseRatio?.toFloat() ?: 0f) / 100f).coerceIn(0f, 1f),
    spentMoreThanPreviousMonth = spentMoreThanPreviousMonth == true,
)

private fun TransactionHistoryResponse.toUiModel(
    positive: Boolean,
    categoryNames: Map<String, String>,
): TransactionItem = TransactionItem(
    icon = categoryName.toCategoryIcon(category?.value),
    merchant = title ?: "제목 없음",
    time = transactionTime.toDisplayTime() ?: transactionDate?.toString().orEmpty(),
    category = categoryName ?: categoryNames[category?.value].orEmpty().ifBlank { category?.value.orEmpty() },
    amount = amount ?: 0L,
    positive = positive,
)

private fun BankramenLocalTime?.toDisplayTime(): String? {
    val hour = this?.hour ?: return null
    val minute = this.minute ?: 0
    return "%02d:%02d".format(hour, minute)
}

private fun String?.toCategoryIcon(code: String?): String = when (code ?: this) {
    "FOOD", "CAFE_SNACK", "CONVENIENCE_MART_MISC" -> "🍽"
    "SHOPPING", "BEAUTY" -> "🛍"
    "TRANSPORT_CAR", "TRAVEL_STAY" -> "🚇"
    "SALARY" -> "💰"
    "SAVINGS_INVESTMENT" -> "📈"
    "EDUCATION" -> "📚"
    "HEALTH_FITNESS" -> "💪"
    else -> "•"
}

private fun <T> Response<T>.requireBody(): T {
    if (!isSuccessful) {
        error("API request failed: HTTP ${code()}")
    }
    return body() ?: error("API response body is empty")
}
