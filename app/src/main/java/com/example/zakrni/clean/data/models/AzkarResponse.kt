package com.example.zakrni.clean.data.models

data class AzkarResponse(
    val adhan_azkar: List<AdhanAzkar>,
    val evening_azkar: List<EveningAzkar>,
    val food_azkar: List<FoodAzkar>,
    val hajj_and_umrah_azkar: List<HajjAndUmrahAzkar>,
    val home_azkar: List<HomeAzkar>,
    val khala_azkar: List<KhalaAzkar>,
    val miscellaneous_azkar: List<MiscellaneousAzkar>,
    val morning_azkar: List<MorningAzkar>,
    val mosque_azkar: List<MosqueAzkar>,
    val prayer_azkar: List<PrayerAzkar>,
    val prayer_later_azkar: List<PrayerLaterAzkar>,
    val sleep_azkar: List<SleepAzkar>,
    val wake_up_azkar: List<WakeUpAzkar>,
    val wudu_azkar: List<WuduAzkar>
)

data class AdhanAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class EveningAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class FoodAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class HajjAndUmrahAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class HomeAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class KhalaAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class MiscellaneousAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class MorningAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class MosqueAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class PrayerAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class PrayerLaterAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class SleepAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class WakeUpAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class WuduAzkar(
    val count: Int,
    val id: Int,
    val text: String
)