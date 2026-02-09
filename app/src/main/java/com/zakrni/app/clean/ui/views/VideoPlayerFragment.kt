package com.zakrni.app.clean.ui.views

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.zakrni.app.R
import com.zakrni.app.clean.ui.utils.LocaleHelper
import com.zakrni.app.databinding.FragmentVideoPlayerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VideoPlayerFragment : Fragment() {

    private var _binding: FragmentVideoPlayerBinding? = null
    private val binding get() = _binding!!
    
    private val args: VideoPlayerFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        loadVideoPreview()
    }
    
    private fun setupUI() {
        val isArabic = LocaleHelper.isArabic(requireContext())
        binding.apply {
            // Back button
            backButton.setOnClickListener {
                findNavController().navigateUp()
            }
            
            // Video info
            videoTitle.text = args.videoTitle
            fullVideoTitle.text = args.videoTitle
            channelName.text = args.channelName
            videoViews.text = if (isArabic) "${args.viewCount} مشاهدة" else "${args.viewCount} views"
            videoDate.text = args.publishedAt
            videoDescription.text = args.description
            
            // Play button - opens YouTube app
            playButton.setOnClickListener {
                openInYouTube()
            }
            
            // Thumbnail click - opens YouTube app
            videoThumbnail.setOnClickListener {
                openInYouTube()
            }
        }
    }
    
    private fun loadVideoPreview() {
        // Load thumbnail
        val thumbnailUrl = "https://img.youtube.com/vi/${args.videoId}/maxresdefault.jpg"
        
        Glide.with(requireContext())
            .load(thumbnailUrl)
            .transform(CenterCrop(), RoundedCorners(24))
            .placeholder(R.drawable.placeholder_video)
            .error(R.drawable.placeholder_video)
            .into(binding.videoThumbnail)
    }
    
    private fun openInYouTube() {
        try {
            // Try to open in YouTube app first
            val youtubeAppIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:${args.videoId}"))
            youtubeAppIntent.setPackage("com.google.android.youtube")
            startActivity(youtubeAppIntent)
        } catch (e: Exception) {
            // Fallback to browser if YouTube app not installed
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=${args.videoId}"))
            startActivity(webIntent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
