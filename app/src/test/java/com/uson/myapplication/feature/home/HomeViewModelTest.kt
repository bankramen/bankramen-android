package com.uson.myapplication.feature.home

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeViewModelTest {
    @Test
    fun `calculate next billing date uses current month when billing day remains`() {
        val nextBillingDate = calculateNextBillingDate(
            today = LocalDate.of(2026, 5, 27),
            desiredDayOfMonth = 31,
        )

        assertEquals(LocalDate.of(2026, 5, 31), nextBillingDate)
    }

    @Test
    fun `calculate next billing date rolls to next month after billing day passes`() {
        val nextBillingDate = calculateNextBillingDate(
            today = LocalDate.of(2026, 5, 27),
            desiredDayOfMonth = 15,
        )

        assertEquals(LocalDate.of(2026, 6, 15), nextBillingDate)
    }

    @Test
    fun `calculate next billing date clamps to end of month`() {
        val nextBillingDate = calculateNextBillingDate(
            today = LocalDate.of(2026, 2, 1),
            desiredDayOfMonth = 31,
        )

        assertEquals(LocalDate.of(2026, 2, 28), nextBillingDate)
    }
}
