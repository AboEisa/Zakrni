package com.example.zakrni.clean.data

import com.example.zakrni.clean.data.network.ApiServices
import com.example.zakrni.clean.data.remote.IRemoteDataSource
import com.example.zakrni.clean.data.remote.RemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object Module {


    @Provides
    @Singleton
    fun apiServices(retrofit: Retrofit) : ApiServices{
        return retrofit.create(ApiServices::class.java)
    }

    @Singleton
    @Provides
    fun getRemoteDataSource(apiService: ApiServices): IRemoteDataSource{
        return RemoteDataSource(apiService)
    }
}