package com.example.zakrni.clean.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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
    private var isUserSeeking = false

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

        currentSurahNumber = getSurahNumber()

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        if (currentSurahNumber in 1..114) {
            viewModel.loadQuranVerses(currentSurahNumber)

            // Auto-play audio when surah is loaded
            viewModel.currentSurah.observe(viewLifecycleOwner) { surah ->
                surah?.let {
                    // Check if we haven't already started playing audio for this surah
                    if (!viewModel.isPlayingSurah(currentSurahNumber) &&
                        !viewModel.hasAutoPlayedSurah(currentSurahNumber)) {
                        lifecycleScope.launch {
                            // Small delay to let UI settle
                            kotlinx.coroutines.delay(500)
                            viewModel.markSurahAsAutoPlayed(currentSurahNumber)
                            viewModel.playSurahAudio(currentSurahNumber, autoPlay = true)
                        }
                    }
                }
            }
        }
    }

    private fun getSurahNumber(): Int {
        return try {
            val args = QuranFragmentArgs.fromBundle(requireArguments())
            val safeArgsSurah = args.surahNumber
            if (safeArgsSurah > 0) {
                safeArgsSurah
            } else {
                throw Exception("Invalid number: $safeArgsSurah")
            }
        } catch (e: Exception) {
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

        // Play/Pause button
        binding.playButton.setOnClickListener {
            if (viewModel.isPlayingSurah(currentSurahNumber)) {
                viewModel.pauseAudio()
            } else if (viewModel.audioPlayerManager.isCurrentSurah(currentSurahNumber)) {
                viewModel.resumeAudio()
            } else {
                viewModel.playSurahAudio(currentSurahNumber)
            }
        }

        // Setup SeekBar listener
        binding.audioProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    isUserSeeking = true
                    updateCurrentTime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false
                seekBar?.let { bar ->
                    val duration = viewModel.getAudioDuration()
                    if (duration > 0) {
                        val position = (bar.progress * duration) / 100
                        viewModel.seekToPosition(position)
                    }
                }
            }
        })
    }

    private fun updateCurrentTime(progress: Int) {
        val duration = viewModel.getAudioDuration()
        if (duration > 0) {
            val currentPosition = (progress * duration) / 100
            binding.currentTime.text = formatTime(currentPosition)
        }
    }

    private fun formatTime(milliseconds: Int): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }

    private fun observeViewModel() {
        viewModel.verses.observe(viewLifecycleOwner) { verses ->
            if (verses.isNotEmpty()) {
                adapter.submitList(verses)
                binding.quranVersesRecycler.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            } else {
                binding.quranVersesRecycler.visibility = View.GONE
            }
        }

        viewModel.currentSurah.observe(viewLifecycleOwner) { surah ->
            surah?.let {
                updateSurahHeader(it)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                binding.progressBar.visibility = View.GONE
                // You can show a Snackbar or Toast here
                viewModel.clearError()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe audio states
        lifecycleScope.launch {
            viewModel.isPlaying.collect { isPlaying ->
                val isCurrentSurah = viewModel.currentPlayingSurah.value == currentSurahNumber
                updatePlayPauseButton(isPlaying && isCurrentSurah)
            }
        }

        lifecycleScope.launch {
            viewModel.currentPlayingSurah.collect { playingSurahNumber ->
                val isCurrentSurah = playingSurahNumber == currentSurahNumber
                updatePlayPauseButton(viewModel.isPlaying.value && isCurrentSurah)
            }
        }

        lifecycleScope.launch {
            viewModel.audioProgress.collect { progress ->
                if (!isUserSeeking) {
                    binding.audioProgress.progress = progress.toInt()
                    updateCurrentTime(progress.toInt())
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isAudioLoading.collect { isLoading ->
                binding.playButton.isEnabled = !isLoading
            }
        }

        // Observe audio duration
        lifecycleScope.launch {
            viewModel.audioPlayerManager.duration.collect { duration ->
                if (duration > 0) {
                    binding.totalTime.text = formatTime(duration)
                } else {
                    binding.totalTime.text = "0:00"
                }
            }
        }
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        if (isPlaying) {
            binding.playButton.setImageResource(android.R.drawable.ic_media_pause)
        } else {
            binding.playButton.setImageResource(android.R.drawable.ic_media_play)
        }
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

    override fun onPause() {
        super.onPause()
        // Optionally pause audio when fragment is paused
        if (viewModel.isPlayingSurah(currentSurahNumber)) {
            viewModel.pauseAudio()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Don't stop audio completely as user might want to continue listening
        // Just make sure to pause if it's for this surah
        if (viewModel.isPlayingSurah(currentSurahNumber)) {
            viewModel.pauseAudio()
        }
        _binding = null
    }
}