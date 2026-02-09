package com.zakrni.app.clean.ui.views

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
import com.zakrni.app.databinding.FragmentDuaBinding
import com.zakrni.app.clean.ui.adapters.HisnDuasAdapter
import com.zakrni.app.clean.ui.viewmodels.DuasViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DuaFragment : Fragment() {

    private var _binding: FragmentDuaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DuasViewModel by viewModels()
    private lateinit var duasAdapter: HisnDuasAdapter

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
        duasAdapter = HisnDuasAdapter()

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = duasAdapter
        }

        duasAdapter.setOnHeaderClickListener { sectionName ->
            viewModel.toggleSection(sectionName)
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnRetry?.setOnClickListener {
            viewModel.refresh()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe loading state
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.isVisible = isLoading
                binding.recyclerView.isVisible = !isLoading && viewModel.error.value == null
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
            // Combine sections, duasMap, and expandedSections for adapter
            combine(
                viewModel.sections,
                viewModel.sectionDuasMap,
                viewModel.expandedSections
            ) { sections, duasMap, expandedSections ->
                Triple(sections, duasMap, expandedSections)
            }.collect { (sections, duasMap, expandedSections) ->
                if (sections.isNotEmpty()) {
                    duasAdapter.submitList(sections, duasMap, expandedSections)
                    binding.recyclerView.isVisible = true
                    binding.errorLayout.isVisible = false
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}