package com.zakrni.app.clean.ui.compose.qibla

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zakrni.app.clean.data.location.LocationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Drives the Qibla compass.
 *
 * Responsibilities:
 *  - Listen to the device orientation sensors (rotation vector, with an
 *    accelerometer + magnetometer fallback) and expose a low-pass filtered
 *    azimuth so the needle moves smoothly.
 *  - Resolve the user's location (via the shared [LocationManager]) and compute
 *    the great-circle bearing to the Kaaba plus the distance to Makkah.
 *
 * All heavy lifting stays here; the screen is a pure renderer of [uiState].
 */
@HiltViewModel
class QiblaViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationManager: LocationManager,
) : ViewModel() {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val _uiState = MutableStateFlow(QiblaUiState())
    val uiState: StateFlow<QiblaUiState> = _uiState.asStateFlow()

    // Reusable matrices/arrays for the orientation computation.
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // Accelerometer + magnetometer fallback buffers.
    private val accelReading = FloatArray(3)
    private val magnetReading = FloatArray(3)
    private var hasAccel = false
    private var hasMagnet = false

    // Low-pass filtered azimuth (device heading, degrees 0..360).
    private var filteredAzimuth = 0f
    private var hasAzimuth = false

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ROTATION_VECTOR -> {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    updateOrientation()
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    System.arraycopy(event.values, 0, accelReading, 0, 3)
                    hasAccel = true
                    if (hasMagnet) updateFromRawSensors()
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    System.arraycopy(event.values, 0, magnetReading, 0, 3)
                    hasMagnet = true
                    if (hasAccel) updateFromRawSensors()
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            val state = when (accuracy) {
                SensorManager.SENSOR_STATUS_UNRELIABLE,
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> CompassAccuracy.LOW
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> CompassAccuracy.MEDIUM
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> CompassAccuracy.HIGH
                else -> CompassAccuracy.UNKNOWN
            }
            _uiState.value = _uiState.value.copy(accuracy = state)
        }
    }

    private fun updateFromRawSensors() {
        if (SensorManager.getRotationMatrix(rotationMatrix, null, accelReading, magnetReading)) {
            updateOrientation()
        }
    }

    private fun updateOrientation() {
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
        // azimuth in radians, [-PI, PI] -> degrees [0, 360)
        val azimuthDeg = ((Math.toDegrees(orientationAngles[0].toDouble()) + 360.0) % 360.0).toFloat()
        filteredAzimuth = if (!hasAzimuth) {
            hasAzimuth = true
            azimuthDeg
        } else {
            lowPass(azimuthDeg, filteredAzimuth)
        }
        publishHeading(filteredAzimuth)
    }

    /**
     * Angle-aware low-pass filter. Interpolates along the shortest arc so the
     * needle never spins the long way around when crossing 0°/360°.
     */
    private fun lowPass(target: Float, current: Float): Float {
        var delta = target - current
        while (delta > 180f) delta -= 360f
        while (delta < -180f) delta += 360f
        val next = current + LOW_PASS_ALPHA * delta
        return (next % 360f + 360f) % 360f
    }

    private fun publishHeading(azimuth: Float) {
        val state = _uiState.value
        val qibla = state.qiblaBearing ?: run {
            _uiState.value = state.copy(deviceAzimuth = azimuth)
            return
        }
        // Angle the needle must point relative to the top of the screen.
        val pointer = ((qibla - azimuth) % 360f + 360f) % 360f
        val diff = angularDistance(azimuth, qibla)
        val aligned = diff <= ALIGNMENT_THRESHOLD_DEG
        _uiState.value = state.copy(
            deviceAzimuth = azimuth,
            qiblaPointerAngle = pointer,
            isAligned = aligned,
        )
    }

    /** Begins listening to sensors. Call from the composable's lifecycle (ON_RESUME). */
    fun startSensors() {
        hasAccel = false
        hasMagnet = false
        val rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationVector != null) {
            sensorManager.registerListener(
                sensorListener, rotationVector, SensorManager.SENSOR_DELAY_GAME,
            )
            _uiState.value = _uiState.value.copy(sensorAvailable = true)
        } else {
            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            if (accelerometer != null && magnetometer != null) {
                sensorManager.registerListener(
                    sensorListener, accelerometer, SensorManager.SENSOR_DELAY_GAME,
                )
                sensorManager.registerListener(
                    sensorListener, magnetometer, SensorManager.SENSOR_DELAY_GAME,
                )
                _uiState.value = _uiState.value.copy(sensorAvailable = true)
            } else {
                _uiState.value = _uiState.value.copy(sensorAvailable = false)
            }
        }
    }

    /** Stops listening to sensors. Call from the composable's lifecycle (ON_PAUSE). */
    fun stopSensors() {
        sensorManager.unregisterListener(sensorListener)
    }

    /**
     * Resolves the current location and computes the Qibla bearing + distance.
     * Safe to call repeatedly (e.g. on a retry tap). Requires location permission
     * to already be granted by the caller.
     */
    fun resolveLocation() {
        if (!locationManager.hasLocationPermission()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                hasLocation = false,
            )
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val location = locationManager.getCurrentLocationAsync()
            if (location == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hasLocation = false,
                )
                return@launch
            }
            val (lat, lng) = location
            val bearing = bearingToKaaba(lat, lng)
            val distanceKm = distanceToKaabaKm(lat, lng)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                hasLocation = true,
                qiblaBearing = bearing,
                distanceKm = distanceKm,
            )
            // Recompute the pointer immediately with the latest heading.
            publishHeading(filteredAzimuth)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopSensors()
    }

    companion object {
        // Kaaba coordinates.
        const val KAABA_LAT = 21.4225
        const val KAABA_LNG = 39.8262

        private const val LOW_PASS_ALPHA = 0.12f
        const val ALIGNMENT_THRESHOLD_DEG = 3f
        private const val EARTH_RADIUS_KM = 6371.0

        /** Initial great-circle bearing (degrees, 0..360 from true north) to the Kaaba. */
        fun bearingToKaaba(lat: Double, lng: Double): Float {
            val phi1 = Math.toRadians(lat)
            val phi2 = Math.toRadians(KAABA_LAT)
            val deltaLambda = Math.toRadians(KAABA_LNG - lng)
            val y = sin(deltaLambda) * cos(phi2)
            val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
            val theta = atan2(y, x)
            return ((Math.toDegrees(theta) + 360.0) % 360.0).toFloat()
        }

        /** Great-circle (haversine) distance in km to the Kaaba. */
        fun distanceToKaabaKm(lat: Double, lng: Double): Double {
            val phi1 = Math.toRadians(lat)
            val phi2 = Math.toRadians(KAABA_LAT)
            val deltaPhi = Math.toRadians(KAABA_LAT - lat)
            val deltaLambda = Math.toRadians(KAABA_LNG - lng)
            val a = sin(deltaPhi / 2) * sin(deltaPhi / 2) +
                cos(phi1) * cos(phi2) * sin(deltaLambda / 2) * sin(deltaLambda / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return EARTH_RADIUS_KM * c
        }

        /** Shortest angular distance between two headings in degrees (0..180). */
        fun angularDistance(a: Float, b: Float): Float {
            val diff = abs(a - b) % 360f
            return if (diff > 180f) 360f - diff else diff
        }
    }
}

enum class CompassAccuracy { UNKNOWN, LOW, MEDIUM, HIGH }

/**
 * Immutable UI state for the Qibla screen.
 *
 * @param deviceAzimuth filtered device heading (deg, 0..360 from true north).
 * @param qiblaBearing great-circle bearing to the Kaaba (deg) or null until located.
 * @param qiblaPointerAngle angle (deg) to rotate the Qibla needle relative to screen-up.
 * @param distanceKm distance to Makkah in km, or null until located.
 * @param isAligned true when the device is pointing within the alignment threshold.
 */
data class QiblaUiState(
    val isLoading: Boolean = false,
    val hasLocation: Boolean = false,
    val sensorAvailable: Boolean = true,
    val deviceAzimuth: Float = 0f,
    val qiblaBearing: Float? = null,
    val qiblaPointerAngle: Float = 0f,
    val distanceKm: Double? = null,
    val isAligned: Boolean = false,
    val accuracy: CompassAccuracy = CompassAccuracy.UNKNOWN,
)
