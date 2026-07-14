package com.uson.myapplication.feature.home

import com.uson.myapplication.core.api.BankramenGson
import com.uson.myapplication.generated.model.BankramenLocalTime
import com.uson.myapplication.generated.model.PushNotificationListResponse
import com.uson.myapplication.generated.model.PushNotificationResponse
import com.uson.myapplication.generated.model.TransactionHistoryResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

class HomeApiRepositoryTest {
    @Test
    fun `falls back to monthly transactions when recent endpoint is empty`() {
        val recent = resolveRecentTransactions(
            recentTransactions = emptyList(),
            monthlyExpenses = listOf(
                transaction(
                    title = "점심",
                    date = LocalDate.of(2026, 5, 27),
                    hour = 12,
                    minute = 30,
                    type = TransactionHistoryResponse.Type.EXPENSE,
                ),
            ),
            monthlyIncomes = listOf(
                transaction(
                    title = "월급",
                    date = LocalDate.of(2026, 5, 28),
                    hour = 9,
                    minute = 0,
                    type = TransactionHistoryResponse.Type.INCOME,
                ),
            ),
            categoryNames = emptyMap(),
        )

        assertEquals(listOf("월급", "점심"), recent.map { it.merchant })
        assertEquals(listOf(true, false), recent.map { it.positive })
    }

    @Test
    fun `prefers recent endpoint transactions when present`() {
        val recent = resolveRecentTransactions(
            recentTransactions = listOf(
                transaction(
                    title = "최근내역",
                    date = LocalDate.of(2026, 5, 29),
                    hour = 10,
                    minute = 15,
                    type = TransactionHistoryResponse.Type.EXPENSE,
                ),
            ),
            monthlyExpenses = listOf(
                transaction(
                    title = "예전내역",
                    date = LocalDate.of(2026, 5, 27),
                    hour = 8,
                    minute = 0,
                    type = TransactionHistoryResponse.Type.EXPENSE,
                ),
            ),
            monthlyIncomes = emptyList(),
            categoryNames = emptyMap(),
        )

        assertEquals(listOf("최근내역"), recent.map { it.merchant })
    }

    @Test
    fun `resolves created transaction id from exact expense match`() {
        val expectedId = UUID.randomUUID()

        val resolvedId = resolveCreatedTransactionId(
            candidates = listOf(
                transaction(
                    id = UUID.randomUUID(),
                    title = "다른내역",
                    amount = 15900L,
                    date = LocalDate.of(2026, 5, 27),
                    hour = 8,
                    minute = 0,
                    type = TransactionHistoryResponse.Type.EXPENSE,
                ),
                transaction(
                    id = expectedId,
                    title = "넷플릭스",
                    amount = 17000L,
                    date = LocalDate.of(2026, 5, 27),
                    hour = 9,
                    minute = 30,
                    type = TransactionHistoryResponse.Type.EXPENSE,
                ),
            ),
            merchant = "넷플릭스",
            amount = 17000L,
            transactionDate = LocalDate.of(2026, 5, 27),
        )

        assertEquals(expectedId, resolvedId)
    }

    @Test
    fun `returns null when created transaction cannot be found`() {
        val resolvedId = resolveCreatedTransactionId(
            candidates = listOf(
                transaction(
                    title = "유튜브",
                    amount = 14900L,
                    date = LocalDate.of(2026, 5, 27),
                    hour = 9,
                    minute = 30,
                    type = TransactionHistoryResponse.Type.EXPENSE,
                ),
            ),
            merchant = "넷플릭스",
            amount = 17000L,
            transactionDate = LocalDate.of(2026, 5, 27),
        )

        assertNull(resolvedId)
    }

    @Test
    fun `returns null rather than choosing between identical transaction candidates`() {
        val resolvedId = resolveCreatedTransactionId(
            candidates = listOf(
                transaction(
                    title = "넷플릭스",
                    amount = 17000L,
                    date = LocalDate.of(2026, 5, 27),
                    hour = 9,
                    minute = 30,
                    type = TransactionHistoryResponse.Type.EXPENSE,
                ),
                transaction(
                    title = "넷플릭스",
                    amount = 17000L,
                    date = LocalDate.of(2026, 5, 27),
                    hour = 10,
                    minute = 30,
                    type = TransactionHistoryResponse.Type.EXPENSE,
                ),
            ),
            merchant = "넷플릭스",
            amount = 17000L,
            transactionDate = LocalDate.of(2026, 5, 27),
        )

        assertNull(resolvedId)
    }

    @Test
    fun `maps push notification response to alert item`() {
        val notificationId = UUID.randomUUID()

        val item = PushNotificationResponse(
            notificationId = notificationId,
            title = "결제 알림",
            body = "스타벅스 5,800원이 기록됐어요",
            unread = true,
            displayTime = "방금 전",
        ).toUiModel()

        assertEquals(notificationId, item.id)
        assertEquals("결제 알림", item.title)
        assertEquals("스타벅스 5,800원이 기록됐어요", item.body)
        assertEquals("방금 전", item.displayTime)
        assertEquals(true, item.unread)
    }

    @Test
    fun `maps push notification fallback text and sent time`() {
        val item = PushNotificationResponse(
            title = "",
            body = null,
            unread = false,
            sentAt = OffsetDateTime.parse("2026-06-05T13:45:00+09:00"),
        ).toUiModel()

        assertEquals("알림", item.title)
        assertEquals("새 알림이 도착했어요", item.body)
        assertEquals("06.05 13:45", item.displayTime)
        assertEquals(false, item.unread)
    }

    @Test
    fun `parses push notification sentAt without timezone offset`() {
        val json = """
            {
              "unreadCount": 1,
              "notifications": [
                {
                  "notificationId": "550e8400-e29b-41d4-a716-446655440000",
                  "title": "결제 내역이 기록됐어요",
                  "body": "스타벅스 강남점 4,500원이 기록됐어요.",
                  "unread": true,
                  "sentAt": "2026-06-05T13:42:59"
                }
              ]
            }
        """.trimIndent()

        val response = BankramenGson.gsonBuilder.create()
            .fromJson(json, PushNotificationListResponse::class.java)

        assertEquals(1L, response.unreadCount)
        assertEquals("결제 내역이 기록됐어요", response.notifications?.single()?.title)
        assertEquals("06.05 13:42", response.notifications?.single()?.toUiModel()?.displayTime)
    }
}

private fun transaction(
    id: UUID = UUID.randomUUID(),
    title: String,
    amount: Long = 1000L,
    date: LocalDate,
    hour: Int,
    minute: Int,
    type: TransactionHistoryResponse.Type,
): TransactionHistoryResponse = TransactionHistoryResponse(
    transactionId = id,
    title = title,
    transactionDate = date,
    transactionTime = BankramenLocalTime(hour = hour, minute = minute),
    amount = amount,
    type = type,
)
