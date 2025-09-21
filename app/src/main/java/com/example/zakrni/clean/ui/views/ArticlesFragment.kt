// ui/views/ArticlesFragment.kt
package com.example.zakrni.clean.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zakrni.R
import com.example.zakrni.clean.ui.adapters.ArticlesAdapter
import com.example.zakrni.clean.ui.viewmodels.ArticlesViewModel
import com.example.zakrni.databinding.FragmentArticlesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArticlesFragment : Fragment() {
    private var _binding: FragmentArticlesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ArticlesViewModel by viewModels()
    private lateinit var articlesAdapter: ArticlesAdapter

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
        setupCategoryChips()
        setupSwipeRefresh()
        observeViewModel()

        viewModel.loadArticles()
    }

    private fun setupRecyclerView() {
        articlesAdapter = ArticlesAdapter { article ->
            // Navigate to article detail
            val bundle = Bundle().apply {
                putInt("article_id", article.id)
                putString("article_title", article.title)
                putString("article_content", article.content)
            }
            findNavController().navigate(R.id.articleDetailFragment, bundle)
        }

        binding.articlesRecyclerView.apply {
            adapter = articlesAdapter
            layoutManager = LinearLayoutManager(context)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (viewModel.hasMorePages.value == true &&
                        viewModel.isLoading.value == false &&
                        (visibleItemCount + firstVisibleItemPosition) >= totalItemCount &&
                        firstVisibleItemPosition >= 0) {
                        viewModel.loadArticles()
                    }
                }
            })
        }
    }

    private fun setupCategoryChips() {
        binding.categoriesChipGroup.setOnCheckedChangeListener { _, checkedId ->
            val category = when (checkedId) {
                R.id.chipAll -> null
                R.id.chipAqidah -> "aqidah"
                R.id.chipFiqh -> "fiqh"
                R.id.chipSirah -> "sirah"
                else -> null
            }
            viewModel.loadArticles(category, refresh = true)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshArticles()
        }
    }

    private fun observeViewModel() {
        viewModel.articles.observe(viewLifecycleOwner) { articles ->
            articlesAdapter.submitList(articles)
            binding.swipeRefresh.isRefreshing = false
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading &&
                viewModel.articles.value.isNullOrEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}