package com.zakrni.app.clean.data.network

import com.zakrni.app.clean.data.models.AzkarResponse
import retrofit2.http.GET

interface AzkarApiService {

    // أذكار الصباح
    @GET("azkar_sabah.json")
    suspend fun getMorningAzkar(): AzkarResponse

    // أذكار المساء
    @GET("azkar_massa.json")
    suspend fun getEveningAzkar(): AzkarResponse

    // بعد الصلاة
    @GET("PostPrayer_azkar.json")
    suspend fun getAfterPrayerAzkar(): AzkarResponse

    // النوم
    @GET("azkar_noom.json")
    suspend fun getSleepAzkar(): AzkarResponse

    // الاستيقاظ
    @GET("azkar_wake.json")
    suspend fun getWakeAzkar(): AzkarResponse

    // المسجد
    @GET("azkar_mosque.json")
    suspend fun getMosqueAzkar(): AzkarResponse

    // الطعام
    @GET("azkar_eating.json")
    suspend fun getEatingAzkar(): AzkarResponse

    // متفرقة
    @GET("azkar_misc.json")
    suspend fun getMiscAzkar(): AzkarResponse
}
