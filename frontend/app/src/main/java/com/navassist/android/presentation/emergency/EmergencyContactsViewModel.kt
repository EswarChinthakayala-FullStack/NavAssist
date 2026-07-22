package com.navassist.android.presentation.emergency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.domain.model.EmergencyContact
import com.navassist.android.domain.repository.UserRepository
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

sealed interface EmergencyEffect {
    data class ShowToast(val message: String) : EmergencyEffect
    data class ShowSnackbar(val message: String) : EmergencyEffect
}

@HiltViewModel
class EmergencyContactsViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _contactsState = MutableStateFlow<UiState<List<EmergencyContact>>>(UiState.Loading)
    val contactsState: StateFlow<UiState<List<EmergencyContact>>> = _contactsState.asStateFlow()

    private var allContacts: List<EmergencyContact> = emptyList()

    private val _effects = MutableSharedFlow<EmergencyEffect>()
    val effects: SharedFlow<EmergencyEffect> = _effects.asSharedFlow()

    init {
        loadEmergencyContacts()
    }

    fun loadEmergencyContacts() {
        _contactsState.value = UiState.Loading
        viewModelScope.launch {
            val result = userRepository.getEmergencyContacts()
            result.onSuccess { list ->
                allContacts = list
                _contactsState.value = UiState.Success(list)
            }.onFailure { err ->
                _contactsState.value = UiState.Error(err.message ?: "Failed to load emergency contacts")
            }
        }
    }

    fun filterContacts(query: String?) {
        if (query.isNullOrBlank()) {
            _contactsState.value = UiState.Success(allContacts)
            return
        }
        val q = query.lowercase().trim()
        val filtered = allContacts.filter {
            it.name.lowercase().contains(q) || it.phone.contains(q) || it.relationship.lowercase().contains(q)
        }
        _contactsState.value = UiState.Success(filtered)
    }

    fun addContact(name: String, phone: String, relationship: String, isPrimary: Boolean) {
        viewModelScope.launch {
            _effects.emit(EmergencyEffect.ShowToast("Saving emergency contact..."))
            val res = userRepository.addEmergencyContact(name, phone, relationship, isPrimary)
            res.onSuccess {
                _effects.emit(EmergencyEffect.ShowToast("Emergency contact saved ✓"))
                loadEmergencyContacts()
            }.onFailure { err ->
                _effects.emit(EmergencyEffect.ShowSnackbar("Failed to add contact: ${err.message}"))
            }
        }
    }

    fun deleteContact(contactId: Int) {
        viewModelScope.launch {
            val res = userRepository.deleteEmergencyContact(contactId)
            res.onSuccess {
                _effects.emit(EmergencyEffect.ShowToast("Emergency contact removed"))
                loadEmergencyContacts()
            }.onFailure { err ->
                _effects.emit(EmergencyEffect.ShowSnackbar("Failed to delete contact: ${err.message}"))
            }
        }
    }

    private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
}
