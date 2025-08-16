package com.example.zakrni.clean.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationManager @Inject constructor(
    private val context: Context
) {

    data class LocationInfo(
        val latitude: Double,
        val longitude: Double,
        val cityName: String,
        val countryName: String,
        val displayName: String
    )

    fun getCurrentLocation(): Pair<Double, Double>? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER
        )

        for (provider in providers) {
            try {
                val location = locationManager.getLastKnownLocation(provider)
                if (location != null) {
                    return Pair(location.latitude, location.longitude)
                }
            } catch (e: Exception) {
                continue
            }
        }

        return null
    }

    suspend fun getCurrentLocationWithName(): LocationInfo? {
        return withContext(Dispatchers.IO) {
            val location = getCurrentLocation() ?: return@withContext null

            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.first, location.second, 1)

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val cityName = getCityName(address)
                    val countryName = getCountryName(address)
                    val displayName = formatLocationName(cityName, countryName)

                    return@withContext LocationInfo(
                        latitude = location.first,
                        longitude = location.second,
                        cityName = cityName,
                        countryName = countryName,
                        displayName = displayName
                    )
                }
            } catch (e: Exception) {
                // Fallback to coordinates if geocoding fails
                return@withContext LocationInfo(
                    latitude = location.first,
                    longitude = location.second,
                    cityName = "Unknown",
                    countryName = "Unknown",
                    displayName = "${location.first.format(2)}, ${location.second.format(2)}"
                )
            }

            null
        }
    }

    private fun getCityName(address: Address): String {
        return when {
            !address.locality.isNullOrEmpty() -> address.locality
            !address.subAdminArea.isNullOrEmpty() -> address.subAdminArea
            !address.adminArea.isNullOrEmpty() -> address.adminArea
            else -> "Unknown City"
        }
    }

    private fun getCountryName(address: Address): String {
        return when {
            !address.countryName.isNullOrEmpty() -> address.countryName
            else -> "Unknown Country"
        }
    }

    private fun formatLocationName(cityName: String, countryName: String): String {
        // Convert to Arabic names if possible
        val arabicCityName = convertCityToArabic(cityName)
        val arabicCountryName = convertCountryToArabic(countryName)

        return "$arabicCityName، $arabicCountryName"
    }

    private fun convertCityToArabic(cityName: String): String {
        return when {
            cityName.contains("Cairo", ignoreCase = true) -> "القاهرة"
            cityName.contains("Alexandria", ignoreCase = true) -> "الإسكندرية"
            cityName.contains("Giza", ignoreCase = true) -> "الجيزة"
            cityName.contains("Mecca", ignoreCase = true) || cityName.contains("Makkah", ignoreCase = true) -> "مكة المكرمة"
            cityName.contains("Medina", ignoreCase = true) || cityName.contains("Madinah", ignoreCase = true) -> "المدينة المنورة"
            cityName.contains("Riyadh", ignoreCase = true) -> "الرياض"
            cityName.contains("Jeddah", ignoreCase = true) -> "جدة"
            cityName.contains("Dubai", ignoreCase = true) -> "دبي"
            cityName.contains("Abu Dhabi", ignoreCase = true) -> "أبو ظبي"
            cityName.contains("Kuwait", ignoreCase = true) -> "الكويت"
            cityName.contains("Doha", ignoreCase = true) -> "الدوحة"
            cityName.contains("Baghdad", ignoreCase = true) -> "بغداد"
            cityName.contains("Damascus", ignoreCase = true) -> "دمشق"
            cityName.contains("Beirut", ignoreCase = true) -> "بيروت"
            cityName.contains("Amman", ignoreCase = true) -> "عمان"
            cityName.contains("Tunis", ignoreCase = true) -> "تونس"
            cityName.contains("Algiers", ignoreCase = true) -> "الجزائر"
            cityName.contains("Casablanca", ignoreCase = true) -> "الدار البيضاء"
            cityName.contains("Rabat", ignoreCase = true) -> "الرباط"
            cityName.contains("Manama", ignoreCase = true) -> "المنامة"
            cityName.contains("Muscat", ignoreCase = true) -> "مسقط"
            else -> cityName // Return original name if no Arabic translation found
        }
    }

    private fun convertCountryToArabic(countryName: String): String {
        return when {
            countryName.contains("Egypt", ignoreCase = true) -> "مصر"
            countryName.contains("Saudi Arabia", ignoreCase = true) -> "السعودية"
            countryName.contains("United Arab Emirates", ignoreCase = true) || countryName.contains("UAE", ignoreCase = true) -> "الإمارات"
            countryName.contains("Kuwait", ignoreCase = true) -> "الكويت"
            countryName.contains("Qatar", ignoreCase = true) -> "قطر"
            countryName.contains("Iraq", ignoreCase = true) -> "العراق"
            countryName.contains("Syria", ignoreCase = true) -> "سوريا"
            countryName.contains("Lebanon", ignoreCase = true) -> "لبنان"
            countryName.contains("Jordan", ignoreCase = true) -> "الأردن"
            countryName.contains("Tunisia", ignoreCase = true) -> "تونس"
            countryName.contains("Algeria", ignoreCase = true) -> "الجزائر"
            countryName.contains("Morocco", ignoreCase = true) -> "المغرب"
            countryName.contains("Bahrain", ignoreCase = true) -> "البحرين"
            countryName.contains("Oman", ignoreCase = true) -> "عمان"
            countryName.contains("Yemen", ignoreCase = true) -> "اليمن"
            countryName.contains("Libya", ignoreCase = true) -> "ليبيا"
            countryName.contains("Sudan", ignoreCase = true) -> "السودان"
            else -> countryName // Return original name if no Arabic translation found
        }
    }

    fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun Double.format(digits: Int) = "%.${digits}f".format(this)
}