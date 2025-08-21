package com.example.zakrni.clean.data.di

import android.content.Context
import com.example.zakrni.clean.data.Repo
import com.example.zakrni.clean.data.location.LocationManager
import com.example.zakrni.clean.data.network.HadithApiService
import com.example.zakrni.clean.data.network.PrayerApiService
import com.example.zakrni.clean.data.remote.IRemoteDataSource
import com.example.zakrni.clean.data.remote.RemoteDataSource
import com.example.zakrni.clean.domain.IRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object Module {

    // Prayer API
    @Provides
    @Singleton
    @PrayerApi
    fun providePrayerRetrofit(): Retrofit {
        return Retrofit.Builder()
            .client(OkHttpClient.Builder().build())
            .baseUrl("https://api.aladhan.com/") // الصلاة + أسماء الله الحسنى
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun providePrayerApiService(@PrayerApi retrofit: Retrofit): PrayerApiService {
        return retrofit.create(PrayerApiService::class.java)
    }

    // ✅ Retrofit for Hadith API
    @Provides
    @Singleton
    @HadithApi
    fun provideHadithRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://hadithapi.com/") // الأحاديث
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideHadithApiService(@HadithApi retrofit: Retrofit): HadithApiService {
        return retrofit.create(HadithApiService::class.java)
    }



    //  Dua & Dhikr API
    @Provides
    @Singleton
    @DuaApi
    fun provideDuaRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://dua-dhikr.vercel.app/") // أدعية + أذكار
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    //  Quran API
    @Provides
    @Singleton
    @QuranApi
    fun provideQuranRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.quran.com/v4/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun getRemoteDataSource(apiService: PrayerApiService,apiService2: HadithApiService): IRemoteDataSource {
        return RemoteDataSource(apiService,apiService2)
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