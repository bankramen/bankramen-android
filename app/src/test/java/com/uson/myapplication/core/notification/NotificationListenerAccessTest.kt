package com.uson.myapplication.core.notification

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationListenerAccessTest {
    @Test
    fun `uses service-specific listener check from Android 8 1`() {
        assertFalse(supportsServiceSpecificListenerCheck(26))
        assertTrue(supportsServiceSpecificListenerCheck(27))
    }
}
