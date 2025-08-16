package com.example.zakrni.clean.ui.views

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.zakrni.R
import com.example.zakrni.clean.ui.models.PresentationPrayerTimesResponse
import com.example.zakrni.clean.ui.utils.PrayerTimeUtils
import com.example.zakrni.clean.ui.viewmodels.PrayerTimesViewModel
import com.example.zakrni.databinding.FragmentPrayerTimesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PrayerTimesFragment : Fragment() {

    private var _binding: FragmentPrayerTimesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PrayerTimesViewModel by viewModels()

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
        checkLocationPermissions()
    }

    private fun checkLocationPermissions() {
        when {
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.checkLocationPermission()
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
                    viewModel.currentPrayer.collect { currentPrayer ->
                        currentPrayer?.let { updateNextPrayerUI(it) }
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
            // Hijri date
            tvDateHijri.text = PrayerTimeUtils.formatHijriDate(prayerTimes.data.date.hijri.date)
            // Gregorian date
            tvDateGregorian.text = PrayerTimeUtils.formatGregorianDate(prayerTimes.data.date.gregorian.date)
        }
    }



    private fun updateNextPrayerUI(nextPrayer: PrayerTimeUtils.PrayerInfo) {
        with(binding) {
            tvPrayerName.text = "لصلاة ${nextPrayer.nameArabic}"
        }
    }

    private fun updateCountdownTimer(remainingTime: String) {
        with(binding) {
            tvRemaining.text = remainingTime
            when {
                remainingTime.startsWith("00:0") -> {
                    tvRemaining.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
                }
                remainingTime.startsWith("00:") -> {
                    tvRemaining.setTextColor(requireContext().getColor(android.R.color.holo_orange_dark))
                }
                else -> {
                    tvRemaining.setTextColor(requireContext().getColor(R.color.selected_text_color))
                }
            }
        }
    }

    private fun showError(error: String) {
        Toast.makeText(requireContext(), "خطأ: $error", Toast.LENGTH_LONG).show()
        binding.progressBar.visibility = View.GONE
    }

    private fun showLocationPermissionDenied() {
        Toast.makeText(
            requireContext(),
            "يرجى السماح بالوصول إلى الموقع لعرض مواقيت الصلاة",
            Toast.LENGTH_LONG
        ).show()
        binding.progressBar.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}