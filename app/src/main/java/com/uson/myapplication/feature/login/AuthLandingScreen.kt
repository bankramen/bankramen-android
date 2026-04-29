package com.uson.myapplication.feature.login

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uson.myapplication.ui.theme.BackgroundGray
import com.uson.myapplication.ui.theme.BrandBlue
import com.uson.myapplication.ui.theme.MyApplicationTheme

private data class OnboardingPage(
    val title: String,
    val body: String,
    val badge: String,
    val colors: List<Color>,
)

private val onboardingPages = listOf(
    OnboardingPage(
        title = "내 자산을 한눈에\n확인해요",
        body = "흩어진 계좌와 카드 내역을 한 화면에서 모아 보고, 이번 달 소비 흐름을 빠르게 파악해요.",
        badge = "자산",
        colors = listOf(Color(0xFFEAF3FF), Color(0xFFDCEBFF)),
    ),
    OnboardingPage(
        title = "수입과 지출을\n쉽게 분석해요",
        body = "월별 리포트와 카테고리 분석으로 어디에 돈을 쓰고 있는지 자연스럽게 이해할 수 있어요.",
        badge = "분석",
        colors = listOf(Color(0xFFFFF3E2), Color(0xFFFFE3B8)),
    ),
    OnboardingPage(
        title = "정기결제를\n놓치지 않아요",
        body = "반복 결제와 예정 지출을 미리 정리해 두고, 알림으로 빠르게 대응할 수 있어요.",
        badge = "관리",
        colors = listOf(Color(0xFFEAFBF2), Color(0xFFD6F5E2)),
    ),
)

@Composable
fun AuthLandingScreen(
    uiState: LoginUiState,
    onPrimaryClick: () -> Unit,
    onOpenShowcase: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pageIndex by rememberSaveable { mutableStateOf(0) }

    if (pageIndex >= onboardingPages.size) {
        LoginScreen(
            uiState = uiState,
            onPrimaryClick = onPrimaryClick,
            modifier = modifier,
        )
        return
    }

    val page = onboardingPages[pageIndex]

    Surface(
        modifier = modifier.fillMaxSize(),
        color = BackgroundGray,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = "둘러보기",
                    modifier = Modifier
                        .background(Color(0xFFF2F4F6), RoundedCornerShape(999.dp))
                        .clickable(onClick = onOpenShowcase)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    color = BrandBlue,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                )
            }
            Spacer(modifier = Modifier.height(84.dp))
            OnboardingIllustration(page = page)
            Spacer(modifier = Modifier.height(36.dp))
            Text(
                text = page.title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 28.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = page.body,
                textAlign = TextAlign.Center,
                color = Color(0xFF8B95A1),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                onboardingPages.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .size(width = if (index == pageIndex) 24.dp else 8.dp, height = 8.dp)
                            .background(
                                if (index == pageIndex) BrandBlue else Color(0xFFD9E0E8),
                                RoundedCornerShape(999.dp),
                            ),
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { pageIndex += 1 },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            ) {
                Text(if (pageIndex == onboardingPages.lastIndex) "시작하기" else "다음")
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun OnboardingIllustration(
    page: OnboardingPage,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(
                brush = Brush.verticalGradient(page.colors),
                shape = RoundedCornerShape(32.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(164.dp)
                .background(Color.White.copy(alpha = 0.88f), RoundedCornerShape(40.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(18.dp + (index * 4).dp)
                                .background(
                                    if (index == 1) BrandBlue else Color.White,
                                    CircleShape,
                                ),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                Box(
                    modifier = Modifier
                        .background(BrandBlue, RoundedCornerShape(999.dp))
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = page.badge,
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AuthLandingScreenPreview() {
    MyApplicationTheme(darkTheme = false, dynamicColor = false) {
        AuthLandingScreen(
            uiState = LoginUiState(),
            onPrimaryClick = {},
            onOpenShowcase = {},
        )
    }
}
