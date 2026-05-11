package com.uson.myapplication.core.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TransactionNotificationParserTest {
    @Test
    fun `self transfer notification is ignored`() {
        val parsed = TransactionNotificationParser.parse(
            sourceKey = "source-key",
            packageName = "com.kakaopay.app",
            title = "나한테 송금",
            text = "나한테 송금 10,000원 완료",
            bigText = "",
            timestamp = 1_234L,
        )

        assertNull(parsed)
    }

    @Test
    fun `normal transfer notification is saved`() {
        val parsed = TransactionNotificationParser.parse(
            sourceKey = "source-key",
            packageName = "com.kakaopay.app",
            title = "송금 완료",
            text = "홍길동님께 10,000원 송금",
            bigText = "",
            timestamp = 1_234L,
        )

        assertNotNull(parsed)
        assertEquals(TransactionType.TRANSFER_OUT, parsed?.transactionType)
        assertEquals("홍길동", parsed?.merchant)
        assertEquals(10_000L, parsed?.amount)
    }
}
