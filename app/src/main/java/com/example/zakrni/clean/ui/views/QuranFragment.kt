package com.example.zakrni.clean.ui.views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zakrni.R
import com.example.zakrni.clean.domain.models.DomainSurah
import com.example.zakrni.clean.ui.adapters.QuranAdapter
import com.example.zakrni.clean.ui.viewmodels.QuranViewModel
import com.example.zakrni.databinding.FragmentQuranBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QuranFragment : Fragment() {
    private var _binding: FragmentQuranBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuranViewModel by activityViewModels()
    private lateinit var adapter: QuranAdapter
    private var currentSurahNumber: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuranBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("QuranFragment", "onViewCreated started")

        currentSurahNumber = getSurahNumber()
        Log.d("QuranFragment", "Final surah number: $currentSurahNumber")

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        if (currentSurahNumber in 1..114) {
            Log.d("QuranFragment", "Loading verses for surah: $currentSurahNumber")
            viewModel.loadQuranVerses(currentSurahNumber)
        } else {
            Log.e("QuranFragment", "Invalid surah number: $currentSurahNumber")
        }
    }

    private fun getSurahNumber(): Int {
        return try {
            // Try Safe Args first
            val args = QuranFragmentArgs.fromBundle(requireArguments())
            val safeArgsSurah = args.surahNumber
            Log.d("QuranFragment", "Safe Args surah number: $safeArgsSurah")
            if (safeArgsSurah > 0) {
                safeArgsSurah
            } else {
                throw Exception("Invalid number: $safeArgsSurah")
            }
        } catch (e: Exception) {
            Log.w("QuranFragment", "Safe Args failed: ${e.message}")
            // Try regular Bundle
            arguments?.getInt("surahNumber", -1) ?: -1
        }
    }

    private fun setupRecyclerView() {
        adapter = QuranAdapter()
        binding.quranVersesRecycler.apply {
            adapter = this@QuranFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(
                androidx.recyclerview.widget.DividerItemDecoration(
                    requireContext(),
                    androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
                )
            )
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Show audio controls
        binding.audioControls.visibility = View.VISIBLE

        binding.playButton.setOnClickListener {
            // Use lifecycleScope to collect the StateFlow value
            lifecycleScope.launch {
                viewModel.isPlaying.collect { isPlaying ->
                    if (isPlaying) {
                        viewModel.pauseAudio()
                    } else {
                        viewModel.playSurahAudio(currentSurahNumber)
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.verses.observe(viewLifecycleOwner) { verses ->
            Log.d("QuranFragment", "Received ${verses.size} verses")
            if (verses.isNotEmpty()) {
                adapter.submitList(verses)
                binding.quranVersesRecycler.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            } else {
                binding.quranVersesRecycler.visibility = View.GONE
                if (viewModel.isLoading.value != true) {
                    Log.d("QuranFragment", "No verses found for this surah")
                }
            }
        }

        viewModel.currentSurah.observe(viewLifecycleOwner) { surah ->
            surah?.let {
                Log.d("QuranFragment", "Updating surah info: ${it.name}")
                updateSurahHeader(it)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Log.e("QuranFragment", "Error: $it")
                binding.progressBar.visibility = View.GONE
                viewModel.clearError()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.d("QuranFragment", "Loading state: $isLoading")
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe audio state using lifecycleScope for StateFlow
        lifecycleScope.launch {
            viewModel.isPlaying.collect { isPlaying ->
                updatePlayPauseButton(isPlaying)
            }
        }

        lifecycleScope.launch {
            viewModel.audioProgress.collect { progress ->
                binding.audioProgress.progress = progress.toInt()
            }
        }
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        binding.playButton.setImageResource(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )
    }

    private fun updateSurahHeader(surah: DomainSurah) {
        binding.apply {
            surahHeader.text = surah.name
            surahType.text = when (surah.revelationType.lowercase()) {
                "meccan" -> "مكية"
                "medinan" -> "مدنية"
                else -> surah.revelationType
            }
            versesCount.text = "${surah.ayahs.size} آية"
            bismillah.visibility = when (surah.number) {
                1, 9 -> View.GONE
                else -> View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.pauseAudio()
        _binding = null
    }
}