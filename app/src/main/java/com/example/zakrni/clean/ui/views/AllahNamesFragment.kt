package com.example.zakrni.clean.ui.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zakrni.clean.ui.adapters.AllahNamesAdapter
import com.example.zakrni.clean.ui.viewmodels.AllahNamesViewModel
import com.example.zakrni.databinding.FragmentAllahNamesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AllahNamesFragment : Fragment() {

    private var _binding: FragmentAllahNamesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AllahNamesAdapter

    private val viewModel: AllahNamesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllahNamesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupAdapter() {
        adapter = AllahNamesAdapter()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AllahNamesFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.allahNamesList.collect { names ->
                adapter.submitList(names)
            }
        }
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
            }
        }
        lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                if (error != null) {
                    showError(error)
                } else {
                    hideError()
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Retry button click
        binding.btnRetry?.setOnClickListener {
            viewModel.retryLoading()
        }
    }

    private fun showError(error: String) {
        binding.errorLayout?.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.tvError?.text = error
    }

    private fun hideError() {
        binding.errorLayout?.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}