package com.uson.myapplication.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uson.myapplication.ui.theme.BackgroundGray
import com.uson.myapplication.ui.theme.BrandBlue
import com.uson.myapplication.ui.theme.MyApplicationTheme
import com.uson.myapplication.ui.theme.SecondaryText

private enum class MainTab(val label: String, val icon: String) {
    Home("홈", "⌂"),
    Stats("통계", "◔"),
    Recurring("정기결제", "↻"),
    Alerts("알림", "🔔"),
}

private enum class OverlaySheet {
    AddEntry,
    EditCategory,
    AddRecurring,
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

data class EditableTransaction(
    val title: String,
    val time: String,
    val category: String,
    val amountLabel: String,
    val positive: Boolean,
    val icon: String,
)

@Composable
fun HomeScreen(
    userId: String?,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var currentTab by rememberSaveable { mutableStateOf(MainTab.Home) }
    var statsMode by rememberSaveable { mutableIntStateOf(0) }
    var showCategoryDetail by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var overlaySheet by rememberSaveable { mutableStateOf<OverlaySheet?>(null) }
    var entryType by rememberSaveable { mutableStateOf("지출") }
    var selectedCategory by rememberSaveable { mutableStateOf("FOOD") }
    var recurringSuggestionAccepted by rememberSaveable { mutableStateOf(false) }
    val recurringToggles = remember { mutableStateListOf(true, true, true, false) }

    val editableTransactions = remember(state.expenses, state.incomes, state.shouldShowSkeleton) {
        buildList {
            if (state.expenses.isNotEmpty()) {
                state.expenses.forEach {
                    add(
                        EditableTransaction(
                            title = it.merchant,
                            time = it.time,
                            category = it.category,
                            amountLabel = it.amountLabel,
                            positive = false,
                            icon = it.icon,
                        ),
                    )
                }
            }
            if (state.incomes.isNotEmpty()) {
                state.incomes.forEach {
                    add(
                        EditableTransaction(
                            title = it.merchant,
                            time = it.time,
                            category = it.category,
                            amountLabel = it.amountLabel,
                            positive = true,
                            icon = it.icon,
                        ),
                    )
                }
            }
            if (isEmpty()) {
                addAll(
                    listOf(
                        EditableTransaction("스타벅스 강남점", "14:30", "식비", "-4,500원", false, "🍔"),
                        EditableTransaction("쿠팡 로켓배송", "10:15", "쇼핑", "-32,000원", false, "🛍"),
                        EditableTransaction("지하철", "08:40", "교통", "-1,400원", false, "🚌"),
                        EditableTransaction("월급", "어제", "급여", "+3,500,000원", true, "💰"),
                        EditableTransaction("넷플릭스", "어제", "구독", "-17,000원", false, "🎮"),
                    ),
                )
            }
        }
    }

    val expensesForList = editableTransactions.filterNot(EditableTransaction::positive)
    val incomesForList = editableTransactions.filter(EditableTransaction::positive)
    val selectedRecurringCount = recurringToggles.count { it }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = BackgroundGray,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = BackgroundGray,
                bottomBar = {
                    MainBottomBar(
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
                            onAddEntry = { overlaySheet = OverlaySheet.AddEntry },
                            onDeleteRecent = { showDeleteDialog = true },
                            onEditTransactionCategory = { overlaySheet = OverlaySheet.EditCategory },
                            onOpenMonthlyReport = {
                                currentTab = MainTab.Stats
                                statsMode = 0
                            },
                            onOpenRecurring = { currentTab = MainTab.Recurring },
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
                            recurringCount = selectedRecurringCount,
                            recurringToggles = recurringToggles,
                            onAddRecurring = { overlaySheet = OverlaySheet.AddRecurring },
                        )

                        MainTab.Alerts -> AlertsPage(
                            onAcceptSuggestion = {
                                recurringSuggestionAccepted = true
                                currentTab = MainTab.Recurring
                            },
                            suggestionAccepted = recurringSuggestionAccepted,
                        )
                    }
                }
            }

            if (showDeleteDialog) {
                DeleteEntryDialog(
                    onDismiss = { showDeleteDialog = false },
                    onDelete = { showDeleteDialog = false },
                )
            }

            when (overlaySheet) {
                OverlaySheet.AddEntry -> EntrySheet(
                    title = "내역 추가",
                    entryType = entryType,
                    selectedCategory = selectedCategory,
                    onSelectType = { entryType = it },
                    onSelectCategory = { selectedCategory = it },
                    onDismiss = { overlaySheet = null },
                    onConfirm = { overlaySheet = null },
                )

                OverlaySheet.EditCategory -> CategoryEditSheet(
                    selectedCategory = selectedCategory,
                    transaction = expensesForList.firstOrNull() ?: editableTransactions.first(),
                    onSelectCategory = { selectedCategory = it },
                    onDismiss = { overlaySheet = null },
                    onConfirm = { overlaySheet = null },
                )

                OverlaySheet.AddRecurring -> RecurringAddSheet(
                    selectedCategory = selectedCategory,
                    onSelectCategory = { selectedCategory = it },
                    onDismiss = { overlaySheet = null },
                    onConfirm = { overlaySheet = null },
                )

                null -> Unit
            }
        }
    }
}

@Composable
private fun HomeDashboardPage(
    state: HomeUiState,
    transactions: List<EditableTransaction>,
    onAddEntry: () -> Unit,
    onDeleteRecent: () -> Unit,
    onEditTransactionCategory: () -> Unit,
    onOpenMonthlyReport: () -> Unit,
    onOpenRecurring: () -> Unit,
) {
    val expenseCardAmount = if (state.shouldShowSkeleton) "1,234,500원" else state.expenseLabel

    ScreenColumn {
        HomeTopBar()
        HomeSummaryCard(
            title = "이번 달 지출",
            amount = expenseCardAmount,
            buttonLabel = "내역 추가",
            onClick = onAddEntry,
        )
        Spacer(modifier = Modifier.height(16.dp))
        HomeRecentCard(
            transactions = transactions,
            onDeleteRecent = onDeleteRecent,
            onShowMore = {},
            onEditTransactionCategory = onEditTransactionCategory,
            showMoreLabel = false,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HomeActionCard(
                modifier = Modifier.weight(1f),
                icon = "📊",
                title = "월별 리포트",
                subtitle = state.expenseComparisonLabel.takeIf { !state.shouldShowSkeleton } ?: "지난 달보다 덜 썼어요",
                onClick = onOpenMonthlyReport,
            )
            HomeActionCard(
                modifier = Modifier.weight(1f),
                icon = "🔄",
                title = "정기결제",
                subtitle = "이번 달 3건 남았어요",
                onClick = onOpenRecurring,
            )
        }
    }
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
            CenteredHeadline(if (state.shouldShowSkeleton) "1,250,000원" else state.expenseLabel)
            Spacer(modifier = Modifier.height(12.dp))
            AccentPill(
                text = state.expenseComparisonLabel.takeIf { !state.shouldShowSkeleton } ?: "지난달보다 17% 감소",
                background = BrandBlue.copy(alpha = 0.12f),
                textColor = BrandBlue,
            )
            Spacer(modifier = Modifier.height(28.dp))
            CenteredCaption("지출 비교")
            Spacer(modifier = Modifier.height(16.dp))
            BarCompare(
                leftLabel = "${state.previousMonth.monthValue}월",
                rightLabel = "${state.yearMonth.monthValue}월",
                leftAmount = state.previousExpense.formatWonOrEmpty(),
                rightAmount = if (state.shouldShowSkeleton) "1,200,000원" else state.expenseLabel,
                leftColor = Color(0xFF8CC7F4),
                rightColor = BrandBlue,
                leftRatio = barRatio(state.previousExpense, state.expense),
                rightRatio = barRatio(state.expense, state.previousExpense),
            )
            Spacer(modifier = Modifier.height(28.dp))
            CenteredCaption("수입 비교")
            Spacer(modifier = Modifier.height(16.dp))
            BarCompare(
                leftLabel = "${state.previousMonth.monthValue}월",
                rightLabel = "${state.yearMonth.monthValue}월",
                leftAmount = state.previousIncome.formatWonOrEmpty(),
                rightAmount = if (state.shouldShowSkeleton) "3,800,000원" else state.incomeLabel,
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
            val rows = state.categories.ifEmpty {
                listOf(
                    CategoryExpenseItem("FOOD", "식비", 450_000L, 0.35f, true),
                    CategoryExpenseItem("SALARY", "급여", 450_000L, 0.35f, false),
                    CategoryExpenseItem("TRANSPORT_CAR", "교통", 450_000L, 0.35f, true),
                    CategoryExpenseItem("TRAVEL_STAY", "여행", 450_000L, 0.35f, false),
                )
            }.take(4)
            rows.forEachIndexed { index, item ->
                CategorySummaryRow(item = item)
                if (index != rows.lastIndex) Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CategoryExpenseDetailPage(
    state: HomeUiState,
    onBack: () -> Unit,
) {
    val rows = state.categories.ifEmpty {
        listOf(
            CategoryExpenseItem("FOOD", "식비", 450_000L, 0.35f, true),
            CategoryExpenseItem("SALARY", "급여", 450_000L, 0.35f, false),
            CategoryExpenseItem("TRANSPORT_CAR", "교통", 450_000L, 0.35f, true),
            CategoryExpenseItem("TRAVEL_STAY", "여행", 450_000L, 0.35f, false),
            CategoryExpenseItem("HOBBY_LEISURE", "취미", 450_000L, 0.35f, false),
        )
    }

    ScreenColumn {
        ScreenHeader(title = "카테고리별 지출", onBack = onBack)
        LargeCard {
            Text(state.topCategoryInsight, style = AppTypography.heroTitle, color = Color.Black)
            Spacer(modifier = Modifier.height(10.dp))
            Text("이번 달 총 ${if (state.shouldShowSkeleton) "1,234,500원" else state.expenseLabel} 지출", style = AppTypography.small, color = SecondaryText)
            Spacer(modifier = Modifier.height(22.dp))
            SegmentedProgressBar()
            Spacer(modifier = Modifier.height(24.dp))
            rows.forEachIndexed { index, item ->
                CategoryDetailRow(item = item)
                if (index != rows.lastIndex) Spacer(modifier = Modifier.height(18.dp))
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
    val incomes = state.incomes.ifEmpty {
        listOf(TransactionItem("💰", "월급", "어제", "급여", 3_500_000L, positive = true))
    }
    val expenses = state.expenses.ifEmpty {
        listOf(
            TransactionItem("🍔", "스타벅스 강남점", "14:30", "식비", 1_400L, false),
            TransactionItem("🛍", "쿠팡 로켓배송", "10:15", "쇼핑", 1_400L, false),
            TransactionItem("🚌", "지하철", "08:40", "교통", 1_400L, false),
            TransactionItem("🍔", "스타벅스 강남점", "14:30", "식비", 1_400L, false),
            TransactionItem("🛍", "쿠팡 로켓배송", "10:15", "쇼핑", 1_400L, false),
        )
    }

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
            CenteredHeadline(if (state.shouldShowSkeleton) "+3,445,100원" else state.balanceLabel, color = BrandBlue)
            Spacer(modifier = Modifier.height(20.dp))
            IncomeExpenseRatioBar()
            Spacer(modifier = Modifier.height(16.dp))
            LegendAmount("수입", if (state.shouldShowSkeleton) "+3,500,000원" else state.incomeLabel, BrandBlue)
            Spacer(modifier = Modifier.height(12.dp))
            LegendAmount("지출", if (state.shouldShowSkeleton) "-3,500,000원" else "-${state.expenseLabel}", Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        MediumCard {
            Text("수입 내역", style = AppTypography.cardTitle)
            Spacer(modifier = Modifier.height(16.dp))
            incomes.forEachIndexed { index, item ->
                TransactionRow(
                    item = EditableTransaction(
                        title = item.merchant,
                        time = item.time,
                        category = item.category,
                        amountLabel = item.amountLabel,
                        positive = true,
                        icon = item.icon,
                    ),
                )
                if (index != incomes.lastIndex) Spacer(modifier = Modifier.height(14.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        MediumCard {
            Text("지출 내역", style = AppTypography.cardTitle)
            Spacer(modifier = Modifier.height(16.dp))
            expenses.forEachIndexed { index, item ->
                TransactionRow(
                    item = EditableTransaction(
                        title = item.merchant,
                        time = item.time,
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

@Composable
private fun RecurringPage(
    recurringCount: Int,
    recurringToggles: List<Boolean>,
    onAddRecurring: () -> Unit,
) {
    val recurringRows = listOf(
        Triple("넷플릭스", "매월 15일", "17,000원"),
        Triple("유튜브 프리미엄", "매월 15일", "17,000원"),
        Triple("아파트 관리비", "매월 15일", "17,000원"),
        Triple("통신비", "매월 10일", "55,000원"),
    )

    ScreenColumn {
        ScreenHeader(title = "정기결제")
        LargeCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("이번 달 예정된 결제", style = AppTypography.small, color = SecondaryText)
                AccentPill("${recurringCount}건", BrandBlue.copy(alpha = 0.12f), BrandBlue)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("177,450원", style = AppTypography.amount, color = Color.Black)
            Spacer(modifier = Modifier.height(24.dp))
            FilledButton("정기결제 추가하기", onAddRecurring)
        }

        Spacer(modifier = Modifier.height(16.dp))

        MediumCard {
            Text("등록된 결제", style = AppTypography.cardTitle)
            Spacer(modifier = Modifier.height(18.dp))
            recurringRows.forEachIndexed { index, row ->
                RecurringRow(
                    title = row.first,
                    subtitle = row.second,
                    amount = row.third,
                    enabled = recurringToggles.getOrElse(index) { false },
                )
                if (index != recurringRows.lastIndex) Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun AlertsPage(
    onAcceptSuggestion: () -> Unit,
    suggestionAccepted: Boolean,
) {
    ScreenColumn {
        AlertsTopBar()
        AlertListItem(
            circleColor = Color(0xFFE5F0FF),
            title = "내일 넷플릭스 결제일이에요",
            body = "17,000원이 결제될 예정입니다.",
            time = "2시간 전",
            unread = true,
        )
        Spacer(modifier = Modifier.height(14.dp))
        AlertListItem(
            circleColor = Color(0xFFFFF0F1),
            title = "10월 월별 리포트가 도착했어요",
            body = "지난달보다 지출이 12% 늘었어요. 확인해보세요!",
            time = "어제",
            unread = false,
        )
        Spacer(modifier = Modifier.height(14.dp))
        SuggestionAlert(
            accepted = suggestionAccepted,
            onAccept = onAcceptSuggestion,
        )
    }
}

@Composable
private fun DeleteEntryDialog(
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
                Text("내역 삭제", style = AppTypography.cardTitle, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Text("이 내역을 삭제하시겠습니까?", style = AppTypography.small, color = SecondaryText)
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
private fun EntrySheet(
    title: String,
    entryType: String,
    selectedCategory: String,
    onSelectType: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    BottomSheetShell(title = title, onDismiss = onDismiss) {
        SegmentedControl(
            selected = entryType,
            labels = listOf("지출", "수입"),
            onSelect = onSelectType,
        )
        Spacer(modifier = Modifier.height(24.dp))
        LabeledAmountField("금액", "0", "원")
        Spacer(modifier = Modifier.height(24.dp))
        LabeledTextField("내역명", "어디서 쓰셨나요?")
        Spacer(modifier = Modifier.height(24.dp))
        Text("카테고리", style = AppTypography.small, color = SecondaryText)
        Spacer(modifier = Modifier.height(12.dp))
        CategoryGrid(selectedCategory = selectedCategory, onSelectCategory = onSelectCategory, limit = 9)
        Spacer(modifier = Modifier.height(20.dp))
        FilledButton("추가하기", onConfirm)
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
        FilledButton("카테고리를 선택하세요", onConfirm, enabled = false)
    }
}

@Composable
private fun RecurringAddSheet(
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    BottomSheetShell(title = "정기결제 추가", onDismiss = onDismiss) {
        LabeledTextField("결제명", "예: 넷플릭스")
        Spacer(modifier = Modifier.height(24.dp))
        LabeledAmountField("결제 금액", "0", "원")
        Spacer(modifier = Modifier.height(24.dp))
        LabeledCalendarField("매월 결제일", "15일")
        Spacer(modifier = Modifier.height(24.dp))
        Text("카테고리", style = AppTypography.small, color = SecondaryText)
        Spacer(modifier = Modifier.height(12.dp))
        CategoryGrid(selectedCategory = selectedCategory, onSelectCategory = onSelectCategory, limit = 9)
        Spacer(modifier = Modifier.height(20.dp))
        FilledButton("추가하기", onConfirm)
    }
}

@Composable
private fun BottomSheetShell(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(onClick = onDismiss),
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFF9CA3AF)),
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(title, style = AppTypography.title, color = Color.Black)
                Spacer(modifier = Modifier.height(24.dp))
                content()
            }
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
    buttonLabel: String,
    onClick: () -> Unit,
) {
    LargeCard(horizontalAlignment = Alignment.Start) {
        Text(title, style = AppTypography.small, color = SecondaryText)
        Spacer(modifier = Modifier.height(12.dp))
        Text(amount, style = AppTypography.amount, color = Color.Black)
        Spacer(modifier = Modifier.height(28.dp))
        FilledButton(buttonLabel, onClick)
    }
}

@Composable
private fun HomeRecentCard(
    transactions: List<EditableTransaction>,
    onDeleteRecent: () -> Unit,
    onShowMore: () -> Unit,
    onEditTransactionCategory: () -> Unit,
    showMoreLabel: Boolean,
) {
    MediumCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("최근 내역", style = AppTypography.cardTitle)
            Text(
                text = if (showMoreLabel) "더보기" else "🗑",
                modifier = Modifier.clickable(onClick = if (showMoreLabel) onShowMore else onDeleteRecent),
                style = AppTypography.small,
                color = SecondaryText,
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        transactions.take(5).forEachIndexed { index, item ->
            TransactionRow(item = item, onClick = onEditTransactionCategory)
            if (index != minOf(4, transactions.lastIndex)) Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun HomeActionCard(
    modifier: Modifier,
    icon: String,
    title: String,
    subtitle: String,
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
            Text(subtitle, style = AppTypography.small, color = SecondaryText)
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
private fun AccentPill(text: String, background: Color, textColor: Color) {
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
        CompareBar(leftLabel, leftAmount, leftColor, leftRatio)
        CompareBar(rightLabel, rightAmount, rightColor, rightRatio)
    }
}

@Composable
private fun CompareBar(
    label: String,
    amount: String,
    color: Color,
    ratio: Float,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(amount, style = AppTypography.tiny)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(56.dp)
                .height((100 * ratio.coerceIn(0.5f, 1f)).dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(color),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = AppTypography.tiny, color = SecondaryText)
    }
}

@Composable
private fun SegmentedProgressBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFD1D1D3)),
    ) {
        listOf(
            Color(0xFF3182F6) to 3f,
            Color(0xFFFFB11B) to 2f,
            Color.Red to 1.8f,
            Color(0xFF6BCB77) to 1f,
            Color(0xFF8B95A1) to 0.8f,
            Color(0xFFD1D1D3) to 0.6f,
        ).forEach { (color, weight) ->
            Box(
                modifier = Modifier
                    .weight(weight)
                    .fillMaxSize()
                    .background(color),
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
private fun IncomeExpenseRatioBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFF2F4F6)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.52f)
                .fillMaxSize()
                .clip(RoundedCornerShape(999.dp))
                .background(BrandBlue),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 132.dp)
                .fillMaxSize()
                .clip(RoundedCornerShape(999.dp))
                .background(Color.Red),
        )
    }
}

@Composable
private fun LegendAmount(label: String, amount: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, style = AppTypography.body, color = SecondaryText)
        Spacer(modifier = Modifier.weight(1f))
        Text(amount, style = AppTypography.body, color = color)
    }
}

@Composable
private fun TransactionRow(
    item: EditableTransaction,
    onClick: (() -> Unit)? = null,
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
    }
}

@Composable
private fun RecurringRow(
    title: String,
    subtitle: String,
    amount: String,
    enabled: Boolean,
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
        Text(amount, style = AppTypography.body, color = if (enabled) Color.Black else SecondaryText)
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = enabled,
            onCheckedChange = {},
            colors = SwitchDefaults.colors(
                checkedTrackColor = BrandBlue,
                uncheckedTrackColor = Color(0xFFD1D1D3),
                checkedThumbColor = Color.White,
                uncheckedThumbColor = Color.White,
            ),
        )
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
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
    ) {
        Row {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFFEE500)))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("정기결제가 의심돼요!", style = AppTypography.body)
                Spacer(modifier = Modifier.height(8.dp))
                Text("스포티파이 9,900원이 매월 결제되고 있어요.", style = AppTypography.small, color = Color(0xFF4E5968))
                Text("정기결제로 등록하시겠습니까?", style = AppTypography.small, color = Color(0xFF4E5968))
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SmallGhostButton("아니오")
                    SmallFilledButton(if (accepted) "완료" else "예", onAccept)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("어제", style = AppTypography.tiny, color = SecondaryText)
            }
        }
    }
}

@Composable
private fun LabeledTextField(label: String, placeholder: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = AppTypography.small, color = SecondaryText)
        Spacer(modifier = Modifier.height(12.dp))
        Text(placeholder, style = AppTypography.input, color = SecondaryText)
        Spacer(modifier = Modifier.height(12.dp))
        DividerLine()
    }
}

@Composable
private fun LabeledAmountField(label: String, value: String, suffix: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = AppTypography.small, color = SecondaryText)
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, style = AppTypography.amountLarge, color = SecondaryText)
            Spacer(modifier = Modifier.weight(1f))
            Text(suffix, style = AppTypography.title, color = SecondaryText)
        }
        Spacer(modifier = Modifier.height(12.dp))
        DividerLine()
    }
}

@Composable
private fun LabeledCalendarField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = AppTypography.small, color = SecondaryText)
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, style = AppTypography.input, color = Color.Black)
            Spacer(modifier = Modifier.weight(1f))
            Text("🗓", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        DividerLine()
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
private fun SmallGhostButton(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF4F6F8))
            .padding(horizontal = 30.dp, vertical = 8.dp),
    ) {
        Text(text, style = AppTypography.tiny)
    }
}

@Composable
private fun SmallFilledButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(BrandBlue)
            .clickable(onClick = onClick)
            .padding(horizontal = 34.dp, vertical = 8.dp),
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

private object AppTypography {
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

private fun Long?.formatWonOrEmpty(): String = this?.let { "%,d원".format(it) } ?: "데이터 없음"

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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    MyApplicationTheme(darkTheme = false, dynamicColor = false) {
        HomeScreen(
            userId = "demo_user",
            onLogoutClick = {},
        )
    }
}
