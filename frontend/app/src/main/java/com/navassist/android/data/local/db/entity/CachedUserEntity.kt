package com.navassist.android.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_users")
data class CachedUserEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "phone") val phone: String,
    @ColumnInfo(name = "email") val email: String? = null,
    @ColumnInfo(name = "role") val role: String,
    @ColumnInfo(name = "full_name") val fullName: String,
    @ColumnInfo(name = "profile_photo_url") val profilePhotoUrl: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: String
)
