package com.uson.myapplication.feature.home

import com.uson.myapplication.core.api.BankramenApiFactory
import com.uson.myapplication.generated.api.CategoryApi
import com.uson.myapplication.generated.api.MonthlyReportApi
import com.uson.myapplication.generated.api.PushNotificationApi
import com.uson.myapplication.generated.api.TransactionApi
import com.uson.myapplication.generated.model.BankramenLocalTime
import com.uson.myapplication.generated.model.CategoryExpense
import com.uson.myapplication.generated.model.CategoryListResponse
import com.uson.myapplication.generated.model.CreateTransactionRequest
import com.uson.myapplication.generated.model.PushNotificationResponse
import com.uson.myapplication.generated.model.RecentTransactionListResponse
import com.uson.myapplication.generated.model.TransactionHistoryResponse
import com.uson.myapplication.generated.model.UpdateTransactionCategoryRequest
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import retrofit2.Response

class HomeApiRepository(
    private val transactionApi: TransactionApi = BankramenApiFactory.createTransactionApi(),
    private val monthlyReportApi: MonthlyReportApi = BankramenApiFactory.createMonthlyReportApi(),
    private val categoryApi: CategoryApi = BankramenApiFactory.createCategoryApi(),
    private val pushNotificationApi: PushNotificationApi = BankramenApiFactory.createPushNotificationApi(),
) {
    suspend fun loadMonth(yearMonth: YearMonth): HomeApiSnapshot = coroutineScope {
        val year = yearMonth.year
        val month = yearMonth.monthValue
        val summary = async { runCatching { monthlyReportApi.getMonthlyAmountSummary(year = year, month = month).requireBody() } }
        val categories = async { runCatching { monthlyReportApi.getMonthlyCategoryExpenses(year = year, month = month).requireBody() } }
        val expenses = async { runCatching { transactionApi.getMonthlyExpenseTransactions(year = year, month = month).requireBody() } }
        val incomes = async { runCatching { transactionApi.getMonthlyIncomeTransactions(year = year, month = month).requireBody() } }
        val recent = async { runCatching { transactionApi.getRecentTransactions(limit = 5).requireBody() } }
        val categoryNames = async {
            runCatching { categoryApi.getCategories().requireBody().toDisplayNameMap() }
                .getOrDefault(emptyMap())
        }
        val summaryResult = summary.await()
        val categoryResult = categories.await()
        val expenseResult = expenses.await()
        val incomeResult = incomes.await()
        val recentResult = recent.await()
        val defaultCategoryNames = categoryNames.await()

        if (listOf(summaryResult, categoryResult, expenseResult, incomeResult, recentResult).all(Result<*>::isFailure)) {
            throw summaryResult.exceptionOrNull()
                ?: categoryResult.exceptionOrNull()
                ?: expenseResult.exceptionOrNull()
                ?: incomeResult.exceptionOrNull()
                ?: recentResult.exceptionOrNull()
                ?: IllegalStateException("API 데이터를 불러오지 못했어요")
        }

        val summaryBody = summaryResult.getOrNull()
        val categoryBody = categoryResult.getOrNull()
        val monthlyExpenses = expenseResult.getOrNull()?.expenses.orEmpty()
        val monthlyIncomes = incomeResult.getOrNull()?.incomes.orEmpty()
        val resolvedRecentTransactions = resolveRecentTransactions(
            recentTransactions = recentResult.getOrNull()?.transactions.orEmpty(),
            monthlyExpenses = monthlyExpenses,
            monthlyIncomes = monthlyIncomes,
            categoryNames = defaultCategoryNames,
        )

        HomeApiSnapshot(
            yearMonth = yearMonth,
            expense = summaryBody?.expense?.currentAmount ?: 0L,
            income = summaryBody?.income?.currentAmount ?: 0L,
            previousExpense = summaryBody?.expense?.previousAmount,
            previousIncome = summaryBody?.income?.previousAmount,
            expenseDifferenceRate = summaryBody?.expense?.differenceRate?.toDouble(),
            incomeDifferenceRate = summaryBody?.income?.differenceRate?.toDouble(),
            totalCategoryExpense = categoryBody?.totalExpense ?: 0L,
            recentTransactions = resolvedRecentTransactions,
            categories = categoryBody?.categories.orEmpty().map { it.toUiModel(defaultCategoryNames) },
            expenses = monthlyExpenses.map {
                it.toUiModel(positive = false, categoryNames = defaultCategoryNames)
            },
            incomes = monthlyIncomes.map {
                it.toUiModel(positive = true, categoryNames = defaultCategoryNames)
            },
        )
    }

    suspend fun createTransaction(
        entryType: String,
        amount: Long,
        merchant: String,
        categoryCode: String,
        transactionDate: LocalDate,
    ) {
        transactionApi.createTransaction(
            CreateTransactionRequest(
                type = entryType.toGeneratedTransactionType(),
                amount = amount,
                title = merchant,
                category = categoryCode.toGeneratedCreateCategory(),
                transactionDate = transactionDate,
            ),
        ).requireUnit()
    }

    suspend fun createExpenseTransactionAndReturnId(
        amount: Long,
        merchant: String,
        categoryCode: String,
        transactionDate: LocalDate,
    ): UUID {
        createTransaction(
            entryType = "지출",
            amount = amount,
            merchant = merchant,
            categoryCode = categoryCode,
            transactionDate = transactionDate,
        )

        val recentExpenses = transactionApi.getRecentTransactions(limit = 10).requireBody()
            .transactions
            .orEmpty()
            .filter { it.type == TransactionHistoryResponse.Type.EXPENSE }
        resolveCreatedTransactionId(
            candidates = recentExpenses,
            merchant = merchant,
            amount = amount,
            transactionDate = transactionDate,
        )?.let { return it }

        val monthlyExpenses = transactionApi.getMonthlyExpenseTransactions(
            year = transactionDate.year,
            month = transactionDate.monthValue,
        ).requireBody().expenses.orEmpty()
        return resolveCreatedTransactionId(
            candidates = monthlyExpenses,
            merchant = merchant,
            amount = amount,
            transactionDate = transactionDate,
        ) ?: error("Created transaction could not be resolved for recurring payment registration")
    }

    suspend fun updateTransactionCategory(
        transactionId: UUID,
        categoryCode: String,
    ): TransactionItem = transactionApi.updateTransactionCategory(
        transactionId = transactionId,
        updateTransactionCategoryRequest = UpdateTransactionCategoryRequest(
            category = categoryCode.toGeneratedUpdateCategory(),
        ),
    ).requireBody().toUiModel(categoryNames = emptyMap())

    suspend fun deleteTransaction(transactionId: UUID) {
        transactionApi.deleteTransaction(transactionId).requireUnit()
    }

    suspend fun loadPushNotifications(limit: Int = 20): PushNotificationSnapshot {
        val response = pushNotificationApi.getPushNotifications(limit = limit.coerceIn(1, 50)).requireBody()
        return PushNotificationSnapshot(
            unreadCount = response.unreadCount ?: 0L,
            notifications = response.notifications.orEmpty().map(PushNotificationResponse::toUiModel),
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
    val recentTransactions: List<TransactionItem>,
    val categories: List<CategoryExpenseItem>,
    val expenses: List<TransactionItem>,
    val incomes: List<TransactionItem>,
)

data class PushNotificationSnapshot(
    val unreadCount: Long,
    val notifications: List<PushNotificationItem>,
)

data class PushNotificationItem(
    val id: UUID?,
    val title: String,
    val body: String,
    val displayTime: String,
    val unread: Boolean,
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
    categoryNames: Map<String, String>,
    positive: Boolean = false,
): TransactionItem = TransactionItem(
    id = transactionId,
    icon = categoryName.toCategoryIcon(category?.value),
    merchant = title ?: "제목 없음",
    time = transactionTime.toDisplayTime() ?: transactionDate?.toString().orEmpty(),
    categoryCode = category?.value.orEmpty(),
    category = categoryName ?: categoryNames[category?.value].orEmpty().ifBlank { category?.value.orEmpty() },
    amount = amount ?: 0L,
    positive = positive || type == TransactionHistoryResponse.Type.INCOME,
)

internal fun PushNotificationResponse.toUiModel(): PushNotificationItem = PushNotificationItem(
    id = notificationId,
    title = title?.takeIf(String::isNotBlank) ?: "알림",
    body = body?.takeIf(String::isNotBlank) ?: "새 알림이 도착했어요",
    displayTime = displayTime?.takeIf(String::isNotBlank)
        ?: sentAt?.format(DateTimeFormatter.ofPattern("MM.dd HH:mm")).orEmpty(),
    unread = unread == true,
)

internal fun resolveRecentTransactions(
    recentTransactions: List<TransactionHistoryResponse>,
    monthlyExpenses: List<TransactionHistoryResponse>,
    monthlyIncomes: List<TransactionHistoryResponse>,
    categoryNames: Map<String, String>,
): List<TransactionItem> {
    val source = recentTransactions.ifEmpty {
        (monthlyExpenses + monthlyIncomes)
            .sortedWith(
                compareByDescending<TransactionHistoryResponse> { it.transactionDate }
                    .thenByDescending { it.transactionTime?.hour ?: -1 }
                    .thenByDescending { it.transactionTime?.minute ?: -1 },
            )
            .take(5)
    }

    return source.map { it.toUiModel(categoryNames = categoryNames) }
}

internal fun resolveCreatedTransactionId(
    candidates: List<TransactionHistoryResponse>,
    merchant: String,
    amount: Long,
    transactionDate: LocalDate,
): UUID? = candidates
    .asSequence()
    .filter { it.type == TransactionHistoryResponse.Type.EXPENSE }
    .filter { it.title == merchant }
    .filter { it.amount == amount }
    .filter { it.transactionDate == transactionDate }
    .mapNotNull(TransactionHistoryResponse::transactionId)
    .distinct()
    .toList()
    .singleOrNull()

private fun String.toGeneratedTransactionType(): CreateTransactionRequest.Type = when (this) {
    "수입" -> CreateTransactionRequest.Type.INCOME
    else -> CreateTransactionRequest.Type.EXPENSE
}

private fun String.toGeneratedCreateCategory(): CreateTransactionRequest.Category =
    CreateTransactionRequest.Category.entries.firstOrNull { it.value == this }
        ?: CreateTransactionRequest.Category.UNCATEGORIZED

private fun String.toGeneratedUpdateCategory(): UpdateTransactionCategoryRequest.Category =
    UpdateTransactionCategoryRequest.Category.entries.firstOrNull { it.value == this }
        ?: UpdateTransactionCategoryRequest.Category.UNCATEGORIZED

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

private fun Response<Unit>.requireUnit() {
    if (!isSuccessful) {
        error("API request failed: HTTP ${code()}")
    }
}
