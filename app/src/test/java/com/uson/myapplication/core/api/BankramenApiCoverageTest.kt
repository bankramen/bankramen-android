package com.uson.myapplication.core.api

import com.uson.myapplication.core.auth.GeneratedAuthGateway
import com.uson.myapplication.core.auth.AuthRawApi
import com.uson.myapplication.core.auth.KakaoAuthorizationCodeRequest
import com.uson.myapplication.core.auth.parseAuthSessionResponse
import com.uson.myapplication.core.auth.parseLoginUrlResponse
import com.uson.myapplication.feature.home.HomeApiRepository
import com.uson.myapplication.generated.api.APIApi
import com.uson.myapplication.generated.api.CategoryApi
import com.uson.myapplication.generated.api.MonthlyReportApi
import com.uson.myapplication.generated.api.TransactionApi
import com.uson.myapplication.generated.infrastructure.Serializer
import com.uson.myapplication.generated.model.CreateRecurringPaymentRequest
import com.uson.myapplication.generated.model.CreatePaymentNotificationTransactionRequest
import com.uson.myapplication.generated.model.CreateTransactionRequest
import com.uson.myapplication.generated.model.DeviceTokenRequest
import com.uson.myapplication.generated.model.KakaoLoginRequest
import com.uson.myapplication.generated.model.TokenRequest
import com.uson.myapplication.generated.model.UpdateTransactionCategoryRequest
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class BankramenApiCoverageTest {
    @Test
    fun generatedSwaggerEndpointsCreateExpectedHttpRequests() = runBlocking {
        val recorder = RecordingInterceptor()
        val retrofit = fakeRetrofit(recorder)
        val apiApi = retrofit.create(APIApi::class.java)
        val categoryApi = retrofit.create(CategoryApi::class.java)
        val monthlyReportApi = retrofit.create(MonthlyReportApi::class.java)
        val transactionApi = retrofit.create(TransactionApi::class.java)

        apiApi.login(KakaoLoginRequest(kakaoAccessToken = "kakao-access-token"))
        apiApi.reissue(TokenRequest(refreshToken = "refresh-token"))
        apiApi.logout(TokenRequest(refreshToken = "refresh-token"))
        apiApi.saveDeviceToken(
            memberId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
            deviceTokenRequest = DeviceTokenRequest(token = "device-token"),
        )
        apiApi.create(
            userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001"),
            createRecurringPaymentRequest = CreateRecurringPaymentRequest(
                transactionId = UUID.fromString("550e8400-e29b-41d4-a716-446655440002"),
                cycle = CreateRecurringPaymentRequest.Cycle.MONTHLY,
                nextBillingDate = LocalDate.of(2026, 9, 15),
            ),
        )
        apiApi.confirm(recurringPaymentId = UUID.fromString("550e8400-e29b-41d4-a716-446655440003"))
        categoryApi.getCategories()
        monthlyReportApi.getMonthlyAmountSummary(year = 2026, month = 8)
        monthlyReportApi.getMonthlyCategoryExpenses(year = 2026, month = 8)
        transactionApi.createTransaction(
            CreateTransactionRequest(
                type = CreateTransactionRequest.Type.EXPENSE,
                amount = 4500,
                title = "스타벅스 강남점",
                category = CreateTransactionRequest.Category.FOOD,
                transactionDate = LocalDate.of(2026, 8, 15),
            ),
        )
        transactionApi.createPaymentNotificationTransaction(
            CreatePaymentNotificationTransactionRequest(title = "스타벅스 강남점", amount = 4500),
        )
        transactionApi.getMonthlyExpenseTransactions(year = 2026, month = 8)
        transactionApi.getMonthlyIncomeTransactions(year = 2026, month = 8)
        transactionApi.getRecentTransactions(limit = 5)
        transactionApi.updateTransactionCategory(
            transactionId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
            updateTransactionCategoryRequest = UpdateTransactionCategoryRequest(
                category = UpdateTransactionCategoryRequest.Category.FOOD,
            ),
        )
        transactionApi.deleteTransaction(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))

        assertEquals(
            setOf(
                "POST /auth/kakao/login",
                "POST /auth/kakao/reissue",
                "POST /auth/kakao/logout",
                "POST /push/token",
                "POST /recurring-payments",
                "PATCH /recurring-payments/550e8400-e29b-41d4-a716-446655440003/confirm",
                "GET /categories",
                "POST /transactions",
                "POST /transactions/payment-notifications",
                "GET /reports/monthly/summary",
                "GET /reports/monthly/categories",
                "GET /transactions/expenses",
                "GET /transactions/incomes",
                "GET /transactions/recent",
                "PATCH /transactions/550e8400-e29b-41d4-a716-446655440000/category",
                "DELETE /transactions/550e8400-e29b-41d4-a716-446655440000",
            ),
            recorder.calls.map { "${it.method} ${it.path}" }.toSet(),
        )
        assertTrue(recorder.calls.any { it.path == "/reports/monthly/summary" && it.query == "year=2026&month=8" })
        assertTrue(recorder.calls.any { it.path == "/transactions/incomes" && it.query == "year=2026&month=8" })
        assertTrue(recorder.calls.any { it.path == "/push/token" })
    }

    @Test
    fun authGatewayCallsCallbackReissueAndLogoutApis() = runBlocking {
        val recorder = RecordingInterceptor()
        val authApi = fakeRetrofit(recorder).create(AuthRawApi::class.java)
        val gateway = GeneratedAuthGateway(authApi)

        gateway.loginWithKakao(
            KakaoAuthorizationCodeRequest(
                authorizationCode = "code-123",
                redirectUri = "bankramen://auth/kakao",
                state = "state-123",
            ),
        )
        gateway.reissue(refreshToken = "refresh-token")
        gateway.logout(refreshToken = "refresh-token")

        assertEquals(
            listOf(
                "GET /auth/kakao/callback",
                "POST /auth/kakao/reissue",
                "POST /auth/kakao/logout",
            ),
            recorder.calls.map { "${it.method} ${it.path}" },
        )
    }

    @Test
    fun authParsersHandlePrimitiveResponses() {
        assertEquals(
            "https://kauth.kakao.com/oauth/authorize?state=abc",
            parseLoginUrlResponse("\"https://kauth.kakao.com/oauth/authorize?state=abc\""),
        )

        val session = parseAuthSessionResponse(
            "\"{\\\"accessToken\\\":\\\"access-token\\\",\\\"refreshToken\\\":\\\"refresh-token\\\",\\\"expiresIn\\\":3600}\"",
        )

        assertEquals("access-token", session.accessToken)
        assertEquals("refresh-token", session.refreshToken)
    }

    @Test
    fun homeRepositoryCallsDataApisAfterLoginFlowCanEnterHome() = runBlocking {
        val recorder = RecordingInterceptor()
        val retrofit = fakeRetrofit(recorder)
        val repository = HomeApiRepository(
            transactionApi = retrofit.create(TransactionApi::class.java),
            monthlyReportApi = retrofit.create(MonthlyReportApi::class.java),
            categoryApi = retrofit.create(CategoryApi::class.java),
        )

        val snapshot = repository.loadMonth(YearMonth.of(2026, 8))

        assertEquals(
            setOf(
                "GET /categories",
                "GET /reports/monthly/summary",
                "GET /reports/monthly/categories",
                "GET /transactions/expenses",
                "GET /transactions/incomes",
                "GET /transactions/recent",
            ),
            recorder.calls.map { "${it.method} ${it.path}" }.toSet(),
        )
        recorder.calls
            .filter { it.path !in setOf("/categories", "/transactions/recent") }
            .forEach { call ->
                assertEquals("year=2026&month=8", call.query)
            }
        assertEquals(YearMonth.of(2026, 8), snapshot.yearMonth)
        assertEquals(1_500_000L, snapshot.previousExpense)
        assertEquals(2_800_000L, snapshot.previousIncome)
        assertEquals("식비", snapshot.categories.first().categoryName)
        assertEquals(1, snapshot.recentTransactions.size)
    }

    private fun fakeRetrofit(recorder: RecordingInterceptor): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://bankramen-api.test/")
            .client(OkHttpClient.Builder().addInterceptor(recorder).build())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(Serializer.gsonBuilder.create()))
            .build()
}

private data class RecordedCall(
    val method: String,
    val path: String,
    val query: String?,
)

private class RecordingInterceptor : Interceptor {
    val calls = mutableListOf<RecordedCall>()

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        calls += RecordedCall(
            method = request.method,
            path = request.url.encodedPath,
            query = request.url.encodedQuery,
        )

        return okhttp3.Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(responseJson(request.url.encodedPath).toResponseBody("application/json".toMediaType()))
            .build()
    }

    private fun responseJson(path: String): String = when (path) {
        "/auth/kakao/login" -> """{"accessToken":"access-token","refreshToken":"refresh-token"}"""
        "/auth/kakao/callback",
        "/auth/kakao/reissue" -> """{"accessToken":"access-token","refreshToken":"refresh-token","expiresIn":3600,"userId":"user-1"}"""
        "/auth/kakao/logout" -> "{}"
        "/push/token" -> "{}"
        "/recurring-payments" -> "{}"
        "/recurring-payments/550e8400-e29b-41d4-a716-446655440003/confirm" -> "{}"
        "/categories" -> """{"categories":[{"code":"FOOD","displayName":"식비"},{"code":"SALARY","displayName":"급여"}]}"""
        "/reports/monthly/summary" -> """
            {
              "yearMonth":"2026-08",
              "expense":{"currentAmount":1250000,"previousAmount":1500000,"hasPreviousMonthData":true,"differenceRate":-17.0},
              "income":{"currentAmount":3000000,"previousAmount":2800000,"hasPreviousMonthData":true,"differenceRate":7.1}
            }
        """.trimIndent()
        "/reports/monthly/categories" -> """
            {
              "yearMonth":"2026-08",
              "totalExpense":1250000,
              "categories":[{"category":"FOOD","categoryName":"식비","expenseAmount":450000,"expenseRatio":35.0,"spentMoreThanPreviousMonth":true}]
            }
        """.trimIndent()
        "/transactions/expenses" -> """
            {
              "yearMonth":"2026-08",
              "expenses":[{"title":"스타벅스 강남점","transactionDate":"2026-08-15","transactionTime":{"hour":14,"minute":30,"second":0,"nano":0},"amount":1400,"category":"FOOD","categoryName":"식비"}]
            }
        """.trimIndent()
        "/transactions/incomes" -> """
            {
              "yearMonth":"2026-08",
              "incomes":[{"title":"월급","transactionDate":"2026-08-25","transactionTime":{"hour":9,"minute":0,"second":0,"nano":0},"amount":3000000,"category":"SALARY","categoryName":"급여"}]
            }
        """.trimIndent()
        "/transactions/recent" -> """
            {
              "transactions":[{"transactionId":"550e8400-e29b-41d4-a716-446655440000","title":"스타벅스 강남점","transactionDate":"2026-08-15","transactionTime":{"hour":14,"minute":30,"second":0,"nano":0},"amount":1400,"type":"EXPENSE","category":"FOOD","categoryName":"식비"}]
            }
        """.trimIndent()
        "/transactions/550e8400-e29b-41d4-a716-446655440000/category" -> """
            {"transactionId":"550e8400-e29b-41d4-a716-446655440000","title":"스타벅스 강남점","transactionDate":"2026-08-15","transactionTime":{"hour":14,"minute":30,"second":0,"nano":0},"amount":1400,"type":"EXPENSE","category":"FOOD","categoryName":"식비"}
        """.trimIndent()
        "/transactions",
        "/transactions/payment-notifications",
        "/transactions/550e8400-e29b-41d4-a716-446655440000" -> "{}"
        else -> "{}"
    }
}
