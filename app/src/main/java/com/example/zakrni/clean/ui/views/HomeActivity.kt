package com.example.zakrni.clean.ui.views

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.zakrni.R
import com.example.zakrni.clean.ui.models.PresentationPrayerTimesResponse
import com.example.zakrni.clean.ui.utils.NetworkManager
import com.example.zakrni.clean.ui.utils.NoInternetDialog
import com.example.zakrni.clean.ui.utils.PrayerTimeUtils
import com.example.zakrni.clean.ui.viewmodels.PrayerTimesViewModel
import com.example.zakrni.databinding.ActivityHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private var _binding: ActivityHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private lateinit var noInternetDialog: NoInternetDialog

    private val prayerTimeViewModel: PrayerTimesViewModel by viewModels()

    @Inject
    lateinit var networkManager: NetworkManager

    // Define which fragments should be full-screen
    private val fullScreenFragments = mapOf(
        R.id.hadithFragment to { HadithFragment() },
        R.id.duaFragment to { DuaFragment() },
        R.id.azkarFragment to { AzkarFragment() },
        R.id.quranFragment2 to { QuranFragment() }, // Changed from quranFragment to quranFragment2
        R.id.allahNamesFragment to { AllahNamesFragment() },
        R.id.tasbehFragment to { TasbehFragment() },
        R.id.quran2Fragment to { Quran2Fragment() },
    )

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                prayerTimeViewModel.checkLocationPermission()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                prayerTimeViewModel.checkLocationPermission()
            }
            else -> {
                showLocationPermissionDenied()
            }
        }
    }

    private val navOptions by lazy {
        NavOptions.Builder()
            .setEnterAnim(R.anim.animation)
            .setExitAnim(R.anim.animation2)
            .setPopEnterAnim(R.anim.animation3)
            .setPopExitAnim(R.anim.animation4)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        // Initialize network dialog
        noInternetDialog = NoInternetDialog(this)

        setupNavigation()
        setupCustomBottomNavigation()
        setupPrayerTimesObservers()
        setupNetworkObserver()
        checkLocationPermissions()

    }

    private fun setupNetworkObserver() {
        lifecycleScope.launch {
            networkManager.isConnected.collect { isConnected ->
                if (!isConnected) {
                    showNoInternetDialog()
                } else {
                    hideNoInternetDialog()
                }
            }
        }
    }

    private fun showNoInternetDialog() {
        if (!noInternetDialog.isShowing()) {
            noInternetDialog.show(
                onRetry = {
                    // Retry action
                    if (networkManager.isNetworkAvailable()) {
                        hideNoInternetDialog()
                    } else {
                        noInternetDialog.showLoading(false) // Hide loading, show retry button again
                        showNoInternetDialog()
                    }
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

    private fun checkLocationPermissions() {
        when {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                prayerTimeViewModel.checkLocationPermission()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                requestLocationPermissions()
            }

            else -> {
                requestLocationPermissions()
            }
        }
    }

    private fun requestLocationPermissions() {
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    private fun setupPrayerTimesObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    prayerTimeViewModel.prayerTimes.collect { prayerTimes ->
                        prayerTimes?.let { updatePrayerTimesUI(it) }
                    }
                }

                launch {
                    prayerTimeViewModel.currentPrayer.collect { currentPrayer ->
                        currentPrayer?.let { updateCurrentPrayerCard(it) }
                    }
                }

                launch {
                    prayerTimeViewModel.nextPrayer.collect { nextPrayer ->
                        nextPrayer?.let { updateNextPrayerInfo(it) }
                    }
                }

                launch {
                    prayerTimeViewModel.remainingTime.collect { remainingTime ->
                        updateCountdownTimer(remainingTime)
                    }
                }

                launch {
                    prayerTimeViewModel.error.collect { error ->
                        if (error != null) {
                            showError(error)
                        }
                    }
                }
            }
        }
    }

    private fun updateCurrentPrayerCard(currentPrayer: PrayerTimeUtils.PrayerInfo) {
        with(binding) {
            prayerName.text = currentPrayer.nameArabic
            prayerTime.text = currentPrayer.time
        }
    }

    private fun updateNextPrayerInfo(nextPrayer: PrayerTimeUtils.PrayerInfo) {
        with(binding) {
            nextPrayerLabel.text = "الصلاة التالية :  ${nextPrayer.nameArabic}"
        }
    }

    private fun updateCountdownTimer(remainingTime: String) {
        binding.nextPrayerTime.text = remainingTime
    }

    private fun updatePrayerTimesUI(prayerTimes: PresentationPrayerTimesResponse) {

    }

    private fun showError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    private fun showLocationPermissionDenied() {
        Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // initial destination
        selectNavItem(R.id.nav_all_categories)

        // Handle destination changes and full-screen fragments
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (fullScreenFragments.containsKey(destination.id)) {
                showFullScreenFragment(destination.id)
            } else {
                hideFullScreenFragment()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun selectNavItem(selectedId: Int) {
        binding.navPrayerTimes.background = null
        binding.navAllMedia.background = null
        binding.navAllCategories.background = null

        val unselectedColor = ContextCompat.getColor(this, android.R.color.white)
        val selectedColor = ContextCompat.getColor(this, R.color.selected_text_color)

        binding.navPrayerTimes.setTextColor(unselectedColor)
        binding.navAllMedia.setTextColor(unselectedColor)
        binding.navAllCategories.setTextColor(unselectedColor)

        val selectedBackground = ContextCompat.getDrawable(this, R.drawable.selected_nav_background)

        when (selectedId) {
            R.id.nav_prayer_times -> {
                binding.navPrayerTimes.background = selectedBackground
                binding.navPrayerTimes.setTextColor(selectedColor)
            }
            R.id.nav_all_media -> {
                binding.navAllMedia.background = selectedBackground
                binding.navAllMedia.setTextColor(selectedColor)
            }
            R.id.nav_all_categories -> {
                binding.navAllCategories.background = selectedBackground
                binding.navAllCategories.setTextColor(selectedColor)
            }
        }
    }

    private fun setupCustomBottomNavigation() {
        binding.navPrayerTimes.setOnClickListener {
            if (navController.currentDestination?.id != R.id.playerTimesFragment) {
                selectNavItem(R.id.nav_prayer_times)
                navController.navigate(R.id.playerTimesFragment, null, navOptions)
            }
        }
        binding.navAllMedia.setOnClickListener {
            if (navController.currentDestination?.id != R.id.allMediaFragment) {
                selectNavItem(R.id.nav_all_media)
                navController.navigate(R.id.allMediaFragment, null, navOptions)
            }
        }
        binding.navAllCategories.setOnClickListener {
            if (navController.currentDestination?.id != R.id.allCategoriesFragment) {
                selectNavItem(R.id.nav_all_categories)
                navController.navigate(R.id.allCategoriesFragment, null, navOptions)
            }
        }

        // Updated destination listener - only handle non-fullscreen fragments
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Only update navigation selection for non-fullscreen fragments
            if (!fullScreenFragments.containsKey(destination.id)) {
                when (destination.id) {
                    R.id.playerTimesFragment -> selectNavItem(R.id.nav_prayer_times)
                    R.id.allMediaFragment -> selectNavItem(R.id.nav_all_media)
                    R.id.allCategoriesFragment -> selectNavItem(R.id.nav_all_categories)
                }
            }
        }
    }

    // Enhanced method to show full-screen fragment with transition
    private fun showFullScreenFragment(destinationId: Int) {
        // Hide bottom navigation container
        binding.navPrayerTimes.visibility = android.view.View.GONE
        binding.navAllMedia.visibility = android.view.View.GONE
        binding.navAllCategories.visibility = android.view.View.GONE

        // First hide main content with animation
        binding.mainContentContainer.animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                binding.mainContentContainer.visibility = android.view.View.GONE

                // Show fullscreen container
                binding.fullscreenFragmentContainer.visibility = android.view.View.VISIBLE
                binding.fullscreenFragmentContainer.alpha = 0f

                // Get the appropriate fragment
                val fragmentFactory = fullScreenFragments[destinationId]
                if (fragmentFactory != null) {
                    val fragment = fragmentFactory.invoke()

                    // Add fragment with custom animation
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.animation,
                            R.anim.animation2,
                            R.anim.animation3,
                            R.anim.animation4
                        )
                        .replace(R.id.fullscreen_fragment_container, fragment)
                        .commit()

                    // Animate fullscreen container in
                    binding.fullscreenFragmentContainer.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .start()
                }
            }
            .start()
    }

    // Enhanced method to hide full-screen fragment with transition
    private fun hideFullScreenFragment() {
        if (binding.fullscreenFragmentContainer.visibility == android.view.View.VISIBLE) {
            // Show bottom navigation again
            binding.navPrayerTimes.visibility = android.view.View.VISIBLE
            binding.navAllMedia.visibility = android.view.View.VISIBLE
            binding.navAllCategories.visibility = android.view.View.VISIBLE

            // Animate fullscreen container out
            binding.fullscreenFragmentContainer.animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction {
                    binding.fullscreenFragmentContainer.visibility = android.view.View.GONE

                    // Clear the full-screen container
                    supportFragmentManager.findFragmentById(R.id.fullscreen_fragment_container)?.let { fragment ->
                        supportFragmentManager.beginTransaction()
                            .setCustomAnimations(
                                R.anim.animation,
                                R.anim.animation2,
                                R.anim.animation3,
                                R.anim.animation4
                            )
                            .remove(fragment)
                            .commit()
                    }

                    // Show main content with animation
                    binding.mainContentContainer.visibility = android.view.View.VISIBLE
                    binding.mainContentContainer.alpha = 0f
                    binding.mainContentContainer.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .start()
                }
                .start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hideNoInternetDialog()
        _binding = null
    }
}