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
    private val clickFlow = MutableSharedFlow<DomainSurah>(extraBufferCapacity = 1)
    private var job: Job? = null

    init {
        job = CoroutineScope(Dispatchers.Main).launch {
            clickFlow
                .debounce(500)
                .collect { surah -> onSurahClick(surah) }
        }
    }

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
                    Log.d("Quran2Adapter", "Clicked on Surah ${surah.number}: ${surah.name}")
                    clickFlow.tryEmit(surah)
                }
            }
        }

        fun bind(surah: DomainSurah) {
            binding.apply {
                surahNumber.text = surah.number.toString()
                surahNameArabic.text = surah.name
                surahNameEnglish.text = surah.englishName
                surahTranslation.text = surah.englishNameTranslation
                revelationType.text = QuranUtils.getRevelationTypeArabic(surah.number)
                ayahsCount.text = "${QuranUtils.getAyahCount(surah.number)} آية"
            }
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        job?.cancel()
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