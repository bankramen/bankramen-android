package com.uson.myapplication.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uson.myapplication.ui.theme.BrandBlue
import com.uson.myapplication.ui.theme.SecondaryText

@Composable
internal fun AutomaticRecordingCard(
    enabled: Boolean,
    notificationCount: Int,
    latestMerchant: String?,
    latestAmount: Long?,
    uploadFailed: Boolean,
    onOpenSettings: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 3.dp,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("자동 기록", style = AppTypography.cardTitle, color = Color.Black)
                AccentPill(
                    text = if (enabled) "켜짐" else "꺼짐",
                    background = if (enabled) BrandBlue.copy(alpha = 0.12f) else Color(0xFFFFF1F1),
                    textColor = if (enabled) BrandBlue else Color(0xFFD92D20),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            when {
                !enabled -> {
                    Text("결제 알림을 읽어 거래 내역으로 자동 기록해요.", style = AppTypography.small, color = SecondaryText)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "설정에서 켜기",
                        modifier = Modifier
                            .background(BrandBlue, RoundedCornerShape(12.dp))
                            .clickable(onClick = onOpenSettings)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        style = AppTypography.small,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }

                notificationCount == 0 -> {
                    Text("준비됐어요. 새 결제 알림이 오면 자동으로 기록할게요.", style = AppTypography.small, color = SecondaryText)
                }

                else -> {
                    Text("최근 기록", style = AppTypography.tiny, color = SecondaryText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${latestMerchant ?: "결제 알림"} · ${latestAmount?.formatWonOrEmpty() ?: "금액 확인 중"}",
                        style = AppTypography.body,
                        color = Color.Black,
                    )
                }
            }
            if (uploadFailed) {
                Spacer(modifier = Modifier.height(12.dp))
                AccentPill(
                    text = "자동 기록은 저장됐지만 서버 반영에 실패했어요",
                    background = Color(0xFFFFF1F1),
                    textColor = Color(0xFFD92D20),
                )
            }
        }
    }
}

@Composable
internal fun MutationErrorBanner(
    message: String,
    onDismiss: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        color = Color(0xFFFFF1F1),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(message, modifier = Modifier.weight(1f), style = AppTypography.small, color = Color(0xFFD92D20))
            Text(
                "확인",
                modifier = Modifier.clickable(onClick = onDismiss).padding(start = 12.dp),
                style = AppTypography.small,
                color = Color(0xFFD92D20),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
