package com.navassist.android.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.navassist.android.data.local.db.converters.DateConverter
import com.navassist.android.data.local.db.converters.ListConverter
import com.navassist.android.data.local.db.dao.CachedUserDao
import com.navassist.android.data.local.db.dao.NotificationDao
import com.navassist.android.data.local.db.dao.SavedLocationDao
import com.navassist.android.data.local.db.dao.TripHistoryDao
import com.navassist.android.data.local.db.entity.CachedUserEntity
import com.navassist.android.data.local.db.entity.NotificationEntity
import com.navassist.android.data.local.db.entity.SavedLocationEntity
import com.navassist.android.data.local.db.entity.TripEntity

@Database(
    entities = [
        SavedLocationEntity::class,
        TripEntity::class,
        NotificationEntity::class,
        CachedUserEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class, ListConverter::class)
abstract class NavAssistDatabase : RoomDatabase() {
    abstract fun savedLocationDao(): SavedLocationDao
    abstract fun tripHistoryDao(): TripHistoryDao
    abstract fun notificationDao(): NotificationDao
    abstract fun cachedUserDao(): CachedUserDao
}
