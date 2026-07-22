package com.navassist.android.data.local.db.dao

import androidx.room.*
import com.navassist.android.data.local.db.entity.CachedUserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedUserDao {
    @Query("SELECT * FROM cached_users LIMIT 1")
    fun getCachedUser(): Flow<CachedUserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: CachedUserEntity)

    @Query("DELETE FROM cached_users")
    suspend fun clearUser()
}
