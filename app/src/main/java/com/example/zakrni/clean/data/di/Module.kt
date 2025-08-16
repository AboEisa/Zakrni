package com.example.zakrni.clean.data.di

import android.content.Context
import com.example.zakrni.clean.data.Repo
import com.example.zakrni.clean.data.location.LocationManager
import com.example.zakrni.clean.data.network.PrayerApiService
import com.example.zakrni.clean.data.remote.IRemoteDataSource
import com.example.zakrni.clean.data.remote.RemoteDataSource
import com.example.zakrni.clean.domain.IRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object Module {

    @Provides
    @Singleton
    fun providePrayerRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.aladhan.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun providePrayerApiService(retrofit: Retrofit): PrayerApiService {
        return retrofit.create(PrayerApiService::class.java)
    }

    @Singleton
    @Provides
    fun getRemoteDataSource(apiService: PrayerApiService): IRemoteDataSource {
        return RemoteDataSource(apiService)
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


}