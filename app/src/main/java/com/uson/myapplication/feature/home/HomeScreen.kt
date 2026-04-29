package com.uson.myapplication.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uson.myapplication.ui.theme.BackgroundGray
import com.uson.myapplication.ui.theme.BrandBlue
import com.uson.myapplication.ui.theme.KakaoYellow
import com.uson.myapplication.ui.theme.MyApplicationTheme
import com.uson.myapplication.ui.theme.SecondaryText
import androidx.compose.ui.graphics.Color

private enum class MainTab(val label: String, val icon: String) {
    Home(label = "홈", icon = "⌂"),
    Stats(label = "통계", icon = "◔"),
    Recurring(label = "정기결제", icon = "↻"),
    Alerts(label = "알림", icon = "🔔"),
}

private enum class HomeDestination(val tab: MainTab) {
    Dashboard(tab = MainTab.Home),
    AddEntry(tab = MainTab.Home),
    SpendingHistory(tab = MainTab.Home),
    StatsOverview(tab = MainTab.Stats),
    MonthlyReport(tab = MainTab.Stats),
    Cashflow(tab = MainTab.Stats),
    RecurringOverview(tab = MainTab.Recurring),
    RecurringDetail(tab = MainTab.Recurring),
    AlertsOverview(tab = MainTab.Alerts),
    AlertDetail(tab = MainTab.Alerts),
    AlertSettings(tab = MainTab.Alerts),
}

private data class TransactionItem(
    val icon: String,
    val merchant: String,
    val time: String,
    val category: String,
    val amount: String,
    val positive: Boolean = false,
)

private data class StatBar(
    val label: String,
    val value: Float,
    val color: Color,
)

private data class RecurringPayment(
    val icon: String,
    val name: String,
    val cycle: String,
    val amount: String,
    val enabled: Boolean,
)

private data class AlertItem(
    val title: String,
    val body: String,
    val time: String,
    val badge: String,
)

private val dashboardTransactions = listOf(
    TransactionItem(icon = "🍔", merchant = "스타벅스 강남점", time = "14:30", category = "식비", amount = "-1,400원"),
    TransactionItem(icon = "🛍", merchant = "쿠팡 로켓배송", time = "10:15", category = "쇼핑", amount = "-10,400원"),
    TransactionItem(icon = "🚌", merchant = "지하철", time = "08:40", category = "교통", amount = "-1,400원"),
    TransactionItem(icon = "💰", merchant = "월급", time = "어제", category = "급여", amount = "+3,500,000원", positive = true),
    TransactionItem(icon = "🎮", merchant = "넷플릭스", time = "어제", category = "구독", amount = "-1,400원"),
)

private val expenseBreakdown = listOf(
    StatBar(label = "식비", value = 0.82f, color = BrandBlue),
    StatBar(label = "쇼핑", value = 0.61f, color = Color(0xFF85B4FF)),
    StatBar(label = "교통", value = 0.33f, color = Color(0xFFC5DDFF)),
    StatBar(label = "구독", value = 0.24f, color = Color(0xFFCBD5E1)),
)

private val recurringPayments = listOf(
    RecurringPayment(icon = "🎬", name = "넷플릭스", cycle = "매월 3일", amount = "17,000원", enabled = true),
    RecurringPayment(icon = "🎧", name = "멜론", cycle = "매월 8일", amount = "10,900원", enabled = true),
    RecurringPayment(icon = "☁", name = "iCloud+", cycle = "매월 12일", amount = "4,400원", enabled = false),
    RecurringPayment(icon = "🧾", name = "통신 요금", cycle = "매월 21일", amount = "59,000원", enabled = true),
)

private val alerts = listOf(
    AlertItem(title = "예산 초과 알림", body = "식비 예산의 92%를 사용했어요. 오늘 저녁 지출은 잠시 확인해 보세요.", time = "방금", badge = "예산"),
    AlertItem(title = "정기결제 예정", body = "넷플릭스 결제가 내일 예정되어 있어요. 잔액을 미리 확인해 두세요.", time = "1시간 전", badge = "정기결제"),
    AlertItem(title = "월별 리포트 도착", body = "이번 달 소비 리포트가 준비됐어요. 지난달보다 7% 절약했어요.", time = "어제", badge = "리포트"),
)

@Composable
fun HomeScreen(
    userId: String?,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var destinationKey by rememberSaveable { mutableStateOf(HomeDestination.Dashboard.name) }
    val destination = HomeDestination.valueOf(destinationKey)

    fun navigate(target: HomeDestination) {
        destinationKey = target.name
    }

    fun navigateToTab(tab: MainTab) {
        destinationKey = when (tab) {
            MainTab.Home -> HomeDestination.Dashboard.name
            MainTab.Stats -> HomeDestination.StatsOverview.name
            MainTab.Recurring -> HomeDestination.RecurringOverview.name
            MainTab.Alerts -> HomeDestination.AlertsOverview.name
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = BackgroundGray,
    ) {
        Scaffold(
            containerColor = BackgroundGray,
            bottomBar = {
                MainBottomBar(
                    selectedTab = destination.tab,
                    onTabSelected = ::navigateToTab,
                )
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundGray)
                    .statusBarsPadding()
                    .padding(innerPadding),
            ) {
                when (destination) {
                    HomeDestination.Dashboard -> DashboardPage(
                        userId = userId,
                        onAddEntry = { navigate(HomeDestination.AddEntry) },
                        onHistory = { navigate(HomeDestination.SpendingHistory) },
                        onStats = { navigate(HomeDestination.MonthlyReport) },
                        onRecurring = { navigate(HomeDestination.RecurringOverview) },
                        onAlerts = { navigate(HomeDestination.AlertsOverview) },
                    )

                    HomeDestination.AddEntry -> AddEntryPage(onBack = { navigate(HomeDestination.Dashboard) })
                    HomeDestination.SpendingHistory -> SpendingHistoryPage(
                        onBack = { navigate(HomeDestination.Dashboard) },
                        onMonthlyReport = { navigate(HomeDestination.MonthlyReport) },
                    )

                    HomeDestination.StatsOverview -> StatsOverviewPage(
                        onMonthlyReport = { navigate(HomeDestination.MonthlyReport) },
                        onCashflow = { navigate(HomeDestination.Cashflow) },
                        onHistory = { navigate(HomeDestination.SpendingHistory) },
                    )

                    HomeDestination.MonthlyReport -> MonthlyReportPage(onBack = { navigate(HomeDestination.StatsOverview) })
                    HomeDestination.Cashflow -> CashflowPage(onBack = { navigate(HomeDestination.StatsOverview) })
                    HomeDestination.RecurringOverview -> RecurringOverviewPage(
                        onDetail = { navigate(HomeDestination.RecurringDetail) },
                        onReport = { navigate(HomeDestination.MonthlyReport) },
                    )

                    HomeDestination.RecurringDetail -> RecurringDetailPage(onBack = { navigate(HomeDestination.RecurringOverview) })
                    HomeDestination.AlertsOverview -> AlertsOverviewPage(
                        onAlertDetail = { navigate(HomeDestination.AlertDetail) },
                        onSettings = { navigate(HomeDestination.AlertSettings) },
                    )

                    HomeDestination.AlertDetail -> AlertDetailPage(onBack = { navigate(HomeDestination.AlertsOverview) })
                    HomeDestination.AlertSettings -> AlertSettingsPage(
                        userId = userId,
                        onBack = { navigate(HomeDestination.AlertsOverview) },
                        onLogoutClick = onLogoutClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardPage(
    userId: String?,
    onAddEntry: () -> Unit,
    onHistory: () -> Unit,
    onStats: () -> Unit,
    onRecurring: () -> Unit,
    onAlerts: () -> Unit,
) {
    ScreenColumn {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD1D1D3)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = userId?.take(1)?.uppercase() ?: "",
                    color = Color.White,
                    style = HomeTextStyle.smallLabel.copy(fontWeight = FontWeight.Bold),
                )
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onAlerts),
                contentAlignment = Alignment.TopEnd,
            ) {
                Text(
                    text = "🔔",
                    fontSize = 20.sp,
                )
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp, end = 2.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.Red),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        RoundedPanel {
            Text(
                text = "이번 달 지출",
                style = HomeTextStyle.smallLabel,
                color = Color(0xFF71717A),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "1,234,500원",
                style = HomeTextStyle.heroValue,
                color = Color.Black,
            )
            Spacer(modifier = Modifier.height(24.dp))
            PrimaryButton(
                text = "내역 추가",
                containerColor = BrandBlue,
                contentColor = Color.White,
                onClick = onAddEntry,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        RoundedPanel {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "최근 내역", style = HomeTextStyle.sectionTitle)
                Text(
                    text = "🗑",
                    modifier = Modifier.clickable(onClick = onHistory),
                    fontSize = 18.sp,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            dashboardTransactions.forEachIndexed { index, item ->
                TransactionRow(item = item)
                if (index != dashboardTransactions.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InsightCard(
                modifier = Modifier.weight(1f),
                icon = "📊",
                title = "월별 리포트",
                body = "저번 달보다 덜 썼어요",
                onClick = onStats,
            )
            InsightCard(
                modifier = Modifier.weight(1f),
                icon = "🔄",
                title = "정기결제",
                body = "이번 달 3건 남았어요",
                onClick = onRecurring,
            )
        }
    }
}

@Composable
private fun AddEntryPage(onBack: () -> Unit) {
    ScreenColumn {
        PageHeader(title = "내역 추가", onBack = onBack)
        Spacer(modifier = Modifier.height(16.dp))
        RoundedPanel {
            ChipRow(labels = listOf("지출", "수입", "이체"), selected = "지출")
            Spacer(modifier = Modifier.height(20.dp))
            LabeledValue(label = "금액", value = "32,000원")
            Spacer(modifier = Modifier.height(16.dp))
            LabeledValue(label = "가맹점", value = "쿠팡 로켓배송")
            Spacer(modifier = Modifier.height(16.dp))
            LabeledValue(label = "카테고리", value = "쇼핑")
            Spacer(modifier = Modifier.height(16.dp))
            LabeledValue(label = "날짜", value = "2026년 4월 23일 10:15")
            Spacer(modifier = Modifier.height(16.dp))
            LabeledValue(label = "메모", value = "생활용품과 간식 구매")
            Spacer(modifier = Modifier.height(24.dp))
            PrimaryButton(
                text = "내역 저장",
                containerColor = BrandBlue,
                contentColor = Color.White,
                onClick = onBack,
            )
        }
    }
}

@Composable
private fun SpendingHistoryPage(
    onBack: () -> Unit,
    onMonthlyReport: () -> Unit,
) {
    ScreenColumn {
        PageHeader(title = "전체 내역", onBack = onBack)
        Spacer(modifier = Modifier.height(16.dp))
        RoundedPanel {
            ChipRow(labels = listOf("전체", "수입", "지출"), selected = "전체")
            Spacer(modifier = Modifier.height(20.dp))
            SummaryStrip(
                leftTitle = "이번 달",
                leftValue = "1,389,000원",
                rightTitle = "저축 가능",
                rightValue = "+1,445,100원",
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "수입 내역", style = HomeTextStyle.sectionTitle)
            Spacer(modifier = Modifier.height(8.dp))
            TransactionRow(item = dashboardTransactions[3])
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "지출 내역", style = HomeTextStyle.sectionTitle)
            Spacer(modifier = Modifier.height(8.dp))
            dashboardTransactions.filterNot { it.positive }.forEachIndexed { index, item ->
                TransactionRow(item = item)
                if (index != 3) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            PrimaryButton(
                text = "월별 리포트 보기",
                containerColor = Color.White,
                contentColor = BrandBlue,
                borderColor = BrandBlue,
                onClick = onMonthlyReport,
            )
        }
    }
}

@Composable
private fun StatsOverviewPage(
    onMonthlyReport: () -> Unit,
    onCashflow: () -> Unit,
    onHistory: () -> Unit,
) {
    ScreenColumn {
        PageHeader(title = "소비 통계")
        Spacer(modifier = Modifier.height(16.dp))
        RoundedPanel {
            ChipRow(labels = listOf("이번 달", "지난 달", "카테고리"), selected = "이번 달")
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "월간 소비 추이", style = HomeTextStyle.sectionTitle)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                ChartColumn(label = "2월", value = "1.25M", barHeight = 96.dp, color = Color(0xFFB9D8FF))
                ChartColumn(label = "3월", value = "1.48M", barHeight = 124.dp, color = BrandBlue)
                ChartColumn(label = "4월", value = "1.23M", barHeight = 104.dp, color = Color(0xFF9BC4FF))
            }
            Spacer(modifier = Modifier.height(20.dp))
            expenseBreakdown.forEach { item ->
                StatProgressRow(item = item)
                Spacer(modifier = Modifier.height(10.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            PrimaryButton(
                text = "월별 리포트",
                containerColor = BrandBlue,
                contentColor = Color.White,
                onClick = onMonthlyReport,
            )
            Spacer(modifier = Modifier.height(10.dp))
            PrimaryButton(
                text = "수입 / 지출 분석",
                containerColor = Color.White,
                contentColor = BrandBlue,
                borderColor = BrandBlue,
                onClick = onCashflow,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        RoundedPanel {
            Text(text = "지출 흐름", style = HomeTextStyle.sectionTitle)
            Spacer(modifier = Modifier.height(16.dp))
            SummaryStrip(
                leftTitle = "평균 결제",
                leftValue = "24,600원",
                rightTitle = "가장 큰 지출",
                rightValue = "쿠팡 320,000원",
            )
            Spacer(modifier = Modifier.height(20.dp))
            PrimaryButton(
                text = "전체 내역 보기",
                containerColor = Color(0xFFF8FAFF),
                contentColor = Color.Black,
                onClick = onHistory,
            )
        }
    }
}

@Composable
private fun MonthlyReportPage(onBack: () -> Unit) {
    ScreenColumn {
        PageHeader(title = "월별 리포트", onBack = onBack)
        Spacer(modifier = Modifier.height(16.dp))
        RoundedPanel {
            Text(text = "이번 달 요약", style = HomeTextStyle.sectionTitle)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "지난달보다 7% 절약했어요",
                style = HomeTextStyle.supportText,
                color = SecondaryText,
            )
            Spacer(modifier = Modifier.height(20.dp))
            SummaryStrip(
                leftTitle = "총 지출",
                leftValue = "1,234,500원",
                rightTitle = "절약 금액",
                rightValue = "-94,000원",
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HighlightCard(
                    modifier = Modifier.weight(1f),
                    title = "가장 많이 쓴 곳",
                    body = "쇼핑",
                    accent = Color(0xFFEEF5FF),
                )
                HighlightCard(
                    modifier = Modifier.weight(1f),
                    title = "가장 많이 아낀 곳",
                    body = "교통",
                    accent = Color(0xFFF3FBF6),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        RoundedPanel {
            Text(text = "카테고리 비교", style = HomeTextStyle.sectionTitle)
            Spacer(modifier = Modifier.height(16.dp))
            expenseBreakdown.forEach { item ->
                StatProgressRow(item = item)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun CashflowPage(onBack: () -> Unit) {
    ScreenColumn {
        PageHeader(title = "수입 / 지출 분석", onBack = onBack)
        Spacer(modifier = Modifier.height(16.dp))
        RoundedPanel {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HighlightCard(
                    modifier = Modifier.weight(1f),
                    title = "총 수입",
                    body = "+3,500,000원",
                    accent = Color(0xFFF0F6FF),
                    contentColor = BrandBlue,
                )
                HighlightCard(
                    modifier = Modifier.weight(1f),
                    title = "총 지출",
                    body = "-1,234,500원",
                    accent = Color(0xFFFFF4F4),
                    contentColor = Color(0xFFE34D4D),
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "이번 달 현금 흐름", style = HomeTextStyle.sectionTitle)
            Spacer(modifier = Modifier.height(12.dp))
            StatProgressRow(item = StatBar("수입", 1f, BrandBlue))
            Spacer(modifier = Modifier.height(10.dp))
            StatProgressRow(item = StatBar("지출", 0.46f, Color(0xFFE34D4D)))
            Spacer(modifier = Modifier.height(10.dp))
            StatProgressRow(item = StatBar("저축", 0.34f, Color(0xFF5FB96F)))
        }

        Spacer(modifier = Modifier.height(16.dp))

        RoundedPanel {
            Text(text = "최근 변동", style = HomeTextStyle.sectionTitle)
            Spacer(modifier = Modifier.height(12.dp))
            dashboardTransactions.take(4).forEachIndexed { index, item ->
                TransactionRow(item = item)
                if (index != 3) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun RecurringOverviewPage(
    onDetail: () -> Unit,
    onReport: () -> Unit,
) {
    ScreenColumn {
        PageHeader(title = "정기결제")
        Spacer(modifier = Modifier.height(16.dp))
        RoundedPanel {
            Text(text = "이번 달 예정 금액", style = HomeTextStyle.smallLabel, color = SecondaryText)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "93,300원", style = HomeTextStyle.heroValue, color = Color.Black)
            Spacer(modifier = Modifier.height(20.dp))
            SummaryStrip(
                leftTitle = "남은 건수",
                leftValue = "3건",
                rightTitle = "다음 결제",
                rightValue = "넷플릭스 · 내일",
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        RoundedPanel {
            Text(text = "서비스별 현황", style = HomeTextStyle.sectionTitle)
            Spacer(modifier = Modifier.height(16.dp))
            recurringPayments.chunked(2).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowItems.forEach { item ->
                        RecurringCard(
                            modifier = Modifier.weight(1f),
                            item = item,
                            onClick = onDetail,
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            PrimaryButton(
                text = "월별 리포트와 함께 보기",
                containerColor = Color.White,
                contentColor = BrandBlue,
                borderColor = BrandBlue,
                onClick = onReport,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        RoundedPanel {
            Text(text = "자동 추적 중", style = HomeTextStyle.sectionTitle)
            Spacer(modifier = Modifier.height(12.dp))
            recurringPayments.forEachIndexed { index, item ->
                RecurringRow(item = item)
                if (index != recurringPayments.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = Color(0xFFF1F5F9),
                    )
                }
            }
        }
    }
}

@Composable
private fun RecurringDetailPage(onBack: () -> Unit) {
    ScreenColumn {
        PageHeader(title = "정기결제 상세", onBack = onBack)
        Spacer(modifier = Modifier.height(16.dp))
        RoundedPanel {
            Text(text = "넷플릭스", style = HomeTextStyle.sectionTitle)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "매월 3일 · 17,000원", style = HomeTextStyle.supportText, color = SecondaryText)
            Spacer(modifier = Modifier.height(20.dp))
            LabeledValue(label = "결제 카드", value = "우리카드 체크")
            Spacer(modifier = Modifier.height(16.dp))
            LabeledValue(label = "알림", value = "결제 1일 전 푸시 알림")
            Spacer(modifier = Modifier.height(16.dp))
            LabeledValue(label = "메모", value = "공유 계정 갱신 전 확인 필요")
            Spacer(modifier = Modifier.height(24.dp))
            PrimaryButton(
                text = "자동 추적 유지",
                containerColor = BrandBlue,
                contentColor = Color.White,
                onClick = onBack,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        RoundedPanel {
            Text(text = "최근 결제", style = HomeTextStyle.sectionTitle)
            Spacer(modifier = Modifier.height(12.dp))
            listOf("2026.04.03", "2026.03.03", "2026.02.03").forEachIndexed { index, date ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = date, style = HomeTextStyle.supportText, color = SecondaryText)
                    Text(text = "17,000원", style = HomeTextStyle.valueText)
                }
                if (index != 2) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = Color(0xFFF1F5F9),
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertsOverviewPage(
    onAlertDetail: () -> Unit,
    onSettings: () -> Unit,
) {
    ScreenColumn {
        PageHeader(title = "알림", trailingLabel = "설정", onTrailingClick = onSettings)
        Spacer(modifier = Modifier.height(16.dp))
        RoundedPanel {
            ChipRow(labels = listOf("전체", "예산", "정기결제", "리포트"), selected = "전체")
            Spacer(modifier = Modifier.height(20.dp))
            alerts.forEachIndexed { index, item ->
                AlertCard(item = item, onClick = onAlertDetail)
                if (index != alerts.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun AlertDetailPage(onBack: () -> Unit) {
    ScreenColumn {
        PageHeader(title = "알림 상세", onBack = onBack)
        Spacer(modifier = Modifier.height(16.dp))
        RoundedPanel {
            Text(text = "예산 초과 알림", style = HomeTextStyle.sectionTitle)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "식비 예산의 92%를 사용했어요. 남은 기간 동안 하루 평균 8,000원 이하로 관리하면 이번 달 목표를 지킬 수 있어요.",
                style = HomeTextStyle.supportText,
                color = SecondaryText,
            )
            Spacer(modifier = Modifier.height(20.dp))
            StatProgressRow(item = StatBar("식비 사용률", 0.92f, Color(0xFFE34D4D)))
            Spacer(modifier = Modifier.height(20.dp))
            HighlightCard(
                title = "추천 액션",
                body = "오늘 저녁은 집에 있는 식재료로 해결해 보세요.",
                accent = Color(0xFFFFF8E6),
                contentColor = Color.Black,
            )
        }
    }
}

@Composable
private fun AlertSettingsPage(
    userId: String?,
    onBack: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    var budgetAlerts by remember { mutableStateOf(true) }
    var recurringAlerts by remember { mutableStateOf(true) }
    var monthlyReportAlerts by remember { mutableStateOf(false) }

    ScreenColumn {
        PageHeader(title = "알림 설정", onBack = onBack)
        Spacer(modifier = Modifier.height(16.dp))
        RoundedPanel {
            Text(text = "계정", style = HomeTextStyle.sectionTitle)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = userId ?: "demo_user",
                style = HomeTextStyle.supportText,
                color = SecondaryText,
            )
            Spacer(modifier = Modifier.height(20.dp))
            ToggleRow(
                title = "예산 초과 알림",
                subtitle = "월별 예산의 80%, 100% 구간에서 알림을 보내요.",
                checked = budgetAlerts,
                onCheckedChange = { budgetAlerts = it },
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF1F5F9))
            ToggleRow(
                title = "정기결제 알림",
                subtitle = "정기결제 하루 전에 알려드려요.",
                checked = recurringAlerts,
                onCheckedChange = { recurringAlerts = it },
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF1F5F9))
            ToggleRow(
                title = "월별 리포트 알림",
                subtitle = "매달 1일 오전에 지난달 리포트를 받아요.",
                checked = monthlyReportAlerts,
                onCheckedChange = { monthlyReportAlerts = it },
            )
            Spacer(modifier = Modifier.height(24.dp))
            PrimaryButton(
                text = "로그아웃",
                containerColor = Color.White,
                contentColor = Color.Black,
                borderColor = Color(0xFFE2E8F0),
                onClick = onLogoutClick,
            )
        }
    }
}

@Composable
private fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 24.dp),
        content = content,
    )
}

@Composable
private fun PageHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    trailingLabel: String? = null,
    onTrailingClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                Text(
                    text = "←",
                    modifier = Modifier
                        .clickable(onClick = onBack)
                        .padding(end = 12.dp),
                    style = HomeTextStyle.sectionTitle,
                )
            }
            Text(text = title, style = HomeTextStyle.sectionTitle)
        }
        if (trailingLabel != null && onTrailingClick != null) {
            Text(
                text = trailingLabel,
                modifier = Modifier.clickable(onClick = onTrailingClick),
                style = HomeTextStyle.supportText.copy(fontWeight = FontWeight.SemiBold),
                color = BrandBlue,
            )
        }
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
                .windowInsetsPadding(WindowInsets.navigationBars)
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
                    Text(
                        text = tab.icon,
                        fontSize = 19.sp,
                        color = if (selected) Color.Black else Color(0xFF9CA3AF),
                    )
                    Text(
                        text = tab.label,
                        style = HomeTextStyle.tinyLabel,
                        color = if (selected) Color.Black else Color(0xFF9CA3AF),
                    )
                }
            }
        }
    }
}

@Composable
private fun RoundedPanel(
    modifier: Modifier = Modifier,
    padding: Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 6.dp,
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(padding),
            content = content,
        )
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    borderColor: Color = Color.Transparent,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
    ) {
        Text(
            text = text,
            style = HomeTextStyle.buttonLabel,
        )
    }
}

@Composable
private fun TransactionRow(item: TransactionItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BackgroundGray),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = item.icon, fontSize = 18.sp)
            }
            Column {
                Text(text = item.merchant, style = HomeTextStyle.valueText)
                Text(
                    text = "${item.time} · ${item.category}",
                    style = HomeTextStyle.supportText,
                    color = SecondaryText,
                )
            }
        }
        Text(
            text = item.amount,
            style = HomeTextStyle.valueText,
            color = if (item.positive) BrandBlue else Color.Black,
        )
    }
}

@Composable
private fun InsightCard(
    modifier: Modifier = Modifier,
    icon: String,
    title: String,
    body: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = Color.White,
        shadowElevation = 6.dp,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = icon, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, style = HomeTextStyle.valueText)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = body, style = HomeTextStyle.supportText, color = SecondaryText)
        }
    }
}

@Composable
private fun ChipRow(
    labels: List<String>,
    selected: String,
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.forEach { label ->
            val isSelected = label == selected
            Surface(
                color = if (isSelected) BrandBlue.copy(alpha = 0.12f) else Color(0xFFF4F6F8),
                shape = RoundedCornerShape(999.dp),
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = HomeTextStyle.supportText.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isSelected) BrandBlue else SecondaryText,
                )
            }
        }
    }
}

@Composable
private fun SummaryStrip(
    leftTitle: String,
    leftValue: String,
    rightTitle: String,
    rightValue: String,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        HighlightCard(
            modifier = Modifier.weight(1f),
            title = leftTitle,
            body = leftValue,
            accent = Color(0xFFF8FAFF),
        )
        HighlightCard(
            modifier = Modifier.weight(1f),
            title = rightTitle,
            body = rightValue,
            accent = Color(0xFFF8FAFF),
        )
    }
}

@Composable
private fun HighlightCard(
    modifier: Modifier = Modifier,
    title: String,
    body: String,
    accent: Color,
    contentColor: Color = Color.Black,
) {
    Surface(
        modifier = modifier,
        color = accent,
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = HomeTextStyle.smallLabel, color = SecondaryText)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = body,
                style = HomeTextStyle.valueText.copy(fontWeight = FontWeight.Bold),
                color = contentColor,
            )
        }
    }
}

@Composable
private fun StatProgressRow(item: StatBar) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = item.label, style = HomeTextStyle.supportText, color = Color.Black)
            Text(
                text = "${(item.value * 100).toInt()}%",
                style = HomeTextStyle.supportText.copy(fontWeight = FontWeight.SemiBold),
                color = SecondaryText,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0xFFF1F5F9)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(item.value)
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(item.color),
            )
        }
    }
}

@Composable
private fun ChartColumn(
    label: String,
    value: String,
    barHeight: Dp,
    color: Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = HomeTextStyle.tinyLabel, color = SecondaryText)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(52.dp)
                .height(barHeight)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(color),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, style = HomeTextStyle.supportText, color = SecondaryText)
    }
}

@Composable
private fun LabeledValue(label: String, value: String) {
    Column {
        Text(text = label, style = HomeTextStyle.smallLabel, color = SecondaryText)
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            color = Color(0xFFF8FAFC),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(
                text = value,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                style = HomeTextStyle.valueText,
            )
        }
    }
}

@Composable
private fun RecurringCard(
    modifier: Modifier = Modifier,
    item: RecurringPayment,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = Color(0xFFF8FAFF),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = item.icon, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = item.name, style = HomeTextStyle.valueText)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = item.amount, style = HomeTextStyle.supportText, color = SecondaryText)
        }
    }
}

@Composable
private fun RecurringRow(item: RecurringPayment) {
    var enabled by remember(item.name) { mutableStateOf(item.enabled) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BackgroundGray),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = item.icon, fontSize = 18.sp)
            }
            Column {
                Text(text = item.name, style = HomeTextStyle.valueText)
                Text(text = "${item.cycle} · ${item.amount}", style = HomeTextStyle.supportText, color = SecondaryText)
            }
        }
        Switch(
            checked = enabled,
            onCheckedChange = { enabled = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = BrandBlue,
            ),
        )
    }
}

@Composable
private fun AlertCard(
    item: AlertItem,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = Color(0xFFF8FAFF),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = BrandBlue.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text(
                        text = item.badge,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = HomeTextStyle.tinyLabel.copy(fontWeight = FontWeight.SemiBold),
                        color = BrandBlue,
                    )
                }
                Text(text = item.time, style = HomeTextStyle.tinyLabel, color = SecondaryText)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = item.title, style = HomeTextStyle.valueText)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = item.body, style = HomeTextStyle.supportText, color = SecondaryText)
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = HomeTextStyle.valueText)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, style = HomeTextStyle.supportText, color = SecondaryText)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = BrandBlue,
            ),
        )
    }
}

private object HomeTextStyle {
    val heroValue
        @Composable get() = androidx.compose.material3.MaterialTheme.typography.headlineMedium.copy(
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Bold,
        )
    val sectionTitle
        @Composable get() = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(
            fontSize = 20.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold,
        )
    val valueText
        @Composable get() = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(
            fontSize = 15.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.SemiBold,
        )
    val supportText
        @Composable get() = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
            fontSize = 13.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
        )
    val smallLabel
        @Composable get() = androidx.compose.material3.MaterialTheme.typography.bodySmall.copy(
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium,
        )
    val tinyLabel
        @Composable get() = androidx.compose.material3.MaterialTheme.typography.bodySmall.copy(
            fontSize = 10.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
        )
    val buttonLabel
        @Composable get() = androidx.compose.material3.MaterialTheme.typography.labelLarge.copy(
            fontSize = 14.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
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
