package com.zakrni.app.clean.ui.views

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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.zakrni.app.clean.data.models.YouTubeVideo
import com.zakrni.app.clean.ui.adapters.VideoAdapter
import com.zakrni.app.clean.ui.utils.LocaleHelper
import com.zakrni.app.clean.ui.viewmodels.ArticlesViewModel
import com.zakrni.app.databinding.FragmentArticlesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

@AndroidEntryPoint
class ArticleFragment : Fragment() {

    private var _binding: FragmentArticlesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ArticlesViewModel by viewModels()
    private val args: ArticleFragmentArgs by navArgs()
    private lateinit var videoAdapter: VideoAdapter

    private var searchJob: Job? = null
    private var isSearchVisible = false
    private var allVideos: List<YouTubeVideo> = emptyList()

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
        setupHeaderForContentType()
        observeViewModel()
        
        // Load videos based on content type
        loadContentByType()
    }
    
    private fun setupHeaderForContentType() {
        val contentType = args.contentType
        val (title, searchHint) = when (contentType) {
            "lectures" -> localizedText("الخطب والمحاضرات", "Lectures & Talks") to
                localizedText("ابحث عن خطبة أو محاضرة...", "Search for a lecture or sermon...")
            "quran" -> localizedText("التلاوات والدروس الصوتية", "Recitations & Audio Lessons") to
                localizedText("ابحث عن تلاوة أو درس...", "Search for a recitation or lesson...")
            "videos" -> localizedText("الفيديوهات الإسلامية", "Islamic Videos") to
                localizedText("ابحث عن فيديو إسلامي...", "Search for an Islamic video...")
            "sermons" -> localizedText("خطب الجمعة", "Friday Sermons") to
                localizedText("ابحث عن خطبة جمعة...", "Search for a Friday sermon...")
            else -> localizedText("كل الوسائط", "All Media") to localizedText("ابحث...", "Search...")
        }
        binding.tvHeaderTitle.text = title
        binding.searchEditText.hint = searchHint
    }
    
    private fun loadContentByType() {
        val contentType = args.contentType
        android.util.Log.d("ArticleFragment", "Loading content type: $contentType")
        viewModel.loadVideosByType(contentType)
    }

    private fun setupRecyclerView() {
        videoAdapter = VideoAdapter { video ->
            openVideoPlayer(video)
        }

        binding.articlesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = videoAdapter
        }
    }

    private fun openVideoPlayer(video: YouTubeVideo) {
        // Open video directly in YouTube app
        try {
            val youtubeAppIntent = android.content.Intent(
                android.content.Intent.ACTION_VIEW, 
                android.net.Uri.parse("vnd.youtube:${video.videoId}")
            )
            youtubeAppIntent.setPackage("com.google.android.youtube")
            startActivity(youtubeAppIntent)
        } catch (e: Exception) {
            // Fallback to browser if YouTube app not installed
            val webIntent = android.content.Intent(
                android.content.Intent.ACTION_VIEW, 
                android.net.Uri.parse("https://www.youtube.com/watch?v=${video.videoId}")
            )
            startActivity(webIntent)
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
                    delay(500)
                    val query = s?.toString()?.trim() ?: ""
                    if (query.length >= 3) {
                        viewModel.searchVideos(query)
                    } else if (query.isEmpty()) {
                        viewModel.loadVideos()
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                val query = binding.searchEditText.text?.toString()?.trim() ?: ""
                if (query.isNotEmpty()) {
                    viewModel.searchVideos(query)
                }
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
        viewModel.loadVideos()
    }

    private fun displayVideos(videos: List<YouTubeVideo>) {
        android.util.Log.d("ArticleFragment", "displayVideos called with ${videos.size} videos")
        binding.progressBar.isVisible = false
        if (videos.isNotEmpty()) {
            videoAdapter.submitList(videos)
            binding.articlesRecyclerView.isVisible = true
            binding.errorLayout.isVisible = false
        } else {
            binding.errorLayout.isVisible = true
            binding.articlesRecyclerView.isVisible = false
            binding.btnRetry.isVisible = !isSearchVisible || binding.searchEditText.text.isNullOrEmpty()
            binding.tvError.text = when {
                isSearchVisible && !binding.searchEditText.text.isNullOrEmpty() ->
                    localizedText("لا توجد نتائج للبحث", "No results found")
                else -> localizedText("لا توجد فيديوهات متاحة", "No videos available")
            }
        }
    }

    private fun localizedText(arabic: String, english: String): String {
        return if (LocaleHelper.isArabic(requireContext())) arabic else english
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
                binding.progressBar.isVisible = isLoading && allVideos.isEmpty()
                binding.swipeRefresh.isRefreshing = isLoading && allVideos.isNotEmpty()
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
            viewModel.videos.collect { videos ->
                allVideos = videos
                displayVideos(videos)
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
        _binding = null
    }
}
