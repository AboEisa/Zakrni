package com.example.zakrni.clean.data.di

import android.content.Context
import com.example.zakrni.clean.data.Repo
import com.example.zakrni.clean.data.location.LocationManager
import com.example.zakrni.clean.data.network.AzkarApiService
import com.example.zakrni.clean.data.network.HadithApiService
import com.example.zakrni.clean.data.network.PrayerApiService
import com.example.zakrni.clean.data.remote.IRemoteDataSource
import com.example.zakrni.clean.data.remote.RemoteDataSource
import com.example.zakrni.clean.domain.IRepo
import com.example.zakrni.clean.ui.utils.NetworkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object Module {

    // Common OkHttp Client with logging
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Prayer API
    @Provides
    @Singleton
    @PrayerApi
    fun providePrayerRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://api.aladhan.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun providePrayerApiService(@PrayerApi retrofit: Retrofit): PrayerApiService {
        return retrofit.create(PrayerApiService::class.java)
    }

    // Hadith API
    @Provides
    @Singleton
    @HadithApi
    fun provideHadithRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://hadithapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideHadithApiService(@HadithApi retrofit: Retrofit): HadithApiService {
        return retrofit.create(HadithApiService::class.java)
    }


    @Provides
    @Singleton
    @DuaApi
    fun provideDuaRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://alquran.vip/APIs/") // Working API base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAzkarApiService(@DuaApi retrofit: Retrofit): AzkarApiService {
        return retrofit.create(AzkarApiService::class.java)
    }

    // Quran API
    @Provides
    @Singleton
    @QuranApi
    fun provideQuranRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://api.quran.com/v4/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun getRemoteDataSource(
        prayerApiService: PrayerApiService,
        hadithApiService: HadithApiService,
        azkarApiService: AzkarApiService
    ): IRemoteDataSource {
        return RemoteDataSource(prayerApiService, hadithApiService, azkarApiService)
    }

    @Singleton
    @Provides
    fun getRepository(remoteDataSource: IRemoteDataSource): IRepo {
        return Repo(remoteDataSource)
    }

    @Provides
    @Singleton
    fun provideLocationManager(@ApplicationContext context: Context): LocationManager {
        return LocationManager(context)
    }

    @Provides
    @Singleton
    fun provideNetworkManager(@ApplicationContext context: Context): NetworkManager {
        return NetworkManager(context)
    }
}