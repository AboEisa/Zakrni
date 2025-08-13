package com.example.zakrni.clean.data.di

import com.example.zakrni.clean.data.network.PrayerApiService
import com.example.zakrni.clean.data.remote.IRemoteDataSource
import com.example.zakrni.clean.data.remote.RemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object Module {



    @Singleton
    @Provides
    fun getRemoteDataSource(apiService: PrayerApiService): IRemoteDataSource {
        return RemoteDataSource(apiService)
    }

    @PrayerApi
    @Provides
    @Singleton
    fun providePrayerRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.aladhan.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    @PrayerApi
    @Provides
    @Singleton
    fun providePrayerApiService(@PrayerApi retrofit: Retrofit): PrayerApiService {
        return retrofit.create(PrayerApiService::class.java)
    }

}


