package com.uson.myapplication.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.foundation.ExperimentalFoundationApi
import com.uson.myapplication.R
import com.uson.myapplication.ui.theme.BalogHomeAttention
import com.uson.myapplication.ui.theme.BalogHomeBorder
import com.uson.myapplication.ui.theme.BalogHomeCanvas
import com.uson.myapplication.ui.theme.BalogHomeCard
import com.uson.myapplication.ui.theme.BalogHomeCerulean
import com.uson.myapplication.ui.theme.BalogHomeCta
import com.uson.myapplication.ui.theme.BalogHomeGreen
import com.uson.myapplication.ui.theme.BalogHomeInk
import com.uson.myapplication.ui.theme.BalogHomeMuted
import com.uson.myapplication.ui.theme.BalogHomeNavy
import java.text.NumberFormat
import java.util.Locale

private val plex = FontFamily(
    Font(R.font.ibm_plex_sans_kr_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_sans_kr_semibold, FontWeight.SemiBold),
    Font(R.font.ibm_plex_sans_kr_bold, FontWeight.Bold),
)
private val remix = FontFamily(Font(R.font.remixicon))
private val won = NumberFormat.getNumberInstance(Locale.KOREA)
private val cardShape = RoundedCornerShape(8.dp)
private val sampleTransactions = listOf(
    EditableTransaction(title = "어니언 성수", time = "09:41", categoryCode = "UNCATEGORIZED", category = "분류 중", amountLabel = "-6,800", positive = false, icon = ""),
    EditableTransaction(title = "스타벅스 강남R점", time = "08:12", categoryCode = "CAFE_SNACK", category = "카페 · 간식", amountLabel = "-5,600", positive = false, icon = ""),
    EditableTransaction(title = "이마트24 뚝섬점", time = "22:07", categoryCode = "CONVENIENCE_MART_MISC", category = "마트 · 편의점", amountLabel = "-4,300", positive = false, icon = ""),
    EditableTransaction(title = "쿠팡", time = "19:32", categoryCode = "UNCATEGORIZED", category = "분류 중", amountLabel = "-32,900", positive = false, icon = ""),
    EditableTransaction(title = "서울교통공사", time = "08:24", categoryCode = "TRANSPORT_CAR", category = "교통", amountLabel = "-1,400", positive = false, icon = ""),
)
private data class RecurringPreview(val title: String, val amount: String, val days: String, val icon: Int)
private val sampleRecurring = listOf(
    RecurringPreview("KT 통신요금", "55,000원 / 월", "D-10", 0xF15A),
    RecurringPreview("아파트 관리비", "87,000원 / 월", "D-15", 0xEE1D),
    RecurringPreview("ChatGPT Plus", "29,000원 / 월", "D-17", 0xF072),
    RecurringPreview("유튜브 프리미엄", "14,900원 / 월", "D-22", 0xF072),
    RecurringPreview("넷플릭스", "17,000원 / 월", "D-28", 0xF072),
)

@Composable
internal fun BalogHomeDashboard(
    state: HomeUiState,
    transactions: List<EditableTransaction>,
    allowMockData: Boolean,
    automaticRecordingEnabled: Boolean,
    onOpenRecordingSettings: () -> Unit,
    onAddEntry: () -> Unit,
    onOpenTransaction: (EditableTransaction) -> Unit,
    onDeleteTransaction: (EditableTransaction) -> Unit,
    onOpenReport: () -> Unit,
    onOpenRecurring: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenTransactions: () -> Unit,
) {
    val showSample = allowMockData && state.expense == 0L && state.recentTransactions.isEmpty()
    val expense = if (showSample) 1_160_200L else state.expense
    val entries = if (showSample) sampleTransactions else transactions
    val month = if (showSample) "2026년 9월" else "${state.yearMonth.year}년 ${state.yearMonth.monthValue}월"
    val recurring = if (showSample) sampleRecurring else state.recurringPayments.map { RecurringPreview(it.name, "${won.format(it.amount)}원 / 월", it.subtitle, 0xF072) }
    CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.copy(lineHeight = TextUnit.Unspecified, platformStyle = PlatformTextStyle(includeFontPadding = false))) {
    Column(Modifier.fillMaxSize().background(BalogHomeCanvas)) {
        Box(Modifier.fillMaxWidth().height(108.dp)) {
            Text("Balog", modifier = Modifier.offset(x = 20.dp, y = 50.dp), fontFamily = plex, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 22.sp, color = BalogHomeInk)
            Text(month, modifier = Modifier.offset(x = 20.dp, y = 78.dp), fontFamily = plex, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 18.sp, color = BalogHomeMuted)
            Box(Modifier.offset(x = 330.dp, y = 50.dp).size(40.dp).background(Color(0xFFCDF0FF), CircleShape).clickable(onClick = onOpenNotifications), contentAlignment = Alignment.Center) {
                HomeIcon(0xEF94, 20, Color(0xFF006B9B))
                val unread = if (showSample) 3L else state.pushNotificationUnreadCount
                if (unread > 0) Box(Modifier.align(Alignment.TopEnd).size(14.dp).background(Color(0xFFD84343), CircleShape), contentAlignment = Alignment.Center) {
                    Text(unread.toString(), fontFamily = plex, fontSize = 9.sp, color = Color.White)
                }
            }
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        ) {
            MonthlyExpenseCard(month, expense, showSample, state)
            Spacer(Modifier.height(12.dp))
            CollectionCard(
                enabled = if (showSample) true else automaticRecordingEnabled,
                summary = if (showSample) {
                    "최근 수집 오늘 09:41 · 카드사 앱 3개 연결"
                } else if (state.notificationSummary.count > 0) {
                    "최근 수집 ${state.notificationSummary.latestMerchant.orEmpty()} ${state.notificationSummary.latestAmount?.let(won::format).orEmpty()}원"
                } else {
                    "최근 수집 내역이 없어요"
                },
                onClick = onOpenRecordingSettings,
            )
            Spacer(Modifier.height(20.dp))
            RecentPaymentsCard(entries, onOpenTransaction, onDeleteTransaction, onOpenTransactions)
            Spacer(Modifier.height(20.dp))
            RecurringCards(recurring, onOpenRecurring)
            Spacer(Modifier.height(20.dp))
            InvestmentCard(showSample, onOpenReport)
            Text("수집된 거래는 자동으로 분류되고, 애매한 항목은 ‘분류 중’으로 표시돼요", modifier = Modifier.padding(bottom = 24.dp), fontSize = 11.sp, color = BalogHomeMuted)
        }
        Box(Modifier.fillMaxWidth().height(68.dp).padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp)) {
            Surface(modifier = Modifier.fillMaxSize().clickable(onClick = onAddEntry), color = BalogHomeCta, shape = cardShape) {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    HomeIcon(0xEA13, 18, BalogHomeCard)
                    Spacer(Modifier.width(8.dp))
                    Text("거래 추가", fontFamily = plex, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = BalogHomeCard)
                }
            }
        }
        HomeBottomBar(onOpenTransactions, onOpenReport, onOpenNotifications)
    }
    }
}

@Composable
private fun MonthlyExpenseCard(month: String, expense: Long, showSample: Boolean, state: HomeUiState) {
    val remaining = (2_500_000L - expense).coerceAtLeast(0L)
    val ratio = (expense.toFloat() / 2_500_000L).coerceIn(0f, 1f)
    Surface(color = BalogHomeNavy, shape = cardShape) {
        Box(Modifier.fillMaxWidth().height(202.dp)) {
            Text("이번 달 총지출", modifier = Modifier.offset(x = 16.dp, y = 18.dp), fontFamily = plex, fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = BalogHomeBorder)
            Surface(modifier = Modifier.align(Alignment.TopEnd).padding(top = 16.dp, end = 16.dp), color = BalogHomeCard.copy(alpha = .15f), shape = CircleShape) {
                Text(month, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontFamily = plex, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = BalogHomeCanvas)
            }
            Row(Modifier.offset(x = 16.dp, y = 46.dp), verticalAlignment = Alignment.Bottom) {
                Text(won.format(expense), fontFamily = plex, fontSize = 30.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold, color = BalogHomeCard)
                Text("원", modifier = Modifier.padding(start = 4.dp, bottom = 2.dp), fontFamily = plex, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = BalogHomeBorder)
            }
            Row(Modifier.offset(x = 16.dp, y = 84.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("지난달 대비", fontSize = 11.5.sp, color = BalogHomeBorder)
                Spacer(Modifier.width(6.dp))
                HomeIcon(0xEA4C, 11, BalogHomeGreen)
                Text(if (showSample) "48,600원 (-4.0%)" else state.expenseComparisonLabel, modifier = Modifier.padding(start = 3.dp), fontFamily = plex, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = BalogHomeGreen)
            }
            HorizontalDivider(modifier = Modifier.offset(x = 16.dp, y = 115.dp).width(318.dp), color = BalogHomeCard.copy(alpha = .15f), thickness = 1.dp)
            Row(Modifier.offset(x = 16.dp, y = 133.dp).width(318.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("예산 사용", fontSize = 11.5.sp, color = BalogHomeBorder)
                Text("${won.format(remaining)}원 남음", fontFamily = plex, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = BalogHomeBorder)
            }
            Box(Modifier.offset(x = 16.dp, y = 158.dp).width(318.dp).height(6.dp).background(BalogHomeCard.copy(alpha = .2f), CircleShape)) {
                Box(Modifier.fillMaxWidth(ratio).height(6.dp).background(BalogHomeCerulean, CircleShape))
            }
            Row(Modifier.offset(x = 16.dp, y = 170.dp).width(318.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("월 예산 2,500,000원", fontFamily = plex, fontSize = 10.5.sp, color = BalogHomeBorder)
                Text("${(ratio * 100).toInt()}% 사용", fontFamily = plex, fontSize = 10.5.sp, color = BalogHomeBorder)
            }
        }
    }
}

@Composable
private fun CollectionCard(enabled: Boolean, summary: String, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), color = BalogHomeCard, border = BorderStroke(1.dp, BalogHomeBorder), shape = cardShape) {
        Row(Modifier.height(62.dp).padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).background(BalogHomeGreen.copy(alpha = .14f), CircleShape), contentAlignment = Alignment.Center) { HomeIcon(0xF04C, 16, BalogHomeGreen) }
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (enabled) "결제 알림 정상 수집 중" else "결제 알림 수집이 꺼져 있어요", fontFamily = plex, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, color = BalogHomeInk)
                    if (enabled) { Spacer(Modifier.width(6.dp)); HomeIcon(0xEB80, 12, BalogHomeGreen) }
                }
                Text(if (enabled) summary else "설정에서 권한을 켜주세요", fontSize = 10.5.sp, color = BalogHomeMuted)
            }
            HomeIcon(0xEA6E, 18, Color(0xFF878E95))
        }
    }
}

@Composable
private fun RecentPaymentsCard(entries: List<EditableTransaction>, onOpen: (EditableTransaction) -> Unit, onDelete: (EditableTransaction) -> Unit, onAll: () -> Unit) {
    Surface(color = BalogHomeCard, border = BorderStroke(1.dp, BalogHomeBorder), shape = cardShape) {
        Column {
            Row(Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("최근 결제", fontFamily = plex, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = BalogHomeInk)
                    Spacer(Modifier.width(8.dp))
                    Text("분류 중 ${entries.count { it.category == "분류 중" || it.categoryCode == "UNCATEGORIZED" }}건", fontFamily = plex, fontSize = 10.5.sp, color = BalogHomeAttention)
                }
                Text("전체보기 ›", modifier = Modifier.clickable(onClick = onAll), fontFamily = plex, fontSize = 11.sp, color = Color(0xFF183C71))
            }
            HorizontalDivider(color = BalogHomeBorder, thickness = 1.dp)
            entries.take(5).forEachIndexed { index, entry ->
                RecentPaymentRow(entry, onOpen, onDelete)
                if (index < minOf(entries.lastIndex, 4)) HorizontalDivider(color = Color(0xFFEEEAE1), thickness = 1.dp)
            }
            if (entries.isEmpty()) Text("최근 결제가 없어요", modifier = Modifier.padding(16.dp), fontSize = 12.sp, color = BalogHomeMuted)
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun RecentPaymentRow(entry: EditableTransaction, onOpen: (EditableTransaction) -> Unit, onDelete: (EditableTransaction) -> Unit) {
    val idle = entry.category == "분류 중" || entry.categoryCode == "UNCATEGORIZED"
    val glyph = when (entry.categoryCode) { "CAFE_SNACK" -> 0xEC06; "CONVENIENCE_MART_MISC" -> 0xF11A; "TRANSPORT_CAR" -> 0xEB11; else -> 0xEEC6 }
    Row(Modifier.fillMaxWidth().height(65.dp).combinedClickable(onClick = { onOpen(entry) }, onLongClick = { onDelete(entry) }).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(36.dp).background(if (idle) Color(0xFFE9ECF0) else Color(0xFFEFF5FE), cardShape), contentAlignment = Alignment.Center) {
            HomeIcon(glyph, 16, if (idle) Color(0xFF848B92) else Color(0xFF183C71))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(entry.title, fontFamily = plex, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = BalogHomeInk, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (entry.title == "서울교통공사") "수동 등록" else "결제 알림", modifier = Modifier.background(Color(0xFFEFF5FE), CircleShape).padding(horizontal = 6.dp, vertical = 1.dp), fontFamily = plex, fontSize = 10.sp, color = Color(0xFF0F2D58))
                Spacer(Modifier.width(6.dp))
                Text(entry.category, fontFamily = plex, fontSize = 11.sp, fontWeight = if (idle) FontWeight.SemiBold else FontWeight.Normal, color = if (idle) BalogHomeAttention else BalogHomeMuted)
                Text(" · ${entry.time}", fontFamily = plex, fontSize = 11.sp, color = BalogHomeMuted)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(entry.amountLabel.removeSuffix("원"), fontFamily = plex, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = BalogHomeInk)
            Text(if (entry.positive) "수입" else "지출", fontFamily = plex, fontSize = 10.5.sp, color = Color(0xFF878E95))
        }
    }
}

@Composable
private fun RecurringCards(entries: List<RecurringPreview>, onAll: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("정기결제 알림", fontFamily = plex, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = BalogHomeInk)
            Text("${entries.size}건   전체보기 ›", modifier = Modifier.clickable(onClick = onAll), fontFamily = plex, fontSize = 11.sp, color = Color(0xFF183C71))
        }
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(entries.size) { index ->
                val entry = entries[index]
                Surface(modifier = Modifier.width(145.dp).clickable(onClick = onAll), color = BalogHomeCard, border = BorderStroke(1.dp, BalogHomeBorder), shape = cardShape) {
                    Column(Modifier.height(148.dp).padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        HomeIcon(entry.icon, 16, Color(0xFF183C71))
                        Text(entry.title, fontFamily = plex, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = BalogHomeInk)
                        Text(entry.amount, fontFamily = plex, fontSize = 10.5.sp, color = BalogHomeMuted)
                        Text(entry.days, fontFamily = plex, fontSize = 10.5.sp, color = BalogHomeAttention)
                    }
                }
            }
        }
    }
}

@Composable
private fun InvestmentCard(showSample: Boolean, onOpen: () -> Unit) {
    Surface(color = BalogHomeCard, border = BorderStroke(1.dp, BalogHomeBorder), shape = cardShape) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("투자 자산", fontFamily = plex, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = BalogHomeInk)
                if (showSample) Text("자세히 ›", modifier = Modifier.clickable(onClick = onOpen), fontFamily = plex, fontSize = 11.sp, color = Color(0xFF183C71))
            }
            Spacer(Modifier.height(8.dp))
            if (showSample) {
                Text("총 평가금액", fontSize = 11.sp, color = BalogHomeMuted)
                Text("48,265,000원", fontFamily = plex, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = BalogHomeInk)
                Text("오늘 상승 +412,000원 (+0.86%)", fontFamily = plex, fontSize = 11.sp, color = BalogHomeGreen)
                Text("오늘 09:42 기준 · 증권 · 은행계좌 통합", fontSize = 10.5.sp, color = BalogHomeMuted)
            } else {
                Text("등록된 자산 정보가 없어요", fontSize = 12.sp, color = BalogHomeMuted)
            }
        }
    }
}

@Composable
private fun HomeBottomBar(onTransactions: () -> Unit, onReport: () -> Unit, onNotifications: () -> Unit) {
    Column(Modifier.fillMaxWidth().height(83.dp).background(BalogHomeCard)) {
        HorizontalDivider(color = BalogHomeBorder, thickness = 1.dp)
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)) {
            listOf("홈" to 0xEE1F, "거래" to 0xECAD, "자산" to 0xEFF6, "리포트" to 0xECCB, "알림" to 0xEF94).forEach { (label, icon) ->
                val itemModifier = when (label) {
                    "거래" -> Modifier.weight(1f).clickable(onClick = onTransactions)
                    "리포트" -> Modifier.weight(1f).clickable(onClick = onReport)
                    "알림" -> Modifier.weight(1f).clickable(onClick = onNotifications)
                    else -> Modifier.weight(1f)
                }
                Column(itemModifier.padding(top = 2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    HomeIcon(icon, 21, if (label == "홈") Color(0xFF183C71) else Color(0xFF878E95))
                    Spacer(Modifier.height(4.dp))
                    Text(label, fontFamily = plex, fontSize = 10.5.sp, fontWeight = if (label == "홈") FontWeight.SemiBold else FontWeight.Normal, color = if (label == "홈") Color(0xFF183C71) else Color(0xFF878E95))
                }
            }
        }
    }
}

@Composable
private fun HomeIcon(code: Int, size: Int, color: Color) {
    Text(String(Character.toChars(code)), fontFamily = remix, fontSize = size.sp, lineHeight = size.sp, color = color)
}
