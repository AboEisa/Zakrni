package com.zakrni.app.clean.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zakrni.app.clean.data.location.LocationManager
import com.zakrni.app.clean.domain.usecases.GetPrayerTimesUseCase
import com.zakrni.app.clean.service.PrayerAlarmManager
import com.zakrni.app.clean.service.PrayerNotificationService
import com.zakrni.app.clean.ui.models.PresentationPrayerTimesResponse
import com.zakrni.app.clean.ui.models.mapToPresentation
import com.zakrni.app.clean.ui.utils.NetworkManager
import com.zakrni.app.clean.ui.utils.PrayerCalcSettings
import com.zakrni.app.clean.ui.utils.PrayerStorageManager
import com.zakrni.app.clean.ui.utils.PrayerTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrayerTimesViewModel @Inject constructor(
    private val prayerTimesUseCase: GetPrayerTimesUseCase,
    private val locationManager: LocationManager,
    @ApplicationContext private val context: Context,
    private val networkManager: NetworkManager,
    private val prayerStorageManager: PrayerStorageManager
) : ViewModel() {

    private val _prayerTimes = MutableStateFlow<PresentationPrayerTimesResponse?>(null)
    val prayerTimes: StateFlow<PresentationPrayerTimesResponse?> get() = _prayerTimes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    private val _currentPrayer = MutableStateFlow<PrayerTimeUtils.PrayerInfo?>(null)
    val currentPrayer: StateFlow<PrayerTimeUtils.PrayerInfo?> get() = _currentPrayer

    private val _nextPrayer = MutableStateFlow<PrayerTimeUtils.PrayerInfo?>(null)
    val nextPrayer: StateFlow<PrayerTimeUtils.PrayerInfo?> get() = _nextPrayer

    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission: StateFlow<Boolean> get() = _hasLocationPermission

    private val _remainingTime = MutableStateFlow("")
    val remainingTime: StateFlow<String> get() = _remainingTime

    private val _locationName = MutableStateFlow(localizedText("جاري تحديد الموقع...", "Detecting location..."))
    val locationName: StateFlow<String> get() = _locationName

    private val _hasExactAlarmPermission = MutableStateFlow(true)
    val hasExactAlarmPermission: StateFlow<Boolean> get() = _hasExactAlarmPermission

    private val _hasFullScreenIntentPermission = MutableStateFlow(true)
    val hasFullScreenIntentPermission: StateFlow<Boolean> get() = _hasFullScreenIntentPermission

    private var timerJob: Job? = null
    private var locationRetryCount = 0
    private val maxLocationRetries = 3
    private var isLoadingPrayerTimes = false

    companion object {
        private const val TAG = "PrayerTimesVM"
    }

    // Initialize AlarmManager with PrayerStorageManager
    private val prayerAlarmManager = PrayerAlarmManager(context, networkManager, prayerStorageManager)

    init {
        checkLocationPermission()
        checkExactAlarmPermission()
        checkFullScreenIntentPermission()
    }

    fun checkLocationPermission() {
        _hasLocationPermission.value = locationManager.hasLocationPermission()
        Log.d(TAG, "📍 Location permission: ${_hasLocationPermission.value}")
        if (_hasLocationPermission.value) {
            loadPrayerTimes()
        }
    }

    fun checkExactAlarmPermission() {
        _hasExactAlarmPermission.value = prayerAlarmManager.hasExactAlarmPermission()
    }

    fun checkFullScreenIntentPermission() {
        _hasFullScreenIntentPermission.value = prayerAlarmManager.hasFullScreenIntentPermission()
    }

    fun requestFullScreenIntentPermission() {
        prayerAlarmManager.requestFullScreenIntentPermission()
    }

    fun loadPrayerTimes() {
        // Don't re-fetch if already loaded or currently loading
        if (_prayerTimes.value != null) {
            Log.d(TAG, "✅ Prayer times already loaded, skipping fetch")
            return
        }
        if (isLoadingPrayerTimes) {
            Log.d(TAG, "⏳ Already loading prayer times, skipping")
            return
        }

        if (!locationManager.hasLocationPermission()) {
            _error.value = localizedText("صلاحية الموقع مطلوبة", "Location permission required")
            Log.w(TAG, "❌ No location permission")
            return
        }

        viewModelScope.launch {
            isLoadingPrayerTimes = true
            _isLoading.value = true
            _error.value = null
            _locationName.value = localizedText("جاري تحديد الموقع...", "Detecting location...")

            try {
                Log.d(TAG, "📍 Getting current location...")
                var locationInfo = locationManager.getCurrentLocationWithName()
                
                // Retry logic if location fails
                while (locationInfo == null && locationRetryCount < maxLocationRetries) {
                    locationRetryCount++
                    Log.d(TAG, "📍 Location retry $locationRetryCount/$maxLocationRetries")
                    _locationName.value = localizedText(
                        "جاري المحاولة مرة أخرى... (${locationRetryCount}/${maxLocationRetries})",
                        "Retrying location... (${locationRetryCount}/${maxLocationRetries})"
                    )
                    delay(1500) // Wait before retry
                    locationInfo = locationManager.getCurrentLocationWithName()
                }
                
                if (locationInfo == null) {
                    Log.w(TAG, "❌ Failed to get location after $maxLocationRetries retries")
                    // Try saved prayer times as fallback (no validity check - old times are better than nothing)
                    val savedTimes = prayerStorageManager.getSavedPrayerTimes()
                    if (savedTimes != null) {
                        Log.d(TAG, "📦 Using saved prayer times as fallback")
                        _locationName.value = localizedText("آخر موقع محفوظ", "Last saved location")
                        updatePrayerInfoFromSaved(savedTimes)
                    } else {
                        _error.value = localizedText(
                            "تعذر تحديد الموقع، تأكد من تفعيل GPS",
                            "Unable to detect location. Please enable GPS."
                        )
                        _locationName.value = localizedText("فشل تحديد الموقع", "Location detection failed")
                    }
                    locationRetryCount = 0 // Reset for next attempt
                    return@launch
                }
                
                locationRetryCount = 0 // Reset on success
                Log.d(TAG, "📍 Location found: ${locationInfo.displayName} (${locationInfo.latitude}, ${locationInfo.longitude})")
                _locationName.value = locationInfo.displayName
                getPrayerTimes(locationInfo.latitude, locationInfo.longitude)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading prayer times", e)
                _error.value = e.message ?: localizedText("خطأ غير معروف", "Unknown error")
            } finally {
                _isLoading.value = false
                isLoadingPrayerTimes = false
            }
        }
    }

    /** Forces a fresh fetch (e.g. after the user changes the calculation method/madhab). */
    fun reloadPrayerTimes() {
        _prayerTimes.value = null
        loadPrayerTimes()
    }

    private suspend fun getPrayerTimes(latitude: Double, longitude: Double) {
        try {
            val method = PrayerCalcSettings.method(context)
            val school = PrayerCalcSettings.school(context)
            Log.d(TAG, "🌐 Fetching prayer times from API for ($latitude, $longitude) method=$method school=$school...")
            val result = prayerTimesUseCase(latitude, longitude, method, school)
            if (result.isSuccess) {
                val presentationData = result.getOrThrow().mapToPresentation()
                _prayerTimes.value = presentationData
                Log.d(TAG, "✅ Prayer times loaded: Fajr=${presentationData.data.timings.Fajr}, " +
                    "Dhuhr=${presentationData.data.timings.Dhuhr}, Asr=${presentationData.data.timings.Asr}, " +
                    "Maghrib=${presentationData.data.timings.Maghrib}, Isha=${presentationData.data.timings.Isha}")

                updatePrayerInfo(presentationData)
                schedulePrayerAlarms(presentationData)
            } else {
                // API failed — try saved prayer times as fallback (offline mode)
                val savedTimes = prayerStorageManager.getSavedPrayerTimes()
                if (savedTimes != null) {
                    Log.d(TAG, "📦 API failed, using saved prayer times (offline fallback)")
                    _locationName.value = localizedText(
                        "بدون إنترنت - أوقات محفوظة",
                        "Offline - using saved prayer times"
                    )
                    updatePrayerInfoFromSaved(savedTimes)
                } else {
                    val errorMsg = result.exceptionOrNull()?.message
                        ?: localizedText("خطأ غير معروف", "Unknown error")
                    Log.e(TAG, "❌ API error: $errorMsg")
                    _error.value = errorMsg
                }
            }
        } catch (e: Exception) {
            // Exception — try saved prayer times as fallback (offline mode)
            val savedTimes = prayerStorageManager.getSavedPrayerTimes()
            if (savedTimes != null) {
                Log.d(TAG, "📦 Exception occurred, using saved prayer times (offline fallback)")
                _locationName.value = localizedText(
                    "بدون إنترنت - أوقات محفوظة",
                    "Offline - using saved prayer times"
                )
                updatePrayerInfoFromSaved(savedTimes)
            } else {
                Log.e(TAG, "❌ Exception in getPrayerTimes", e)
                _error.value = e.message ?: localizedText("خطأ غير معروف", "Unknown error")
            }
        }
    }

    private fun updatePrayerInfoFromSaved(timings: com.zakrni.app.clean.ui.models.PresentationTimings) {
        val (current, next) = PrayerTimeUtils.getCurrentAndNextPrayer(timings)
        _currentPrayer.value = current
        _nextPrayer.value = next

        schedulePrayerAlarmsFromSaved(timings)

        next?.let { nextPrayerInfo ->
            startNotificationService(nextPrayerInfo)
            startCountdownTimer(nextPrayerInfo.timeRemainingInSeconds)
        }
    }

    private fun updatePrayerInfo(presentationData: PresentationPrayerTimesResponse) {
        val (current, next) = PrayerTimeUtils.getCurrentAndNextPrayer(presentationData.data.timings)
        _currentPrayer.value = current
        _nextPrayer.value = next

        next?.let { nextPrayerInfo ->
            startNotificationService(nextPrayerInfo)
            startCountdownTimer(nextPrayerInfo.timeRemainingInSeconds)
        }
    }

    private fun schedulePrayerAlarms(presentationData: PresentationPrayerTimesResponse) {
        try {
            prayerAlarmManager.scheduleAllPrayerAlarms(presentationData.data.timings)

            // Prayer times are now saved automatically in PrayerAlarmManager via PrayerStorageManager

        } catch (e: Exception) {
            _error.value = localizedText(
                "فشل جدولة تنبيهات الصلاة: ${e.message}",
                "Failed to schedule prayer alarms: ${e.message}"
            )
        }
    }

    private fun schedulePrayerAlarmsFromSaved(timings: com.zakrni.app.clean.ui.models.PresentationTimings) {
        try {
            prayerAlarmManager.scheduleAllPrayerAlarms(timings)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to schedule alarms from saved timings", e)
        }
    }

    private fun startNotificationService(nextPrayerInfo: PrayerTimeUtils.PrayerInfo) {
        PrayerNotificationService.startService(
            context = context,
            prayerName = nextPrayerInfo.name,
            prayerNameArabic = nextPrayerInfo.nameArabic,
            prayerTime = nextPrayerInfo.time,
            remainingSeconds = nextPrayerInfo.timeRemainingInSeconds
        )
    }

    private fun startCountdownTimer(remainingSeconds: Long) {
        timerJob?.cancel()
        // Calculate the exact target time from wall clock
        val targetTimeMillis = System.currentTimeMillis() + (remainingSeconds * 1000)

        timerJob = viewModelScope.launch {
            while (true) {
                // Always recalculate from wall clock — never drift
                val remaining = ((targetTimeMillis - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
                if (remaining <= 0) break

                _remainingTime.value = PrayerTimeUtils.formatTimeRemaining(remaining)
                delay(1000)
            }
            // Timer reached zero — show briefly then auto-transition to next prayer
            _remainingTime.value = "00:00"
            delay(3000)
            refreshPrayerInfo()
        }
    }

    private fun localizedText(arabic: String, english: String): String {
        val appLocale = context.resources.configuration.locales[0]
        val language = appLocale?.language ?: "en"
        return if (language.equals("ar", ignoreCase = true)) arabic else english
    }

    /**
     * Refresh current/next prayer info and restart countdown
     * Called when the countdown reaches 0 to auto-transition to the next prayer
     */
    private fun refreshPrayerInfo() {
        val timings = _prayerTimes.value?.data?.timings ?: return
        val (current, next) = PrayerTimeUtils.getCurrentAndNextPrayer(timings)
        _currentPrayer.value = current
        _nextPrayer.value = next

        next?.let { nextPrayerInfo ->
            startNotificationService(nextPrayerInfo)
            startCountdownTimer(nextPrayerInfo.timeRemainingInSeconds)
        }
    }
}
