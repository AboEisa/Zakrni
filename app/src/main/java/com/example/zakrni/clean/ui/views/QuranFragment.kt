package com.example.zakrni.clean.ui.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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

//    private val viewModel: QuranViewModel by viewModels()
    private val viewModel: QuranViewModel by activityViewModels()
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
        try {
            val args = QuranFragmentArgs.fromBundle(requireArguments())
            val surahNumber = args.surahNumber
            Log.d("QuranFragment", "Received surah number: $surahNumber")
            setupRecyclerView()
            setupClickListeners()
            observeViewModel()
            viewModel.loadQuranVerses(surahNumber)
        } catch (e: Exception) {
            Log.e("QuranFragment", "Error: No Surah selected - ${e.message}")
            Toast.makeText(requireContext(), R.string.no_surah_selected, Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
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

        binding.playButton.setOnClickListener {
            Toast.makeText(requireContext(), R.string.audio_playback_coming_soon, Toast.LENGTH_SHORT).show()
        }

        binding.shareButton.setOnClickListener {
            shareCurrentSurah()
        }
    }

    private fun observeViewModel() {
        viewModel.verses.observe(viewLifecycleOwner) { verses ->
            Log.d("QuranFragment", "Received ${verses.size} verses")
            binding.quranVersesRecycler.visibility = if (verses.isNotEmpty()) {
                adapter.submitList(verses)
                View.VISIBLE
            } else {
                Toast.makeText(requireContext(), R.string.no_verses_found, Toast.LENGTH_SHORT).show()
                View.GONE
            }
        }

        viewModel.currentSurah.observe(viewLifecycleOwner) { surah ->
            surah?.let {
                updateSurahHeader(it)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Log.e("QuranFragment", "Error: $it")
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun updateSurahHeader(surah: DomainSurah) {
        binding.apply {
            surahHeader.text = surah.name
            surahType.text = when (surah.revelationType.lowercase()) {
                "meccan" -> getString(R.string.meccan)
                "medinan" -> getString(R.string.medinan)
                else -> surah.revelationType
            }
            versesCount.text = getString(R.string.verses_count, surah.ayahs.size)
            bismillah.visibility = when (surah.number) {
                1, 9 -> View.GONE
                else -> View.VISIBLE
            }
        }
    }

    private fun shareCurrentSurah() {
        val surahName = binding.surahHeader.text.toString()
        val shareText = getString(R.string.share_surah_text, surahName)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_surah_title)))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}