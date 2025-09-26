package com.example.zakrni.clean.ui.views

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zakrni.clean.ui.adapters.ArticleAdapter
import com.example.zakrni.clean.ui.models.PresentationArticle
import com.example.zakrni.clean.ui.viewmodels.ArticlesViewModel
import com.example.zakrni.databinding.FragmentArticlesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

@AndroidEntryPoint
class ArticleFragment : Fragment() {

    private var _binding: FragmentArticlesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ArticlesViewModel by viewModels()
    private lateinit var articleAdapter: ArticleAdapter

    private var searchJob: Job? = null
    private var isSearchVisible = false
    private var allArticles: List<PresentationArticle> = emptyList()

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
        setupSearchFunctionality()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        articleAdapter = ArticleAdapter { article ->
            // Handle article click - navigate to detail or open URL
        }

        binding.articlesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = articleAdapter
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            if (isSearchVisible) {
                hideSearch()
            } else {
                findNavController().navigateUp()
            }
        }

        binding.searchButton.setOnClickListener {
            toggleSearch()
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        binding.btnRetry.setOnClickListener {
            viewModel.refresh()
        }
    }

    private fun setupSearchFunctionality() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchJob?.cancel()

                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(300)
                    val query = s?.toString()?.trim() ?: ""
                    performSearch(query)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                true
            } else {
                false
            }
        }
    }

    private fun toggleSearch() {
        if (isSearchVisible) {
            hideSearch()
        } else {
            showSearch()
        }
    }

    private fun showSearch() {
        isSearchVisible = true
        binding.searchCard.isVisible = true
        binding.searchEditText.requestFocus()
        showKeyboard()
    }

    private fun hideSearch() {
        isSearchVisible = false
        binding.searchCard.isVisible = false
        binding.searchEditText.text?.clear()
        hideKeyboard()
        displayArticles(allArticles)
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            displayArticles(allArticles)
        } else {
            val filteredArticles = allArticles.filter { article ->
                article.title?.contains(query, ignoreCase = true) == true ||
                        article.shortdescription?.contains(query, ignoreCase = true) == true
            }
            displayArticles(filteredArticles)
        }
    }

    private fun displayArticles(articles: List<PresentationArticle>) {
        if (articles.isNotEmpty()) {
            articleAdapter.submitList(articles)
            binding.articlesRecyclerView.isVisible = true
            binding.errorLayout.isVisible = false
        } else {
            binding.errorLayout.isVisible = true
            binding.articlesRecyclerView.isVisible = false
            binding.btnRetry.isVisible = !isSearchVisible || binding.searchEditText.text.isNullOrEmpty()
            binding.tvError.text = when {
                isSearchVisible && !binding.searchEditText.text.isNullOrEmpty() -> "لا توجد نتائج للبحث"
                allArticles.isEmpty() -> "لا توجد مقالات متاحة"
                else -> "لا توجد مقالات متاحة"
            }
        }
    }

    private fun showKeyboard() {
        val imm = getSystemService(requireContext(), InputMethodManager::class.java)
        imm?.showSoftInput(binding.searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(requireContext(), InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.isVisible = isLoading && allArticles.isEmpty()
                binding.swipeRefresh.isRefreshing = isLoading && allArticles.isNotEmpty()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collect { error ->
                if (error != null) {
                    binding.errorLayout.isVisible = true
                    binding.articlesRecyclerView.isVisible = false
                    binding.progressBar.isVisible = false
                    binding.btnRetry.isVisible = true
                    binding.tvError.text = error
                } else if (!isSearchVisible || binding.searchEditText.text.isNullOrEmpty()) {
                    binding.errorLayout.isVisible = false
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.articleData.collect { response ->
                response?.let { articleResponse ->
                    allArticles = articleResponse.articles

                    val articlesToDisplay = if (isSearchVisible && !binding.searchEditText.text.isNullOrEmpty()) {
                        val query = binding.searchEditText.text.toString().trim()
                        allArticles.filter { article ->
                            article.title?.contains(query, ignoreCase = true) == true ||
                                    article.shortdescription?.contains(query, ignoreCase = true) == true
                        }
                    } else {
                        allArticles
                    }

                    displayArticles(articlesToDisplay)
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
        _binding = null
    }
}