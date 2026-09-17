package com.uson.myapplication.feature.transactions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.uson.myapplication.R
import com.uson.myapplication.feature.home.TransactionItem
import com.uson.myapplication.ui.theme.BalogHomeBorder
import com.uson.myapplication.ui.theme.BalogHomeCard
import com.uson.myapplication.ui.theme.BalogHomeInk
import com.uson.myapplication.ui.theme.BalogHomeMuted

internal val TransactionPlex = FontFamily(
    Font(R.font.ibm_plex_sans_kr_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_sans_kr_semibold, FontWeight.SemiBold),
    Font(R.font.ibm_plex_sans_kr_bold, FontWeight.Bold),
)
internal val TransactionRemix = FontFamily(Font(R.font.remixicon))
internal val TransactionCardShape = RoundedCornerShape(8.dp)

@Composable
internal fun TransactionIcon(code: Int, size: Int, color: Color) {
    Text(String(Character.toChars(code)), fontFamily = TransactionRemix, fontSize = size.sp, lineHeight = size.sp, color = color)
}

@Composable
internal fun TransactionRow(item: TransactionItem, onClick: () -> Unit) {
    val pending = item.categoryCode == "UNCATEGORIZED"
    val icon = when (item.categoryCode) {
        "CAFE_SNACK" -> 0xEC06
        "CONVENIENCE_MART_MISC" -> 0xF11A
        "TRANSPORT_CAR" -> 0xEB11
        "FOOD" -> 0xED5B
        "SHOPPING" -> 0xF118
        "HEALTH_FITNESS" -> 0xEE6F
        else -> 0xEEC6
    }
    Row(Modifier.fillMaxWidth().height(65.dp).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(36.dp).background(if (pending) Color(0xFFE9ECF0) else Color(0xFFEFF5FE), TransactionCardShape), contentAlignment = Alignment.Center) {
            TransactionIcon(icon, 16, if (pending) Color(0xFF848B92) else Color(0xFF183C71))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.merchant, fontFamily = TransactionPlex, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = BalogHomeInk, maxLines = 1)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (item.manual) "수동 등록" else "결제 알림", modifier = Modifier.background(Color(0xFFEFF5FE), CircleShape).padding(horizontal = 6.dp, vertical = 1.dp), fontFamily = TransactionPlex, fontSize = 10.sp, color = Color(0xFF0F2D58))
                Spacer(Modifier.width(6.dp))
                Text(item.category, fontFamily = TransactionPlex, fontSize = 11.sp, fontWeight = if (pending) FontWeight.SemiBold else FontWeight.Normal, color = if (pending) Color(0xFFD68E14) else BalogHomeMuted)
                Text(" · ${item.time}", fontFamily = TransactionPlex, fontSize = 11.sp, color = BalogHomeMuted)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(item.amountLabel.removeSuffix("원"), fontFamily = TransactionPlex, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = BalogHomeInk)
            Text(if (item.positive) "수입" else "지출", fontFamily = TransactionPlex, fontSize = 10.5.sp, color = Color(0xFF878E95))
        }
    }
}

@Composable
internal fun TransactionBottomBar(onHome: () -> Unit, onReports: () -> Unit, onAlerts: () -> Unit) {
    Column(Modifier.fillMaxWidth().height(83.dp).background(BalogHomeCard)) {
        HorizontalDivider(color = BalogHomeBorder, thickness = 1.dp)
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)) {
            listOf("홈" to 0xEE1F, "거래" to 0xECAD, "자산" to 0xEFF6, "리포트" to 0xECCB, "알림" to 0xEF94).forEach { (label, icon) ->
                val modifier = when (label) {
                    "홈" -> Modifier.weight(1f).clickable(onClick = onHome)
                    "리포트" -> Modifier.weight(1f).clickable(onClick = onReports)
                    "알림" -> Modifier.weight(1f).clickable(onClick = onAlerts)
                    else -> Modifier.weight(1f)
                }
                Column(modifier.padding(top = 2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    val selected = label == "거래"
                    TransactionIcon(icon, 21, if (selected) Color(0xFF183C71) else Color(0xFF878E95))
                    Spacer(Modifier.height(4.dp))
                    Text(label, fontFamily = TransactionPlex, fontSize = 10.5.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, color = if (selected) Color(0xFF183C71) else Color(0xFF878E95))
                }
            }
        }
    }
}

internal data class CategoryOption(val code: String, val label: String, val icon: Int)
internal val TransactionCategories = listOf(
    CategoryOption("CAFE_SNACK", "카페 · 간식", 0xEC06), CategoryOption("FOOD", "식비", 0xED5B),
    CategoryOption("CONVENIENCE_MART_MISC", "마트 · 편의점", 0xF11A), CategoryOption("TRANSPORT_CAR", "교통", 0xEB11),
    CategoryOption("SHOPPING", "온라인 쇼핑", 0xF118), CategoryOption("HOBBY_LEISURE", "구독 · 정기결제", 0xF072),
    CategoryOption("HEALTH_FITNESS", "의료 · 건강", 0xEE6F), CategoryOption("LIVING", "생활 · 주거", 0xEE1D),
    CategoryOption("UNCATEGORIZED", "미분류", 0xEEC6),
)
