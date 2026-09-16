package com.uson.myapplication.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eygraber.compose.placeholder.material3.placeholder
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uson.myapplication.core.notification.isNotificationListenerAccessGranted
import com.uson.myapplication.core.notification.openNotificationListenerSettings
import com.uson.myapplication.ui.theme.BackgroundGray
import com.uson.myapplication.ui.theme.BrandBlue
import com.uson.myapplication.ui.theme.MyApplicationTheme
import com.uson.myapplication.ui.theme.SecondaryText

private enum class MainTab(val label: String, val icon: String) {
    Home("홈", "⌂"),
    Stats("통계", "◔"),
    Recurring("정기결제", "↻"),
    Alerts("알림", "◉"),
}

private enum class OverlaySheet {
    AddEntry,
    EditCategory,
    AddRecurring,
}

internal enum class RecurringAddMode(val label: String) {
    Existing("기존 지출 선택"),
    Manual("직접 입력"),
}

private data class CategoryChoice(
    val code: String,
    val label: String,
    val icon: String,
)

private val categoryChoices = listOf(
    CategoryChoice("FOOD", "식비", "🍔"),
    CategoryChoice("CAFE_SNACK", "카페/간식", "🍹"),
    CategoryChoice("CONVENIENCE_MART_MISC", "편의점/마트/잡화", "🏪"),
    CategoryChoice("SHOPPING", "쇼핑", "🛍"),
    CategoryChoice("HOBBY_LEISURE", "취미/여가", "🎮"),
    CategoryChoice("HEALTH_FITNESS", "의료/건강/피트니스", "🏥"),
    CategoryChoice("BEAUTY", "미용", "🔎"),
    CategoryChoice("TRANSPORT_CAR", "교통/자동차", "🚌"),
    CategoryChoice("TRAVEL_STAY", "여행/숙박", "✈"),
    CategoryChoice("EDUCATION", "교육", "🎓"),
    CategoryChoice("LIVING", "생활", "🛁"),
    CategoryChoice("DONATION_SPONSORSHIP", "기부/후원", "❤"),
    CategoryChoice("UNCATEGORIZED", "카테고리 없음", "◌"),
    CategoryChoice("ATM_WITHDRAWAL", "ATM 출금", "🏧"),
    CategoryChoice("TRANSFER", "이체", "↔"),
    CategoryChoice("SALARY", "급여", "💰"),
    CategoryChoice("SAVINGS_INVESTMENT", "저축/투자", "📈"),
)

private fun categoryCodeFor(value: String): String =
    categoryChoices.firstOrNull { it.code == value || it.label == value }?.code ?: "UNCATEGORIZED"

data class EditableTransaction(
    val id: java.util.UUID? = null,
    val title: String,
    val time: String,
    val categoryCode: String,
    val category: String,
    val amountLabel: String,
    val positive: Boolean,
    val icon: String,
    val deleting: Boolean = false,
)

internal data class EditableRecurringPayment(
    val id: java.util.UUID,
    val title: String,
    val subtitle: String,
    val amountLabel: String,
    val confirmed: Boolean,
    val deleting: Boolean,
)

@Composable
fun HomeScreen(
    userId: String?,
    onLogoutClick: () -> Unit,
    allowMockData: Boolean = false,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var automaticRecordingEnabled by remember {
        mutableStateOf(isNotificationListenerAccessGranted(context))
    }
    var currentTab by rememberSaveable { mutableStateOf(MainTab.Home) }
    var statsMode by rememberSaveable { mutableIntStateOf(0) }
    var showCategoryDetail by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var overlaySheet by rememberSaveable { mutableStateOf<OverlaySheet?>(null) }
    var entryType by rememberSaveable { mutableStateOf("지출") }
    var selectedCategory by rememberSaveable { mutableStateOf("FOOD") }
    var recurringSuggestionAccepted by rememberSaveable { mutableStateOf(false) }
    var recurringSuggestionDismissed by rememberSaveable { mutableStateOf(false) }
    var entryAmount by rememberSaveable { mutableStateOf("") }
    var entryMerchant by rememberSaveable { mutableStateOf("") }
    var recurringDay by rememberSaveable { mutableStateOf("15") }
    var recurringAddMode by rememberSaveable { mutableStateOf(RecurringAddMode.Existing) }
    var recurringManualAmount by rememberSaveable { mutableStateOf("") }
    var recurringManualMerchant by rememberSaveable { mutableStateOf("") }
    var recurringManualCategory by rememberSaveable { mutableStateOf("FOOD") }
    var selectedTransaction by remember { mutableStateOf<EditableTransaction?>(null) }
    var selectedRecurringPayment by remember { mutableStateOf<EditableRecurringPayment?>(null) }
    var selectedRecurringTransactionId by rememberSaveable { mutableStateOf<String?>(null) }

    DisposableEffect(context, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                automaticRecordingEnabled = isNotificationListenerAccessGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val editableTransactions = remember(state.recentTransactions, state.deletingTransactionId) {
        state.recentTransactions.map {
            EditableTransaction(
                id = it.id,
                title = it.merchant,
                time = it.time,
                categoryCode = it.categoryCode,
                category = it.category,
                amountLabel = it.amountLabel,
                positive = it.positive,
                icon = it.icon,
                deleting = state.deletingTransactionId == it.id,
            )
        }
    }

    val recurringCandidates = remember(state.expenses) {
        state.expenses.map {
            EditableTransaction(
                id = it.id,
                title = it.merchant,
                time = it.time,
                categoryCode = it.categoryCode,
                category = it.category,
                amountLabel = it.amountLabel,
                positive = false,
                icon = it.icon,
            )
        }
    }
    val editableRecurringPayments = remember(
        state.recurringPayments,
        state.deletingRecurringPaymentId,
    ) {
        state.recurringPayments.map {
            EditableRecurringPayment(
                id = it.id,
                title = it.name,
                subtitle = it.subtitle,
                amountLabel = it.amount.formatWon(),
                confirmed = it.confirmed,
                deleting = state.deletingRecurringPaymentId == it.id,
            )
        }
    }
    Surface(
        modifier = modifier.fillMaxSize(),
        color = BackgroundGray,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = BackgroundGray,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    if (currentTab != MainTab.Home) MainBottomBar(
                        selectedTab = currentTab,
                        onTabSelected = {
                            currentTab = it
                            showCategoryDetail = false
                        },
                    )
                },
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundGray)
                        .padding(innerPadding),
                ) {
                    when (currentTab) {
                        MainTab.Home -> HomeDashboardPage(
                            state = state,
                            transactions = editableTransactions,
                            allowMockData = allowMockData,
                            automaticRecordingEnabled = automaticRecordingEnabled,
                            onOpenAutomaticRecordingSettings = {
                                openNotificationListenerSettings(context)
                            },
                            onAddEntry = { overlaySheet = OverlaySheet.AddEntry },
                            onDeleteTransaction = {
                                selectedTransaction = it
                                showDeleteDialog = true
                            },
                            onEditTransactionCategory = {
                                selectedTransaction = it
                                selectedCategory = it.categoryCode.ifBlank { categoryCodeFor(it.category) }
                                overlaySheet = OverlaySheet.EditCategory
                            },
                            onOpenMonthlyReport = {
                                currentTab = MainTab.Stats
                                statsMode = 0
                            },
                            onOpenRecurring = { currentTab = MainTab.Recurring },
                            onOpenNotifications = { currentTab = MainTab.Alerts },
                        )

                        MainTab.Stats -> {
                            if (showCategoryDetail) {
                                CategoryExpenseDetailPage(
                                    state = state,
                                    onBack = { showCategoryDetail = false },
                                )
                            } else if (statsMode == 0) {
                                MonthlyReportPage(
                                    state = state,
                                    onPreviousMonth = viewModel::showPreviousMonth,
                                    onNextMonth = viewModel::showNextMonth,
                                    onSelectMonthly = { statsMode = 0 },
                                    onSelectCashflow = { statsMode = 1 },
                                    onOpenCategoryDetail = { showCategoryDetail = true },
                                )
                            } else {
                                IncomeExpensePage(
                                    state = state,
                                    onPreviousMonth = viewModel::showPreviousMonth,
                                    onNextMonth = viewModel::showNextMonth,
                                    onSelectMonthly = { statsMode = 0 },
                                    onSelectCashflow = { statsMode = 1 },
                                )
                            }
                        }

                        MainTab.Recurring -> RecurringPage(
                            state = state,
                            recurringPayments = editableRecurringPayments,
                            recurringCandidates = recurringCandidates,
                            onAddRecurring = { overlaySheet = OverlaySheet.AddRecurring },
                            onDeleteRecurring = { recurringPayment ->
                                selectedRecurringPayment = recurringPayment
                            },
                        )

                        MainTab.Alerts -> AlertsPage(
                            state = state,
                            onAcceptSuggestion = {
                                recurringSuggestionAccepted = true
                                recurringSuggestionDismissed = false
                                currentTab = MainTab.Recurring
                            },
                            onDismissSuggestion = {
                                recurringSuggestionDismissed = true
                                recurringSuggestionAccepted = false
                            },
                            suggestionAccepted = recurringSuggestionAccepted,
                            suggestionDismissed = recurringSuggestionDismissed,
                        )
                    }
                }
            }

            if (showDeleteDialog) {
                selectedTransaction?.let { transaction ->
                    DeleteEntryDialog(
                    transaction = transaction,
                    onDismiss = {
                        showDeleteDialog = false
                        selectedTransaction = null
                    },
                    onDelete = {
                        viewModel.deleteTransaction(transaction.id)
                        showDeleteDialog = false
                        selectedTransaction = null
                    },
                )
                }
            }

            selectedRecurringPayment?.let { recurringPayment ->
                DeleteRecurringPaymentDialog(
                    recurringPayment = recurringPayment,
                    onDismiss = { selectedRecurringPayment = null },
                    onDelete = {
                        viewModel.deleteRecurringPayment(recurringPayment.id)
                        selectedRecurringPayment = null
                    },
                )
            }

            when (overlaySheet) {
                OverlaySheet.AddEntry -> EntrySheet(
                    title = "내역 추가",
                    entryType = entryType,
                    amount = entryAmount,
                    merchant = entryMerchant,
                    selectedCategory = selectedCategory,
                    onSelectType = { entryType = it },
                    onAmountChange = { entryAmount = it.filter(Char::isDigit) },
                    onMerchantChange = { entryMerchant = it },
                    onSelectCategory = { selectedCategory = it },
                    onDismiss = {
                        overlaySheet = null
                        entryAmount = ""
                        entryMerchant = ""
                    },
                    onConfirm = {
                        viewModel.addTransaction(
                            entryType = entryType,
                            amountText = entryAmount,
                            merchant = entryMerchant,
                            categoryCode = selectedCategory,
                        )
                        overlaySheet = null
                        entryAmount = ""
                        entryMerchant = ""
                    },
                )

                OverlaySheet.EditCategory -> CategoryEditSheet(
                    selectedCategory = selectedCategory,
                    transaction = selectedTransaction ?: editableTransactions.firstOrNull()
                        ?: EditableTransaction(
                            title = "내역 없음",
                            time = "",
                            categoryCode = "UNCATEGORIZED",
                            category = "UNCATEGORIZED",
                            amountLabel = "0원",
                            positive = false,
                            icon = "•",
                        ),
                    onSelectCategory = { selectedCategory = it },
                    onDismiss = {
                        overlaySheet = null
                        selectedTransaction = null
                    },
                    onConfirm = {
                        viewModel.updateTransactionCategory(
                            transactionId = selectedTransaction?.id,
                            categoryCode = selectedCategory,
                        )
                        overlaySheet = null
                        selectedTransaction = null
                    },
                )

                OverlaySheet.AddRecurring -> RecurringAddSheet(
                    recurringCandidates = recurringCandidates,
                    addMode = recurringAddMode,
                    selectedTransactionId = selectedRecurringTransactionId,
                    manualAmount = recurringManualAmount,
                    manualMerchant = recurringManualMerchant,
                    manualCategory = recurringManualCategory,
                    dayOfMonth = recurringDay,
                    isSubmitting = state.recurringRegistrationInFlight,
                    onSelectMode = { recurringAddMode = it },
                    onSelectTransaction = { selectedRecurringTransactionId = it },
                    onManualAmountChange = { recurringManualAmount = it.filter(Char::isDigit) },
                    onManualMerchantChange = { recurringManualMerchant = it },
                    onManualCategoryChange = { recurringManualCategory = it },
                    onDayOfMonthChange = { recurringDay = it.filter(Char::isDigit).take(2) },
                    onDismiss = {
                        overlaySheet = null
                        recurringAddMode = RecurringAddMode.Existing
                        recurringDay = "15"
                        recurringManualAmount = ""
                        recurringManualMerchant = ""
                        recurringManualCategory = "FOOD"
                        selectedRecurringTransactionId = null
                    },
                    onConfirm = {
                        when (recurringAddMode) {
                            RecurringAddMode.Existing -> {
                                val selectedRecurringTransaction = recurringCandidates.firstOrNull {
                                    it.id?.toString() == selectedRecurringTransactionId
                                }
                                if (selectedRecurringTransaction?.id != null &&
                                    recurringDay.toIntOrNull()?.let { it in 1..31 } == true
                                ) {
                                    viewModel.registerRecurringPayment(
                                        transactionId = selectedRecurringTransaction.id,
                                        billingDayText = recurringDay,
                                    )
                                    overlaySheet = null
                                    recurringAddMode = RecurringAddMode.Existing
                                    recurringDay = "15"
                                    recurringManualAmount = ""
                                    recurringManualMerchant = ""
                                    recurringManualCategory = "FOOD"
                                    selectedRecurringTransactionId = null
                                }
                            }

                            RecurringAddMode.Manual -> {
                                if (recurringDay.toIntOrNull()?.let { it in 1..31 } == true) {
                                    viewModel.registerRecurringPaymentManually(
                                        amountText = recurringManualAmount,
                                        merchant = recurringManualMerchant,
                                        categoryCode = recurringManualCategory,
                                        billingDayText = recurringDay,
                                    )
                                    overlaySheet = null
                                    recurringAddMode = RecurringAddMode.Existing
                                    recurringDay = "15"
                                    recurringManualAmount = ""
                                    recurringManualMerchant = ""
                                    recurringManualCategory = "FOOD"
                                    selectedRecurringTransactionId = null
                                }
                            }
                        }
                    },
                )

                null -> Unit
            }

            state.mutationErrorMessage?.let { message ->
                MutationErrorBanner(
                    message = message,
                    onDismiss = viewModel::dismissMutationError,
                )
            }
        }
    }
}

@Composable
private fun HomeDashboardPage(
    state: HomeUiState,
    transactions: List<EditableTransaction>,
    allowMockData: Boolean,
    automaticRecordingEnabled: Boolean,
    onOpenAutomaticRecordingSettings: () -> Unit,
    onAddEntry: () -> Unit,
    onDeleteTransaction: (EditableTransaction) -> Unit,
    onEditTransactionCategory: (EditableTransaction) -> Unit,
    onOpenMonthlyReport: () -> Unit,
    onOpenRecurring: () -> Unit,
    onOpenNotifications: () -> Unit,
) {
    BalogHomeDashboard(
        state = state,
        transactions = transactions,
        allowMockData = allowMockData,
        automaticRecordingEnabled = automaticRecordingEnabled,
        onOpenRecordingSettings = onOpenAutomaticRecordingSettings,
        onAddEntry = onAddEntry,
        onOpenTransaction = onEditTransactionCategory,
        onDeleteTransaction = onDeleteTransaction,
        onOpenReport = onOpenMonthlyReport,
        onOpenRecurring = onOpenRecurring,
        onOpenNotifications = onOpenNotifications,
    )
}

@Composable
private fun MonthlyReportPage(
    state: HomeUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectMonthly: () -> Unit,
    onSelectCashflow: () -> Unit,
    onOpenCategoryDetail: () -> Unit,
) {
    ScreenColumn {
        ScreenHeader(title = "월별 리포트")
        SegmentedTabs(
            selectedIndex = 0,
            labels = listOf("월별 리포트", "수입/지출 통계"),
            onFirst = onSelectMonthly,
            onSecond = onSelectCashflow,
        )
        LargeCard {
            MonthSwitcher(
                yearMonth = state.yearMonth,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
            )
            Spacer(modifier = Modifier.height(20.dp))
            CenteredCaption("이번 달 총 지출")
            Spacer(modifier = Modifier.height(8.dp))
            PlaceholderHeadline(
                text = state.expenseLabel,
                isLoading = state.shouldShowSkeleton,
            )
            Spacer(modifier = Modifier.height(12.dp))
            PlaceholderAccentPill(
                text = state.expenseComparisonLabel,
                background = BrandBlue.copy(alpha = 0.12f),
                textColor = BrandBlue,
                isLoading = state.shouldShowSkeleton,
            )
            Spacer(modifier = Modifier.height(28.dp))
            CenteredCaption("지출 비교")
            Spacer(modifier = Modifier.height(16.dp))
            BarCompare(
                isLoading = state.shouldShowSkeleton,
                leftLabel = "${state.previousMonth.monthValue}월",
                rightLabel = "${state.yearMonth.monthValue}월",
                leftAmount = state.previousExpense.formatWonOrEmpty(),
                rightAmount = state.expenseLabel,
                leftColor = Color(0xFF8CC7F4),
                rightColor = BrandBlue,
                leftRatio = barRatio(state.previousExpense, state.expense),
                rightRatio = barRatio(state.expense, state.previousExpense),
            )
            Spacer(modifier = Modifier.height(28.dp))
            CenteredCaption("수입 비교")
            Spacer(modifier = Modifier.height(16.dp))
            BarCompare(
                isLoading = state.shouldShowSkeleton,
                leftLabel = "${state.previousMonth.monthValue}월",
                rightLabel = "${state.yearMonth.monthValue}월",
                leftAmount = state.previousIncome.formatWonOrEmpty(),
                rightAmount = state.incomeLabel,
                leftColor = Color(0xFFC7EECF),
                rightColor = Color(0xFF69C66F),
                leftRatio = barRatio(state.previousIncome, state.income),
                rightRatio = barRatio(state.income, state.previousIncome),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        MediumCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("카테고리별 지출", style = AppTypography.cardTitle)
                Text(
                    "자세히",
                    modifier = Modifier.clickable(onClick = onOpenCategoryDetail),
                    style = AppTypography.small,
                    color = SecondaryText,
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            if (state.shouldShowSkeleton) {
                repeat(4) { index ->
                    SkeletonCategorySummaryRow()
                    if (index != 3) Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                state.categories.take(4).forEachIndexed { index, item ->
                    CategorySummaryRow(item = item)
                    if (index != minOf(3, state.categories.take(4).lastIndex)) Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryExpenseDetailPage(
    state: HomeUiState,
    onBack: () -> Unit,
) {
    val rows = if (state.shouldShowSkeleton) emptyList() else state.categories

    ScreenColumn {
        ScreenHeader(title = "카테고리별 지출", onBack = onBack)
        LargeCard {
            PlaceholderInsightText(
                text = state.topCategoryInsight,
                isLoading = state.shouldShowSkeleton,
            )
            Spacer(modifier = Modifier.height(10.dp))
            PlaceholderBodyLine(
                text = "이번 달 총 ${state.expenseLabel} 지출",
                isLoading = state.shouldShowSkeleton,
                widthFraction = 0.45f,
            )
            Spacer(modifier = Modifier.height(22.dp))
            SegmentedProgressBar(
                items = state.expenseRatioItems,
                isLoading = state.shouldShowSkeleton,
            )
            Spacer(modifier = Modifier.height(24.dp))
            if (state.shouldShowSkeleton) {
                repeat(5) { index ->
                    SkeletonCategorySummaryRow()
                    if (index != 4) Spacer(modifier = Modifier.height(18.dp))
                }
            } else {
                rows.forEachIndexed { index, item ->
                    CategoryDetailRow(item = item)
                    if (index != rows.lastIndex) Spacer(modifier = Modifier.height(18.dp))
                }
            }
        }
    }
}

@Composable
private fun IncomeExpensePage(
    state: HomeUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectMonthly: () -> Unit,
    onSelectCashflow: () -> Unit,
) {
    val incomes = state.incomes
    val expenses = state.expenses

    ScreenColumn {
        ScreenHeader(title = "월별 리포트")
        SegmentedTabs(
            selectedIndex = 1,
            labels = listOf("월별 리포트", "수입/지출 통계"),
            onFirst = onSelectMonthly,
            onSecond = onSelectCashflow,
        )
        LargeCard {
            MonthSwitcher(
                yearMonth = state.yearMonth,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
            )
            Spacer(modifier = Modifier.height(22.dp))
            CenteredCaption("이번 달 요약")
            Spacer(modifier = Modifier.height(8.dp))
            PlaceholderHeadline(
                text = state.balanceLabel,
                color = BrandBlue,
                isLoading = state.shouldShowSkeleton,
            )
            Spacer(modifier = Modifier.height(20.dp))
            IncomeExpenseRatioBar(
                income = state.income,
                expense = state.expense,
                isLoading = state.shouldShowSkeleton,
            )
            Spacer(modifier = Modifier.height(16.dp))
            LegendAmount("수입", state.incomeLabel, BrandBlue, state.shouldShowSkeleton)
            Spacer(modifier = Modifier.height(12.dp))
            LegendAmount("지출", "-${state.expenseLabel}", Color.Red, state.shouldShowSkeleton)
        }

        Spacer(modifier = Modifier.height(16.dp))

        MediumCard {
            Text("수입 내역", style = AppTypography.cardTitle)
            Spacer(modifier = Modifier.height(16.dp))
            if (state.shouldShowSkeleton) {
                repeat(3) { index ->
                    SkeletonTransactionRow()
                    if (index != 2) Spacer(modifier = Modifier.height(14.dp))
                }
            } else if (incomes.isEmpty()) {
                Text("내역이 없어요", style = AppTypography.small, color = SecondaryText)
            } else {
                incomes.forEachIndexed { index, item ->
                    TransactionRow(
                        item = EditableTransaction(
                            id = item.id,
                            title = item.merchant,
                            time = item.time,
                            categoryCode = item.categoryCode,
                            category = item.category,
                            amountLabel = item.amountLabel,
                            positive = true,
                            icon = item.icon,
                        ),
                    )
                    if (index != incomes.lastIndex) Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        MediumCard {
            Text("지출 내역", style = AppTypography.cardTitle)
            Spacer(modifier = Modifier.height(16.dp))
            if (state.shouldShowSkeleton) {
                repeat(3) { index ->
                    SkeletonTransactionRow()
                    if (index != 2) Spacer(modifier = Modifier.height(14.dp))
                }
            } else if (expenses.isEmpty()) {
                Text("내역이 없어요", style = AppTypography.small, color = SecondaryText)
            } else {
                expenses.forEachIndexed { index, item ->
                    TransactionRow(
                        item = EditableTransaction(
                            id = item.id,
                            title = item.merchant,
                            time = item.time,
                            categoryCode = item.categoryCode,
                            category = item.category,
                            amountLabel = item.amountLabel,
                            positive = false,
                            icon = item.icon,
                        ),
                    )
                    if (index != expenses.lastIndex) Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
internal fun RecurringPage(
    state: HomeUiState,
    recurringPayments: List<EditableRecurringPayment>,
    recurringCandidates: List<EditableTransaction>,
    onAddRecurring: () -> Unit,
    onDeleteRecurring: (EditableRecurringPayment) -> Unit,
) {
    ScreenColumn {
        ScreenHeader(title = "정기결제")
        LargeCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("정기결제 API", style = AppTypography.small, color = SecondaryText)
                AccentPill("직접 등록 연동", BrandBlue.copy(alpha = 0.12f), BrandBlue)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "기존 지출을 기준으로 정기결제를 등록하고, 서버에 저장된 정기결제 목록을 함께 확인할 수 있어요.",
                style = AppTypography.body,
                color = Color.Black,
            )
            state.recurringErrorMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                AccentPill(message, Color(0xFFFFF1F1), Color(0xFFD92D20))
            }
            state.recurringRegistrationMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                AccentPill(message, BrandBlue.copy(alpha = 0.12f), BrandBlue)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "이번 달 예정 금액 ${state.recurringScheduledTotalAmount.formatWon()}",
                style = AppTypography.body,
                color = Color.Black,
            )
            Spacer(modifier = Modifier.height(24.dp))
            FilledButton(
                text = if (state.recurringRegistrationInFlight) "등록 중..." else "정기결제 추가하기",
                onClick = onAddRecurring,
                enabled = !state.recurringRegistrationInFlight,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        MediumCard {
            Text("등록된 정기결제", style = AppTypography.cardTitle)
            Spacer(modifier = Modifier.height(18.dp))
            if (state.shouldShowSkeleton) {
                repeat(3) { index ->
                    SkeletonTransactionRow()
                    if (index != 2) Spacer(modifier = Modifier.height(14.dp))
                }
            } else if (recurringPayments.isEmpty()) {
                Text("등록된 정기결제가 아직 없어요.", style = AppTypography.small, color = SecondaryText)
            } else {
                recurringPayments.forEachIndexed { index, item ->
                    RecurringRow(
                        title = item.title,
                        subtitle = item.subtitle,
                        amount = item.amountLabel,
                        confirmed = item.confirmed,
                        deleting = item.deleting,
                        onDelete = { onDeleteRecurring(item) },
                    )
                    if (index != recurringPayments.lastIndex) Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        MediumCard {
            Text("등록 가능한 최근 지출", style = AppTypography.cardTitle)
            Spacer(modifier = Modifier.height(18.dp))
            if (recurringCandidates.isEmpty()) {
                Text("정기결제로 등록할 지출 내역이 아직 없어요.", style = AppTypography.small, color = SecondaryText)
            } else {
                recurringCandidates.take(5).forEachIndexed { index, item ->
                    TransactionRow(item = item)
                    if (index != minOf(4, recurringCandidates.lastIndex)) Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
internal fun AlertsPage(
    state: HomeUiState,
    onAcceptSuggestion: () -> Unit,
    suggestionAccepted: Boolean,
    onDismissSuggestion: () -> Unit,
    suggestionDismissed: Boolean,
) {
    ScreenColumn {
        AlertsTopBar()
        MediumCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("푸시 알림", style = AppTypography.cardTitle)
                if (state.pushNotificationUnreadCount > 0L) {
                    Text(
                        "읽지 않음 ${state.pushNotificationUnreadCount}건",
                        style = AppTypography.tiny,
                        color = BrandBlue,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            when {
                state.pushNotificationsLoading && state.pushNotifications.isEmpty() -> {
                    Text("푸시 알림을 불러오는 중이에요.", style = AppTypography.small, color = SecondaryText)
                }

                state.pushNotificationErrorMessage != null -> {
                    Text(state.pushNotificationErrorMessage, style = AppTypography.small, color = Color(0xFFB91C1C))
                }

                state.pushNotifications.isEmpty() -> {
                    Text("아직 도착한 푸시 알림이 없어요.", style = AppTypography.small, color = SecondaryText)
                }

                else -> {
                    state.pushNotifications.take(5).forEachIndexed { index, notification ->
                        PushNotificationRow(notification = notification)
                        if (index != minOf(4, state.pushNotifications.lastIndex)) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
            if (!suggestionAccepted && !suggestionDismissed) {
                Spacer(modifier = Modifier.height(18.dp))
                SuggestionAlert(
                    accepted = suggestionAccepted,
                    onAccept = onAcceptSuggestion,
                    onDismiss = onDismissSuggestion,
                )
            }
        }
    }
}

@Composable
private fun PushNotificationRow(notification: PushNotificationItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (notification.unread) Color(0xFFEAF2FF) else Color(0xFFF4F6F8)),
            contentAlignment = Alignment.Center,
        ) {
            Text("◉", fontSize = 14.sp, color = if (notification.unread) BrandBlue else SecondaryText)
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(notification.title, style = AppTypography.small, color = Color.Black, fontWeight = FontWeight.Bold)
                if (notification.unread) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(BrandBlue),
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(notification.body, style = AppTypography.small, color = SecondaryText)
            if (notification.displayTime.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(notification.displayTime, style = AppTypography.tiny, color = SecondaryText)
            }
        }
    }
}

@Composable
private fun DeleteEntryDialog(
    transaction: EditableTransaction,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("내역 삭제", style = AppTypography.cardTitle, color = Color.Black) },
        text = {
            Text(
                "${transaction.title} ${transaction.amountLabel} 내역을 삭제하시겠습니까?",
                style = AppTypography.small,
                color = SecondaryText,
            )
        },
        confirmButton = { DialogActionButton("삭제", Color(0xFFFF1F1F), Color.White, onDelete) },
        dismissButton = { DialogActionButton("취소", Color(0xFFF4F6F8), Color.Black, onDismiss) },
    )
}

@Composable
private fun EntrySheet(
    title: String,
    entryType: String,
    amount: String,
    merchant: String,
    selectedCategory: String,
    onSelectType: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onMerchantChange: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val isFormValid = amount.isNotBlank() && merchant.isNotBlank() && selectedCategory.isNotBlank()

    BottomSheetShell(title = title, onDismiss = onDismiss) {
        SegmentedControl(
            selected = entryType,
            labels = listOf("지출", "수입"),
            onSelect = onSelectType,
        )
        Spacer(modifier = Modifier.height(24.dp))
        LabeledAmountField("금액", amount, "원", onAmountChange)
        Spacer(modifier = Modifier.height(24.dp))
        LabeledTextField("내역명", merchant, "어디서 쓰셨나요?", onMerchantChange)
        Spacer(modifier = Modifier.height(24.dp))
        Text("카테고리", style = AppTypography.small, color = SecondaryText)
        Spacer(modifier = Modifier.height(12.dp))
        CategoryGrid(selectedCategory = selectedCategory, onSelectCategory = onSelectCategory, limit = 9)
        Spacer(modifier = Modifier.height(20.dp))
        FilledButton("추가하기", onConfirm, enabled = isFormValid)
    }
}

@Composable
private fun CategoryEditSheet(
    selectedCategory: String,
    transaction: EditableTransaction,
    onSelectCategory: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    BottomSheetShell(title = "카테고리 변경", onDismiss = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF4F6F8),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(transaction.title, style = AppTypography.body)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(transaction.time, style = AppTypography.small, color = SecondaryText)
                }
                Text(transaction.amountLabel, style = AppTypography.body)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("카테고리를 선택하세요", style = AppTypography.small, color = SecondaryText)
        Spacer(modifier = Modifier.height(12.dp))
        CategoryGrid(selectedCategory = selectedCategory, onSelectCategory = onSelectCategory, limit = 12)
        Spacer(modifier = Modifier.height(20.dp))
        FilledButton("카테고리 변경", onConfirm, enabled = selectedCategory.isNotBlank())
    }
}

@Composable
internal fun RecurringAddSheet(
    recurringCandidates: List<EditableTransaction>,
    addMode: RecurringAddMode,
    selectedTransactionId: String?,
    manualAmount: String,
    manualMerchant: String,
    manualCategory: String,
    dayOfMonth: String,
    isSubmitting: Boolean,
    onSelectMode: (RecurringAddMode) -> Unit,
    onSelectTransaction: (String?) -> Unit,
    onManualAmountChange: (String) -> Unit,
    onManualMerchantChange: (String) -> Unit,
    onManualCategoryChange: (String) -> Unit,
    onDayOfMonthChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val isDayValid = dayOfMonth.toIntOrNull()?.let { it in 1..31 } == true
    val isManualValid = manualAmount.toLongOrNull()?.let { it > 0L } == true &&
        manualMerchant.isNotBlank() &&
        manualCategory.isNotBlank()
    val isFormValid = when (addMode) {
        RecurringAddMode.Existing -> selectedTransactionId != null && isDayValid
        RecurringAddMode.Manual -> isManualValid && isDayValid
    }

    BottomSheetShell(title = "정기결제 추가", onDismiss = onDismiss) {
        SegmentedControl(
            selected = addMode.label,
            labels = RecurringAddMode.entries.map(RecurringAddMode::label),
            onSelect = { label ->
                onSelectMode(RecurringAddMode.entries.first { it.label == label })
            },
        )
        Spacer(modifier = Modifier.height(24.dp))
        when (addMode) {
            RecurringAddMode.Existing -> {
                Text("기준이 될 지출 내역", style = AppTypography.small, color = SecondaryText)
                Spacer(modifier = Modifier.height(12.dp))
                if (recurringCandidates.isEmpty()) {
                    Text("등록 가능한 지출 내역이 없어요. 직접 입력 모드로 추가할 수 있어요.", style = AppTypography.small, color = SecondaryText)
                } else {
                    recurringCandidates.take(6).forEachIndexed { index, transaction ->
                        SelectableRecurringTransactionRow(
                            transaction = transaction,
                            selected = transaction.id?.toString() == selectedTransactionId,
                            onClick = { onSelectTransaction(transaction.id?.toString()) },
                        )
                        if (index != minOf(5, recurringCandidates.lastIndex)) Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            RecurringAddMode.Manual -> {
                LabeledAmountField("금액", manualAmount, "원", onManualAmountChange)
                Spacer(modifier = Modifier.height(24.dp))
                LabeledTextField("내역명", manualMerchant, "예: 넷플릭스", onManualMerchantChange)
                Spacer(modifier = Modifier.height(24.dp))
                Text("카테고리", style = AppTypography.small, color = SecondaryText)
                Spacer(modifier = Modifier.height(12.dp))
                CategoryGrid(
                    selectedCategory = manualCategory,
                    onSelectCategory = onManualCategoryChange,
                    limit = 9,
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        LabeledCalendarField("매월 결제일", dayOfMonth, onDayOfMonthChange)
        Spacer(modifier = Modifier.height(24.dp))
        FilledButton(
            text = if (isSubmitting) "등록 중..." else "추가하기",
            onClick = onConfirm,
            enabled = isFormValid && !isSubmitting,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetShell(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFF9CA3AF)),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 12.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(title, style = AppTypography.title, color = Color.Black)
            Spacer(modifier = Modifier.height(24.dp))
            content()
        }
    }
}

@Composable
private fun HomeTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFD1D5DB)))
        Box {
            Text("🔔", fontSize = 22.sp, color = SecondaryText)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color.Red),
            )
        }
    }
}

@Composable
private fun HomeSummaryCard(
    title: String,
    amount: String,
    isLoading: Boolean,
    buttonLabel: String,
    onClick: () -> Unit,
) {
    LargeCard(horizontalAlignment = Alignment.Start) {
        Text(title, style = AppTypography.small, color = SecondaryText)
        Spacer(modifier = Modifier.height(12.dp))
        PlaceholderHeadline(
            text = amount,
            isLoading = isLoading,
            color = Color.Black,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))
        FilledButton(buttonLabel, onClick)
    }
}

@Composable
private fun HomeRecentCard(
    transactions: List<EditableTransaction>,
    isLoading: Boolean,
    errorMessage: String?,
    onDeleteTransaction: (EditableTransaction) -> Unit,
    onEditTransactionCategory: (EditableTransaction) -> Unit,
) {
    MediumCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("최근 내역", style = AppTypography.cardTitle)
        }
        Spacer(modifier = Modifier.height(18.dp))
        if (isLoading) {
            repeat(5) { index ->
                SkeletonTransactionRow()
                if (index != 4) Spacer(modifier = Modifier.height(14.dp))
            }
        } else if (!errorMessage.isNullOrBlank()) {
            Text(errorMessage, style = AppTypography.small, color = Color(0xFFD92D20))
        } else if (transactions.isEmpty()) {
            Text("최근 내역이 없어요", style = AppTypography.small, color = SecondaryText)
        } else {
            transactions.take(5).forEachIndexed { index, item ->
                TransactionRow(
                    item = item,
                    onClick = { onEditTransactionCategory(item) },
                    onDelete = { onDeleteTransaction(item) },
                )
                if (index != minOf(4, transactions.lastIndex)) Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun HomeActionCard(
    modifier: Modifier,
    icon: String,
    title: String,
    subtitle: String,
    isLoading: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 3.dp,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5F0FF)),
                contentAlignment = Alignment.Center,
            ) {
                Text(icon, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, style = AppTypography.body)
            Spacer(modifier = Modifier.height(6.dp))
            PlaceholderBodyLine(
                text = subtitle,
                isLoading = isLoading,
                widthFraction = 0.8f,
            )
        }
    }
}

@Composable
private fun ScreenColumn(
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
        content = content,
    )
}

@Composable
private fun ScreenHeader(
    title: String,
    onBack: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(104.dp)
            .background(Color.White),
    ) {
        if (onBack != null) {
            Text(
                "←",
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 18.dp)
                    .clickable(onClick = onBack),
                style = AppTypography.title,
            )
        }
        Text(
            text = title,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp),
            style = AppTypography.title,
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun AlertsTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(104.dp)
            .background(Color.White),
    ) {
        Text(
            "알림",
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp),
            style = AppTypography.title,
        )
        Text(
            "모두 읽음",
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = 20.dp),
            style = AppTypography.small,
            color = SecondaryText,
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun SegmentedTabs(
    selectedIndex: Int,
    labels: List<String>,
    onFirst: () -> Unit,
    onSecond: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFD1D1D3))
            .padding(4.dp),
    ) {
        labels.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selected) Color.White else Color.Transparent)
                    .clickable(onClick = if (index == 0) onFirst else onSecond)
                    .padding(vertical = 12.dp),
            ) {
                Text(
                    text = label,
                    modifier = Modifier.align(Alignment.Center),
                    style = AppTypography.small,
                    color = if (selected) Color.Black else SecondaryText,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(18.dp))
}

@Composable
private fun SegmentedControl(
    selected: String,
    labels: List<String>,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE5E7EB))
            .padding(4.dp),
    ) {
        labels.forEach { label ->
            val selectedItem = selected == label
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selectedItem) Color.White else Color.Transparent)
                    .clickable { onSelect(label) }
                    .padding(vertical = 10.dp),
            ) {
                Text(
                    label,
                    modifier = Modifier.align(Alignment.Center),
                    style = AppTypography.body,
                    color = if (selectedItem) Color.Black else SecondaryText,
                )
            }
        }
    }
}

@Composable
private fun LargeCard(
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = horizontalAlignment,
            content = content,
        )
    }
}

@Composable
private fun MediumCard(
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 3.dp,
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(modifier = Modifier.padding(24.dp), content = content)
    }
}

@Composable
private fun MonthSwitcher(
    yearMonth: java.time.YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("‹", modifier = Modifier.clickable(onClick = onPreviousMonth), style = AppTypography.title)
        Spacer(modifier = Modifier.width(18.dp))
        Text("${yearMonth.year}년 ${yearMonth.monthValue}월", style = AppTypography.cardTitle)
        Spacer(modifier = Modifier.width(18.dp))
        Text("›", modifier = Modifier.clickable(onClick = onNextMonth), style = AppTypography.title)
    }
}

@Composable
private fun CenteredCaption(text: String) {
    Text(text, style = AppTypography.small, color = SecondaryText, textAlign = TextAlign.Center)
}

@Composable
private fun CenteredHeadline(text: String, color: Color = Color.Black) {
    Text(text, style = AppTypography.amount, color = color, textAlign = TextAlign.Center)
}

@Composable
private fun PlaceholderHeadline(
    text: String,
    isLoading: Boolean,
    color: Color = Color.Black,
    textAlign: TextAlign = TextAlign.Center,
    modifier: Modifier = Modifier,
) {
    Text(
        text = if (isLoading) " " else text,
        style = AppTypography.amount,
        color = color,
        textAlign = textAlign,
        modifier = modifier
            .skeletonPlaceholder(visible = isLoading, shape = RoundedCornerShape(12.dp)),
    )
}

@Composable
private fun PlaceholderAccentPill(
    text: String,
    background: Color,
    textColor: Color,
    isLoading: Boolean,
) {
    if (isLoading) {
        Box(
            modifier = Modifier
                .width(144.dp)
                .height(30.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0xFFF2F4F6))
                .skeletonPlaceholder(visible = true, shape = RoundedCornerShape(999.dp)),
        )
        return
    }

    AccentPill(text = text, background = background, textColor = textColor)
}

@Composable
private fun PlaceholderBodyLine(
    text: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    textColor: Color = SecondaryText,
    widthFraction: Float = 0.6f,
    textAlign: TextAlign = TextAlign.Start,
) {
    Text(
        text = if (isLoading) " " else text,
        style = AppTypography.small,
        color = textColor,
        textAlign = textAlign,
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .skeletonPlaceholder(visible = isLoading, shape = RoundedCornerShape(8.dp)),
    )
}

@Composable
private fun PlaceholderTinyLine(
    text: String,
    isLoading: Boolean,
    width: androidx.compose.ui.unit.Dp,
) {
    Text(
        text = if (isLoading) " " else text,
        style = AppTypography.tiny,
        modifier = Modifier
            .width(width)
            .skeletonPlaceholder(visible = isLoading, shape = RoundedCornerShape(8.dp)),
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun PlaceholderInsightText(
    text: String,
    isLoading: Boolean,
) {
    Text(
        text = if (isLoading) " \n " else text,
        style = AppTypography.heroTitle,
        color = Color.Black,
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .skeletonPlaceholder(visible = isLoading, shape = RoundedCornerShape(12.dp)),
    )
}

@Composable
internal fun AccentPill(text: String, background: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(text, style = AppTypography.tiny, color = textColor)
    }
}

@Composable
private fun BarCompare(
    isLoading: Boolean,
    leftLabel: String,
    rightLabel: String,
    leftAmount: String,
    rightAmount: String,
    leftColor: Color,
    rightColor: Color,
    leftRatio: Float,
    rightRatio: Float,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        CompareBar(leftLabel, leftAmount, leftColor, leftRatio, isLoading)
        CompareBar(rightLabel, rightAmount, rightColor, rightRatio, isLoading)
    }
}

@Composable
private fun CompareBar(
    label: String,
    amount: String,
    color: Color,
    ratio: Float,
    isLoading: Boolean,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        PlaceholderTinyLine(
            text = amount,
            isLoading = isLoading,
            width = 56.dp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(56.dp)
                .height((100 * ratio.coerceIn(0.5f, 1f)).dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(if (isLoading) Color(0xFFE5E7EB) else color)
                .skeletonPlaceholder(
                    visible = isLoading,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                ),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = AppTypography.tiny, color = SecondaryText)
    }
}

@Composable
private fun SegmentedProgressBar(
    items: List<StatBar>,
    isLoading: Boolean,
) {
    if (isLoading || items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0xFFF2F4F6))
                .skeletonPlaceholder(visible = true, shape = RoundedCornerShape(999.dp)),
        )
        return
    }

    val normalizedItems = items.filter { it.value > 0f }
    val fallbackItems = normalizedItems.ifEmpty { listOf(StatBar(label = "지출", value = 1f, color = BrandBlue)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFD1D1D3)),
    ) {
        fallbackItems.forEach { item ->
            Box(
                modifier = Modifier
                    .weight(item.value.coerceAtLeast(0.001f))
                    .fillMaxSize()
                    .background(item.color),
            )
        }
    }
}

@Composable
private fun CategorySummaryRow(item: CategoryExpenseItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(item.barColor))
        Spacer(modifier = Modifier.width(12.dp))
        Text(item.categoryName, style = AppTypography.body)
        Spacer(modifier = Modifier.weight(1f))
        Text(item.amount.formatWonCompact(), style = AppTypography.body)
        Spacer(modifier = Modifier.width(16.dp))
        Text("${(item.ratio * 100).toInt()}%", style = AppTypography.small, color = SecondaryText)
    }
}

@Composable
private fun CategoryDetailRow(item: CategoryExpenseItem) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CategoryIcon(icon = item.categoryEmoji())
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.categoryName, style = AppTypography.body)
            Text("${(item.ratio * 100).toInt()}%", style = AppTypography.small, color = SecondaryText)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(item.amount.formatWonCompact(), style = AppTypography.body)
            Text(
                if (item.spentMoreThanPreviousMonth) "지난달보다 많이 씀" else "지난달보다 적게 씀",
                style = AppTypography.tiny,
                color = if (item.spentMoreThanPreviousMonth) Color.Red else BrandBlue,
            )
        }
    }
}

@Composable
private fun IncomeExpenseRatioBar(
    income: Long,
    expense: Long,
    isLoading: Boolean,
) {
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0xFFF2F4F6))
                .skeletonPlaceholder(visible = true, shape = RoundedCornerShape(999.dp)),
        )
        return
    }

    val safeIncome = income.coerceAtLeast(0L)
    val safeExpense = expense.coerceAtLeast(0L)
    val total = safeIncome + safeExpense
    val incomeWeight = if (total == 0L) 1f else safeIncome.toFloat().coerceAtLeast(1f)
    val expenseWeight = if (total == 0L) 1f else safeExpense.toFloat().coerceAtLeast(1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFF2F4F6)),
    ) {
        Box(
            modifier = Modifier
                .weight(incomeWeight)
                .fillMaxSize()
                .background(BrandBlue),
        )
        Box(
            modifier = Modifier
                .weight(expenseWeight)
                .fillMaxSize()
                .background(Color.Red),
        )
    }
}

@Composable
private fun LegendAmount(
    label: String,
    amount: String,
    color: Color,
    isLoading: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, style = AppTypography.body, color = SecondaryText)
        Spacer(modifier = Modifier.weight(1f))
        PlaceholderBodyLine(
            text = amount,
            isLoading = isLoading,
            textColor = color,
            widthFraction = 0.25f,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun TransactionRow(
    item: EditableTransaction,
    onClick: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryIcon(item.icon)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, style = AppTypography.body)
            Text("${item.time} · ${item.category}", style = AppTypography.small, color = SecondaryText)
        }
        Text(
            item.amountLabel,
            style = AppTypography.body,
            color = if (item.positive) BrandBlue else Color.Black,
        )
        if (onDelete != null) {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (item.deleting) "삭제 중" else "삭제",
                modifier = Modifier.clickable(enabled = !item.deleting, onClick = onDelete),
                style = AppTypography.small,
                color = SecondaryText,
            )
        }
    }
}

@Composable
private fun SelectableRecurringTransactionRow(
    transaction: EditableTransaction,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (selected) BrandBlue.copy(alpha = 0.12f) else Color(0xFFF4F6F8),
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryIcon(transaction.icon)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.title, style = AppTypography.body)
                Text("${transaction.time} · ${transaction.category}", style = AppTypography.small, color = SecondaryText)
            }
            Text(transaction.amountLabel, style = AppTypography.body, color = Color.Black)
        }
    }
}

@Composable
private fun RecurringRow(
    title: String,
    subtitle: String,
    amount: String,
    confirmed: Boolean,
    deleting: Boolean,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryIcon(if (title.contains("통신") || title.contains("관리비")) "🛁" else "🎮")
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = AppTypography.body)
            Text(subtitle, style = AppTypography.small, color = SecondaryText)
        }
        Text(amount, style = AppTypography.body, color = Color.Black)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = if (deleting) "..." else "🗑",
            modifier = Modifier
                .clickable(enabled = !deleting, onClick = onDelete)
                .padding(4.dp),
            style = AppTypography.small,
            color = SecondaryText,
        )
        Spacer(modifier = Modifier.width(8.dp))
        AccentPill(
            text = if (confirmed) "확정" else "후보",
            background = if (confirmed) BrandBlue.copy(alpha = 0.12f) else Color(0xFFF4F6F8),
            textColor = if (confirmed) BrandBlue else SecondaryText,
        )
    }
}

@Composable
private fun DeleteRecurringPaymentDialog(
    recurringPayment: EditableRecurringPayment,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(onClick = onDismiss),
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 44.dp),
            color = Color.White,
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("정기결제 삭제", style = AppTypography.cardTitle, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "${recurringPayment.title}을(를) 삭제하시겠습니까?",
                    style = AppTypography.small,
                    color = SecondaryText,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DialogActionButton(
                        label = "취소",
                        background = Color(0xFFF4F6F8),
                        content = Color.Black,
                        onClick = onDismiss,
                    )
                    DialogActionButton(
                        label = "삭제",
                        background = Color(0xFFFF1F1F),
                        content = Color.White,
                        onClick = onDelete,
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertListItem(
    circleColor: Color,
    title: String,
    body: String,
    time: String,
    unread: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
    ) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(circleColor))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = AppTypography.body)
                if (unread) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(BrandBlue))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(body, style = AppTypography.small, color = Color(0xFF4E5968))
            Spacer(modifier = Modifier.height(10.dp))
            Text(time, style = AppTypography.tiny, color = SecondaryText)
        }
    }
}

@Composable
private fun SuggestionAlert(
    accepted: Boolean,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
    ) {
        val compact = maxWidth < 360.dp

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFFEE500)))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("정기결제가 의심돼요!", style = AppTypography.body)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "스포티파이 9,900원이 매월 결제되고 있어요.",
                    style = AppTypography.small,
                    color = Color(0xFF4E5968),
                )
                Text(
                    "정기결제로 등록하시겠습니까?",
                    style = AppTypography.small,
                    color = Color(0xFF4E5968),
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (compact) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SmallGhostButton(
                            text = "아니오",
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        SmallFilledButton(
                            text = if (accepted) "완료" else "예",
                            onClick = onAccept,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SmallGhostButton("아니오", onDismiss)
                        SmallFilledButton(if (accepted) "완료" else "예", onAccept)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("어제", style = AppTypography.tiny, color = SecondaryText)
            }
        }
    }
}

@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = AppTypography.small, color = SecondaryText)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, style = AppTypography.input, color = SecondaryText) },
            singleLine = true,
            textStyle = AppTypography.input.copy(color = Color.Black),
            shape = RoundedCornerShape(16.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun LabeledAmountField(
    label: String,
    value: String,
    suffix: String,
    onValueChange: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = AppTypography.small, color = SecondaryText)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("0", style = AppTypography.amountLarge, color = SecondaryText) },
            suffix = { Text(suffix, style = AppTypography.title, color = SecondaryText) },
            singleLine = true,
            textStyle = AppTypography.amountLarge.copy(color = Color.Black),
            shape = RoundedCornerShape(16.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun LabeledCalendarField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = AppTypography.small, color = SecondaryText)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("15", style = AppTypography.input, color = SecondaryText) },
            suffix = { Text("일", style = AppTypography.body, color = SecondaryText) },
            singleLine = true,
            textStyle = AppTypography.input.copy(color = Color.Black),
            shape = RoundedCornerShape(16.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun CategoryGrid(
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    limit: Int,
) {
    categoryChoices.take(limit).chunked(3).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            row.forEach { choice ->
                CategoryGridItem(
                    modifier = Modifier.weight(1f),
                    choice = choice,
                    selected = choice.code == selectedCategory,
                    onClick = { onSelectCategory(choice.code) },
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun CategoryGridItem(
    modifier: Modifier,
    choice: CategoryChoice,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = if (selected) BrandBlue else Color(0xFFF4F6F8),
        shadowElevation = if (selected) 6.dp else 0.dp,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 18.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(choice.icon, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                choice.label,
                style = AppTypography.tiny,
                color = if (selected) Color.White else Color.Black,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FilledButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandBlue,
            contentColor = Color.White,
            disabledContainerColor = Color(0xFFD1D5DB),
            disabledContentColor = SecondaryText,
        ),
    ) {
        Text(text, style = AppTypography.button)
    }
}

@Composable
private fun DialogActionButton(
    label: String,
    background: Color,
    content: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(102.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = AppTypography.small, color = content)
    }
}

@Composable
private fun SmallGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF4F6F8))
            .clickable(onClick = onClick)
            .padding(horizontal = 30.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = AppTypography.tiny)
    }
}

@Composable
private fun SmallFilledButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(BrandBlue)
            .clickable(onClick = onClick)
            .padding(horizontal = 34.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = AppTypography.tiny, color = Color.White)
    }
}

@Composable
private fun DividerLine() {
    HorizontalDivider(color = Color.Black, thickness = 1.dp)
}

@Composable
private fun CategoryIcon(icon: String) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(0xFFF4F6F8)),
        contentAlignment = Alignment.Center,
    ) {
        Text(icon, fontSize = 18.sp)
    }
}

@Composable
private fun MainBottomBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MainTab.entries.forEach { tab ->
                val selected = tab == selectedTab
                Column(
                    modifier = Modifier
                        .width(72.dp)
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(tab.icon, fontSize = 20.sp, color = if (selected) Color.Black else Color(0xFFB0B8C1))
                    Text(tab.label, style = AppTypography.tiny, color = if (selected) Color.Black else Color(0xFFB0B8C1))
                }
            }
        }
    }
}

internal object AppTypography {
    val title
        @Composable get() = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold,
        )
    val heroTitle
        @Composable get() = androidx.compose.material3.MaterialTheme.typography.headlineMedium.copy(
            fontSize = 19.sp,
            lineHeight = 33.sp,
            fontWeight = FontWeight.Bold,
        )
    val amount
        @Composable get() = androidx.compose.material3.MaterialTheme.typography.headlineMedium.copy(
            fontSize = 26.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.Bold,
        )
    val amountLarge
        @Composable get() = androidx.compose.material3.MaterialTheme.typography.headlineMedium.copy(
            fontSize = 30.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.Bold,
        )
    val cardTitle
        @Composable get() = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
            fontSize = 15.sp,
            lineHeight = 27.sp,
            fontWeight = FontWeight.Bold,
        )
    val body
        @Composable get() = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(
            fontSize = 14.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.SemiBold,
        )
    val input
        @Composable get() = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(
            fontSize = 18.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Normal,
        )
    val small
        @Composable get() = androidx.compose.material3.MaterialTheme.typography.bodySmall.copy(
            fontSize = 12.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
        )
    val tiny
        @Composable get() = androidx.compose.material3.MaterialTheme.typography.bodySmall.copy(
            fontSize = 10.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )
    val button
        @Composable get() = androidx.compose.material3.MaterialTheme.typography.labelLarge.copy(
            fontSize = 15.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.SemiBold,
        )
}

private fun Long.formatWonCompact(): String = "%,d원".format(this)

internal fun Long?.formatWonOrEmpty(): String = this?.let { "%,d원".format(it) } ?: "데이터 없음"

private fun barRatio(amount: Long?, otherAmount: Long?): Float {
    val safeAmount = amount ?: 0L
    val maxAmount = maxOf(safeAmount, otherAmount ?: 0L)
    return when {
        maxAmount <= 0L -> 0.55f
        else -> (safeAmount.toFloat() / maxAmount.toFloat()).coerceIn(0.55f, 1f)
    }
}

private fun CategoryExpenseItem.categoryEmoji(): String = when (category) {
    "FOOD" -> "🍔"
    "SALARY" -> "💰"
    "TRANSPORT_CAR" -> "🚌"
    "TRAVEL_STAY" -> "✈"
    "HOBBY_LEISURE" -> "🎮"
    else -> "•"
}

@Composable
private fun Modifier.skeletonPlaceholder(
    visible: Boolean,
    shape: Shape = RoundedCornerShape(8.dp),
): Modifier = placeholder(
    visible = visible,
    shape = shape,
)

@Composable
private fun SkeletonTransactionRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFF2F4F6))
                .skeletonPlaceholder(visible = true, shape = CircleShape),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(13.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF2F4F6))
                    .skeletonPlaceholder(visible = true, shape = RoundedCornerShape(6.dp)),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.35f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF2F4F6))
                    .skeletonPlaceholder(visible = true, shape = RoundedCornerShape(6.dp)),
            )
        }
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(13.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF2F4F6))
                .skeletonPlaceholder(visible = true, shape = RoundedCornerShape(6.dp)),
        )
    }
}

@Composable
private fun SkeletonCategorySummaryRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Color(0xFFF2F4F6))
                .skeletonPlaceholder(visible = true, shape = CircleShape),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF2F4F6))
                .skeletonPlaceholder(visible = true, shape = RoundedCornerShape(6.dp)),
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF2F4F6))
                .skeletonPlaceholder(visible = true, shape = RoundedCornerShape(6.dp)),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .width(30.dp)
                .height(10.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF2F4F6))
                .skeletonPlaceholder(visible = true, shape = RoundedCornerShape(6.dp)),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    MyApplicationTheme(darkTheme = false, dynamicColor = false) {
        HomeScreen(
            userId = "demo_user",
            onLogoutClick = {},
            allowMockData = true,
        )
    }
}
