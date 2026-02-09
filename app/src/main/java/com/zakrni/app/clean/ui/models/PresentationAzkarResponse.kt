package com.zakrni.app.clean.ui.models

// Generic azkar item that works with all categories
data class PresentationAzkar(
    val text: String,        // maps from Content.zekr
    val count: Int,          // maps from Content.repeat
    val bless: String        // maps from Content.bless
)

// Updated PresentationAzkarResponse to match your adapter expectations
data class PresentationAzkarResponse(
    val morning_azkar: List<PresentationMorningAzkar> = emptyList(),
    val evening_azkar: List<PresentationEveningAzkar> = emptyList(),
    val sleep_azkar: List<PresentationSleepAzkar> = emptyList(),
    val wake_up_azkar: List<PresentationWakeUpAzkar> = emptyList(),
    val prayer_azkar: List<PresentationPrayerAzkar> = emptyList(),
    val food_azkar: List<PresentationFoodAzkar> = emptyList(),
    val wudu_azkar: List<PresentationWuduAzkar> = emptyList(),
    val adhan_azkar: List<PresentationAdhanAzkar> = emptyList(),
    val mosque_azkar: List<PresentationMosqueAzkar> = emptyList(),
    val home_azkar: List<PresentationHomeAzkar> = emptyList(),
    val miscellaneous_azkar: List<PresentationMiscellaneousAzkar> = emptyList(),
    val hajj_and_umrah_azkar: List<PresentationHajjAndUmrahAzkar> = emptyList(),
    val khala_azkar: List<PresentationKhalaAzkar> = emptyList(),
    val prayer_later_azkar: List<PresentationPrayerLaterAzkar> = emptyList()
)

// Type aliases to maintain adapter compatibility
typealias PresentationMorningAzkar = PresentationAzkar
typealias PresentationEveningAzkar = PresentationAzkar
typealias PresentationSleepAzkar = PresentationAzkar
typealias PresentationWakeUpAzkar = PresentationAzkar
typealias PresentationPrayerAzkar = PresentationAzkar
typealias PresentationFoodAzkar = PresentationAzkar
typealias PresentationWuduAzkar = PresentationAzkar
typealias PresentationAdhanAzkar = PresentationAzkar
typealias PresentationMosqueAzkar = PresentationAzkar
typealias PresentationHomeAzkar = PresentationAzkar
typealias PresentationMiscellaneousAzkar = PresentationAzkar
typealias PresentationHajjAndUmrahAzkar = PresentationAzkar
typealias PresentationKhalaAzkar = PresentationAzkar
typealias PresentationPrayerLaterAzkar = PresentationAzkar