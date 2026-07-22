package com.navassist.android.data.remote.api

import com.navassist.android.data.remote.dto.auth.*
import com.navassist.android.data.remote.dto.user.UserResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): TokenPairResponseDto

    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequestDto): TokenPairResponseDto

    @POST("auth/send-otp")
    suspend fun sendOtp(@Body request: OtpRequestDto): Unit

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequestDto): TokenPairResponseDto

    @POST("auth/refresh")
    suspend fun refreshToken(): TokenPairResponseDto

    @GET("auth/me")
    suspend fun getCurrentUser(): UserResponseDto

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequestDto): Unit

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequestDto): Unit

    @POST("auth/logout")
    suspend fun logout(): Unit
}
