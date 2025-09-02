package com.example.zakrni.clean.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.zakrni.clean.domain.models.DomainQuranVerseResponse
import com.example.zakrni.databinding.ItemQuranBinding

class QuranAdapter : RecyclerView.Adapter<QuranAdapter.QuranViewHolder>() {
    private var verses = listOf<DomainQuranVerseResponse>()

    fun submitList(verses: List<DomainQuranVerseResponse>) {
        this.verses = verses
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuranViewHolder {
        val binding = ItemQuranBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuranViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuranViewHolder, position: Int) {
        holder.bind(verses[position])
    }

    override fun getItemCount() = verses.size

    inner class QuranViewHolder(private val binding: ItemQuranBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(verse: DomainQuranVerseResponse) {
            binding.verseText.text = verse.text
            binding.verseNumber.text = verse.numberInSurah.toString()
        }
    }
}