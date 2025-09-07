// 5. Updated AzkarFragment.kt - Fixed to work with the new ViewModel
package com.example.zakrni.clean.ui.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zakrni.databinding.FragmentAzkarBinding
import com.example.zakrni.clean.ui.adapters.AzkarAdapter
import com.example.zakrni.clean.ui.models.PresentationAzkarResponse
import com.example.zakrni.clean.ui.utils.AzkarType
import com.example.zakrni.clean.ui.viewmodels.AzkarViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AzkarFragment : Fragment() {

    private var _binding: FragmentAzkarBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AzkarViewModel by viewModels()
    private lateinit var azkarAdapter: AzkarAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAzkarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        azkarAdapter = AzkarAdapter()

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = azkarAdapter
            setHasFixedSize(true)
        }

        azkarAdapter.setOnHeaderClickListener { type ->
            viewModel.toggleSection(type)
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnRetry.setOnClickListener {
            viewModel.refresh()
            viewModel.clearError()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.azkarData.collect { azkarResponse ->
                    azkarResponse?.let {
                        updateAdapterData(it)
                        if (viewModel.error.value == null) {
                            hideError()
                        }
                    }
                }
            }

            launch {
                viewModel.expandedSections.collect { expandedSections ->
                    viewModel.azkarData.value?.let { azkarResponse ->
                        azkarAdapter.submitList(azkarResponse, expandedSections)
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE

                if (!isLoading) {
                    binding.recyclerView.visibility = View.VISIBLE
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collect { error ->
                if (error != null) {
                    if (viewModel.azkarData.value == null) {
                        showError(error)
                    } else {
                        showPartialError(error)
                    }
                } else {
                    hideError()
                }
            }
        }
    }

    private fun updateAdapterData(azkarResponse: PresentationAzkarResponse) {
        azkarAdapter.submitList(azkarResponse, viewModel.expandedSections.value)
    }

    private fun showError(errorMessage: String) {
        binding.apply {
            errorLayout.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            progressBar.visibility = View.GONE
            tvError.text = errorMessage
        }
    }

    private fun showPartialError(errorMessage: String) {
        binding.apply {
            errorLayout.visibility = View.GONE
            recyclerView.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            tvError.text = errorMessage
        }
    }

    private fun hideError() {
        binding.apply {
            errorLayout.visibility = View.GONE
            if (viewModel.isLoading.value != true) {
                recyclerView.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}