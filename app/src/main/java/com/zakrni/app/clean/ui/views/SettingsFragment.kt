package com.zakrni.app.clean.ui.views

import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.zakrni.app.R
import com.zakrni.app.clean.ads.SubscriptionDialog
import com.zakrni.app.clean.ads.SubscriptionManager
import com.zakrni.app.clean.ui.utils.DialogStyler
import com.zakrni.app.clean.service.PrayerAlarmManager
import com.zakrni.app.clean.ui.utils.AudioPreferences
import com.zakrni.app.clean.ui.utils.PrayerStorageManager
import com.zakrni.app.clean.ui.utils.ThemeManager
import com.zakrni.app.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var subscriptionManager: SubscriptionManager

    @Inject
    lateinit var prayerAlarmManager: PrayerAlarmManager

    @Inject
    lateinit var prayerStorageManager: PrayerStorageManager

    private lateinit var audioPreferences: AudioPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        audioPreferences = AudioPreferences(requireContext())

        setupUI()
        loadSettings()
        setupListeners()
    }

    private fun setupUI() {
        // Back button — use activity back press dispatcher (fullscreen fragment pattern)
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Premium card — open subscription dialog
        binding.premiumCard.setOnClickListener {
            SubscriptionDialog(requireActivity(), subscriptionManager).show()
        }

        // Observe subscription status to update UI
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                subscriptionManager.isSubscribedState.collect { isSubscribed ->
                    if (isSubscribed) {
                        binding.premiumCard.visibility = View.GONE
                    } else {
                        binding.premiumCard.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun loadSettings() {
        val context = requireContext()

        updateLanguageValue()

        // Dark Mode
        binding.switchDarkMode.isChecked = ThemeManager.isDarkMode(context)

        // Font Size
        when (ThemeManager.getFontSize(context)) {
            0 -> binding.fontSmall.isChecked = true
            1 -> binding.fontMedium.isChecked = true
            2 -> binding.fontLarge.isChecked = true
        }

        // General Notifications
        val notificationsEnabled = ThemeManager.isNotificationsEnabled(context)
        binding.switchNotifications.isChecked = notificationsEnabled

        // Prayer Notifications
        binding.switchPrayerNotifications.isChecked = ThemeManager.isPrayerNotificationsEnabled(context)
        binding.switchPrayerNotifications.isEnabled = notificationsEnabled

        // Azkar Reminders
        binding.switchAzkarReminders.isChecked = ThemeManager.isAzkarRemindersEnabled(context)
        binding.switchAzkarReminders.isEnabled = notificationsEnabled

        // Auto Play (from AudioPreferences — the actual source of truth)
        binding.switchAutoPlay.isChecked = audioPreferences.isAutoPlayEnabled
        
        // Check and show full-screen intent permission warning for Android 12+
        checkFullScreenIntentPermission()
    }

    private fun setupListeners() {
        val context = requireContext()

        binding.cardLanguage.setOnClickListener {
            showLanguageDialog()
        }

        // Dark Mode
        binding.switchDarkMode.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener
            if (ThemeManager.isDarkMode(context) == isChecked) return@setOnCheckedChangeListener

            ThemeManager.setDarkMode(context, isChecked)
        }

        // Font Size
        binding.fontSizeGroup.setOnCheckedChangeListener { _, checkedId ->
            val size = when (checkedId) {
                R.id.fontSmall -> 0
                R.id.fontMedium -> 1
                R.id.fontLarge -> 2
                else -> 1
            }
            ThemeManager.setFontSize(context, size)
        }

        // General Notifications — master toggle
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            ThemeManager.setNotificationsEnabled(context, isChecked)

            // Enable/disable dependent switches
            binding.switchPrayerNotifications.isEnabled = isChecked
            binding.switchAzkarReminders.isEnabled = isChecked

            if (!isChecked) {
                // If master is OFF, turn off prayer & azkar too
                binding.switchPrayerNotifications.isChecked = false
                binding.switchAzkarReminders.isChecked = false
                ThemeManager.setPrayerNotificationsEnabled(context, false)
                ThemeManager.setAzkarRemindersEnabled(context, false)

                // Cancel all prayer alarms
                prayerAlarmManager.cancelAllAlarms()
            } else {
                // When turning back ON, re-enable prayer notifications by default
                binding.switchPrayerNotifications.isChecked = true
                ThemeManager.setPrayerNotificationsEnabled(context, true)
            }
        }

        // Prayer Notifications toggle
        binding.switchPrayerNotifications.setOnCheckedChangeListener { _, isChecked ->
            ThemeManager.setPrayerNotificationsEnabled(context, isChecked)
            if (!isChecked) {
                // Cancel all scheduled prayer alarms
                prayerAlarmManager.cancelAllAlarms()
            } else {
                // When enabling prayer notifications, check full-screen intent permission
                checkFullScreenIntentPermission()
                reschedulePrayerAlarmsIfPossible()
            }
        }

        // Azkar Reminders toggle
        binding.switchAzkarReminders.setOnCheckedChangeListener { _, isChecked ->
            ThemeManager.setAzkarRemindersEnabled(context, isChecked)
            if (isChecked) {
                com.zakrni.app.clean.service.AzkarReminderWorker.schedule(requireContext())
            } else {
                com.zakrni.app.clean.service.AzkarReminderWorker.cancel(requireContext())
            }
        }

        // Auto Play — sync with AudioPreferences (the actual source)
        binding.switchAutoPlay.setOnCheckedChangeListener { _, isChecked ->
            audioPreferences.isAutoPlayEnabled = isChecked
            ThemeManager.setAutoPlayEnabled(context, isChecked)
        }

        // Rate App
        binding.cardRateApp.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = android.net.Uri.parse("market://details?id=${context.packageName}")
                startActivity(intent)
            } catch (e: Exception) {
                // If Play Store is not available
            }
        }

        // Share App
        binding.cardShareApp.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_title))
                putExtra(
                    Intent.EXTRA_TEXT,
                    "${getString(R.string.share_app_text)}\nhttps://play.google.com/store/apps/details?id=${context.packageName}"
                )
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app_via)))
        }
    }

    private fun updateLanguageValue() {
        val languageCode = ThemeManager.getCurrentAppLanguage(requireContext())
        binding.tvLanguageValue.text = when (languageCode) {
            ThemeManager.LANGUAGE_ENGLISH -> getString(R.string.language_english)
            else -> getString(R.string.language_arabic)
        }
    }

    private fun showLanguageDialog() {
        val context = requireContext()
        val currentLanguage = ThemeManager.getCurrentAppLanguage(context)
        val options = arrayOf(
            getString(R.string.language_arabic),
            getString(R.string.language_english)
        )
        val checkedIndex = if (currentLanguage == ThemeManager.LANGUAGE_ENGLISH) 1 else 0

        val dialog = DialogStyler.builder(context)
            .setTitle(R.string.settings_language_dialog_title)
            .setSingleChoiceItems(options, checkedIndex) { choiceDialog, which ->
                val selectedLanguage = if (which == 1) {
                    ThemeManager.LANGUAGE_ENGLISH
                } else {
                    ThemeManager.LANGUAGE_ARABIC
                }

                if (selectedLanguage != currentLanguage) {
                    ThemeManager.setAppLanguage(context, selectedLanguage)
                    updateLanguageValue()
                }
                choiceDialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        showStyledDialog(dialog)
    }

    /**
     * Reschedule prayer alarms when notifications are re-enabled.
     * Uses saved timings so alerts return immediately without waiting for a fresh API fetch.
     */
    private fun reschedulePrayerAlarmsIfPossible() {
        val context = requireContext()
        if (!ThemeManager.isNotificationsEnabled(context) || !ThemeManager.isPrayerNotificationsEnabled(context)) {
            return
        }

        val savedTimings = prayerStorageManager.getSavedPrayerTimes() ?: return
        prayerAlarmManager.scheduleAllPrayerAlarms(savedTimings)
    }

    /**
     * Check if full-screen intent permission is granted on Android 12+
     * This permission is required for the prayer alert screen to show up on locked screens
     */
    private fun checkFullScreenIntentPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34+
            val notificationManager = requireContext().getSystemService(NotificationManager::class.java)
            if (!notificationManager.canUseFullScreenIntent()) {
                showFullScreenIntentPermissionDialog()
            }
        }
    }
    
    /**
     * Show dialog explaining the need for full-screen intent permission
     */
    private fun showFullScreenIntentPermissionDialog() {
        val dialog = DialogStyler.builder(requireContext())
            .setTitle(R.string.fullscreen_permission_title)
            .setMessage(R.string.fullscreen_permission_message)
            .setIcon(R.drawable.ic_dua)
            .setPositiveButton(R.string.fullscreen_permission_open_settings) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                        data = Uri.parse("package:${requireContext().packageName}")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    // Fallback to app settings if specific intent not supported
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${requireContext().packageName}")
                    }
                    startActivity(intent)
                }
            }
            .setNegativeButton(R.string.fullscreen_permission_later, null)
            .create()

        showStyledDialog(dialog)
    }

    private fun showStyledDialog(dialog: AlertDialog) {
        dialog.show()
        DialogStyler.apply(dialog, requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
