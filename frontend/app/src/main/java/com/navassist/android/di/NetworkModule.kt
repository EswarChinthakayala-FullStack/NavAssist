package com.navassist.android.di

import com.navassist.android.core.network.*
import com.navassist.android.data.remote.api.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        return NetworkClient.createOkHttpClient(authInterceptor, tokenAuthenticator)
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return NetworkClient.createRetrofit(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideAuthApi(factory: ApiServiceFactory): AuthApi = factory.createService(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideUsersApi(factory: ApiServiceFactory): UsersApi = factory.createService(UsersApi::class.java)

    @Provides
    @Singleton
    fun provideAssistantsApi(factory: ApiServiceFactory): AssistantsApi = factory.createService(AssistantsApi::class.java)

    @Provides
    @Singleton
    fun provideKycApi(factory: ApiServiceFactory): KycApi = factory.createService(KycApi::class.java)

    @Provides
    @Singleton
    fun provideBookingsApi(factory: ApiServiceFactory): BookingsApi = factory.createService(BookingsApi::class.java)

    @Provides
    @Singleton
    fun providePaymentsApi(factory: ApiServiceFactory): PaymentsApi = factory.createService(PaymentsApi::class.java)

    @Provides
    @Singleton
    fun provideWalletApi(factory: ApiServiceFactory): WalletApi = factory.createService(WalletApi::class.java)

    @Provides
    @Singleton
    fun provideMessageApi(factory: ApiServiceFactory): MessageApi = factory.createService(MessageApi::class.java)

    @Provides
    @Singleton
    fun provideSosApi(factory: ApiServiceFactory): SosApi = factory.createService(SosApi::class.java)

    @Provides
    @Singleton
    fun provideAdminApi(factory: ApiServiceFactory): AdminApi = factory.createService(AdminApi::class.java)
}
