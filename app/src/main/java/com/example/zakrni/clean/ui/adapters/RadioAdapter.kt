
// ui/adapters/RadioAdapter.kt
package com.example.zakrni.clean.ui.adapters

import PresentationRadioStation
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zakrni.R
class RadioAdapter(
    private val onStationClick: (PresentationRadioStation) -> Unit
) : ListAdapter<PresentationRadioStation, RadioAdapter.RadioViewHolder>(DiffCallback()) {

    private var currentPlayingId: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioViewHolder {
        val binding = ItemRadioBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RadioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RadioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updatePlayingStation(stationId: Int?) {
        val previousId = currentPlayingId
        currentPlayingId = stationId

        previousId?.let {
            val prevIndex = currentList.indexOfFirst { it.id == previousId }
            if (prevIndex >= 0) notifyItemChanged(prevIndex)
        }
        stationId?.let {
            val newIndex = currentList.indexOfFirst { it.id == stationId }
            if (newIndex >= 0) notifyItemChanged(newIndex)
        }
    }


    inner class RadioViewHolder(
        private val binding: ItemRadioBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onStationClick(getItem(adapterPosition))
                }
            }
        }

        fun bind(station: PresentationRadioStation) {
            binding.apply {
                stationName.text = station.name
                stationDescription.text = station.description

                val isPlaying = station.id == currentPlayingId
                playButton.setImageResource(
                    if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                )

                Glide.with(stationLogo)
                    .load(station.logoUrl)
                    .placeholder(R.drawable.radio_placeholder)
                    .circleCrop()
                    .into(stationLogo)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PresentationRadioStation>() {
        override fun areItemsTheSame(old: PresentationRadioStation, new: PresentationRadioStation) =
            old.id == new.id
        override fun areContentsTheSame(old: PresentationRadioStation, new: PresentationRadioStation) =
            old == new
    }
}

