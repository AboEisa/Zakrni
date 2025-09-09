package com.example.zakrni.clean.data.di

import android.content.Context
import androidx.room.Room
import com.example.zakrni.clean.data.Repo
import com.example.zakrni.clean.data.local.ILocalDataSource
import com.example.zakrni.clean.data.local.LocalDataSource
import com.example.zakrni.clean.data.local.QuranDatabase
import com.example.zakrni.clean.data.local.QuranDao
import com.example.zakrni.clean.data.location.LocationManager
import com.example.zakrni.clean.data.network.AzkarApiService
import com.example.zakrni.clean.data.network.HadithApiService
import com.example.zakrni.clean.data.network.PrayerApiService
import com.example.zakrni.clean.data.network.QuranApiService
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
import javax.inject.Named
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
    @Named("PrayerApi")
    fun providePrayerRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://api.aladhan.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun providePrayerApiService(@Named("PrayerApi") retrofit: Retrofit): PrayerApiService {
        return retrofit.create(PrayerApiService::class.java)
    }

    // Hadith API
    @Provides
    @Singleton
    @Named("HadithApi")
    fun provideHadithRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://hadithapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideHadithApiService(@Named("HadithApi") retrofit: Retrofit): HadithApiService {
        return retrofit.create(HadithApiService::class.java)
    }

    @Provides
    @Singleton
    @Named("DuaApi")
    fun provideDuaRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://ahegazy.github.io/muslimKit/json/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAzkarApiService(@Named("DuaApi") retrofit: Retrofit): AzkarApiService {
        return retrofit.create(AzkarApiService::class.java)
    }

    @Provides
    @Singleton
    @Named("QuranApi")
    fun provideQuranRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://api.alquran.cloud/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideQuranApiService(@Named("QuranApi") retrofit: Retrofit): QuranApiService {
        return retrofit.create(QuranApiService::class.java)
    }

    @Singleton
    @Provides
    fun getRemoteDataSource(
        prayerApiService: PrayerApiService,
        hadithApiService: HadithApiService,
        azkarApiService: AzkarApiService,
        quranApiService: QuranApiService
    ): IRemoteDataSource {
        return RemoteDataSource(prayerApiService, hadithApiService, azkarApiService, quranApiService)
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

    @Provides
    @Singleton
    fun provideQuranDatabase(@ApplicationContext context: Context): QuranDatabase {
        return Room.databaseBuilder(context, QuranDatabase::class.java, "QuranDatabase")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideQuranDao(database: QuranDatabase): QuranDao {
        return database.quranDao()
    }

    @Singleton
    @Provides
    fun getLocalDataSource(
        quranDao: QuranDao
    ): ILocalDataSource {
        return LocalDataSource(quranDao)
    }

    @Singleton
    @Provides
    fun getRepository(
        remoteDataSource: IRemoteDataSource,
        localDataSource: ILocalDataSource
    ): IRepo {
        return Repo(remoteDataSource, localDataSource)
    }
}