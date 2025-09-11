package com.example.zakrni.clean.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zakrni.clean.data.location.LocationManager
import com.example.zakrni.clean.domain.usecases.GetPrayerTimesUseCase
import com.example.zakrni.clean.service.PrayerAlarmManager
import com.example.zakrni.clean.service.PrayerNotificationService
import com.example.zakrni.clean.ui.models.PresentationPrayerTimesResponse
import com.example.zakrni.clean.ui.models.mapToPresentation
import com.example.zakrni.clean.ui.utils.NetworkManager
import com.example.zakrni.clean.ui.utils.PrayerTimeUtils
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
   private val networkManager: NetworkManager
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

    private val _locationName = MutableStateFlow("جاري تحديد الموقع...")
    val locationName: StateFlow<String> get() = _locationName

    private val _hasExactAlarmPermission = MutableStateFlow(true)
    val hasExactAlarmPermission: StateFlow<Boolean> get() = _hasExactAlarmPermission

    private var timerJob: Job? = null
    private var nextPrayerTimeInSeconds: Long = 0

    // Initialize AlarmManager
    private val prayerAlarmManager = PrayerAlarmManager(context,networkManager)

    init {
        checkLocationPermission()
        checkExactAlarmPermission()
    }

    fun checkLocationPermission() {
        _hasLocationPermission.value = locationManager.hasLocationPermission()
        if (_hasLocationPermission.value) {
            loadPrayerTimes()
        }
    }

    fun checkExactAlarmPermission() {
        _hasExactAlarmPermission.value = prayerAlarmManager.hasExactAlarmPermission()
    }

    fun requestExactAlarmPermission() {
        prayerAlarmManager.requestExactAlarmPermission()
    }

    fun loadPrayerTimes() {
        if (!locationManager.hasLocationPermission()) {
            _error.value = "Location permission required"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val locationInfo = locationManager.getCurrentLocationWithName()
                if (locationInfo == null) {
                    _error.value = "Unable to get location"
                    return@launch
                }
                _locationName.value = locationInfo.displayName
                getPrayerTimes(locationInfo.latitude, locationInfo.longitude)
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun getPrayerTimes(latitude: Double, longitude: Double) {
        try {
            val result = prayerTimesUseCase(latitude, longitude)
            if (result.isSuccess) {
                val presentationData = result.getOrThrow().mapToPresentation()
                _prayerTimes.value = presentationData

                updatePrayerInfo(presentationData)
                schedulePrayerAlarms(presentationData)
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Unknown error"
            }
        } catch (e: Exception) {
            _error.value = e.message ?: "Unknown error"
        }
    }

    private fun updatePrayerInfo(presentationData: PresentationPrayerTimesResponse) {
        val (current, next) = PrayerTimeUtils.getCurrentAndNextPrayer(presentationData.data.timings)
        _currentPrayer.value = current
        _nextPrayer.value = next

        next?.let { nextPrayerInfo ->
            nextPrayerTimeInSeconds = nextPrayerInfo.timeRemainingInSeconds

            startNotificationService(nextPrayerInfo)
            startCountdownTimer()
        }
    }


    private fun schedulePrayerAlarms(presentationData: PresentationPrayerTimesResponse) {
        try {
            prayerAlarmManager.scheduleAllPrayerAlarms(presentationData.data.timings)

            // Save prayer times to SharedPreferences for device reboot recovery
            savePrayerTimesToPreferences(presentationData)

        } catch (e: Exception) {
            _error.value = "Failed to schedule prayer alarms: ${e.message}"
        }
    }


    private fun savePrayerTimesToPreferences(presentationData: PresentationPrayerTimesResponse) {
        val sharedPrefs = context.getSharedPreferences("prayer_times", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putString("fajr", presentationData.data.timings.Fajr)
            putString("dhuhr", presentationData.data.timings.Dhuhr)
            putString("asr", presentationData.data.timings.Asr)
            putString("maghrib", presentationData.data.timings.Maghrib)
            putString("isha", presentationData.data.timings.Isha)
            putLong("last_updated", System.currentTimeMillis())
            apply()
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

    private fun startCountdownTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (nextPrayerTimeInSeconds > 0) {
                val formattedTime = PrayerTimeUtils.formatTimeRemaining(nextPrayerTimeInSeconds)
                _remainingTime.value = formattedTime
                delay(1000)
            }
        }
    }
}