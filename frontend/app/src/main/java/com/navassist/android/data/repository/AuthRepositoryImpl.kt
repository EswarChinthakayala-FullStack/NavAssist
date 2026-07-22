package com.navassist.android.data.repository

import com.navassist.android.data.preferences.SessionDataStore
import com.navassist.android.data.remote.api.AuthApi
import com.navassist.android.data.remote.dto.auth.LoginRequestDto
import com.navassist.android.data.remote.dto.auth.OtpRequestDto
import com.navassist.android.data.remote.dto.auth.SignupRequestDto
import com.navassist.android.data.remote.dto.auth.VerifyOtpRequestDto
import com.navassist.android.data.remote.dto.user.UserResponseDto
import com.navassist.android.domain.model.User
import com.navassist.android.domain.model.UserRole
import com.navassist.android.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val sessionDataStore: SessionDataStore
) : AuthRepository {

    override val currentUserState: Flow<User?> = sessionDataStore.userId.map { id ->
        if (id == null) null
        else User(id = id, fullName = "NavAssist User", email = "", phone = "", role = UserRole.GUEST)
    }

    override suspend fun login(phone: String, password: String): Result<User> {
        return try {
            val formattedPhone = formatPhone(phone)
            val tokenPair = authApi.login(LoginRequestDto(phone = formattedPhone, password = password))
            sessionDataStore.saveTokens(tokenPair.accessToken, tokenPair.refreshToken)
            val userDto = authApi.getCurrentUser()
            sessionDataStore.saveUserSession(userDto.id.toString(), userDto.role)
            Result.success(userDto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendOtp(phone: String): Result<Unit> {
        return try {
            authApi.sendOtp(OtpRequestDto(phone = phone))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyOtp(phone: String, otp: String): Result<User> {
        return try {
            val tokenPair = authApi.verifyOtp(VerifyOtpRequestDto(phone = phone, otp = otp))
            sessionDataStore.saveTokens(tokenPair.accessToken, tokenPair.refreshToken)
            val userDto = authApi.getCurrentUser()
            sessionDataStore.saveUserSession(userDto.id.toString(), userDto.role)
            Result.success(userDto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        role: String
    ): Result<User> {
        return try {
            val tokenPair = authApi.signup(
                SignupRequestDto(
                    phone = phone.ifBlank { email },
                    name = fullName,
                    role = role,
                    email = email,
                    password = password
                )
            )
            sessionDataStore.saveTokens(tokenPair.accessToken, tokenPair.refreshToken)
            val userDto = authApi.getCurrentUser()
            sessionDataStore.saveUserSession(userDto.id.toString(), userDto.role)
            Result.success(userDto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): Result<User> {
        return try {
            val userDto = authApi.getCurrentUser()
            Result.success(userDto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        try {
            authApi.logout()
        } catch (e: Exception) {
            // Ignore network failures on logout
        } finally {
            sessionDataStore.clearSession()
        }
    }
}

private fun UserResponseDto.toDomain(): User {
    return User(
        id = id.toString(),
        fullName = fullName,
        email = email ?: "",
        phone = phone,
        role = parseRole(role),
        profilePictureUrl = profilePhotoUrl,
        rating = 5.0f
    )
}

private fun parseRole(roleStr: String): UserRole {
    return when (roleStr.uppercase()) {
        "ASSISTANT" -> UserRole.ASSISTANT
        "ADMIN", "ADMINISTRATOR" -> UserRole.ADMIN
        else -> UserRole.GUEST
    }
}

private fun formatPhone(phone: String): String {
    val trimmed = phone.trim()
    return when {
        trimmed.startsWith("+") -> trimmed
        trimmed.length == 10 -> "+91$trimmed"
        else -> "+$trimmed"
    }
}
