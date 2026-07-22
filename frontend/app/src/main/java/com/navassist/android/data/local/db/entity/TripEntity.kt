package com.navassist.android.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "guest_id") val guestId: Int,
    @ColumnInfo(name = "assistant_id") val assistantId: Int? = null,
    @ColumnInfo(name = "assistant_name") val assistantName: String? = null,
    @ColumnInfo(name = "assistant_photo") val assistantPhoto: String? = null,
    @ColumnInfo(name = "pickup_address") val pickupAddress: String,
    @ColumnInfo(name = "pickup_lat") val pickupLat: Double,
    @ColumnInfo(name = "pickup_lng") val pickupLng: Double,
    @ColumnInfo(name = "destination_address") val destinationAddress: String,
    @ColumnInfo(name = "dest_lat") val destLat: Double,
    @ColumnInfo(name = "dest_lng") val destLng: Double,
    @ColumnInfo(name = "fare_amount") val fareAmount: Double,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "created_at") val createdAt: String
)
