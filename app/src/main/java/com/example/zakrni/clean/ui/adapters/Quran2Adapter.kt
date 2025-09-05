package com.example.zakrni.clean.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zakrni.clean.domain.models.DomainSurah
import com.example.zakrni.databinding.ItemQuran2Binding
import com.example.zakrni.clean.ui.utils.QuranUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.debounce

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
                surahNumber.text = surah.number.toString()
                surahNameArabic.text = surah.name
                surahNameEnglish.text = surah.englishName
                surahTranslation.text = surah.englishNameTranslation
                revelationType.text = QuranUtils.getRevelationTypeArabic(surah.number)
                ayahsCount.text = "${QuranUtils.getAyahCount(surah.number)} آية"

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
