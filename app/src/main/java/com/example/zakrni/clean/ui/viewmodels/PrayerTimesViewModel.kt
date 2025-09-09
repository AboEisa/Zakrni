package com.example.zakrni.clean.ui.viewmodels

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zakrni.R
import com.example.zakrni.clean.App
import com.example.zakrni.clean.data.location.LocationManager
import com.example.zakrni.clean.domain.usecases.GetPrayerTimesUseCase
import com.example.zakrni.clean.service.PrayerNotificationService
import com.example.zakrni.clean.ui.models.PresentationPrayerTimesResponse
import com.example.zakrni.clean.ui.models.mapToPresentation
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
    @ApplicationContext private val context: Context
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

    private var timerJob: Job? = null
    private var nextPrayerTimeInSeconds: Long = 0

    init {
        checkLocationPermission()
    }

    fun checkLocationPermission() {
        _hasLocationPermission.value = locationManager.hasLocationPermission()
        if (_hasLocationPermission.value) {
            loadPrayerTimes()
        }
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
                // Get location with name
                val locationInfo = locationManager.getCurrentLocationWithName()
                if (locationInfo == null) {
                    _error.value = "Unable to get location"
                    return@launch
                }

                // Update location name
                _locationName.value = locationInfo.displayName

                // Get prayer times
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

                // Calculate current and next prayer
                updatePrayerInfo(presentationData)
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

        // Start countdown timer for next prayer
        next?.let { nextPrayerInfo ->
            nextPrayerTimeInSeconds = nextPrayerInfo.timeRemainingInSeconds

            // Start notification service
            startNotificationService(nextPrayerInfo)

            // Start local countdown timer
            startCountdownTimer()
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
        // Cancel previous timer if running
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            while (nextPrayerTimeInSeconds > 0) {
                val formattedTime = PrayerTimeUtils.formatTimeRemaining(nextPrayerTimeInSeconds)
                _remainingTime.value = formattedTime

                delay(1000) // Wait 1 second
                nextPrayerTimeInSeconds--
            }

            // 🔔 لما العد التنازلي يخلص
            if (nextPrayerTimeInSeconds <= 0) {
                _remainingTime.value = "00:00"

                // أرسل إشعار جديد للصلاة الحالية
                _nextPrayer.value?.let { next ->
                    PrayerNotificationService.showPrayerAlert(
                        context,
                        next.name,
                        next.nameArabic
                    )
                }

                // بعدها حدث المواقيت عشان ينتقل للصلاة التالية
                _prayerTimes.value?.let { updatePrayerInfo(it) }
            }
        }
    }



    fun showPrayerTimeNotification(context: Context, prayerName: String, prayerNameArabic: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, App.PRAYER_CHANNEL_ID)
            .setContentTitle("حان الآن وقت $prayerName ($prayerNameArabic)")
            .setContentText("أدِ الصلاة في وقتها")
            .setSmallIcon(R.drawable.ic_dua)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        // خلي ID مختلف عشان يظهر إشعار جديد مش يستبدل القديم
        val uniqueId = System.currentTimeMillis().toInt()
        notificationManager.notify(uniqueId, notification)
    }


    fun retry() {
        loadPrayerTimes()
    }

    fun stopNotificationService() {
        PrayerNotificationService.stopService(context)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}