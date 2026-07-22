package com.navassist.android.data.local.db.dao

import androidx.room.*
import com.navassist.android.data.local.db.entity.TripEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripHistoryDao {
    @Query("SELECT * FROM trips ORDER BY created_at DESC")
    fun getAllTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :id LIMIT 1")
    suspend fun getTripById(id: Int): TripEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrips(trips: List<TripEntity>)

    @Query("DELETE FROM trips")
    suspend fun clearAll()
}
