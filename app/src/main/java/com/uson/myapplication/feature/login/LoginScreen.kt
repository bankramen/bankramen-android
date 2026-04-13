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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uson.myapplication.ui.theme.MyApplicationTheme

private val LoginBackgroundColor = Color(0xFFF2F4F6)
private val BrandBlueColor = Color(0xFF3182F6)
private val SecondaryTextColor = Color(0xFF8B95A1)
private val KakaoYellowColor = Color(0xFFFEE500)
private val StatusBarIndicatorColor = Color(0xFF111111)
private val PermissionReadyColor = Color(0xFF1AA672)
private val PermissionPendingColor = Color(0xFFE67E22)

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onPrimaryClick: () -> Unit,
    onApiHealthRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = LoginBackgroundColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LoginBackgroundColor)
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            FakeStatusBar()
            LoginContent(
                uiState = uiState,
                onPrimaryClick = onPrimaryClick,
                onApiHealthRefresh = onApiHealthRefresh,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onPrimaryClick: () -> Unit,
    onApiHealthRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(148.dp))
            LogoMark()
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "금융의 모든 것\n뱅크라면에서",
                color = Color.Black,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 20.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "흩어진 내 자산을 한눈에 확인하고\n똑똑하게 관리하세요",
                color = SecondaryTextColor,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
            Spacer(modifier = Modifier.height(20.dp))
            NotificationAccessStatus(uiState = uiState)
            Spacer(modifier = Modifier.height(12.dp))
            AuthStatus(uiState = uiState)
            Spacer(modifier = Modifier.height(12.dp))
            AuthContractStatus(uiState = uiState)
            Spacer(modifier = Modifier.height(12.dp))
            ApiHealthStatus(
                uiState = uiState,
                onRefreshClick = onApiHealthRefresh,
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(
                onClick = onPrimaryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KakaoYellowColor,
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
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = if (uiState.notificationAccessGranted) {
                            if (uiState.isAuthenticated) "자동 로그인으로 계속하기" else "카카오 로그인 연결 준비"
                        } else {
                            "알림 접근 권한 설정하기"
                        },
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 13.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "디버그 저장 경로: ${uiState.notificationDebugPath}",
                color = SecondaryTextColor,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                ),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "저장된 알림 ${uiState.parsedNotificationCount}건 · 최근 ${uiState.latestMerchant ?: "없음"} ${uiState.latestAmountLabel}",
                color = SecondaryTextColor,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                ),
            )
            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}

@Composable
private fun AuthContractStatus(
    uiState: LoginUiState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF7F8FA),
                shape = RoundedCornerShape(16.dp),
            )
            .padding(16.dp),
    ) {
        Text(
            text = uiState.authContractLabel,
            color = SecondaryTextColor,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun AuthStatus(
    uiState: LoginUiState,
    modifier: Modifier = Modifier,
) {
    val statusColor = if (uiState.isAuthenticated) PermissionReadyColor else BrandBlueColor

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = statusColor.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp),
            )
            .padding(16.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = uiState.authStatusLabel,
                color = statusColor,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = uiState.authStatusDetail,
                color = SecondaryTextColor,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                ),
            )
        }
    }
}

@Composable
private fun ApiHealthStatus(
    uiState: LoginUiState,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF4F8FF),
                shape = RoundedCornerShape(16.dp),
            )
            .padding(16.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "API 상태 ${uiState.apiHealthLabel}",
                color = BrandBlueColor,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            uiState.apiServerTime?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    color = SecondaryTextColor,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onRefreshClick,
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(if (uiState.isCheckingApi) "확인 중..." else "API 상태 다시 확인")
            }
        }
    }
}

@Composable
private fun NotificationAccessStatus(
    uiState: LoginUiState,
    modifier: Modifier = Modifier,
) {
    val statusColor = if (uiState.notificationAccessGranted) PermissionReadyColor else PermissionPendingColor
    val statusText = if (uiState.notificationAccessGranted) {
        "알림 접근 권한이 연결되어 있어요"
    } else {
        "자동 가계부를 위해 알림 접근 권한이 필요해요"
    }

    Box(
        modifier = modifier
            .background(
                color = statusColor.copy(alpha = 0.10f),
                shape = RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = statusText,
            color = statusColor,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun FakeStatusBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(LoginBackgroundColor)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "9:41",
            color = Color.Black,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(12.dp)
                    .background(StatusBarIndicatorColor, RoundedCornerShape(2.dp)),
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(StatusBarIndicatorColor, RoundedCornerShape(999.dp)),
            )
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(12.dp)
                    .background(StatusBarIndicatorColor, RoundedCornerShape(2.dp)),
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
                elevation = 14.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = BrandBlueColor.copy(alpha = 0.20f),
                ambientColor = BrandBlueColor.copy(alpha = 0.20f),
            )
            .background(
                color = BrandBlueColor,
                shape = RoundedCornerShape(24.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "BANK\nRAMEN",
            color = Color.White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
private fun KakaoTalkIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(18.dp)) {
        val bubblePath = Path().apply {
            moveTo(size.width * 0.16f, size.height * 0.20f)
            quadraticTo(size.width * 0.16f, size.height * 0.08f, size.width * 0.30f, size.height * 0.08f)
            lineTo(size.width * 0.70f, size.height * 0.08f)
            quadraticTo(size.width * 0.84f, size.height * 0.08f, size.width * 0.84f, size.height * 0.20f)
            lineTo(size.width * 0.84f, size.height * 0.56f)
            quadraticTo(size.width * 0.84f, size.height * 0.68f, size.width * 0.70f, size.height * 0.68f)
            lineTo(size.width * 0.49f, size.height * 0.68f)
            lineTo(size.width * 0.31f, size.height * 0.88f)
            lineTo(size.width * 0.35f, size.height * 0.68f)
            lineTo(size.width * 0.30f, size.height * 0.68f)
            quadraticTo(size.width * 0.16f, size.height * 0.68f, size.width * 0.16f, size.height * 0.56f)
            close()
        }

        drawPath(
            path = bubblePath,
            color = Color.Black,
            style = Fill,
        )

        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint().apply {
                color = Color.Black.toArgb()
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = 7.sp.toPx()
                isFakeBoldText = true
                isAntiAlias = true
            }
            val baseline = size.height * 0.42f - (paint.ascent() + paint.descent()) / 2f
            canvas.nativeCanvas.drawText("T", size.width * 0.50f, baseline, paint)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreviewPermissionPending() {
    MyApplicationTheme(darkTheme = false, dynamicColor = false) {
        LoginScreen(
            uiState = LoginUiState(
                notificationAccessGranted = false,
                notificationDebugPath = "/data/user/0/com.uson.myapplication/files/notification-debug/parsed-transactions.log",
                apiHealthLabel = "연결 실패",
                authStatusLabel = "로그인 필요",
                authStatusDetail = "저장된 세션이 없어요",
                authContractLabel = "REST 계약 준비 완료 (/api/v1/auth/kakao/login, /api/v1/auth/token/reissue)",
            ),
            onPrimaryClick = {},
            onApiHealthRefresh = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreviewPermissionGranted() {
    MyApplicationTheme(darkTheme = false, dynamicColor = false) {
        LoginScreen(
            uiState = LoginUiState(
                notificationAccessGranted = true,
                notificationDebugPath = "/data/user/0/com.uson.myapplication/files/notification-debug/parsed-transactions.log",
                parsedNotificationCount = 3,
                latestMerchant = "스타벅스",
                latestAmountLabel = "5900원",
                apiHealthLabel = "정상 (ok)",
                apiServerTime = "2026-04-13T12:00:00Z",
                authStatusLabel = "자동 로그인 가능",
                authStatusDetail = "refresh_token 기반 리이슈 준비 완료",
                isAuthenticated = true,
                authContractLabel = "REST 계약 준비 완료 (/api/v1/auth/kakao/login, /api/v1/auth/token/reissue)",
            ),
            onPrimaryClick = {},
            onApiHealthRefresh = {},
        )
    }
}
