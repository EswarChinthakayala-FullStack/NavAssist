package com.navassist.android.presentation.admin.kyc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.data.remote.api.AdminKycItemDto
import com.navassist.android.domain.repository.AdminRepository
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface KycDetailEffect {
    data class ShowToast(val message: String) : KycDetailEffect
    data class ShowSnackbar(val message: String) : KycDetailEffect
    object NavigateBack : KycDetailEffect
}

@HiltViewModel
class KycReviewDetailViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _detailState = MutableStateFlow<UiState<AdminKycItemDto>>(UiState.Loading)
    val detailState: StateFlow<UiState<AdminKycItemDto>> = _detailState.asStateFlow()

    private val _effects = MutableSharedFlow<KycDetailEffect>()
    val effects: SharedFlow<KycDetailEffect> = _effects.asSharedFlow()

    fun loadAssistantDetail(assistantId: Int) {
        _detailState.value = UiState.Loading
        viewModelScope.launch {
            val res = adminRepository.getPendingKycQueue()
            res.onSuccess { list ->
                val match = list.find { it.id == assistantId || it.userId == assistantId }
                    ?: AdminKycItemDto(id = assistantId, userId = assistantId, fullName = "Applicant Guide #$assistantId", verificationStatus = "PENDING")
                _detailState.value = UiState.Success(match)
            }.onFailure { err ->
                _detailState.value = UiState.Error(err.message ?: "Failed to load assistant detail")
            }
        }
    }

    fun approveKyc(assistantId: Int) {
        viewModelScope.launch {
            _effects.emit(KycDetailEffect.ShowToast("Approving assistant KYC..."))
            val res = adminRepository.approveKyc(assistantId)
            res.onSuccess {
                _effects.emit(KycDetailEffect.ShowToast("Assistant Approved Successfully ✓"))
                _effects.emit(KycDetailEffect.NavigateBack)
            }.onFailure { err ->
                _effects.emit(KycDetailEffect.ShowSnackbar("Failed to approve KYC: ${err.message}"))
            }
        }
    }

    fun rejectKyc(assistantId: Int, reason: String) {
        viewModelScope.launch {
            _effects.emit(KycDetailEffect.ShowToast("Rejecting assistant KYC..."))
            val res = adminRepository.rejectKyc(assistantId, reason)
            res.onSuccess {
                _effects.emit(KycDetailEffect.ShowToast("Assistant KYC Rejected"))
                _effects.emit(KycDetailEffect.NavigateBack)
            }.onFailure { err ->
                _effects.emit(KycDetailEffect.ShowSnackbar("Failed to reject KYC: ${err.message}"))
            }
        }
    }
}
