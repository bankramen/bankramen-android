package com.uson.myapplication.feature.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uson.myapplication.core.notification.NotificationStore
import com.uson.myapplication.core.notification.NotificationSummary
import com.uson.myapplication.core.notification.NotificationUploadStatusStore
import com.uson.myapplication.core.recurring.RecurringPaymentEntry
import com.uson.myapplication.core.recurring.RecurringPaymentRepository
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val yearMonth: YearMonth = YearMonth.now(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val mutationErrorMessage: String? = null,
    val hasLoadedData: Boolean = false,
    val expense: Long = 0L,
    val income: Long = 0L,
    val previousExpense: Long? = null,
    val previousIncome: Long? = null,
    val expenseDifferenceRate: Double? = null,
    val incomeDifferenceRate: Double? = null,
    val totalCategoryExpense: Long = 0L,
    val recentTransactions: List<TransactionItem> = emptyList(),
    val categories: List<CategoryExpenseItem> = emptyList(),
    val expenses: List<TransactionItem> = emptyList(),
    val incomes: List<TransactionItem> = emptyList(),
    val recurringPayments: List<RecurringPaymentItem> = emptyList(),
    val recurringScheduledTotalAmount: Long = 0L,
    val recurringErrorMessage: String? = null,
    val deletingTransactionId: UUID? = null,
    val deletingRecurringPaymentId: UUID? = null,
    val recurringRegistrationInFlight: Boolean = false,
    val recurringRegistrationMessage: String? = null,
    val pushNotifications: List<PushNotificationItem> = emptyList(),
    val pushNotificationUnreadCount: Long = 0L,
    val pushNotificationErrorMessage: String? = null,
    val pushNotificationsLoading: Boolean = false,
    val notificationSummary: NotificationSummary = NotificationSummary(),
    val notificationUploadFailed: Boolean = false,
) {
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

    val hasVisibleData: Boolean
        get() = hasLoadedData ||
            expense != 0L ||
            income != 0L ||
            previousExpense != null ||
            previousIncome != null ||
            totalCategoryExpense != 0L ||
            recentTransactions.isNotEmpty() ||
            categories.isNotEmpty() ||
            expenses.isNotEmpty() ||
            incomes.isNotEmpty() ||
            recurringPayments.isNotEmpty() ||
            recurringScheduledTotalAmount != 0L

    val shouldShowSkeleton: Boolean
        get() = isLoading && !hasVisibleData
}

data class TransactionItem(
    val id: UUID? = null,
    val icon: String,
    val merchant: String,
    val time: String,
    val categoryCode: String,
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

data class RecurringPaymentItem(
    val id: UUID,
    val name: String,
    val subtitle: String,
    val amount: Long,
    val confirmed: Boolean,
    val enabled: Boolean,
)

class HomeViewModel(
    application: Application,
    private val repository: HomeApiRepository = HomeApiRepository(),
    private val recurringPaymentRepository: RecurringPaymentRepository = RecurringPaymentRepository(application.applicationContext),
) : AndroidViewModel(application) {
    constructor(application: Application) : this(
        application = application,
        repository = HomeApiRepository(),
        recurringPaymentRepository = RecurringPaymentRepository(application.applicationContext),
    )

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeAutomaticRecording()
        refresh()
    }

    private fun observeAutomaticRecording() {
        val context = getApplication<Application>().applicationContext
        viewModelScope.launch {
            NotificationStore.get(context).summary.collect { summary ->
                _uiState.update { it.copy(notificationSummary = summary) }
            }
        }
        viewModelScope.launch {
            NotificationUploadStatusStore.get(context).status.collect { uploadStatus ->
                _uiState.update { it.copy(notificationUploadFailed = uploadStatus.hasFailure) }
            }
        }
    }

    fun refresh(
        yearMonth: YearMonth = _uiState.value.yearMonth,
        recurringSuccessMessage: String? = null,
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    yearMonth = yearMonth,
                    isLoading = true,
                    errorMessage = null,
                    recurringErrorMessage = null,
                    recurringRegistrationMessage = recurringSuccessMessage,
                    pushNotificationErrorMessage = null,
                    pushNotificationsLoading = true,
                )
            }
            val monthResult = runCatching { repository.loadMonth(yearMonth) }
            val recurringResult = recurringPaymentRepository.getRecurringPayments()
            val pushNotificationResult = runCatching { repository.loadPushNotifications(limit = 20) }

            monthResult
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
                            recentTransactions = snapshot.recentTransactions,
                            categories = snapshot.categories,
                            expenses = snapshot.expenses,
                            incomes = snapshot.incomes,
                            recurringRegistrationInFlight = false,
                            deletingTransactionId = null,
                            recurringRegistrationMessage = recurringSuccessMessage,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            yearMonth = yearMonth,
                            isLoading = false,
                            hasLoadedData = it.hasVisibleData,
                            errorMessage = throwable.message ?: "API 데이터를 불러오지 못했어요",
                            recurringRegistrationInFlight = false,
                            deletingTransactionId = null,
                        )
                    }
                }

            recurringResult
                .onSuccess { snapshot ->
                    _uiState.update {
                        it.copy(
                            recurringPayments = snapshot.items.map(RecurringPaymentEntry::toUiModel),
                            recurringScheduledTotalAmount = snapshot.monthlyScheduledTotalAmount,
                            recurringErrorMessage = null,
                            deletingRecurringPaymentId = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            recurringErrorMessage = throwable.message ?: "정기결제 목록을 불러오지 못했어요",
                        )
                    }
                }

            pushNotificationResult
                .onSuccess { snapshot ->
                    _uiState.update {
                        it.copy(
                            pushNotifications = snapshot.notifications,
                            pushNotificationUnreadCount = snapshot.unreadCount,
                            pushNotificationErrorMessage = null,
                            pushNotificationsLoading = false,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            pushNotificationErrorMessage = throwable.message ?: "푸시 알림을 불러오지 못했어요",
                            pushNotificationsLoading = false,
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

    fun addTransaction(
        entryType: String,
        amountText: String,
        merchant: String,
        categoryCode: String,
    ) {
        val amount = amountText.toLongOrNull() ?: return
        val title = merchant.trim()
        if (title.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(mutationErrorMessage = null) }
            runCatching {
                repository.createTransaction(
                    entryType = entryType,
                    amount = amount,
                    merchant = title,
                    categoryCode = categoryCode,
                    transactionDate = LocalDate.now(),
                )
            }.onSuccess {
                refresh()
            }.onFailure { throwable ->
                _uiState.update { it.copy(mutationErrorMessage = throwable.message ?: "내역을 추가하지 못했어요") }
            }
        }
    }

    fun deleteTransaction(transactionId: UUID?) {
        transactionId ?: run {
            _uiState.update { it.copy(mutationErrorMessage = "삭제할 내역을 찾지 못했어요. 새로고침 후 다시 시도해주세요") }
            return
        }
        if (_uiState.value.deletingTransactionId == transactionId) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    deletingTransactionId = transactionId,
                    mutationErrorMessage = null,
                )
            }
            runCatching { repository.deleteTransaction(transactionId) }
                .onSuccess { refresh() }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            deletingTransactionId = null,
                            mutationErrorMessage = throwable.message ?: "내역을 삭제하지 못했어요",
                        )
                    }
                }
        }
    }

    fun updateTransactionCategory(
        transactionId: UUID?,
        categoryCode: String,
    ) {
        transactionId ?: run {
            _uiState.update { it.copy(mutationErrorMessage = "변경할 내역을 찾지 못했어요. 새로고침 후 다시 시도해주세요") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(mutationErrorMessage = null) }
            runCatching { repository.updateTransactionCategory(transactionId, categoryCode) }
                .onSuccess { refresh() }
                .onFailure { throwable ->
                    _uiState.update { it.copy(mutationErrorMessage = throwable.message ?: "카테고리를 변경하지 못했어요") }
                }
        }
    }

    fun registerRecurringPayment(
        transactionId: UUID?,
        billingDayText: String,
        today: LocalDate = LocalDate.now(),
    ) {
        transactionId ?: run {
            _uiState.update { it.copy(recurringErrorMessage = "정기결제로 등록할 거래를 선택해주세요") }
            return
        }

        val billingDay = billingDayText.toIntOrNull()
        if (billingDay == null || billingDay !in 1..31) {
            _uiState.update { it.copy(recurringErrorMessage = "결제일은 1일부터 31일 사이여야 해요") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    recurringRegistrationInFlight = true,
                    recurringRegistrationMessage = null,
                    errorMessage = null,
                )
            }
            runCatching {
                recurringPaymentRepository.createRecurringPayment(
                    transactionId = transactionId,
                    nextBillingDate = calculateNextBillingDate(
                        today = today,
                        desiredDayOfMonth = billingDay,
                    ),
                ).getOrThrow()
            }.onSuccess {
                refresh(recurringSuccessMessage = "정기결제로 등록했어요")
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        recurringRegistrationInFlight = false,
                        recurringErrorMessage = throwable.message ?: "정기결제를 등록하지 못했어요",
                    )
                }
            }
        }
    }

    fun registerRecurringPaymentManually(
        amountText: String,
        merchant: String,
        categoryCode: String,
        billingDayText: String,
        today: LocalDate = LocalDate.now(),
    ) {
        val amount = amountText.toLongOrNull()
        if (amount == null || amount <= 0L) {
            _uiState.update { it.copy(recurringErrorMessage = "금액을 올바르게 입력해주세요") }
            return
        }

        val title = merchant.trim()
        if (title.isBlank()) {
            _uiState.update { it.copy(recurringErrorMessage = "내역명을 입력해주세요") }
            return
        }

        val billingDay = billingDayText.toIntOrNull()
        if (billingDay == null || billingDay !in 1..31) {
            _uiState.update { it.copy(recurringErrorMessage = "결제일은 1일부터 31일 사이여야 해요") }
            return
        }
        viewModelScope.launch {
            var createdTransactionId: UUID? = null
            _uiState.update {
                it.copy(
                    recurringRegistrationInFlight = true,
                    recurringRegistrationMessage = null,
                    errorMessage = null,
                )
            }
            runCatching {
                createdTransactionId = repository.createExpenseTransactionAndReturnId(
                    amount = amount,
                    merchant = title,
                    categoryCode = categoryCode,
                    transactionDate = today,
                )
                recurringPaymentRepository.createRecurringPayment(
                    transactionId = createdTransactionId,
                    nextBillingDate = calculateNextBillingDate(
                        today = today,
                        desiredDayOfMonth = billingDay,
                    ),
                ).getOrThrow()
            }.onSuccess {
                refresh(recurringSuccessMessage = "정기결제로 등록했어요")
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        recurringRegistrationInFlight = false,
                        recurringErrorMessage = if (createdTransactionId == null) {
                            throwable.message ?: "정기결제를 등록하지 못했어요"
                        } else {
                            "내역은 추가됐지만 정기결제 등록에 실패했어요. 최근 내역에서 확인해주세요."
                        },
                    )
                }
            }
        }
    }

    fun deleteRecurringPayment(recurringPaymentId: UUID?) {
        recurringPaymentId ?: run {
            _uiState.update { it.copy(recurringErrorMessage = "삭제할 정기결제를 찾지 못했어요. 새로고침 후 다시 시도해주세요") }
            return
        }
        if (_uiState.value.deletingRecurringPaymentId == recurringPaymentId) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    deletingRecurringPaymentId = recurringPaymentId,
                    recurringErrorMessage = null,
                    recurringRegistrationMessage = null,
                )
            }
            runCatching {
                recurringPaymentRepository.deleteRecurringPayment(recurringPaymentId).getOrThrow()
            }.onSuccess {
                refresh(recurringSuccessMessage = "정기결제를 삭제했어요")
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        deletingRecurringPaymentId = null,
                        recurringErrorMessage = throwable.message ?: "정기결제를 삭제하지 못했어요",
                    )
                }
            }
        }
    }

    fun dismissMutationError() {
        _uiState.update { it.copy(mutationErrorMessage = null) }
    }
}

internal fun calculateNextBillingDate(
    today: LocalDate,
    desiredDayOfMonth: Int,
): LocalDate {
    val currentMonth = YearMonth.from(today)
    val currentMonthDate = currentMonth.atDay(
        desiredDayOfMonth.coerceAtMost(currentMonth.lengthOfMonth()),
    )
    if (!currentMonthDate.isBefore(today)) return currentMonthDate

    val nextMonth = currentMonth.plusMonths(1)
    return nextMonth.atDay(
        desiredDayOfMonth.coerceAtMost(nextMonth.lengthOfMonth()),
    )
}

internal fun Long.formatWon(): String = "${NumberFormat.getNumberInstance(Locale.KOREA).format(this)}원"

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

private fun RecurringPaymentEntry.toUiModel(): RecurringPaymentItem = RecurringPaymentItem(
    id = id,
    name = name.ifBlank { categoryDisplayName.ifBlank { "정기결제" } },
    subtitle = buildString {
        append(
            when (cycle) {
                "YEARLY" -> "매년"
                else -> "매월"
            },
        )
        if (billingDay > 0) append(" ${billingDay}일")
        if (categoryDisplayName.isNotBlank()) append(" · $categoryDisplayName")
    },
    amount = amount,
    confirmed = confirmed,
    enabled = confirmed,
)
