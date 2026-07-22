package com.navassist.android.data.repository

import com.navassist.android.data.local.db.dao.SavedLocationDao
import com.navassist.android.data.local.db.entity.SavedLocationEntity
import com.navassist.android.data.remote.api.UsersApi
import com.navassist.android.data.remote.dto.user.EmergencyContactDto
import com.navassist.android.data.remote.dto.user.SavedLocationDto
import com.navassist.android.data.remote.dto.user.UpdateProfileRequestDto
import com.navassist.android.data.remote.dto.user.UserResponseDto
import com.navassist.android.domain.model.EmergencyContact
import com.navassist.android.domain.model.SavedLocation
import com.navassist.android.domain.model.User
import com.navassist.android.domain.model.UserRole
import com.navassist.android.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val usersApi: UsersApi,
    private val savedLocationDao: SavedLocationDao
) : UserRepository {

    override suspend fun getMyProfile(): Result<User> {
        return try {
            val dto = usersApi.getMyProfile()
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(fullName: String?, email: String?): Result<User> {
        return try {
            val dto = usersApi.updateMyProfile(UpdateProfileRequestDto(fullName = fullName, email = email))
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getSavedLocations(): Flow<List<SavedLocation>> {
        return savedLocationDao.getAllSavedLocations().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun refreshSavedLocations(): Result<Unit> {
        return try {
            val dtos = usersApi.getSavedLocations()
            savedLocationDao.clearAll()
            savedLocationDao.insertAll(dtos.map { it.toEntity() })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addSavedLocation(
        label: String,
        address: String,
        latitude: Double,
        longitude: Double
    ): Result<SavedLocation> {
        return try {
            val request = SavedLocationDto(
                label = label,
                address = address,
                latitude = latitude,
                longitude = longitude
            )
            val dto = usersApi.addSavedLocation(request)
            savedLocationDao.insertLocation(dto.toEntity())
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSavedLocation(id: Int): Result<Unit> {
        return try {
            usersApi.deleteSavedLocation(id)
            savedLocationDao.deleteById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getEmergencyContacts(): Result<List<EmergencyContact>> {
        return try {
            val dtos = usersApi.getEmergencyContacts()
            Result.success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addEmergencyContact(
        name: String,
        phone: String,
        relationship: String,
        isPrimary: Boolean
    ): Result<EmergencyContact> {
        return try {
            val dto = usersApi.addEmergencyContact(
                EmergencyContactDto(
                    name = name,
                    phone = phone,
                    relationship = relationship,
                    isPrimary = isPrimary
                )
            )
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteEmergencyContact(id: Int): Result<Unit> {
        return try {
            usersApi.deleteEmergencyContact(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun UserResponseDto.toDomain(): User {
    return User(
        id = id.toString(),
        fullName = fullName,
        email = email ?: "",
        phone = phone,
        role = if (role.uppercase() == "ASSISTANT") UserRole.ASSISTANT else UserRole.GUEST,
        profilePictureUrl = profilePhotoUrl,
        rating = 5.0f
    )
}

private fun SavedLocationEntity.toDomain(): SavedLocation {
    return SavedLocation(
        id = id,
        label = label,
        customLabel = customLabel,
        address = address,
        latitude = latitude,
        longitude = longitude,
        placeId = placeId
    )
}

private fun SavedLocationDto.toDomain(): SavedLocation {
    return SavedLocation(
        id = id ?: 0,
        label = label,
        customLabel = customLabel,
        address = address,
        latitude = latitude,
        longitude = longitude,
        placeId = placeId
    )
}

private fun SavedLocationDto.toEntity(): SavedLocationEntity {
    return SavedLocationEntity(
        id = id ?: 0,
        label = label,
        customLabel = customLabel,
        address = address,
        latitude = latitude,
        longitude = longitude,
        placeId = placeId,
        createdAt = System.currentTimeMillis().toString()
    )
}

private fun EmergencyContactDto.toDomain(): EmergencyContact {
    return EmergencyContact(
        id = id ?: 0,
        name = name,
        phone = phone,
        relationship = relationship ?: "Guardian",
        isPrimary = isPrimary ?: false
    )
}
