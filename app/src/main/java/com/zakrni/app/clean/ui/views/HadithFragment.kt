package com.zakrni.app.clean.ui.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zakrni.app.R
import com.zakrni.app.clean.ui.adapters.HadithAdapter
import com.zakrni.app.clean.ui.viewmodels.HadithViewModel
import com.zakrni.app.databinding.FragmentHadithBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HadithFragment : Fragment() {

    private var _binding: FragmentHadithBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HadithViewModel by viewModels()
    private val hadithAdapter: HadithAdapter by lazy { HadithAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHadithBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.hadithRecyclerView.apply {
            adapter = hadithAdapter
            layoutManager = LinearLayoutManager(context)

            // Add scroll listener for pagination
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val visibleItemCount = layoutManager.childCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    // Load more when near the end (3 items from bottom)
                    if ((visibleItemCount + firstVisibleItemPosition + 3) >= totalItemCount) {
                        viewModel.loadNextPage()
                    }
                }
            })
        }
    }



    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.retryButton?.setOnClickListener {
            viewModel.refresh()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Observe hadiths list
                launch {
                    viewModel.hadithsList.collect { hadiths ->
                        hadithAdapter.submitList(hadiths)
                        updateUIVisibility(hadiths.isEmpty())
                    }
                }

                // Observe loading state
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        // Stop refresh animation when loading completes
                        if (binding.swipeRefreshLayout?.isRefreshing == true && !isLoading) {
                            binding.swipeRefreshLayout?.isRefreshing = false
                        }

                        // Show progress bar only for initial loading
                        binding.progressBar?.visibility = if (isLoading && hadithAdapter.itemCount == 0) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    }
                }

                // Observe loading more state


                // Observe error state
                launch {
                    viewModel.error.collect { error ->
                        if (error != null) {
                            showErrorState(error)
                        } else {
                            hideErrorState()
                        }
                    }
                }
            }
        }
    }

    private fun updateUIVisibility(isEmpty: Boolean) {
        if (isEmpty && viewModel.error.value == null) {
            binding.emptyStateLayout?.visibility = View.VISIBLE
            binding.hadithRecyclerView.visibility = View.GONE
        } else {
            binding.emptyStateLayout?.visibility = View.GONE
            binding.hadithRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun showErrorState(error: String) {
        binding.errorLayout?.visibility = View.VISIBLE
        binding.errorMessage?.text = error
        binding.hadithRecyclerView.visibility = View.GONE
        binding.emptyStateLayout?.visibility = View.GONE

        // Stop refresh animation if running
        if (binding.swipeRefreshLayout?.isRefreshing == true) {
            binding.swipeRefreshLayout?.isRefreshing = false
        }
    }

    private fun hideErrorState() {
        binding.errorLayout?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}