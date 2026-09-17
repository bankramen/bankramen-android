package com.uson.myapplication.feature.transactions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.uson.myapplication.feature.home.TransactionItem
import com.uson.myapplication.ui.theme.BalogHomeBorder
import com.uson.myapplication.ui.theme.BalogHomeCanvas
import com.uson.myapplication.ui.theme.BalogHomeCard
import com.uson.myapplication.ui.theme.BalogHomeCta
import com.uson.myapplication.ui.theme.BalogHomeGreen
import com.uson.myapplication.ui.theme.BalogHomeInk
import com.uson.myapplication.ui.theme.BalogHomeMuted
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

private val listWon = NumberFormat.getNumberInstance(Locale.KOREA)

@Composable
internal fun TransactionsListScreen(
    state: TransactionsUiState,
    onKind: (TransactionKind) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTransaction: (TransactionItem) -> Unit,
    onNew: () -> Unit,
    onHome: () -> Unit,
    onReports: () -> Unit,
    onAlerts: () -> Unit,
) {
    var searchVisible by remember { mutableStateOf(false) }
    var categoryVisible by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<String?>(null) }
    val visible = state.visibleItems.filter { item ->
        item.merchant.contains(query, ignoreCase = true) && (category == null || item.categoryCode == category)
    }
    Column(Modifier.fillMaxSize().background(BalogHomeCanvas)) {
        Box(Modifier.fillMaxWidth().height(92.dp)) {
            Text("거래", modifier = Modifier.offset(x = 20.dp, y = 50.dp), fontFamily = TransactionPlex, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = BalogHomeInk)
            Box(Modifier.align(Alignment.BottomEnd).padding(end = 60.dp, bottom = 6.dp).size(40.dp).semantics { contentDescription = "거래 검색" }.clickable { searchVisible = !searchVisible }, contentAlignment = Alignment.Center) {
                TransactionIcon(0xF0D1, 19, BalogHomeMuted)
            }
            Box(Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = 6.dp).size(40.dp).semantics { contentDescription = "카테고리 필터" }.clickable { categoryVisible = !categoryVisible }, contentAlignment = Alignment.Center) {
                TransactionIcon(0xED25, 19, BalogHomeMuted)
            }
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 0.dp)) {
            if (searchVisible) item {
                OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), singleLine = true, placeholder = { Text("거래 검색", fontFamily = TransactionPlex, fontSize = 12.sp) })
            }
            if (categoryVisible) item {
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                    item { CategoryFilter("전체", category == null) { category = null } }
                    items(TransactionCategories) { option -> CategoryFilter(option.label, category == option.code) { category = option.code } }
                }
            }
            item { MonthSelector(state.month, onPreviousMonth, onNextMonth) }
            item { MonthSummary(state) }
            item { KindSelector(state.kind, onKind) }
            if (state.loading) item { Text("거래를 불러오는 중이에요", modifier = Modifier.padding(vertical = 24.dp), fontFamily = TransactionPlex, fontSize = 12.sp, color = BalogHomeMuted) }
            else if (state.error != null) item { Text(state.error, modifier = Modifier.padding(vertical = 24.dp), fontFamily = TransactionPlex, fontSize = 12.sp, color = Color(0xFFD84343)) }
            else if (visible.isEmpty()) item { Text("이달의 거래가 없어요", modifier = Modifier.padding(vertical = 24.dp), fontFamily = TransactionPlex, fontSize = 12.sp, color = BalogHomeMuted) }
            else groupedRows(visible, state.month.atDay(1), onTransaction)
            item { Spacer(Modifier.height(24.dp)) }
        }
        Surface(modifier = Modifier.fillMaxWidth().height(68.dp).padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp).clickable(onClick = onNew), color = BalogHomeCta, shape = TransactionCardShape) {
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                TransactionIcon(0xEA13, 18, BalogHomeCard)
                Spacer(Modifier.width(8.dp))
                Text("거래 추가", fontFamily = TransactionPlex, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = BalogHomeCard)
            }
        }
        TransactionBottomBar(onHome, onReports, onAlerts)
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.groupedRows(items: List<TransactionItem>, fallbackDate: LocalDate, onClick: (TransactionItem) -> Unit) {
    val groups = items.groupBy { it.date ?: fallbackDate }.toSortedMap(compareByDescending { it })
    groups.forEach { (date, rows) ->
        item(key = "header-$date") { DateHeader(date, rows.sumOf(TransactionItem::amount)) }
        items(rows, key = { it.id ?: "${it.merchant}-${it.time}" }) { row ->
            Surface(color = BalogHomeCard, border = BorderStroke(1.dp, BalogHomeBorder)) { TransactionRow(row) { onClick(row) } }
        }
    }
}

@Composable
private fun MonthSelector(month: YearMonth, previous: () -> Unit, next: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(45.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).semantics { contentDescription = "이전 달" }.clickable(onClick = previous), contentAlignment = Alignment.Center) { TransactionIcon(0xEA64, 20, BalogHomeMuted) }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${month.year}년 ${month.monthValue}월", fontFamily = TransactionPlex, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp, color = BalogHomeInk)
            if (month == YearMonth.now()) Text("이번 달", fontFamily = TransactionPlex, fontSize = 9.5.sp, color = BalogHomeGreen)
        }
        Box(Modifier.size(40.dp).semantics { contentDescription = "다음 달" }.clickable(enabled = month < YearMonth.now(), onClick = next), contentAlignment = Alignment.Center) { TransactionIcon(0xEA6E, 20, if (month < YearMonth.now()) BalogHomeMuted else BalogHomeBorder) }
    }
}

@Composable
private fun MonthSummary(state: TransactionsUiState) {
    val amount = if (state.kind == TransactionKind.Expense) state.expense else state.income
    Surface(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), color = BalogHomeCard, border = BorderStroke(1.dp, BalogHomeBorder), shape = TransactionCardShape) {
        Column(Modifier.padding(16.dp)) {
            Text(if (state.kind == TransactionKind.Expense) "이번 달 총지출" else "이번 달 총수입", fontFamily = TransactionPlex, fontSize = 11.5.sp, color = BalogHomeMuted)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(listWon.format(amount), fontFamily = TransactionPlex, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = BalogHomeInk)
                Text("원", modifier = Modifier.padding(start = 3.dp, bottom = 2.dp), fontFamily = TransactionPlex, fontSize = 13.sp, color = BalogHomeMuted)
            }
            Text(if (state.kind == TransactionKind.Expense) "지난달 대비 ${state.differenceRate?.let { "$it%" } ?: "데이터 없음"} · 총 ${state.expenses.size}건" else "이번 달 입금 · 총 ${state.incomes.size}건", fontFamily = TransactionPlex, fontSize = 10.5.sp, color = BalogHomeMuted)
        }
    }
}

@Composable
private fun KindSelector(selected: TransactionKind, onSelected: (TransactionKind) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp).background(Color(0xFFE9ECF0), CircleShape).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        TransactionKind.entries.forEach { kind ->
            val active = kind == selected
            Surface(modifier = Modifier.weight(1f).clickable { onSelected(kind) }, color = if (active) BalogHomeCard else Color.Transparent, border = if (active) BorderStroke(1.dp, BalogHomeBorder) else null, shape = CircleShape) {
                Text(if (kind == TransactionKind.Expense) "지출" else "수입", modifier = Modifier.padding(vertical = 7.dp), fontFamily = TransactionPlex, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal, fontSize = 12.5.sp, color = if (active) BalogHomeInk else BalogHomeMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}

@Composable
private fun DateHeader(date: LocalDate, total: Long) {
    Row(Modifier.fillMaxWidth().height(38.dp).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("${date.monthValue}월 ${date.dayOfMonth}일 (${weekday(date.dayOfWeek)})", fontFamily = TransactionPlex, fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp, color = Color(0xFF4E5760))
        Text("-${listWon.format(total)}", fontFamily = TransactionPlex, fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp, color = BalogHomeMuted)
    }
}

private fun weekday(day: DayOfWeek) = listOf("월", "화", "수", "목", "금", "토", "일")[day.value - 1]

@Composable
private fun CategoryFilter(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(modifier = Modifier.clickable(onClick = onClick), color = if (selected) Color(0xFF183C71) else BalogHomeCard, border = BorderStroke(1.dp, if (selected) Color(0xFF183C71) else BalogHomeBorder), shape = CircleShape) {
        Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), fontFamily = TransactionPlex, fontSize = 10.5.sp, color = if (selected) Color.White else BalogHomeMuted)
    }
}
