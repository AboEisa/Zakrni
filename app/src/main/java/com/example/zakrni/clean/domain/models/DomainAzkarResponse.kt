package com.example.zakrni.clean.domain.models

data class DomainAzkarResponse(
    val adhan_azkar: List<DomainAdhanAzkar>,
    val evening_azkar: List<DomainEveningAzkar>,
    val food_azkar: List<DomainFoodAzkar>,
    val hajj_and_umrah_azkar: List<DomainHajjAndUmrahAzkar>,
    val home_azkar: List<DomainHomeAzkar>,
    val khala_azkar: List<DomainKhalaAzkar>,
    val miscellaneous_azkar: List<DomainMiscellaneousAzkar>,
    val morning_azkar: List<DomainMorningAzkar>,
    val mosque_azkar: List<DomainMosqueAzkar>,
    val prayer_azkar: List<DomainPrayerAzkar>,
    val prayer_later_azkar: List<DomainPrayerLaterAzkar>,
    val sleep_azkar: List<DomainSleepAzkar>,
    val wake_up_azkar: List<DomainWakeUpAzkar>,
    val wudu_azkar: List<DomainWuduAzkar>
)

data class DomainAdhanAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class DomainEveningAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class DomainFoodAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class DomainHajjAndUmrahAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class DomainHomeAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class DomainKhalaAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class DomainMiscellaneousAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class DomainMorningAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class DomainMosqueAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class DomainPrayerAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class DomainPrayerLaterAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class DomainSleepAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class DomainWakeUpAzkar(
    val count: Int,
    val id: Int,
    val text: String
)

data class DomainWuduAzkar(
    val count: Int,
    val id: Int,
    val text: String
)