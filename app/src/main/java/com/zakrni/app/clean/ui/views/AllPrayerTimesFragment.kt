package com.zakrni.app.clean.ui.views

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.zakrni.app.R
import com.zakrni.app.clean.ui.models.PresentationPrayerTimesResponse
import com.zakrni.app.clean.ui.models.PresentationTimings
import com.zakrni.app.clean.ui.utils.LocaleHelper
import com.zakrni.app.clean.ui.utils.PrayerTimeUtils
import com.zakrni.app.clean.ui.viewmodels.PrayerTimesViewModel
import com.zakrni.app.databinding.FragmentAllPrayerTimesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class AllPrayerTimesFragment : Fragment() {

    private var _binding: FragmentAllPrayerTimesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PrayerTimesViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllPrayerTimesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Back button
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        
        setupObservers()
        highlightCurrentPrayer()
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
                        prayerTimes?.let { updatePrayerTimesUI(it) }
                    }
                }

                launch {
                    viewModel.nextPrayer.collect { nextPrayer ->
                        nextPrayer?.let { updateNextPrayerUI(it) }
                    }
                }

                launch {
                    viewModel.remainingTime.collect { remainingTime ->
                        binding.tvRemainingTime.text = remainingTime
                    }
                }

                launch {
                    viewModel.locationName.collect { locationName ->
                        binding.tvLocation.text = locationName
                    }
                }
            }
        }
    }

    private fun updatePrayerTimesUI(prayerTimes: PresentationPrayerTimesResponse) {
        val timings = prayerTimes.data.timings
        
        binding.apply {
            // Update dates
            tvHijriDate.text = PrayerTimeUtils.formatHijriDateFromApi(prayerTimes)
            tvGregorianDate.text = PrayerTimeUtils.formatGregorianDateFromApi(prayerTimes)
            
            // Update prayer times
            tvFajrTime.text = formatTime(timings.Fajr)
            tvSunriseTime.text = formatTime(timings.Sunrise)
            tvDhuhrTime.text = formatTime(timings.Dhuhr)
            tvAsrTime.text = formatTime(timings.Asr)
            tvMaghribTime.text = formatTime(timings.Maghrib)
            tvIshaTime.text = formatTime(timings.Isha)
        }
        
        highlightCurrentPrayer()
    }

    private fun formatTime(time: String): String {
        return try {
            val cleanTime = time.split(" ")[0] // Remove timezone offset
            val inputFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
            val date = inputFormat.parse(cleanTime)
            date?.let { outputFormat.format(it) } ?: time
        } catch (e: Exception) {
            time
        }
    }

    private fun updateNextPrayerUI(nextPrayer: PrayerTimeUtils.PrayerInfo) {
        binding.apply {
            val prayerDisplayName = if (LocaleHelper.isArabic(requireContext())) {
                nextPrayer.nameArabic
            } else {
                nextPrayer.name
            }
            tvNextPrayerName.text = getString(R.string.next_prayer_format, prayerDisplayName)
        }
    }

    private fun highlightCurrentPrayer() {
        try {
            val timings = viewModel.prayerTimes.value?.data?.timings ?: return
            val (currentPrayer, _) = PrayerTimeUtils.getCurrentAndNextPrayer(timings)
            binding.apply {
                resetCardHighlights()
                val currentCard = when (currentPrayer?.name) {
                    "Fajr" -> cardFajr
                    "Dhuhr" -> cardDhuhr
                    "Asr" -> cardAsr
                    "Maghrib" -> cardMaghrib
                    "Isha" -> cardIsha
                    else -> null
                }
                currentCard?.let {
                    // Use a subtle highlight for active prayer on dark bg
                    val accent = ContextCompat.getColor(requireContext(), R.color.islamic_gold)
                    val gradientDrawable = GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        intArrayOf(
                            ColorUtils.setAlphaComponent(accent, 0x20),
                            ColorUtils.setAlphaComponent(accent, 0x10)
                        )
                    )
                    gradientDrawable.cornerRadius = 0f
                    it.background = gradientDrawable

                    // Smooth scale animation
                    val scaleX = ObjectAnimator.ofFloat(it, View.SCALE_X, 0.97f, 1.02f, 1f)
                    val scaleY = ObjectAnimator.ofFloat(it, View.SCALE_Y, 0.97f, 1.02f, 1f)
                    val alpha = ObjectAnimator.ofFloat(it, View.ALPHA, 0.7f, 1f)
                    AnimatorSet().apply {
                        playTogether(scaleX, scaleY, alpha)
                        duration = 600
                        interpolator = OvershootInterpolator(1.2f)
                        start()
                    }
                }
            }
        } catch (e: Exception) {
            // Prevent crash if resource not found
            e.printStackTrace()
        }
    }

    private fun resetCardHighlights() {
        try {
            binding.apply {
                listOf(cardFajr, cardSunrise, cardDhuhr, cardAsr, cardMaghrib, cardIsha).forEach { card ->
                    card.setBackgroundColor(Color.TRANSPARENT)
                    card.scaleX = 1f
                    card.scaleY = 1f
                    card.alpha = 1f
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showError(error: String) {
        Toast.makeText(
            requireContext(),
            getString(R.string.error_message_format, error),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLocationPermissionDenied() {
        Toast.makeText(
            requireContext(),
            getString(R.string.location_permission_required_for_prayers),
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
