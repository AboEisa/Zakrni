package com.example.zakrni.clean.ui.views

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.zakrni.clean.ui.utils.NetworkManager
import com.example.zakrni.clean.ui.utils.NoInternetDialog
import com.example.zakrni.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var noInternetDialog: NoInternetDialog

    @Inject
    lateinit var networkManager: NetworkManager

    // Permission launcher for notifications
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, proceed normally
            checkFirstTimeUser()
        } else {
            // Permission denied, still proceed but inform user
            checkFirstTimeUser()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        noInternetDialog = NoInternetDialog(this)

        // Request notification permission first
        requestNotificationPermission()

        onClick()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                checkFirstTimeUser()
            }
        } else {
            checkFirstTimeUser()
        }
    }

    private fun sendTestNotification() {
        val notification = NotificationCompat.Builder(this, "APP_CHANNEL")
        notification.setContentTitle("مرحبا بك في تطبيق ذكرني")
        notification.setContentText("تم تفعيل الإشعارات بنجاح - Notifications enabled successfully")
        notification.setSmallIcon(android.R.drawable.ic_dialog_info)
        notification.setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(999, notification.build())
    }

    private fun checkFirstTimeUser() {
        val isFirstTime = sharedPreferences.getBoolean("is_first_time", true)

        if (!isFirstTime) {
            // User has registered before - go directly to Home without checking network
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        } else {
            // New user - only monitor network here
            observeNetworkStatusForNewUser()
        }
    }

    private fun observeNetworkStatusForNewUser() {
        lifecycleScope.launch {
            networkManager.isConnected.collect { isConnected ->
                if (!isConnected) {
                    showNoInternetDialog()
                } else {
                    hideNoInternetDialog()
                    // If network becomes available while dialog is showing, proceed
                    if (noInternetDialog.isShowing()) {
                        proceedToHome()
                    }
                }
            }
        }
    }

    private fun showNoInternetDialog() {
        if (!noInternetDialog.isShowing()) {
            noInternetDialog.show(
                onRetry = {
                    // Retry action
                    checkNetworkAndProceed()
                },
                onCancel = {
                    // Close the app when cancel is clicked
                    finishAffinity()
                }
            )
        }
    }

    private fun hideNoInternetDialog() {
        if (noInternetDialog.isShowing()) {
            noInternetDialog.dismiss()
        }
    }

    private fun checkNetworkAndProceed() {
        if (networkManager.isNetworkAvailable()) {
            proceedToHome()
        } else {
            // Show dialog again if no internet
            noInternetDialog.showLoading(false) // Hide loading, show retry button again
            showNoInternetDialog()
        }
    }

    private fun proceedToHome() {
        // Send welcome notification
        sendTestNotification()

        sharedPreferences.edit().putBoolean("is_first_time", false).apply()
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun onClick() {
        binding.btnStart.setOnClickListener {
            checkNetworkAndProceed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hideNoInternetDialog()
        _binding = null
    }
}