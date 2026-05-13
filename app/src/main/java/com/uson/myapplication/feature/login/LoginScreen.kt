package com.uson.myapplication.feature.login

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uson.myapplication.R
import com.uson.myapplication.ui.theme.BrandBlue
import com.uson.myapplication.ui.theme.KakaoYellow
import com.uson.myapplication.ui.theme.MyApplicationTheme
import com.uson.myapplication.ui.theme.SecondaryText

private val OnboardingBlue = Color(0xFF3182F6)
private val OnboardingBlueSoft = Color(0xFFE8F3FF)
private val DotInactive = Color(0xFFD1D1D3)
private val LoginBodyText = Color(0xFF8B95A1)

private data class PostLoginOnboardingPage(
    @DrawableRes val imageRes: Int,
    val title: String,
    val subtitle: String,
    val imageWidth: androidx.compose.ui.unit.Dp,
    val imageHeight: androidx.compose.ui.unit.Dp,
)

private val postLoginOnboardingPages = listOf(
    PostLoginOnboardingPage(
        imageRes = R.drawable.onboarding_wallet,
        title = "계좌 등록 없이\n시작할 수 있어요",
        subtitle = "직접 입력하여 가볍게 시작해보세요",
        imageWidth = 120.dp,
        imageHeight = 90.dp,
    ),
    PostLoginOnboardingPage(
        imageRes = R.drawable.onboarding_chart,
        title = "내 소비 패턴을\n한눈에 분석해요",
        subtitle = "어디에 얼마나 썼는지 쉽게 확인하세요",
        imageWidth = 167.dp,
        imageHeight = 88.dp,
    ),
    PostLoginOnboardingPage(
        imageRes = R.drawable.onboarding_bell,
        title = "놓치기 쉬운 결제도\n알아서 챙겨드려요",
        subtitle = "정기결제 알림으로 똑똑하게 관리하세요",
        imageWidth = 120.dp,
        imageHeight = 93.dp,
    ),
)

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            LogoMark()
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "금융의 모든 것\n뱅크라면에서",
                color = Color.Black,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 20.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "흩어진 내 자산을 한눈에 확인하고\n똑똑하게 관리하세요",
                color = LoginBodyText,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
            if (uiState.authStatusLabel != "로그인 필요") {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = uiState.authStatusLabel,
                    color = BrandBlue,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
                if (uiState.authStatusDetail.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = uiState.authStatusDetail,
                        color = SecondaryText,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1.72f))
            Button(
                onClick = onPrimaryClick,
                modifier = Modifier
                    .fillMaxWidth()
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
                    Spacer(modifier = Modifier.width(10.dp))
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
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Composable
fun PostLoginOnboardingScreen(
    onFinished: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pageIndex by rememberSaveable { mutableIntStateOf(0) }
    val page = postLoginOnboardingPages[pageIndex]

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PageIndicator(
                    current = pageIndex,
                    total = postLoginOnboardingPages.size,
                )
                Text(
                    text = "건너뛰기",
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onSkip)
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    color = Color(0xFF71717A),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 12.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            OnboardingArtwork(page = page)
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = page.title,
                color = Color.Black,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 20.sp,
                    lineHeight = 33.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = page.subtitle,
                color = Color(0xFF71717A),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Normal,
                ),
            )
            Spacer(modifier = Modifier.weight(1.55f))
            Button(
                onClick = {
                    if (pageIndex == postLoginOnboardingPages.lastIndex) {
                        onFinished()
                    } else {
                        pageIndex += 1
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OnboardingBlue,
                    contentColor = Color.White,
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            ) {
                Text(
                    text = if (pageIndex == postLoginOnboardingPages.lastIndex) "시작하기" else "다음",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun OnboardingArtwork(page: PostLoginOnboardingPage) {
    Box(
        modifier = Modifier
            .size(128.dp)
            .clip(CircleShape)
            .background(OnboardingBlueSoft),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = null,
            modifier = Modifier.size(width = page.imageWidth, height = page.imageHeight),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun PageIndicator(
    current: Int,
    total: Int,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .size(width = if (index == current) 24.dp else 6.dp, height = 6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (index == current) OnboardingBlue else DotInactive),
            )
        }
    }
}

@Composable
private fun LogoMark(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(80.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = BrandBlue.copy(alpha = 0.22f),
                ambientColor = BrandBlue.copy(alpha = 0.22f),
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
                lineHeight = 23.sp,
                fontWeight = FontWeight.Bold,
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
                    size.width * 0.44f,
                    size.height * 0.93f,
                    size.width * 0.47f,
                    size.height * 0.93f,
                    size.width * 0.5f,
                    size.height * 0.93f,
                )
                cubicTo(
                    size.width * 0.84f,
                    size.height * 0.93f,
                    size.width * 0.97f,
                    size.height * 0.75f,
                    size.width * 0.97f,
                    size.height * 0.50f,
                )
                cubicTo(
                    size.width * 0.97f,
                    size.height * 0.32f,
                    size.width * 0.84f,
                    size.height * 0.14f,
                    size.width * 0.5f,
                    size.height * 0.14f,
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PostLoginOnboardingScreenPreview() {
    MyApplicationTheme(darkTheme = false, dynamicColor = false) {
        PostLoginOnboardingScreen(
            onFinished = {},
            onSkip = {},
        )
    }
}
