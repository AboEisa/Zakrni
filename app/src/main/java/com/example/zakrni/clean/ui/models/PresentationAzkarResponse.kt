package com.example.zakrni.clean.ui.models

data class PresentationAzkarResponse(
    val adhan_azkar: List<PresentationAdhanAzkar>,
    val evening_azkar: List<PresentationEveningAzkar>,
    val food_azkar: List<PresentationFoodAzkar>,
    val hajj_and_umrah_azkar: List<PresentationHajjAndUmrahAzkar>,
    val home_azkar: List<PresentationHomeAzkar>,
    val khala_azkar: List<PresentationKhalaAzkar>,
    val miscellaneous_azkar: List<PresentationMiscellaneousAzkar>,
    val morning_azkar: List<PresentationMorningAzkar>,
    val mosque_azkar: List<PresentationMosqueAzkar>,
    val prayer_azkar: List<PresentationPrayerAzkar>,
    val prayer_later_azkar: List<PresentationPrayerLaterAzkar>,
    val sleep_azkar: List<PresentationSleepAzkar>,
    val wake_up_azkar: List<PresentationWakeUpAzkar>,
    val wudu_azkar: List<PresentationWuduAzkar>
)

data class PresentationAdhanAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class PresentationEveningAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class PresentationFoodAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class PresentationHajjAndUmrahAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class PresentationHomeAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class PresentationKhalaAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class PresentationMiscellaneousAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class PresentationMorningAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class PresentationMosqueAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class PresentationPrayerAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class PresentationPrayerLaterAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class PresentationSleepAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class PresentationWakeUpAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class PresentationWuduAzkar(
    val count: Int,
    val id: Int,
    val text: String
)