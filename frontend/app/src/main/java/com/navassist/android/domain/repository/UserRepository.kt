package com.navassist.android.domain.repository

import com.navassist.android.domain.model.EmergencyContact
import com.navassist.android.domain.model.SavedLocation
import com.navassist.android.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getMyProfile(): Result<User>
    suspend fun updateProfile(fullName: String?, email: String?): Result<User>
    fun getSavedLocations(): Flow<List<SavedLocation>>
    suspend fun refreshSavedLocations(): Result<Unit>
    suspend fun addSavedLocation(label: String, address: String, latitude: Double, longitude: Double): Result<SavedLocation>
    suspend fun deleteSavedLocation(id: Int): Result<Unit>
    suspend fun getEmergencyContacts(): Result<List<EmergencyContact>>
    suspend fun addEmergencyContact(name: String, phone: String, relationship: String = "Guardian", isPrimary: Boolean = false): Result<EmergencyContact>
    suspend fun deleteEmergencyContact(id: Int): Result<Unit>
}
