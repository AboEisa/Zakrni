package com.example.zakrni.clean.ui.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zakrni.databinding.FragmentDuaBinding
import com.example.zakrni.clean.ui.adapters.DuasAdapter
import com.example.zakrni.clean.ui.viewmodels.DuasViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DuaFragment : Fragment() {

    private var _binding: FragmentDuaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DuasViewModel by viewModels()
    private lateinit var duasAdapter: DuasAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDuaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        duasAdapter = DuasAdapter()

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = duasAdapter
        }

        duasAdapter.setOnHeaderClickListener { duaType ->
            viewModel.toggleSection(duaType)
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

//        binding.btnRetry.setOnClickListener {
//            viewModel.refresh()
//        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe loading state
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.isVisible = isLoading
                binding.recyclerView.isVisible = !isLoading
                binding.errorLayout.isVisible = false
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Observe error state
            viewModel.error.collect { error ->
                if (error != null) {
                    binding.errorLayout.isVisible = true
                    binding.recyclerView.isVisible = false
                    binding.progressBar.isVisible = false
                    binding.tvError.text = error
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Observe dua data and expanded sections
            viewModel.duaData.collect { duaData ->
                if (duaData != null) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.expandedSections.collect { expandedSections ->
                            duasAdapter.submitList(duaData, expandedSections)
                            binding.recyclerView.isVisible = true
                            binding.errorLayout.isVisible = false
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}