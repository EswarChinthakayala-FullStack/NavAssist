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

sealed interface KycQueueEffect {
    data class ShowToast(val message: String) : KycQueueEffect
    data class ShowSnackbar(val message: String) : KycQueueEffect
}

@HiltViewModel
class KycReviewQueueViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _queueState = MutableStateFlow<UiState<List<AdminKycItemDto>>>(UiState.Loading)
    val queueState: StateFlow<UiState<List<AdminKycItemDto>>> = _queueState.asStateFlow()

    private var allQueueItems: List<AdminKycItemDto> = emptyList()

    private val _effects = MutableSharedFlow<KycQueueEffect>()
    val effects: SharedFlow<KycQueueEffect> = _effects.asSharedFlow()

    init {
        loadPendingQueue()
    }

    fun loadPendingQueue() {
        _queueState.value = UiState.Loading
        viewModelScope.launch {
            val res = adminRepository.getPendingKycQueue()
            res.onSuccess { list ->
                allQueueItems = list
                _queueState.value = UiState.Success(list)
            }.onFailure { err ->
                _queueState.value = UiState.Error(err.message ?: "Failed to load pending KYC queue")
            }
        }
    }

    fun filterQueue(query: String?) {
        if (query.isNullOrBlank()) {
            _queueState.value = UiState.Success(allQueueItems)
            return
        }
        val q = query.lowercase().trim()
        val filtered = allQueueItems.filter {
            (it.fullName ?: "").lowercase().contains(q) || it.id.toString().contains(q)
        }
        _queueState.value = UiState.Success(filtered)
    }

    fun approveKyc(assistantId: Int, name: String) {
        viewModelScope.launch {
            _effects.emit(KycQueueEffect.ShowToast("Approving $name..."))
            val res = adminRepository.approveKyc(assistantId)
            res.onSuccess {
                _effects.emit(KycQueueEffect.ShowToast("$name KYC Approved ✓"))
                loadPendingQueue()
            }.onFailure { err ->
                _effects.emit(KycQueueEffect.ShowSnackbar("Failed to approve KYC: ${err.message}"))
            }
        }
    }

    fun rejectKyc(assistantId: Int, name: String, reason: String) {
        viewModelScope.launch {
            _effects.emit(KycQueueEffect.ShowToast("Rejecting $name..."))
            val res = adminRepository.rejectKyc(assistantId, reason)
            res.onSuccess {
                _effects.emit(KycQueueEffect.ShowToast("$name KYC Rejected"))
                loadPendingQueue()
            }.onFailure { err ->
                _effects.emit(KycQueueEffect.ShowSnackbar("Failed to reject KYC: ${err.message}"))
            }
        }
    }
}
