package com.navassist.android.domain.usecase.auth

import com.navassist.android.domain.model.User
import com.navassist.android.domain.repository.AuthRepository
import javax.inject.Inject

class SignupUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        role: String = "guest"
    ): Result<User> {
        if (fullName.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Name and password are required."))
        }
        return authRepository.register(
            fullName = fullName.trim(),
            email = email.trim(),
            phone = phone.trim(),
            password = password,
            role = role
        )
    }
}
