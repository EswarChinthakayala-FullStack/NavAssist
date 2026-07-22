package com.navassist.android.domain.usecase.auth

import com.navassist.android.domain.model.User
import com.navassist.android.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password must not be blank."))
        }
        return authRepository.login(email.trim(), password)
    }
}
