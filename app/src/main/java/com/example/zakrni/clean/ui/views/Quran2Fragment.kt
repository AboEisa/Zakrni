package com.example.zakrni.clean.ui.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels

import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zakrni.clean.ui.adapters.Quran2Adapter
import com.example.zakrni.clean.ui.viewmodels.QuranViewModel


import com.example.zakrni.databinding.FragmentQuran2Binding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Quran2Fragment : Fragment() {

    private var _binding: FragmentQuran2Binding? = null
    private val binding get() = _binding!!

    private val viewModel: QuranViewModel by viewModels()
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

        println("DEBUG: Quran2Fragment - onViewCreated called")

        setupRecyclerView()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        println("DEBUG: Quran2Fragment - Setting up RecyclerView")
        adapter = Quran2Adapter { surah ->
            println("DEBUG: Quran2Fragment - Surah clicked: ${surah.number} - ${surah.englishName}")
            // Navigate to QuranFragment with surah number
            navigateToSurahVerses(surah.number)
        }

        binding.quranVersesRecycler.apply {
            adapter = this@Quran2Fragment.adapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }
        println("DEBUG: Quran2Fragment - RecyclerView setup complete")
    }

    private fun observeViewModel() {
        println("DEBUG: Quran2Fragment - Setting up observers")

        viewModel.surahs.observe(viewLifecycleOwner) { surahs ->
            println("DEBUG: Quran2Fragment - Surahs observer triggered with ${surahs.size} surahs")
            if (surahs.isNotEmpty()) {
                println("DEBUG: Quran2Fragment - Submitting ${surahs.size} surahs to adapter")
                surahs.forEachIndexed { index, surah ->
                    println("DEBUG: Quran2Fragment - Surah ${index + 1}: ${surah.number} - ${surah.englishName}")
                }

                adapter.submitList(surahs) {
                    println("DEBUG: Quran2Fragment - List submitted to adapter, items count: ${adapter.itemCount}")
                    binding.quranVersesRecycler.visibility = View.VISIBLE
                }
            } else {
                println("DEBUG: Quran2Fragment - No surahs received, hiding RecyclerView")
                binding.quranVersesRecycler.visibility = View.GONE
                Toast.makeText(context, "No surahs found", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            println("DEBUG: Quran2Fragment - Loading state: $isLoading")
            // You can show/hide a loading indicator here if you have one
            if (isLoading) {
                binding.quranVersesRecycler.visibility = View.GONE
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                println("DEBUG: Quran2Fragment - Error occurred: $error")
                Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                binding.quranVersesRecycler.visibility = View.GONE
                viewModel.clearError()
            }
        }
    }

    private fun setupClickListeners() {
        // Back button
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

    }

    private fun navigateToSurahVerses(surahNumber: Int) {
        try {
            val action = Quran2FragmentDirections
                .actionQuran2FragmentToQuranFragment2()

            // Navigate with bundle since we can't use Safe Args without arguments in action
            val bundle = Bundle().apply {
                putInt("surah_number", surahNumber)
               // putString("surah_name", getItem(position).name) // if you have access to the surah
            }

            findNavController().navigate(action.actionId, bundle)

        } catch (e: Exception) {
            println("DEBUG: Quran2Fragment - Navigation error: ${e.message}")
            Toast.makeText(context, "Error navigating: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}