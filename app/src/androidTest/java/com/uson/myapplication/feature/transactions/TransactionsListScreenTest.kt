package com.uson.myapplication.feature.transactions

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.uson.myapplication.feature.home.TransactionItem
import com.uson.myapplication.ui.theme.MyApplicationTheme
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TransactionsListScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun listMatchesReferenceAndOpensExistingEditFlow() {
        val item = TransactionItem(
            id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
            icon = "",
            merchant = "어니언 성수",
            time = "09:41",
            categoryCode = "UNCATEGORIZED",
            category = "분류 중",
            amount = 6_800,
            date = LocalDate.of(2026, 9, 15),
        )
        var selected: TransactionItem? = null
        composeRule.setContent {
            MyApplicationTheme(darkTheme = false, dynamicColor = false) {
                TransactionsListScreen(
                    state = TransactionsUiState(
                        month = YearMonth.of(2026, 9),
                        loading = false,
                        expense = 1_160_200,
                        differenceRate = -4.0,
                        expenses = listOf(item),
                    ),
                    onKind = {},
                    onPreviousMonth = {},
                    onNextMonth = {},
                    onTransaction = { selected = it },
                    onNew = {},
                    onHome = {},
                    onReports = {},
                    onAlerts = {},
                )
            }
        }
        composeRule.onNodeWithText("1,160,200").assertIsDisplayed()
        composeRule.onNodeWithText("어니언 성수").performClick()
        assertEquals(item, selected)
    }
}
