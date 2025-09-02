package com.example.zakrni.clean.ui.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zakrni.R
import com.example.zakrni.clean.ui.adapters.QuranAdapter
import com.example.zakrni.clean.viewmodels.QuranViewModel
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

        // Load Surah 1 (Al-Fatiha) as default
        viewModel.loadQuranVerses(1)

        binding.playButton.setOnClickListener {
            viewModel.togglePlay()
        }
    }

    private fun setupRecyclerView() {
        adapter = QuranAdapter()
        binding.quranVersesRecycler.apply {
            adapter = this@QuranFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeViewModel() {
        viewModel.verses.observe(viewLifecycleOwner) { verses ->
            println("DEBUG: Received ${verses.size} verses")
            if (verses.isNotEmpty()) {
                adapter.submitList(verses)
                binding.quranVersesRecycler.visibility = View.VISIBLE
            } else {
                println("DEBUG: No verses received")
                Toast.makeText(context, "No verses found", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.surahName.observe(viewLifecycleOwner) { name ->
            binding.surahHeader.text = name
            println("DEBUG: Surah name set to: $name")
        }

        viewModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
            updatePlayButton(isPlaying)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            println("DEBUG: Loading state: $isLoading")
            // If you have a progress bar, show/hide it here
            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Handle nullable error properly
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                println("DEBUG: Error occurred: $error")
                Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                // Clear the error after showing it
                viewModel.clearError()
            }
        }
    }

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