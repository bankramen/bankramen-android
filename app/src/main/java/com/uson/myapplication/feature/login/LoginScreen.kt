package com.uson.myapplication.feature.login

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uson.myapplication.ui.theme.BackgroundGray
import com.uson.myapplication.ui.theme.BrandBlue
import com.uson.myapplication.ui.theme.KakaoYellow
import com.uson.myapplication.ui.theme.MyApplicationTheme
import com.uson.myapplication.ui.theme.SecondaryText

private data class IntroPage(
    val emoji: String,
    val title: String,
    val subtitle: String,
)

private val introPages = listOf(
    IntroPage(
        emoji = "🎁",
        title = "흩어진 자산을\n한 화면에 모아봐요",
        subtitle = "계좌, 카드, 생활비를 한 번에 정리해서\n오늘의 소비 흐름을 빠르게 파악할 수 있어요.",
    ),
    IntroPage(
        emoji = "📊",
        title = "월별 리포트로\n소비 패턴을 확인해요",
        subtitle = "언제, 어디서, 얼마나 썼는지 비교해서\n절약 포인트를 바로 찾을 수 있어요.",
    ),
    IntroPage(
        emoji = "💸",
        title = "지출 카테고리를\n자동으로 정리해요",
        subtitle = "식비, 쇼핑, 교통처럼 자주 쓰는 항목을\n깔끔하게 모아서 보기 쉽게 보여드려요.",
    ),
    IntroPage(
        emoji = "🔔",
        title = "정기결제와 예산 알림을\n놓치지 않아요",
        subtitle = "예산 초과와 결제 예정일을 알려줘서\n지출 관리가 훨씬 쉬워져요.",
    ),
)

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pageIndex by rememberSaveable { mutableIntStateOf(0) }
    val isFinalPage = pageIndex == introPages.size

    Surface(
        modifier = modifier.fillMaxSize(),
        color = BackgroundGray,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGray),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .statusBarsPadding()
                    .background(BackgroundGray),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 56.dp)
                    .background(Color.White),
            ) {
                if (isFinalPage) {
                    FinalLoginPage(
                        notificationReady = uiState.notificationAccessGranted,
                        onPrimaryClick = onPrimaryClick,
                    )
                } else {
                    IntroContent(
                        page = introPages[pageIndex],
                        pageIndex = pageIndex,
                        pageCount = introPages.size + 1,
                        onBack = { pageIndex = (pageIndex - 1).coerceAtLeast(0) },
                        onNext = { pageIndex += 1 },
                    )
                }
            }
        }
    }
}

@Composable
private fun IntroContent(
    page: IntroPage,
    pageIndex: Int,
    pageCount: Int,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (pageIndex == 0) "" else "←",
                modifier = Modifier
                    .width(24.dp)
                    .padding(top = 6.dp)
                    .then(if (pageIndex == 0) Modifier else Modifier),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = Color.Black,
            )
            if (pageIndex > 0) {
                Text(
                    text = "←",
                    modifier = Modifier
                        .width(24.dp)
                        .offset(x = (-24).dp)
                        .padding(top = 6.dp),
                    color = Color.Transparent,
                )
            }
            Text(
                text = "${pageIndex + 1} / $pageCount",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = SecondaryText,
            )
        }

        if (pageIndex > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-28).dp),
                horizontalArrangement = Arrangement.Start,
            ) {
                Text(
                    text = "←",
                    modifier = Modifier.padding(top = 6.dp),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color.Black,
                )
            }
        }

        Spacer(modifier = Modifier.height(132.dp))
        IntroIconTile(emoji = page.emoji)
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = page.title,
            color = Color.Black,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 24.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp,
            ),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = page.subtitle,
            color = SecondaryText,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(modifier = Modifier.weight(1f))
        PageIndicator(current = pageIndex, total = pageCount)
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 56.dp)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandBlue,
                contentColor = Color.White,
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        ) {
            Text(
                text = "다음",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
    }
}

@Composable
private fun FinalLoginPage(
    notificationReady: Boolean,
    onPrimaryClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(184.dp))
        LogoMark()
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "금융의 모든 것\n뱅크라면에서",
            color = Color.Black,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 20.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp,
            ),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (notificationReady) {
                "준비가 끝났어요. 카카오 로그인 후\n지출과 리포트를 바로 확인할 수 있어요."
            } else {
                "흩어진 내 자산을 한눈에 확인하고\n똑똑하게 관리하세요"
            },
            color = SecondaryText,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(modifier = Modifier.weight(1f))
        PageIndicator(current = introPages.size, total = introPages.size + 1)
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onPrimaryClick,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 56.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = KakaoYellow,
                contentColor = Color.Black,
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KakaoTalkIcon()
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = "카카오로 시작하기",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 13.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
        }
    }
}

@Composable
private fun IntroIconTile(emoji: String) {
    Box(
        modifier = Modifier
            .size(92.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFFF4F8FF)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = emoji,
                fontSize = 28.sp,
            )
        }
    }
}

@Composable
private fun PageIndicator(
    current: Int,
    total: Int,
) {
    val dots = remember(total) { List(total) { it } }
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        dots.forEach { index ->
            Box(
                modifier = Modifier
                    .size(if (index == current) 18.dp else 6.dp, 6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (index == current) BrandBlue else Color(0xFFD9E2EC)),
            )
        }
    }
}

@Composable
private fun LogoMark(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(84.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = BrandBlue.copy(alpha = 0.20f),
                ambientColor = BrandBlue.copy(alpha = 0.20f),
            )
            .background(
                color = BrandBlue,
                shape = RoundedCornerShape(24.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "BANK\nRAMEN",
            color = Color.White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 18.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp,
            ),
        )
    }
}

@Composable
private fun KakaoTalkIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bubblePath = Path().apply {
                moveTo(size.width * 0.5f, size.height * 0.14f)
                cubicTo(
                    size.width * 0.16f, size.height * 0.14f,
                    size.width * 0.03f, size.height * 0.32f,
                    size.width * 0.03f, size.height * 0.50f,
                )
                cubicTo(
                    size.width * 0.03f, size.height * 0.68f,
                    size.width * 0.15f, size.height * 0.83f,
                    size.width * 0.30f, size.height * 0.89f,
                )
                lineTo(size.width * 0.24f, size.height * 0.99f)
                lineTo(size.width * 0.40f, size.height * 0.92f)
                cubicTo(
                    size.width * 0.44f, size.height * 0.93f,
                    size.width * 0.47f, size.height * 0.93f,
                    size.width * 0.5f, size.height * 0.93f,
                )
                cubicTo(
                    size.width * 0.84f, size.height * 0.93f,
                    size.width * 0.97f, size.height * 0.75f,
                    size.width * 0.97f, size.height * 0.50f,
                )
                cubicTo(
                    size.width * 0.97f, size.height * 0.32f,
                    size.width * 0.84f, size.height * 0.14f,
                    size.width * 0.5f, size.height * 0.14f,
                )
                close()
            }

            drawPath(
                path = bubblePath,
                color = Color.Black,
                style = Fill,
            )
        }

        Text(
            text = "TALK",
            color = KakaoYellow,
            modifier = Modifier.offset(y = (-0.5).dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 4.5.sp,
                lineHeight = 4.5.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.15).sp,
            ),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    MyApplicationTheme(darkTheme = false, dynamicColor = false) {
        LoginScreen(
            uiState = LoginUiState(),
            onPrimaryClick = {},
        )
    }
}
