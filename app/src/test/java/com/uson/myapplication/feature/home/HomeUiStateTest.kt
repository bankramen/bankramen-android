package com.uson.myapplication.feature.home

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeUiStateTest {
    @Test
    fun `shows skeleton while first load has no visible data`() {
        val state = HomeUiState(
            isLoading = true,
            hasLoadedData = false,
        )

        assertTrue(state.shouldShowSkeleton)
    }

    @Test
    fun `hides skeleton while refreshing existing server data`() {
        val state = HomeUiState(
            isLoading = true,
            hasLoadedData = true,
            recentTransactions = listOf(
                TransactionItem(
                    icon = "🍽",
                    merchant = "점심",
                    time = "12:30",
                    categoryCode = "FOOD",
                    category = "식비",
                    amount = 12000L,
                ),
            ),
        )

        assertFalse(state.shouldShowSkeleton)
    }
}
