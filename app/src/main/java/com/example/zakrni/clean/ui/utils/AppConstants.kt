package com.example.zakrni.clean.ui.utils

object AppConstants {

    // App Information
    const val APP_NAME = "Zakrni - ذكرني"
    const val APP_PACKAGE = "com.example.zakrni"

    // Prayer Times
    const val DEFAULT_METHOD = 5
    const val DEFAULT_SCHOOL = 0
    const val DEFAULT_LATITUDE_ADJUSTMENT = 0
    const val DEFAULT_MIDNIGHTMODE = 0
    const val DEFAULT_TIMEZONESTRING = "auto"

    // Notification Settings
    const val PRAYER_NOTIFICATION_DELAY = 10 * 60 * 1000L
    const val AUTO_SWIPE_DELAY = 10 * 1000L
    const val COUNTDOWN_UPDATE_INTERVAL = 1000L

    // Network Settings
    const val NETWORK_TIMEOUT = 30000L // 30 seconds
    const val RETRY_ATTEMPTS = 3
    const val CACHE_EXPIRY = 24 * 60 * 60 * 1000L // 24 hours

    // Location Settings
    const val LOCATION_UPDATE_INTERVAL = 60 * 60 * 1000L // 1 hour
    const val LOCATION_ACCURACY_THRESHOLD = 100.0 // meters

    // SharedPreferences Keys
    object PreferenceKeys {
        const val PRAYER_TIMES = "prayer_times"
        const val LOCATION_DATA = "location_data"
        const val USER_SETTINGS = "user_settings"
        const val NOTIFICATION_SETTINGS = "notification_settings"
        const val QURAN_DATA = "quran_data"

        // Specific keys
        const val IS_FIRST_LAUNCH = "is_first_launch"
        const val LAST_UPDATE_TIME = "last_update_time"
        const val SELECTED_METHOD = "selected_method"
        const val NOTIFICATION_ENABLED = "notification_enabled"
        const val LOCATION_PERMISSION_REQUESTED = "location_permission_requested"
        const val AUTO_SWIPE_ENABLED = "auto_swipe_enabled"
    }

    // API Endpoints
    object ApiEndpoints {
        const val BASE_URL = "https://api.aladhan.com/v1/"
        const val PRAYER_TIMES = "timings"
        const val QIBLA_DIRECTION = "qibla"
        const val CALENDAR = "calendar"
        const val METHODS = "methods"
    }

    // Error Messages
    object ErrorMessages {
        const val NETWORK_ERROR = "خطأ في الاتصال بالإنترنت"
        const val LOCATION_ERROR = "خطأ في تحديد الموقع"
        const val PERMISSION_ERROR = "الرجاء منح الصلاحيات المطلوبة"
        const val DATA_ERROR = "خطأ في تحميل البيانات"
        const val UNKNOWN_ERROR = "خطأ غير معروف"
    }

    // Success Messages
    object SuccessMessages {
        const val DATA_LOADED = "تم تحميل البيانات بنجاح"
        const val NOTIFICATION_SCHEDULED = "تم جدولة التنبيهات"
        const val SETTINGS_SAVED = "تم حفظ الإعدادات"
        const val LOCATION_UPDATED = "تم تحديث الموقع"
    }

    // Animation Durations
    object AnimationDurations {
        const val SHORT = 150L
        const val MEDIUM = 300L
        const val LONG = 500L
        const val EXTRA_LONG = 800L
    }

    // Request Codes
    object RequestCodes {
        const val LOCATION_PERMISSION = 1001
        const val NOTIFICATION_PERMISSION = 1002
        const val EXACT_ALARM_PERMISSION = 1003
        const val OVERLAY_PERMISSION = 1004
    }

    // Prayer Names
    object PrayerNames {
        val NAMES_ENGLISH = listOf("Fajr", "Sunrise", "Dhuhr", "Asr", "Sunset", "Maghrib", "Isha")
        val NAMES_ARABIC = listOf("الفجر", "الشروق", "الظهر", "العصر", "المغرب", "المغرب", "العشاء")

        fun getArabicName(englishName: String): String {
            val index = NAMES_ENGLISH.indexOf(englishName)
            return if (index != -1) NAMES_ARABIC[index] else englishName
        }
    }

    // Notification IDs
    object NotificationIds {
        const val PRAYER_ALERT_BASE = 2000
        const val COUNTDOWN_BASE = 3000
        const val OFFLINE_STATUS = 4000
        const val GENERAL_BASE = 5000

        fun getPrayerAlertId(prayerName: String): Int {
            return PRAYER_ALERT_BASE + prayerName.hashCode()
        }

        fun getCountdownId(prayerName: String): Int {
            return COUNTDOWN_BASE + prayerName.hashCode()
        }
    }

    // File Names
    object FileNames {
        const val DATABASE_NAME = "zakrni_database"
        const val CACHE_DIRECTORY = "zakrni_cache"
        const val LOG_FILE = "zakrni_log.txt"
        const val EXPORT_FILE = "zakrni_export.json"
    }
}