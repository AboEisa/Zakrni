// ui/views/VideosFragment.kt
package com.example.zakrni.clean.ui.views

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zakrni.clean.domain.models.DomainVideo
import com.example.zakrni.clean.ui.adapters.VideosAdapter
import com.example.zakrni.clean.ui.viewmodels.VideosViewModel
import com.example.zakrni.databinding.FragmentVideosBinding
import dagger.hilt.android.AndroidEntryPoint
import android.view.LayoutInflater
import android.view.ViewGroup

@AndroidEntryPoint
class VideosFragment : Fragment() {
    private var _binding: FragmentVideosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VideosViewModel by viewModels()
    private lateinit var videosAdapter: VideosAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVideosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
    private fun observeViewModel() {
        viewModel.videos.observe(viewLifecycleOwner) { videos ->
            videosAdapter.submitList(videos)
            binding.emptyStateTextView.visibility = if (videos.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearErrorMessage()
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        observeViewModel()
        viewModel.loadVideos()
    }

    private fun setupRecyclerView() {
        videosAdapter = VideosAdapter { video ->
            playVideo(video)
        }

        binding.videosRecyclerView.apply {
            adapter = videosAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchVideos(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    viewModel.loadVideos()
                }
                return true
            }
        })
    }

    private fun playVideo(video: DomainVideo) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.videoUrl))
        startActivity(intent)
    }
}