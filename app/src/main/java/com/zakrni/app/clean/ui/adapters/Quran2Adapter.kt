package com.zakrni.app.clean.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zakrni.app.clean.domain.models.DomainSurah
import com.zakrni.app.clean.ui.utils.LocaleHelper
import com.zakrni.app.clean.ui.utils.QuranUtils
import com.zakrni.app.databinding.ItemQuran2Binding

class Quran2Adapter(
    private val onSurahClick: (DomainSurah) -> Unit
) : ListAdapter<DomainSurah, Quran2Adapter.SurahViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurahViewHolder {
        val binding = ItemQuran2Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SurahViewHolder(binding, onSurahClick)
    }

    override fun onBindViewHolder(holder: SurahViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SurahViewHolder(
        private val binding: ItemQuran2Binding,
        private val onSurahClick: (DomainSurah) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(surah: DomainSurah) {
            binding.apply {
                val isArabic = LocaleHelper.isArabic(root.context)
                val ayahCount = QuranUtils.getAyahCount(surah.number)

                surahNumber.text = surah.number.toString()
                if (isArabic) {
                    surahNameArabic.text = surah.name
                    surahNameEnglish.text = surah.englishName
                    surahTranslation.text = surah.englishNameTranslation
                } else {
                    surahNameArabic.text = surah.englishName
                    surahNameEnglish.text = surah.englishNameTranslation
                    surahTranslation.text = "Surah ${surah.number}"
                }
                revelationType.text = QuranUtils.getRevelationType(surah.number, isArabic)
                ayahsCount.text = if (isArabic) "$ayahCount آية" else "$ayahCount verses"

                root.setOnClickListener {
                    onSurahClick(surah)
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<DomainSurah>() {
        override fun areItemsTheSame(oldItem: DomainSurah, newItem: DomainSurah) =
            oldItem.number == newItem.number

        override fun areContentsTheSame(oldItem: DomainSurah, newItem: DomainSurah) =
            oldItem == newItem
    }
}
