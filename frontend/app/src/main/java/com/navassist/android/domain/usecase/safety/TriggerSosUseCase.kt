package com.navassist.android.domain.usecase.safety

import com.navassist.android.domain.repository.SosRepository
import javax.inject.Inject

class TriggerSosUseCase @Inject constructor(
    private val sosRepository: SosRepository
) {
    suspend operator fun invoke(bookingId: String?, latitude: Double, longitude: Double): Result<Unit> {
        return sosRepository.triggerSos(bookingId, latitude, longitude)
    }
}
