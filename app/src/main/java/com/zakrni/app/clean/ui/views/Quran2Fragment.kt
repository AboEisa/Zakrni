package com.zakrni.app.clean.ui.views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.zakrni.app.R
import com.zakrni.app.clean.ui.adapters.Quran2Adapter
import com.zakrni.app.clean.ui.utils.LocaleHelper
import com.zakrni.app.clean.ui.viewmodels.QuranViewModel
import com.zakrni.app.databinding.FragmentQuran2Binding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class Quran2Fragment : Fragment() {
    private var _binding: FragmentQuran2Binding? = null
    private val binding get() = _binding!!

    private val viewModel: QuranViewModel by activityViewModels()
    private lateinit var adapter: Quran2Adapter
    private var isNavigating = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuran2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Quran2Fragment", "onViewCreated called")
        setupRecyclerView()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = Quran2Adapter { surah ->
            Log.d("Quran2Fragment", "Surah clicked: ${surah.number} - ${surah.name}")
            navigateToSurahVerses(surah.number)
        }
        binding.quranVersesRecycler.apply {
            adapter = this@Quran2Fragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.surahs.collectLatest { surahs ->
                        Log.d("Quran2Fragment", "Received ${surahs.size} surahs")
                        if (surahs.isNotEmpty()) {
                            adapter.submitList(surahs)
                            showContent()
                        } else {
                            binding.quranVersesRecycler.visibility = View.GONE
                        }
                    }
                }

                launch {
                    viewModel.isLoading.collectLatest { isLoading ->
                        if (isLoading && adapter.currentList.isEmpty()) {
                            showLoading()
                        }
                    }
                }

                launch {
                    viewModel.error.collectLatest { error ->
                        error?.let {
                            Log.e("Quran2Fragment", "Error: $it")
                            hideLoading()
                            viewModel.clearError()
                        }
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.apply {
            quranVersesRecycler.visibility = View.GONE
            loadingContainer.visibility = View.VISIBLE
            val pulseAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.pulse_fade)
            shimmerContainer.startAnimation(pulseAnim)
        }
    }

    private fun hideLoading() {
        binding.apply {
            shimmerContainer.clearAnimation()
            loadingContainer.visibility = View.GONE
        }
    }

    private fun showContent() {
        binding.apply {
            shimmerContainer.clearAnimation()
            loadingContainer.visibility = View.GONE

            quranVersesRecycler.alpha = 0f
            quranVersesRecycler.visibility = View.VISIBLE
            quranVersesRecycler.animate()
                .alpha(1f)
                .setDuration(350)
                .start()
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun navigateToSurahVerses(surahNumber: Int) {
        Log.d("Quran2Fragment", "🚀 navigateToSurahVerses called with surahNumber: $surahNumber")

        if (surahNumber !in 1..114 || isNavigating) return
        isNavigating = true

        // Show loading overlay immediately — no white screen lag
        val isArabic = LocaleHelper.isArabic(requireContext())
        val surah = adapter.currentList.find { it.number == surahNumber }
        val surahName = if (isArabic) {
            surah?.name ?: "السورة"
        } else {
            surah?.englishName ?: "Surah"
        }
        binding.navLoadingOverlay.visibility = View.VISIBLE
        binding.navLoadingSurahName.text = if (isArabic) {
            "جاري فتح $surahName..."
        } else {
            "Opening $surahName..."
        }

        // Pre-load the surah data so QuranFragment has it ready
        viewModel.loadQuranVerses(surahNumber)

        // Wait for data to actually load, then navigate instantly
        lifecycleScope.launch {
            // Poll until data is ready or timeout after 5 seconds
            var waited = 0L
            while (viewModel.isLoading.value && waited < 5000L) {
                delay(50)
                waited += 50
            }

            try {
                val bundle = Bundle().apply {
                    putInt("surahNumber", surahNumber)
                }
                findNavController().navigate(R.id.action_quran2Fragment_to_quranFragment, bundle)
                Log.d("Quran2Fragment", "✅ Navigation successful to surah $surahNumber (waited ${waited}ms)")
            } catch (e: Exception) {
                Log.e("Quran2Fragment", "❌ Navigation error: ${e.message}", e)
                binding.navLoadingOverlay.visibility = View.GONE
            } finally {
                isNavigating = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reset overlay when coming back from QuranFragment
        isNavigating = false
        _binding?.navLoadingOverlay?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
