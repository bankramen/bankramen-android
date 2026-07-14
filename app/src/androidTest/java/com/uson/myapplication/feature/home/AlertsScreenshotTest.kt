package com.uson.myapplication.feature.home

import android.graphics.Bitmap
import android.os.Environment
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.uson.myapplication.ui.theme.MyApplicationTheme
import java.io.File
import java.io.FileOutputStream
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AlertsScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun alertsPage_compactWidth_rendersAndSuggestionActionsWork() {
        var acceptClicks = 0
        var dismissClicks = 0

        composeRule.setContent {
            MyApplicationTheme(darkTheme = false, dynamicColor = false) {
                androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.width(320.dp)) {
                    AlertsPage(
                        notificationDebugEvents = emptyList(),
                        parsedNotifications = emptyList(),
                        onAcceptSuggestion = { acceptClicks += 1 },
                        suggestionAccepted = false,
                        onDismissSuggestion = { dismissClicks += 1 },
                        suggestionDismissed = false,
                    )
                }
            }
        }

        composeRule.onNodeWithText("알림 API").assertIsDisplayed()
        composeRule.onNodeWithText("정기결제가 의심돼요!").assertIsDisplayed()
        composeRule.onNodeWithText("아니오").assertIsDisplayed()
        composeRule.onNodeWithText("예").assertIsDisplayed()
        saveRootScreenshot("alerts_compact_screen.png")

        composeRule.onNodeWithText("예").performClick()
        assertEquals(1, acceptClicks)

        composeRule.onNodeWithText("아니오").performClick()
        assertEquals(1, dismissClicks)
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
