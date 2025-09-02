package com.example.zakrni.clean.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zakrni.clean.domain.models.DomainAyah
import com.example.zakrni.databinding.ItemQuranBinding

class QuranAdapter : ListAdapter<DomainAyah, QuranAdapter.QuranViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuranViewHolder {
        val binding = ItemQuranBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuranViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuranViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class QuranViewHolder(private val binding: ItemQuranBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(verse: DomainAyah) {
            binding.apply {
                verseText.text = verse.text
                verseNumber.text = verse.numberInSurah.toString()
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<DomainAyah>() {
        override fun areItemsTheSame(oldItem: DomainAyah, newItem: DomainAyah): Boolean {
            return oldItem.number == newItem.number
        }

        override fun areContentsTheSame(oldItem: DomainAyah, newItem: DomainAyah): Boolean {
            return oldItem == newItem
        }
    }
}