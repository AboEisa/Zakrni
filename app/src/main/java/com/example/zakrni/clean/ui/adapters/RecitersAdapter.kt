
// ui/adapters/RecitersAdapter.kt
package com.example.zakrni.clean.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zakrni.R
import com.example.zakrni.databinding.ItemReciterBinding
import com.example.zakrni.clean.ui.models.PresentationReciter

class RecitersAdapter(
    private val onReciterClick: (PresentationReciter) -> Unit
) : ListAdapter<PresentationReciter, RecitersAdapter.ReciterViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReciterViewHolder {
        val binding = ItemReciterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ReciterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReciterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReciterViewHolder(
        private val binding: ItemReciterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onReciterClick(getItem(adapterPosition))
                }
            }
        }

        fun bind(reciter: PresentationReciter) {
            binding.apply {
                reciterName.text = reciter.nameAr
                reciterStyle.text = reciter.rewaya
                surahCount.text = reciter.surahCount

                Glide.with(reciterPhoto)
                    .load(reciter.photoUrl)
                    .placeholder(R.drawable.reciter_placeholder)
                    .circleCrop()
                    .into(reciterPhoto)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PresentationReciter>() {
        override fun areItemsTheSame(old: PresentationReciter, new: PresentationReciter) =
            old.id == new.id
        override fun areContentsTheSame(old: PresentationReciter, new: PresentationReciter) =
            old == new
    }
}