package com.zakrni.app.clean.ui.views

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.zakrni.app.R
import com.zakrni.app.clean.ui.models.PresentationPrayerTimesResponse
import com.zakrni.app.clean.ui.utils.DialogStyler
import com.zakrni.app.clean.ui.utils.LocaleHelper
import com.zakrni.app.clean.ui.utils.PrayerTimeUtils
import com.zakrni.app.clean.ui.viewmodels.PrayerTimesViewModel
import com.zakrni.app.databinding.FragmentPrayerTimesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PrayerTimesFragment : Fragment() {

    private var _binding: FragmentPrayerTimesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PrayerTimesViewModel by activityViewModels()

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                viewModel.checkLocationPermission()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                viewModel.checkLocationPermission()
            }
            else -> {
                showLocationPermissionDenied()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrayerTimesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
        
        // Only request permissions if not already granted - ViewModel is shared from activity
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissions()
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check permissions when coming back from settings
        viewModel.checkFullScreenIntentPermission()
        checkFullScreenIntentPermission()
    }

    private fun checkFullScreenIntentPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            viewLifecycleOwner.lifecycleScope.launch {
                if (!viewModel.hasFullScreenIntentPermission.value) {
                    val dialog = DialogStyler.builder(requireContext())
                        .setTitle(R.string.fullscreen_permission_title)
                        .setMessage(R.string.fullscreen_permission_message)
                        .setPositiveButton(R.string.fullscreen_permission_open_settings) { _, _ ->
                            viewModel.requestFullScreenIntentPermission()
                        }
                        .setNegativeButton(R.string.fullscreen_permission_later, null)
                        .create()

                    dialog.show()
                    DialogStyler.apply(dialog, requireContext())
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnViewAllTimes.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_playerTimesFragment_to_allPrayerTimesFragment)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun requestLocationPermissions() {
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    }
                }

                launch {
                    viewModel.error.collect { error ->
                        if (error != null) {
                            showError(error)
                        }
                    }
                }

                launch {
                    viewModel.prayerTimes.collect { prayerTimes ->
                        prayerTimes?.let { updateUI(it) }
                    }
                }

                launch {
                    viewModel.nextPrayer.collect { nextPrayer ->
                        nextPrayer?.let { updateNextPrayerUI(it) }
                    }
                }

                launch {
                    viewModel.remainingTime.collect { remainingTime ->
                        updateCountdownTimer(remainingTime)
                    }
                }

                // Add observer for location name
                launch {
                    viewModel.locationName.collect { locationName ->
                        binding.tvLocation.text = locationName
                    }
                }
            }
        }
    }

    private fun updateUI(prayerTimes: PresentationPrayerTimesResponse) {
        with(binding) {
            tvDateHijri.text = PrayerTimeUtils.formatHijriDateFromApi(prayerTimes)
            tvDateGregorian.text = PrayerTimeUtils.formatGregorianDateFromApi(prayerTimes)
        }
    }

    private fun updateNextPrayerUI(nextPrayer: PrayerTimeUtils.PrayerInfo) {
        with(binding) {
            val prayerDisplayName = if (LocaleHelper.isArabic(requireContext())) {
                nextPrayer.nameArabic
            } else {
                nextPrayer.name
            }
            tvPrayerName.text = getString(R.string.time_for_prayer_format, prayerDisplayName)
        }
    }

    private fun updateCountdownTimer(remainingTime: String) {
        with(binding) {
            tvRemaining.text = remainingTime
            when {
                remainingTime.startsWith("00:0") -> {
                    tvRemaining.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.status_error)
                    )
                }
                remainingTime.startsWith("00:") -> {
                    tvRemaining.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.status_warning)
                    )
                }
                else -> {
                    tvRemaining.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.prayer_text_white)
                    )
                }
            }
        }
    }

    private fun showError(error: String) {
        Toast.makeText(
            requireContext(),
            getString(R.string.error_message_format, error),
            Toast.LENGTH_LONG
        ).show()
        binding.progressBar.visibility = View.GONE
    }

    private fun showLocationPermissionDenied() {
        Toast.makeText(
            requireContext(),
            getString(R.string.location_permission_required_for_prayers),
            Toast.LENGTH_LONG
        ).show()
        binding.progressBar.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
