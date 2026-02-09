package com.zakrni.app.clean.ui.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.core.app.NotificationCompat
import com.zakrni.app.R
import com.zakrni.app.clean.App
import com.zakrni.app.clean.ui.views.HomeActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkManager @Inject constructor(
    private val context: Context
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private var wasOffline = false

    companion object {
        private const val OFFLINE_NOTIFICATION_ID = 9001
        private const val BACK_ONLINE_NOTIFICATION_ID = 9002
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            val wasOfflineBefore = !_isConnected.value
            _isConnected.value = true

            if (wasOfflineBefore) {
                // Device is back online
                dismissOfflineNotification()
                showBackOnlineNotification()
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            _isConnected.value = false
            wasOffline = true
            showOfflineNotification()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

            val wasConnected = _isConnected.value
            _isConnected.value = hasInternet

            if (!hasInternet && wasConnected) {
                // Just went offline
                wasOffline = true
                showOfflineNotification()
            } else if (hasInternet && !wasConnected && wasOffline) {
                // Back online after being offline
                dismissOfflineNotification()
                showBackOnlineNotification()
                wasOffline = false
            }
        }
    }

    init {
        checkInitialConnection()
        registerNetworkCallback()
    }

    private fun checkInitialConnection() {
        _isConnected.value = isNetworkAvailable()
        if (!_isConnected.value) {
            showOfflineNotification()
        }
    }

    private fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    fun isNetworkAvailable(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected == true
        }
    }

    // 🚀 Show offline notification
    private fun showOfflineNotification() {
        val intent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, App.PRAYER_CHANNEL_ID)
            .setContentTitle(
                if (LocaleHelper.isArabic(context)) {
                    "📵 لا يوجد اتصال بالإنترنت"
                } else {
                    "📵 No internet connection"
                }
            )
            .setContentText(
                if (LocaleHelper.isArabic(context)) {
                    "أوقات الصلاة قد لا تكون محدثة"
                } else {
                    "Prayer times may not be updated"
                }
            )
            .setSmallIcon(R.drawable.ic_dua)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true) // Make it persistent
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(0xFFFF5722.toInt()) // Orange color for warning
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        if (LocaleHelper.isArabic(context)) {
                            "الجهاز غير متصل بالإنترنت. أوقات الصلاة والتنبيهات قد لا تعمل بشكل صحيح حتى يعود الاتصال."
                        } else {
                            "Your device is offline. Prayer times and alerts may not work correctly until the connection is restored."
                        }
                    )
            )
            .build()

        notificationManager.notify(OFFLINE_NOTIFICATION_ID, notification)
    }

    // 🚀 Show back online notification
    private fun showBackOnlineNotification() {
        val intent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, App.PRAYER_CHANNEL_ID)
            .setContentTitle(
                if (LocaleHelper.isArabic(context)) {
                    "✅ تم استعادة الاتصال"
                } else {
                    "✅ Connection restored"
                }
            )
            .setContentText(
                if (LocaleHelper.isArabic(context)) {
                    "أوقات الصلاة ستعمل بشكل طبيعي الآن"
                } else {
                    "Prayer times will work normally now"
                }
            )
            .setSmallIcon(R.drawable.ic_dua)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(0xFF4CAF50.toInt()) // Green color for success
            .setTimeoutAfter(5000) // Auto-dismiss after 5 seconds
            .build()

        notificationManager.notify(BACK_ONLINE_NOTIFICATION_ID, notification)
    }

    // 🚀 Dismiss offline notification
    private fun dismissOfflineNotification() {
        notificationManager.cancel(OFFLINE_NOTIFICATION_ID)
    }

    fun unregisterNetworkCallback() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
        dismissOfflineNotification()
    }
}
