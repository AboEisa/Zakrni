package com.example.zakrni.clean.ui.views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zakrni.R
import com.example.zakrni.clean.ui.adapters.Quran2Adapter
import com.example.zakrni.clean.ui.viewmodels.QuranViewModel
import com.example.zakrni.databinding.FragmentQuran2Binding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Quran2Fragment : Fragment() {
    private var _binding: FragmentQuran2Binding? = null
    private val binding get() = _binding!!

    private val viewModel: QuranViewModel by activityViewModels()
    private lateinit var adapter: Quran2Adapter

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
        Log.d("Quran2Fragment", "Setting up RecyclerView")
        adapter = Quran2Adapter { surah ->
            Log.d("Quran2Fragment", "Surah clicked: ${surah.number} - ${surah.englishName}")
            navigateToSurahVerses(surah.number)
        }
        binding.quranVersesRecycler.apply {
            adapter = this@Quran2Fragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
        Log.d("Quran2Fragment", "RecyclerView setup complete")
    }

    private fun observeViewModel() {
        Log.d("Quran2Fragment", "Setting up observers")
        viewModel.surahs.observe(viewLifecycleOwner) { surahs ->
            Log.d("Quran2Fragment", "Surahs observer triggered with ${surahs.size} surahs")
            if (surahs.isNotEmpty()) {
                Log.d("Quran2Fragment", "Submitting ${surahs.size} surahs to adapter")
                surahs.forEachIndexed { index, surah ->
                    Log.d("Quran2Fragment", "Surah ${index + 1}: ${surah.number} - ${surah.englishName}")
                }
                adapter.submitList(surahs) {
                    Log.d("Quran2Fragment", "List submitted to adapter, items count: ${adapter.itemCount}")
                    binding.quranVersesRecycler.visibility = View.VISIBLE
                }
            } else {
                Log.d("Quran2Fragment", "No surahs received, hiding RecyclerView")
                binding.quranVersesRecycler.visibility = View.GONE
                 Toast.makeText(requireContext(), R.string.no_surahs_found, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.d("Quran2Fragment", "Loading state: $isLoading")
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.quranVersesRecycler.visibility = if (isLoading || viewModel.surahs.value.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Log.e("Quran2Fragment", "Error occurred: $it")
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                binding.quranVersesRecycler.visibility = View.GONE
                viewModel.clearError()
            }
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun navigateToSurahVerses(surahNumber: Int) {
        try {
            val action = Quran2FragmentDirections.actionQuran2FragmentToQuranFragment(surahNumber)
            findNavController().navigate(action)
            Log.d("Quran2Fragment", "Navigated to QuranFragment with surahNumber: $surahNumber")
        } catch (e: Exception) {
            Log.e("Quran2Fragment", "Navigation error: ${e.message}")
            Toast.makeText(requireContext(), R.string.error_navigating, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}