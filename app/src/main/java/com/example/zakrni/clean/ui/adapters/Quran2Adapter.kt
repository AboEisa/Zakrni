package com.example.zakrni.clean.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zakrni.clean.domain.models.DomainSurah
import com.example.zakrni.databinding.ItemQuran2Binding

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
                    onSurahClick(getItem(position))
                }
            }
        }

        fun bind(surah: DomainSurah) {
            binding.apply {
                surahNumber.text = surah.number.toString()
                surahNameArabic.text = surah.name
                surahNameEnglish.text = surah.englishName
                surahTranslation.text = surah.englishNameTranslation
                revelationType.text = when (surah.revelationType.lowercase()) {
                    "meccan" -> "مكية"
                    "medinan" -> "مدنية"
                    else -> surah.revelationType
                }
                ayahsCount.text = "${getAyahCount(surah.number)} آية"
            }
        }

        // Helper function to get ayah count for each surah
        // You can replace this with actual data from your API or add ayahCount to DomainSurah
        private fun getAyahCount(surahNumber: Int): Int {
            return when (surahNumber) {
                1 -> 7      // Al-Fatiha
                2 -> 286    // Al-Baqarah
                3 -> 200    // Ali 'Imran
                4 -> 176    // An-Nisa
                5 -> 120    // Al-Ma'idah
                6 -> 165    // Al-An'am
                7 -> 206    // Al-A'raf
                8 -> 75     // Al-Anfal
                9 -> 129    // At-Tawbah
                10 -> 109   // Yunus
                11 -> 123   // Hud
                12 -> 111   // Yusuf
                13 -> 43    // Ar-Ra'd
                14 -> 52    // Ibrahim
                15 -> 99    // Al-Hijr
                16 -> 128   // An-Nahl
                17 -> 111   // Al-Isra
                18 -> 110   // Al-Kahf
                19 -> 98    // Maryam
                20 -> 135   // Taha
                21 -> 112   // Al-Anbya
                22 -> 78    // Al-Hajj
                23 -> 118   // Al-Mu'minun
                24 -> 64    // An-Nur
                25 -> 77    // Al-Furqan
                26 -> 227   // Ash-Shu'ara
                27 -> 93    // An-Naml
                28 -> 88    // Al-Qasas
                29 -> 69    // Al-Ankabut
                30 -> 60    // Ar-Rum
                // Add more as needed, or get from API
                else -> 0
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