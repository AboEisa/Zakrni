package com.example.zakrni.clean.data

import com.example.zakrni.clean.data.local.ILocalDataSource
import com.example.zakrni.clean.data.models.mapToDomain
import com.example.zakrni.clean.data.network.PrayerApiService
import com.example.zakrni.clean.data.remote.IRemoteDataSource
import com.example.zakrni.clean.domain.IRepo
import com.example.zakrni.clean.domain.models.DomainPrayerTimesResponse
import javax.inject.Inject

class Repo @Inject constructor(
    private val remoteDataSource: IRemoteDataSource,
//    private val localDataSource: ILocalDataSource,

) : IRepo{
    override suspend fun getPrayerTimes(
        latitude: Double,
        longitude: Double
    ): Result<DomainPrayerTimesResponse> {
       return try {
           val data = remoteDataSource.getPrayerTimes(latitude, longitude)
           Result.success(data.getOrThrow().mapToDomain())
       }catch ( e: Exception) {
           Result.failure(e)
       }

    }




}