package com.navassist.android.di

import android.content.Context
import androidx.room.Room
import com.navassist.android.data.local.db.NavAssistDatabase
import com.navassist.android.data.local.db.dao.CachedUserDao
import com.navassist.android.data.local.db.dao.NotificationDao
import com.navassist.android.data.local.db.dao.SavedLocationDao
import com.navassist.android.data.local.db.dao.TripHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NavAssistDatabase {
        return Room.databaseBuilder(
            context,
            NavAssistDatabase::class.java,
            "navassist_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideSavedLocationDao(database: NavAssistDatabase): SavedLocationDao {
        return database.savedLocationDao()
    }

    @Provides
    @Singleton
    fun provideTripHistoryDao(database: NavAssistDatabase): TripHistoryDao {
        return database.tripHistoryDao()
    }

    @Provides
    @Singleton
    fun provideNotificationDao(database: NavAssistDatabase): NotificationDao {
        return database.notificationDao()
    }

    @Provides
    @Singleton
    fun provideCachedUserDao(database: NavAssistDatabase): CachedUserDao {
        return database.cachedUserDao()
    }
}
