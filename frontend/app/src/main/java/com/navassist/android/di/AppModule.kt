package com.navassist.android.di

import android.content.Context
import com.navassist.android.core.session.SessionManager
import com.navassist.android.data.preferences.SessionDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSessionDataStore(@ApplicationContext context: Context): SessionDataStore {
        return SessionDataStore(context)
    }

    @Provides
    @Singleton
    fun provideSessionManager(
        sessionDataStore: SessionDataStore,
        sessionValidator: com.navassist.android.core.session.SessionValidator
    ): SessionManager {
        return SessionManager(sessionDataStore, sessionValidator)
    }
}
