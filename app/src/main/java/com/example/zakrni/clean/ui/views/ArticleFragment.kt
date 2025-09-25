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
import com.example.zakrni.clean.ui.adapters.ArticleAdapter
import com.example.zakrni.clean.ui.viewmodels.ArticlesViewModel
import com.example.zakrni.databinding.FragmentArticlesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ArticleFragment : Fragment() {

    private var _binding: FragmentArticlesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ArticlesViewModel by viewModels()
    private lateinit var articleAdapter: ArticleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArticlesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        articleAdapter = ArticleAdapter { article ->
            // Handle article click
            // You can navigate to detail view or open URL
            // For example: openArticleUrl(article.apiurl)
        }

        binding.articlesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = articleAdapter
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.searchButton.setOnClickListener {
            // Implement search functionality
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        binding.btnRetry.setOnClickListener {
            viewModel.refresh()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe loading state
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.isVisible = isLoading && viewModel.articleData.value == null
                binding.swipeRefresh.isRefreshing = isLoading && viewModel.articleData.value != null
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Observe error state
            viewModel.error.collect { error ->
                if (error != null) {
                    binding.errorLayout.isVisible = true
                    binding.articlesRecyclerView.isVisible = false
                    binding.progressBar.isVisible = false
                    binding.tvError.text = error
                } else {
                    binding.errorLayout.isVisible = false
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Observe article data
            viewModel.articleData.collect { response ->
                response?.let { articleResponse ->
                    if (articleResponse.articles.isNotEmpty()) {
                        articleAdapter.submitList(articleResponse.articles)
                        binding.articlesRecyclerView.isVisible = true
                        binding.errorLayout.isVisible = false
                        binding.swipeRefresh.isRefreshing = false
                    } else {
                        // Handle empty state
                        binding.errorLayout.isVisible = true
                        binding.articlesRecyclerView.isVisible = false
                        binding.tvError.text = "لا توجد مقالات متاحة"
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