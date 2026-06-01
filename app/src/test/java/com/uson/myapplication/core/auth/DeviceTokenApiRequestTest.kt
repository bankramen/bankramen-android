package com.uson.myapplication.core.auth

import com.uson.myapplication.core.api.BankramenGson
import com.uson.myapplication.generated.api.APIApi
import com.uson.myapplication.generated.model.DeviceTokenRequest
import java.util.UUID
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class DeviceTokenApiRequestTest {
    @Test
    fun `save device token sends member id header and token body`() = runBlocking {
        val recorder = RecordingInterceptor()
        val api = Retrofit.Builder()
            .baseUrl("https://bankramen-api.test/")
            .client(OkHttpClient.Builder().addInterceptor(recorder).build())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(BankramenGson.gsonBuilder.create()))
            .build()
            .create(APIApi::class.java)

        api.saveDeviceToken(
            memberId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
            deviceTokenRequest = DeviceTokenRequest(token = "device-token"),
        )

        assertEquals("POST", recorder.method)
        assertEquals("550e8400-e29b-41d4-a716-446655440000", recorder.memberIdHeader)
        assertEquals("""{"token":"device-token"}""", recorder.body)
    }
}

private class RecordingInterceptor : Interceptor {
    var method: String? = null
    var memberIdHeader: String? = null
    var body: String? = null

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        method = request.method
        memberIdHeader = request.header("memberId")
        body = request.body?.let { requestBody ->
            Buffer().use { buffer ->
                requestBody.writeTo(buffer)
                buffer.readUtf8()
            }
        }

        return okhttp3.Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("{}".toResponseBody("application/json".toMediaType()))
            .build()
    }
}
