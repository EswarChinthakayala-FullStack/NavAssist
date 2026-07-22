package com.navassist.android.data.remote.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignupRequestDto(
    @SerialName("phone") val phone: String,
    @SerialName("name") val name: String,
    @SerialName("role") val role: String = "guest",
    @SerialName("email") val email: String? = null,
    @SerialName("password") val password: String
)

@Serializable
data class LoginRequestDto(
    @SerialName("phone") val phone: String,
    @SerialName("password") val password: String
)

@Serializable
data class OtpRequestDto(
    @SerialName("phone") val phone: String
)

@Serializable
data class VerifyOtpRequestDto(
    @SerialName("phone") val phone: String,
    @SerialName("otp") val otp: String
)

@Serializable
data class TokenPairResponseDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String = "bearer",
    @SerialName("debug_otp") val debugOtp: String? = null,
    @SerialName("debug_email_code") val debugEmailCode: String? = null
)

@Serializable
data class ForgotPasswordRequestDto(
    @SerialName("phone") val phone: String
)

@Serializable
data class ResetPasswordRequestDto(
    @SerialName("phone") val phone: String,
    @SerialName("otp") val otp: String,
    @SerialName("new_password") val newPassword: String
)
