package com.uson.myapplication.core.transaction

import com.uson.myapplication.core.notification.ParsedTransactionNotification
import com.uson.myapplication.core.notification.TransactionType
import com.uson.myapplication.generated.api.TransactionApi
import com.uson.myapplication.generated.model.CreatePaymentNotificationTransactionRequest
import com.uson.myapplication.generated.model.CreateTransactionRequest
import com.uson.myapplication.generated.model.MonthlyExpenseTransactionListResponse
import com.uson.myapplication.generated.model.MonthlyIncomeTransactionListResponse
import com.uson.myapplication.generated.model.RecentTransactionListResponse
import com.uson.myapplication.generated.model.TransactionHistoryResponse
import com.uson.myapplication.generated.model.UpdateTransactionCategoryRequest
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

class NotificationTransactionUploaderTest {
    @Test
    fun `payment notification builds request before dispatching api call`() = runBlocking {
        val fakeApi = FakeTransactionApi()
        fakeApi.response = Response.error(500, "{}".toResponseBody("application/json".toMediaType()))
        val uploader = NotificationTransactionUploader(transactionApi = fakeApi)

        val result = runCatching {
            uploader.upload(
                ParsedTransactionNotification(
                    sourceKey = "source-key",
                    packageName = "com.kakaopay.app",
                    title = "카카오페이",
                    body = "스타벅스 강남점에서 4,500원 결제",
                    amount = 4_500L,
                    merchant = "스타벅스 강남점",
                    timestamp = 1_234L,
                    paymentMethod = "KAKAO_PAY",
                    transactionType = TransactionType.PAYMENT,
                ),
            ).getOrThrow()
        }

        assertTrue(result.isFailure)
        assertEquals("스타벅스 강남점", fakeApi.lastRequest?.title)
        assertEquals(4_500L, fakeApi.lastRequest?.amount)
    }

    @Test
    fun `income notification uploads as income transaction`() = runBlocking {
        val fakeApi = FakeTransactionApi()
        val uploader = NotificationTransactionUploader(transactionApi = fakeApi)

        val outcome = uploader.upload(
            ParsedTransactionNotification(
                sourceKey = "source-key",
                packageName = "com.kbankwith.smartbank",
                title = "입금 완료",
                body = "홍길동님이 10,000원 보냈어요",
                amount = 10_000L,
                merchant = "홍길동",
                timestamp = 1_234L,
                paymentMethod = "K_BANK",
                transactionType = TransactionType.INCOME,
            ),
        ).getOrThrow()

        assertEquals(NotificationUploadOutcome.Uploaded, outcome)
        assertEquals(CreateTransactionRequest.Type.INCOME, fakeApi.lastTransactionRequest?.type)
        assertEquals(CreateTransactionRequest.Category.TRANSFER, fakeApi.lastTransactionRequest?.category)
        assertEquals("홍길동", fakeApi.lastTransactionRequest?.title)
        assertEquals(10_000L, fakeApi.lastTransactionRequest?.amount)
        assertEquals(LocalDate.of(1970, 1, 1), fakeApi.lastTransactionRequest?.transactionDate)
    }

    @Test
    fun `transfer notification is not uploaded`() = runBlocking {
        val fakeApi = FakeTransactionApi()
        val uploader = NotificationTransactionUploader(transactionApi = fakeApi)

        val outcome = uploader.upload(
            ParsedTransactionNotification(
                sourceKey = "source-key",
                packageName = "com.kakaopay.app",
                title = "송금 완료",
                body = "홍길동님께 10,000원 송금",
                amount = 10_000L,
                merchant = "홍길동",
                timestamp = 1_234L,
                paymentMethod = "KAKAO_PAY",
                transactionType = TransactionType.TRANSFER_OUT,
            ),
        ).getOrThrow()

        assertEquals(NotificationUploadOutcome.Skipped, outcome)
        assertNull(fakeApi.lastRequest)
        assertNull(fakeApi.lastTransactionRequest)
    }

    @Test
    fun `missing amount is not uploaded`() = runBlocking {
        val fakeApi = FakeTransactionApi()
        val uploader = NotificationTransactionUploader(transactionApi = fakeApi)

        val outcome = uploader.upload(
            ParsedTransactionNotification(
                sourceKey = "source-key",
                packageName = "com.kakaopay.app",
                title = "카카오페이",
                body = "스타벅스 강남점 결제",
                amount = null,
                merchant = "스타벅스 강남점",
                timestamp = 1_234L,
                paymentMethod = "KAKAO_PAY",
                transactionType = TransactionType.PAYMENT,
            ),
        ).getOrThrow()

        assertEquals(NotificationUploadOutcome.Skipped, outcome)
        assertNull(fakeApi.lastRequest)
        assertNull(fakeApi.lastTransactionRequest)
    }
}

private class FakeTransactionApi : TransactionApi {
    var lastRequest: CreatePaymentNotificationTransactionRequest? = null
    var lastTransactionRequest: CreateTransactionRequest? = null
    var response: Response<Unit> = Response.success(Unit)

    override suspend fun createPaymentNotificationTransaction(
        createPaymentNotificationTransactionRequest: CreatePaymentNotificationTransactionRequest,
    ): Response<Unit> {
        lastRequest = createPaymentNotificationTransactionRequest
        return response
    }

    override suspend fun createTransaction(createTransactionRequest: CreateTransactionRequest): Response<Unit> {
        lastTransactionRequest = createTransactionRequest
        return response
    }

    override suspend fun deleteTransaction(transactionId: UUID): Response<Unit> =
        error("Not needed in this test")

    override suspend fun getMonthlyExpenseTransactions(year: Int, month: Int): Response<MonthlyExpenseTransactionListResponse> =
        error("Not needed in this test")

    override suspend fun getMonthlyIncomeTransactions(year: Int, month: Int): Response<MonthlyIncomeTransactionListResponse> =
        error("Not needed in this test")

    override suspend fun getRecentTransactions(limit: Int): Response<RecentTransactionListResponse> =
        error("Not needed in this test")

    override suspend fun updateTransactionCategory(
        transactionId: UUID,
        updateTransactionCategoryRequest: UpdateTransactionCategoryRequest,
    ): Response<TransactionHistoryResponse> = error("Not needed in this test")
}
