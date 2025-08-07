package com.example.zakrni.clean.data.local

import com.example.zakrni.clean.data.network.ApiServices
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private val apiServices: ApiServices
): ILocalDataSource {



}