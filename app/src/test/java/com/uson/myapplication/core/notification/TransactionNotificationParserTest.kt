package com.uson.myapplication.core.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TransactionNotificationParserTest {
    @Test
    fun `payment notification is parsed for supported package`() {
        val parsed = TransactionNotificationParser.parse(
            sourceKey = "source-key",
            packageName = "com.kakaopay.app",
            title = "카카오페이",
            text = "스타벅스 강남점에서 4,500원 결제",
            bigText = "",
            timestamp = 1_234L,
        )

        assertNotNull(parsed)
        assertEquals(TransactionType.PAYMENT, parsed?.transactionType)
        assertEquals("스타벅스 강남점", parsed?.merchant)
        assertEquals(4_500L, parsed?.amount)
        assertEquals("KAKAO_PAY", parsed?.paymentMethod)
    }

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

    @Test
    fun `kbank transfer notification is parsed for supported package`() {
        val parsed = TransactionNotificationParser.parse(
            sourceKey = "source-key",
            packageName = "com.kbankwith.smartbank",
            title = "이체 완료",
            text = "홍길동님께 10,000원 송금",
            bigText = "",
            timestamp = 1_234L,
        )

        assertNotNull(parsed)
        assertEquals(TransactionType.TRANSFER_OUT, parsed?.transactionType)
        assertEquals("홍길동", parsed?.merchant)
        assertEquals(10_000L, parsed?.amount)
        assertEquals("K_BANK", parsed?.paymentMethod)
    }

    @Test
    fun `unsupported package is ignored`() {
        val parsed = TransactionNotificationParser.parse(
            sourceKey = "source-key",
            packageName = "com.example.unsupported",
            title = "결제 완료",
            text = "어딘가에서 10,000원 결제",
            bigText = "",
            timestamp = 1_234L,
        )

        assertNull(parsed)
    }

    @Test
    fun `spoofed package with a trusted prefix is ignored`() {
        val parsed = TransactionNotificationParser.parse(
            sourceKey = "source-key",
            packageName = "com.kbank.evil",
            title = "결제 완료",
            text = "어딘가에서 10,000원 결제",
            bigText = "",
            timestamp = 1_234L,
        )

        assertNull(parsed)
    }
}
