package com.navassist.android.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.core.session.SessionManager
import com.navassist.android.data.remote.api.UsersApi
import com.navassist.android.data.remote.dto.user.UpdateProfileRequestDto
import com.navassist.android.domain.model.AssistantProfileData
import com.navassist.android.domain.repository.AssistantRepository
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

data class UnifiedUserProfile(
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "PASSENGER",
    val isAssistant: Boolean = false,
    val avatarUrl: String? = null,
    // Assistant specific fields
    val bio: String? = null,
    val experienceYears: Int = 0,
    val serviceRadiusKm: Double = 10.0,
    val verificationStatus: String = "VERIFIED",
    val trustScore: Float = 98f,
    val rating: Float = 5.0f,
    val totalTrips: Int = 0,
    val profileCompletionPct: Int = 85
)

sealed interface ProfileEffect {
    data class ShowToast(val message: String) : ProfileEffect
    data class ShowSnackbar(val message: String) : ProfileEffect
}

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val usersApi: UsersApi,
    private val assistantRepository: AssistantRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _profileState = MutableStateFlow<UiState<UnifiedUserProfile>>(UiState.Loading)
    val profileState: StateFlow<UiState<UnifiedUserProfile>> = _profileState.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _effects = MutableSharedFlow<ProfileEffect>()
    val effects: SharedFlow<ProfileEffect> = _effects.asSharedFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        _profileState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val userDto = usersApi.getMyProfile()
                val isAssistant = userDto.role.uppercase() == "ASSISTANT" || sessionManager.userSession.value?.role?.uppercase() == "ASSISTANT"

                var assistantData: AssistantProfileData? = null
                if (isAssistant) {
                    val res = assistantRepository.getMyAssistantProfile()
                    res.onSuccess { assistantData = it }
                }

                val profile = UnifiedUserProfile(
                    userId = userDto.id.toString(),
                    fullName = userDto.fullName ?: "User",
                    email = userDto.email ?: "",
                    phone = userDto.phone,
                    role = userDto.role,
                    isAssistant = isAssistant,
                    avatarUrl = userDto.profilePhotoUrl ?: assistantData?.photoUrl,
                    bio = assistantData?.bio,
                    experienceYears = assistantData?.experienceYears ?: 0,
                    serviceRadiusKm = assistantData?.serviceRadiusKm ?: 10.0,
                    verificationStatus = assistantData?.verificationStatus ?: "VERIFIED",
                    trustScore = assistantData?.trustScore ?: 98f,
                    rating = assistantData?.rating ?: 5.0f,
                    totalTrips = assistantData?.totalTrips ?: 0,
                    profileCompletionPct = if (isAssistant) 85 else 100
                )

                _profileState.value = UiState.Success(profile)
            } catch (e: Exception) {
                _profileState.value = UiState.Error(e.message ?: "Failed to load user profile")
            }
        }
    }

    fun saveProfileChanges(
        fullName: String,
        email: String,
        bio: String? = null,
        experienceYears: Int? = null,
        serviceRadiusKm: Double? = null
    ) {
        val currentState = (_profileState.value as? UiState.Success)?.data ?: return
        _isSaving.value = true

        viewModelScope.launch {
            try {
                // Step 1: Update User profile
                val updateReq = UpdateProfileRequestDto(
                    fullName = fullName,
                    email = email
                )
                usersApi.updateMyProfile(updateReq)

                // Step 2: If Assistant, sequentially update Assistant profile
                if (currentState.isAssistant) {
                    val assistRes = assistantRepository.updateMyAssistantProfile(
                        bio = bio,
                        experienceYears = experienceYears,
                        serviceRadiusKm = serviceRadiusKm
                    )
                    if (assistRes.isFailure) {
                        _isSaving.value = false
                        _effects.emit(ProfileEffect.ShowSnackbar("Assistant profile update failed: ${assistRes.exceptionOrNull()?.message}"))
                        return@launch
                    }
                }

                _isSaving.value = false
                _effects.emit(ProfileEffect.ShowToast("Profile Updated Successfully ✓"))

                // Reload fresh state
                loadUserProfile()

            } catch (e: Exception) {
                _isSaving.value = false
                _effects.emit(ProfileEffect.ShowSnackbar("Failed to update profile: ${e.message}"))
            }
        }
    }
}
