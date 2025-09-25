// ui/adapters/VideosAdapter.kt
package com.example.zakrni.clean.ui.adapters

import PresentationVideo
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zakrni.R
import com.example.zakrni.databinding.ItemVideoBinding

class VideosAdapter(
    private val onVideoClick: (PresentationVideo) -> Unit
) : ListAdapter<PresentationVideo, VideosAdapter.VideoViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VideoViewHolder(
        private val binding: ItemVideoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onVideoClick(getItem(position))
                }
            }
        }

        fun bind(video: PresentationVideo) {
            binding.apply {
                videoTitle.text = video.title
                videoDescription.text = video.description

                Glide.with(thumbnail)
                    .load(video.thumbnailUrl)
                    .placeholder(R.drawable.video_placeholder)
                    .into(thumbnail)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PresentationVideo>() {
        override fun areItemsTheSame(old: PresentationVideo, new: PresentationVideo) =
            old.id == new.id
        override fun areContentsTheSame(old: PresentationVideo, new: PresentationVideo) =
            old == new
    }
}