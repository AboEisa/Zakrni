// ui/adapters/AudioAdapter.kt
package com.example.zakrni.clean.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zakrni.R
import com.example.zakrni.databinding.ItemAudioBinding
import com.example.zakrni.clean.ui.models.PresentationAudio

class AudioAdapter(
    private val onPlayClick: (PresentationAudio) -> Unit
) : ListAdapter<PresentationAudio, AudioAdapter.AudioViewHolder>(DiffCallback()) {

    private var currentPlayingId: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val binding = ItemAudioBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AudioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updatePlayingItem(audioId: Int?) {
        val previousId = currentPlayingId
        currentPlayingId = audioId

        previousId?.let {
            val index = currentList.indexOfFirst { it.id == previousId }
            if (index != -1) notifyItemChanged(index)
        }
        audioId?.let {
            val index = currentList.indexOfFirst { it.id == audioId }
            if (index != -1) notifyItemChanged(index)
        }
    }

    inner class AudioViewHolder(
        private val binding: ItemAudioBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(audio: PresentationAudio) {
            binding.apply {
                audioTitle.text = audio.title

                val isPlaying = audio.id == currentPlayingId
                playButton.setImageResource(
                    if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                )

                progressBar.progress = if (isPlaying) audio.progress else 0

                playButton.setOnClickListener {
                    onPlayClick(audio)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PresentationAudio>() {
        override fun areItemsTheSame(old: PresentationAudio, new: PresentationAudio) =
            old.id == new.id
        override fun areContentsTheSame(old: PresentationAudio, new: PresentationAudio) =
            old == new
    }
}