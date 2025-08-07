package com.example.zakrni.clean.data

import com.example.zakrni.clean.data.local.ILocalDataSource
import com.example.zakrni.clean.data.network.ApiServices
import com.example.zakrni.clean.data.remote.IRemoteDataSource
import com.example.zakrni.clean.domain.IRepo
import javax.inject.Inject

class Repo @Inject constructor(
    private val remoteDataSource: IRemoteDataSource,
    private val localDataSource: ILocalDataSource,
    private val apiServices: ApiServices
) : IRepo{



}