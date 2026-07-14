package com.uson.myapplication.core.auth

import com.uson.myapplication.generated.model.TokenRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthRawApi {
    @GET("auth/kakao/login")
    suspend fun login(): Response<ResponseBody>

    @GET("auth/kakao/callback")
    suspend fun callback(
        @Query("code") code: String,
        @Query("state") state: String,
    ): Response<ResponseBody>

    @POST("auth/kakao/reissue")
    suspend fun reissue(@Body tokenRequest: TokenRequest): Response<ResponseBody>

    @POST("auth/kakao/logout")
    suspend fun logout(@Body tokenRequest: TokenRequest): Response<ResponseBody>
}
