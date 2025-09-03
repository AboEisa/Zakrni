package com.example.zakrni.clean.ui.views

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zakrni.R
import com.example.zakrni.clean.domain.models.DomainSurah
import com.example.zakrni.clean.ui.adapters.QuranAdapter
import com.example.zakrni.clean.ui.viewmodels.QuranViewModel

import com.example.zakrni.databinding.FragmentQuranBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuranFragment : Fragment() {
    private var _binding: FragmentQuranBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuranViewModel by viewModels()
    private lateinit var adapter: QuranAdapter

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

        setupRecyclerView()
        observeViewModel()
        setupClickListeners()

        // Get surah number from arguments
        val surahNumber = arguments?.getInt("surah_number", 1) ?: 1
        val surahName = arguments?.getString("surah_name", "") ?: ""

        println("DEBUG: QuranFragment - Loading Surah $surahNumber: $surahName")

        // Load the verses for this surah
        viewModel.loadQuranVerses(surahNumber)
    }

    private fun setupClickListeners() {
        // Back button
        binding.backButton?.setOnClickListener {
            findNavController().navigateUp()
        }

        // Play button
        binding.playButton?.setOnClickListener {
            // TODO: Implement audio playback
            Toast.makeText(context, "Audio playback coming soon", Toast.LENGTH_SHORT).show()
        }

        // Share button
        binding.shareButton?.setOnClickListener {
            shareCurrentVerse()
        }

        // Bookmark button
        binding.bookmarkButton?.setOnClickListener {
            toggleBookmark()
        }
    }

    private fun observeViewModel() {
        viewModel.verses.observe(viewLifecycleOwner) { verses ->
            println("DEBUG: QuranFragment - Received ${verses.size} verses")
            if (verses.isNotEmpty()) {
                adapter.submitList(verses)
                binding.quranVersesRecycler.visibility = View.VISIBLE
            } else {
                binding.quranVersesRecycler.visibility = View.GONE
                Toast.makeText(context, "No verses found", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.currentSurah.observe(viewLifecycleOwner) { surah ->
            surah?.let {
                updateSurahHeader(it)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Show/hide loading indicator
            binding.audioProgress?.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun updateSurahHeader(surah: DomainSurah) {
        binding.apply {
            surahHeader.text = surah.name
            surahType.text = when(surah.revelationType.lowercase()) {
                "meccan" -> "مكية"
                "medinan" -> "مدنية"
                else -> surah.revelationType
            }
            versesCount.text = "${surah.ayahs.size} آيات"

            // Hide Bismillah for Surah At-Tawbah (9) and show for others except Al-Fatihah (1)
            bismillah.visibility = when(surah.number) {
                9 -> View.GONE
                else -> View.VISIBLE
            }
        }
    }

    private fun shareCurrentVerse() {
        // Get currently visible verse or selected verse
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Verse text here") // Add actual verse text
        }
        startActivity(Intent.createChooser(shareIntent, "Share Verse"))
    }

    private fun toggleBookmark() {
        // TODO: Implement bookmark functionality
        Toast.makeText(context, "Bookmark feature coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun setupRecyclerView() {
        adapter = QuranAdapter()
        binding.quranVersesRecycler.apply {
            adapter = this@QuranFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }
    }

//    private fun observeViewModel() {
//        viewModel.verses.observe(viewLifecycleOwner) { verses ->
//            println("DEBUG: Received ${verses.size} verses")
//            if (verses.isNotEmpty()) {
//                adapter.submitList(verses)
//                binding.quranVersesRecycler.visibility = View.VISIBLE
//            } else {
//                println("DEBUG: No verses received")
//                Toast.makeText(context, "No verses found", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        viewModel.surahName.observe(viewLifecycleOwner) { name ->
//            binding.surahHeader.text = name
//            println("DEBUG: Surah name set to: $name")
//        }
//
//        viewModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
//            updatePlayButton(isPlaying)
//        }
//
//        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
//            println("DEBUG: Loading state: $isLoading")
//            // If you have a progress bar, show/hide it here
//            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
//        }
//
//        // Handle nullable error properly
//        viewModel.error.observe(viewLifecycleOwner) { error ->
//            if (error != null) {
//                println("DEBUG: Error occurred: $error")
//                Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
//                // Clear the error after showing it
//                viewModel.clearError()
//            }
//        }
//    }

    private fun updatePlayButton(isPlaying: Boolean?) {
        binding.playButton.setImageResource(
            if (isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}