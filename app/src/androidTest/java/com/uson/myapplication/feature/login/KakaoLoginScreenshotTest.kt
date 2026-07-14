package com.uson.myapplication.feature.login

import android.graphics.Bitmap
import android.os.Environment
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.uson.myapplication.ui.theme.MyApplicationTheme
import java.io.File
import java.io.FileOutputStream
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class KakaoLoginScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun kakaoLoginScreen_rendersAndPrimaryClickWorks() {
        var primaryClicks = 0

        composeRule.setContent {
            MyApplicationTheme(darkTheme = false, dynamicColor = false) {
                LoginScreen(
                    uiState = LoginUiState(
                        authStatusLabel = "로그인 필요",
                        authStatusDetail = "저장된 세션이 없어요",
                    ),
                    onPrimaryClick = { primaryClicks += 1 },
                )
            }
        }

        composeRule.onNodeWithText("카카오로 시작하기").assertIsDisplayed()
        saveRootScreenshot("kakao_login_screen.png")

        composeRule.onNodeWithText("카카오로 시작하기").performClick()
        assertEquals(1, primaryClicks)
    }

    @Test
    fun postLoginOnboarding_rendersAndFinishes() {
        var finishedClicks = 0

        composeRule.setContent {
            MyApplicationTheme(darkTheme = false, dynamicColor = false) {
                PostLoginOnboardingScreen(
                    onFinished = { finishedClicks += 1 },
                    onSkip = {},
                )
            }
        }

        composeRule.onNodeWithText("계좌 등록 없이\n시작할 수 있어요").assertIsDisplayed()
        saveRootScreenshot("post_login_onboarding_screen.png")

        composeRule.onNodeWithText("다음").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("내 소비 패턴을\n한눈에 분석해요").assertIsDisplayed()
        composeRule.onNodeWithText("다음").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("놓치기 쉬운 결제도\n알아서 챙겨드려요").assertIsDisplayed()
        composeRule.onNodeWithText("시작하기").assertIsDisplayed().performClick()

        assertEquals(1, finishedClicks)
    }

    private fun saveRootScreenshot(fileName: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val screenshotDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: context.filesDir
        val screenshotFile = File(screenshotDir, fileName)

        FileOutputStream(screenshotFile).use { output ->
            composeRule.onRoot()
                .captureToImage()
                .asAndroidBitmap()
                .compress(Bitmap.CompressFormat.PNG, 100, output)
        }
    }
}
