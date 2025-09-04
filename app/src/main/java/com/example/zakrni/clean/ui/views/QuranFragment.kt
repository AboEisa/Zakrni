package com.example.zakrni.clean.ui.views

import android.content.Intent
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
import com.example.zakrni.clean.domain.models.DomainSurah
import com.example.zakrni.clean.ui.adapters.QuranAdapter
import com.example.zakrni.clean.ui.viewmodels.QuranViewModel
import com.example.zakrni.databinding.FragmentQuranBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuranFragment : Fragment() {
    private var _binding: FragmentQuranBinding? = null
    private val binding get() = _binding!!

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
        Log.d("QuranFragment", "onViewCreated started")

        Log.d("QuranFragment", "Arguments bundle: $arguments")
        arguments?.keySet()?.forEach { key ->
            Log.d("QuranFragment", "Argument $key = ${arguments?.get(key)}")
        }

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        val surahNumber = getSurahNumber()
        Log.d("QuranFragment", "Final surah number: $surahNumber")

        if (surahNumber in 1..114) {
            Log.d("QuranFragment", "Loading verses for surah: $surahNumber")
            viewModel.loadQuranVerses(surahNumber)
        } else {
            Log.e("QuranFragment", "Invalid surah number: $surahNumber")
        }
    }

    private fun getSurahNumber(): Int {
        return try {
            // Method 1: Try Safe Args first
            Log.d("QuranFragment", "Trying Safe Args...")
            val args = QuranFragmentArgs.fromBundle(requireArguments())
            val safeArgsSurah = args.surahNumber
            Log.d("QuranFragment", "Safe Args surah number: $safeArgsSurah")
            if (safeArgsSurah > 0) {
                return safeArgsSurah
            } else {
                throw Exception("Safe Args returned invalid number: $safeArgsSurah")
            }
        } catch (e: Exception) {
            Log.w("QuranFragment", "Safe Args failed: ${e.message}")

            // Method 2: Try regular Bundle
            try {
                Log.d("QuranFragment", "Trying Bundle method...")
                val bundleSurah = arguments?.getInt("surahNumber", -1) ?: -1
                Log.d("QuranFragment", "Bundle surah number: $bundleSurah")
                if (bundleSurah > 0) {
                    return bundleSurah
                } else {
                    throw Exception("Bundle returned invalid number: $bundleSurah")
                }
            } catch (e2: Exception) {
                Log.w("QuranFragment", "Bundle method failed: ${e2.message}")

                // Method 3: Check if arguments exist at all
                if (arguments == null) {
                    Log.e("QuranFragment", "No arguments bundle found!")
                } else {
                    Log.e("QuranFragment", "Arguments exist but surahNumber not found")
                }
                return -1
            }
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




    }

    private fun observeViewModel() {
        viewModel.verses.observe(viewLifecycleOwner) { verses ->
            Log.d("QuranFragment", "Received ${verses.size} verses")
            if (verses.isNotEmpty()) {
                adapter.submitList(verses)
                binding.quranVersesRecycler.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                Log.d("QuranFragment", "Displayed ${verses.size} verses successfully")
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
                1, 9 -> View.GONE // الفاتحة والتوبة
                else -> View.VISIBLE
            }
        }
        Log.d("QuranFragment", "Updated surah header: ${surah.name} - ${surah.ayahs.size} verses")
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}