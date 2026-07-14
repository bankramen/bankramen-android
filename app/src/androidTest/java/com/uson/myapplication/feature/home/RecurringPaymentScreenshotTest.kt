package com.uson.myapplication.feature.home

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.uson.myapplication.ui.theme.MyApplicationTheme
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import org.junit.Rule
import org.junit.Test

class RecurringPaymentScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun recurringPaymentPage_compactWidth_rendersApiBackedStateAndAddSheet() {
        composeRule.setContent {
            MyApplicationTheme(darkTheme = false, dynamicColor = false) {
                androidx.compose.foundation.layout.Box(modifier = Modifier.width(320.dp)) {
                    RecurringPage(
                        state = HomeUiState(
                            isLoading = false,
                            hasLoadedData = true,
                            recurringScheduledTotalAmount = 26_900L,
                            recurringRegistrationMessage = "정기결제로 등록했어요",
                        ),
                        recurringPayments = listOf(
                            EditableRecurringPayment(
                                id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                                title = "넷플릭스",
                                subtitle = "매월 15일 · 생활",
                                amountLabel = "17,000원",
                                confirmed = true,
                                deleting = false,
                            ),
                            EditableRecurringPayment(
                                id = UUID.fromString("00000000-0000-0000-0000-000000000002"),
                                title = "스포티파이",
                                subtitle = "매월 22일 · 취미/여가",
                                amountLabel = "9,900원",
                                confirmed = false,
                                deleting = false,
                            ),
                        ),
                        recurringCandidates = listOf(
                            EditableTransaction(
                                id = UUID.fromString("00000000-0000-0000-0000-000000000010"),
                                title = "유튜브 프리미엄",
                                time = "06.01 09:30",
                                categoryCode = "HOBBY_LEISURE",
                                category = "취미/여가",
                                amountLabel = "-14,900원",
                                positive = false,
                                icon = "↻",
                            ),
                        ),
                        onAddRecurring = {},
                        onDeleteRecurring = {},
                    )
                }
            }
        }

        composeRule.onNodeWithText("정기결제 API").assertIsDisplayed()
        composeRule.onNodeWithText("직접 등록 연동").assertIsDisplayed()
        composeRule.onNodeWithText("이번 달 예정 금액 26,900원").assertIsDisplayed()
        composeRule.onNodeWithText("정기결제로 등록했어요").assertIsDisplayed()
        composeRule.onNodeWithText("넷플릭스").assertIsDisplayed()
        composeRule.onNodeWithText("스포티파이").assertIsDisplayed()
        saveRootScreenshot("recurring_payment_api_screen.png")

        composeRule.onNodeWithText("유튜브 프리미엄").performScrollTo().assertIsDisplayed()
        saveRootScreenshot("recurring_payment_candidates.png")
    }

    @Test
    fun recurringPaymentAddSheet_manualMode_rendersFormForApiCreateRequest() {
        composeRule.setContent {
            MyApplicationTheme(darkTheme = false, dynamicColor = false) {
                androidx.compose.foundation.layout.Box(modifier = Modifier.width(320.dp)) {
                    RecurringAddSheet(
                        recurringCandidates = emptyList(),
                        addMode = RecurringAddMode.Manual,
                        selectedTransactionId = null,
                        manualAmount = "17000",
                        manualMerchant = "넷플릭스",
                        manualCategory = "LIVING",
                        dayOfMonth = "15",
                        isSubmitting = false,
                        onSelectMode = {},
                        onSelectTransaction = {},
                        onManualAmountChange = {},
                        onManualMerchantChange = {},
                        onManualCategoryChange = {},
                        onDayOfMonthChange = {},
                        onDismiss = {},
                        onConfirm = {},
                    )
                }
            }
        }

        composeRule.onNodeWithText("정기결제 추가").assertIsDisplayed()
        composeRule.onNodeWithText("직접 입력").assertIsDisplayed()
        composeRule.onNodeWithText("금액").assertIsDisplayed()
        composeRule.onNodeWithText("넷플릭스").assertIsDisplayed()
        composeRule.onNodeWithText("매월 결제일").assertIsDisplayed()
        saveRootScreenshot("recurring_payment_add_sheet.png")
    }

    private fun saveRootScreenshot(fileName: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val screenshotDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: context.filesDir
        val screenshotFile = File(screenshotDir, fileName)

        val bitmap = composeRule.onRoot()
            .captureToImage()
            .asAndroidBitmap()

        FileOutputStream(screenshotFile).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        exportScreenshotToPictures(fileName, bitmap)
    }

    private fun exportScreenshotToPictures(fileName: String, bitmap: Bitmap) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/BankramenScreenshots")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Could not create screenshot media item")
        resolver.openOutputStream(uri)?.use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        } ?: error("Could not open screenshot media output stream")
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
    }
}
