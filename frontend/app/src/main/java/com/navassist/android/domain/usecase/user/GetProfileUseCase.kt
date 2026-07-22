package com.navassist.android.domain.usecase.user

import com.navassist.android.domain.model.User
import com.navassist.android.domain.repository.UserRepository
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<User> {
        return userRepository.getMyProfile()
    }
}
