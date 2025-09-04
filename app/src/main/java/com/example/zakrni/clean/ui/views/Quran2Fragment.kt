package com.example.zakrni.clean.ui.views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        viewModel.surahs.observe(viewLifecycleOwner) { surahs ->
            Log.d("Quran2Fragment", "Received ${surahs.size} surahs")
            if (surahs.isNotEmpty()) {
                adapter.submitList(surahs)
                binding.quranVersesRecycler.visibility = View.VISIBLE
            } else {
                binding.quranVersesRecycler.visibility = View.GONE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Log.e("Quran2Fragment", "Error: $it")
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
            Log.d("Quran2Fragment", "Navigating to surah: $surahNumber")

            if (surahNumber !in 1..114) {
                Log.e("Quran2Fragment", "Invalid surah number: $surahNumber")
                return
            }

            try {
                Log.d("Quran2Fragment", "Trying Safe Args navigation...")
                val action = Quran2FragmentDirections.actionQuran2FragmentToQuranFragment(surahNumber)
                findNavController().navigate(action)
                Log.d("Quran2Fragment", "Safe Args navigation successful")
            } catch (e: Exception) {
                Log.w("Quran2Fragment", "Safe Args failed, trying Bundle method: ${e.message}")

                val bundle = Bundle().apply {
                    putInt("surahNumber", surahNumber)
                }

                findNavController().navigate(
                    R.id.action_quran2Fragment_to_quranFragment,
                    bundle
                )
                Log.d("Quran2Fragment", "Bundle navigation successful")
            }

        } catch (e: Exception) {
            Log.e("Quran2Fragment", "Navigation error: ${e.message}", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}