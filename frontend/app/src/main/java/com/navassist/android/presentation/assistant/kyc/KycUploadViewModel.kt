package com.navassist.android.presentation.assistant.kyc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.domain.repository.KycRepository
import com.navassist.android.domain.repository.KycStatusDomain
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed interface KycUploadEffect {
    data class ShowToast(val message: String) : KycUploadEffect
    data class ShowSnackbar(val message: String) : KycUploadEffect
    object NavigateToDashboard : KycUploadEffect
}

@HiltViewModel
class KycUploadViewModel @Inject constructor(
    private val kycRepository: KycRepository
) : ViewModel() {

    private val _kycState = MutableStateFlow<UiState<KycStatusDomain>>(UiState.Loading)
    val kycState: StateFlow<UiState<KycStatusDomain>> = _kycState.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _uploadProgressPct = MutableStateFlow(0)
    val uploadProgressPct: StateFlow<Int> = _uploadProgressPct.asStateFlow()

    private val _effects = MutableSharedFlow<KycUploadEffect>()
    val effects: SharedFlow<KycUploadEffect> = _effects.asSharedFlow()

    private var pollingJob: Job? = null

    init {
        loadKycStatus()
    }

    fun loadKycStatus() {
        _kycState.value = UiState.Loading
        viewModelScope.launch {
            val result = kycRepository.getKycStatus()
            result.onSuccess { status ->
                _kycState.value = UiState.Success(status)
                handlePollingState(status.verificationStatus)
            }.onFailure { err ->
                _kycState.value = UiState.Error(err.message ?: "Failed to load KYC status")
            }
        }
    }

    private fun handlePollingState(statusStr: String) {
        pollingJob?.cancel()
        if (statusStr.equals("PENDING", ignoreCase = true)) {
            startPolling()
        }
    }

    private fun startPolling() {
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(20000) // Poll status every 20s while PENDING
                val result = kycRepository.getKycStatus()
                result.onSuccess { status ->
                    _kycState.value = UiState.Success(status)
                    if (!status.verificationStatus.equals("PENDING", ignoreCase = true)) {
                        pollingJob?.cancel()
                        if (status.verificationStatus.equals("VERIFIED", ignoreCase = true) ||
                            status.verificationStatus.equals("APPROVED", ignoreCase = true)) {
                            _effects.emit(KycUploadEffect.ShowToast("Verification Approved! ✓"))
                        }
                    }
                }
            }
        }
    }

    fun submitDocuments(aadhaarNumber: String, docFrontFile: File, docBackFile: File) {
        if (aadhaarNumber.length != 12) {
            viewModelScope.launch {
                _effects.emit(KycUploadEffect.ShowSnackbar("Aadhaar number must be exactly 12 digits."))
            }
            return
        }

        _isUploading.value = true
        _uploadProgressPct.value = 10
        viewModelScope.launch {
            _uploadProgressPct.value = 40
            delay(300)
            _uploadProgressPct.value = 70

            val result = kycRepository.uploadDocuments(aadhaarNumber, docFrontFile, docBackFile)
            _isUploading.value = false
            _uploadProgressPct.value = 100

            result.onSuccess { status ->
                _kycState.value = UiState.Success(status)
                _effects.emit(KycUploadEffect.ShowToast("Documents submitted successfully for review ✓"))
                startPolling()
            }.onFailure { err ->
                _effects.emit(KycUploadEffect.ShowSnackbar("Upload failed: ${err.message}"))
            }
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }
}
