package com.navassist.android.domain.repository

import com.navassist.android.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(phone: String, password: String): Result<User>
    suspend fun sendOtp(phone: String): Result<Unit>
    suspend fun verifyOtp(phone: String, otp: String): Result<User>
    suspend fun register(fullName: String, email: String, phone: String, password: String, role: String): Result<User>
    suspend fun getCurrentUser(): Result<User>
    suspend fun logout()
    val currentUserState: Flow<User?>
}
