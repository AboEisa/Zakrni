package com.example.zakrni.clean.data.remote

import com.example.zakrni.clean.data.network.ApiServices
import javax.inject.Inject

class RemoteDataSource @Inject constructor(private val apiServices: ApiServices): IRemoteDataSource {
}