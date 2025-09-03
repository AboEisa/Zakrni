package com.example.zakrni.clean.ui.views

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
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
        setupClickListeners()
        observeViewModel()

        // Get surah information from arguments
        val surahNumber = arguments?.getInt("surah_number", 1) ?: 1
        val surahName = arguments?.getString("surah_name", "") ?: ""

        println("DEBUG: QuranFragment - Loading Surah $surahNumber: $surahName")

        // Load the verses for this surah
        viewModel.loadQuranVerses(surahNumber)
    }

    private fun setupRecyclerView() {
        adapter = QuranAdapter()
        binding.quranVersesRecycler.apply {
            adapter = this@QuranFragment.adapter
            layoutManager = LinearLayoutManager(context)
            // Add some spacing between items
            addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(
                context,
                androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
            ))
        }
    }

    private fun setupClickListeners() {
        // Back button
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Play button (if you implement audio later)
        binding.playButton?.setOnClickListener {
            Toast.makeText(context, "Audio playback coming soon", Toast.LENGTH_SHORT).show()
        }

        // Share button
        binding.shareButton.setOnClickListener {
            shareCurrentSurah()
        }


    }

    private fun observeViewModel() {
        // Observe verses
        viewModel.verses.observe(viewLifecycleOwner) { verses ->
            println("DEBUG: QuranFragment - Received ${verses.size} verses")
            if (verses.isNotEmpty()) {
                adapter.submitList(verses)
                binding.quranVersesRecycler.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            } else {
                binding.quranVersesRecycler.visibility = View.GONE
                Toast.makeText(context, "No verses found", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe current surah info
        viewModel.currentSurah.observe(viewLifecycleOwner) { surah ->
            surah?.let {
                updateSurahHeader(it)
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe errors
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
                binding.progressBar.visibility = View.GONE
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

            // Hide Bismillah for Surah At-Tawbah (9) and Al-Fatihah (1) since it's already in the first verse
            bismillah.visibility = when(surah.number) {
                1, 9 -> View.GONE  // Al-Fatihah and At-Tawbah
                else -> View.VISIBLE
            }
        }
    }

    private fun shareCurrentSurah() {
        val surahName = binding.surahHeader.text.toString()
        val shareText = "Reading $surahName from the Quran"

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(shareIntent, "Share Surah"))
    }

    private fun toggleBookmark() {
        Toast.makeText(context, "Bookmark feature coming soon", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}