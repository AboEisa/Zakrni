package com.example.zakrni.clean.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zakrni.clean.domain.models.DomainSurah
import com.example.zakrni.databinding.ItemQuran2Binding
import com.example.zakrni.clean.ui.utils.QuranUtils

class Quran2Adapter(
    private val onSurahClick: (DomainSurah) -> Unit
) : ListAdapter<DomainSurah, Quran2Adapter.SurahViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurahViewHolder {
        val binding = ItemQuran2Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SurahViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SurahViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SurahViewHolder(private val binding: ItemQuran2Binding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val surah = getItem(position)
                    println("DEBUG: Clicked on Surah ${surah.number}: ${surah.name}")
                    onSurahClick(surah)
                }
            }
        }

        fun bind(surah: DomainSurah) {
            binding.apply {
                surahNumber.text = surah.number.toString()
                surahNameArabic.text = surah.name
                surahNameEnglish.text = surah.englishName
                surahTranslation.text = surah.englishNameTranslation

                // Use QuranUtils for revelation type
                revelationType.text = QuranUtils.getRevelationTypeArabic(surah.number)

                // Use QuranUtils for ayah count
                ayahsCount.text = "${QuranUtils.getAyahCount(surah.number)} آية"
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<DomainSurah>() {
        override fun areItemsTheSame(oldItem: DomainSurah, newItem: DomainSurah): Boolean {
            return oldItem.number == newItem.number
        }

        override fun areContentsTheSame(oldItem: DomainSurah, newItem: DomainSurah): Boolean {
            return oldItem == newItem
        }
    }
}