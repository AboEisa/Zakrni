package com.zakrni.app.clean.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zakrni.app.clean.domain.models.DomainAyah
import com.zakrni.app.databinding.ItemQuranBinding

class QuranAdapter : ListAdapter<DomainAyah, QuranAdapter.QuranViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuranViewHolder {
        val binding = ItemQuranBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuranViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuranViewHolder, position: Int) {
        try {
            val verse = getItem(position)
            Log.d("QuranAdapter", "Binding verse at position $position: verse ${verse.numberInSurah}")
            holder.bind(verse)
        } catch (e: Exception) {
            Log.e("QuranAdapter", "Error binding verse at position $position: ${e.message}")
        }
    }

    override fun getItemCount(): Int {
        val count = super.getItemCount()
        Log.d("QuranAdapter", "Item count: $count")
        return count
    }

    override fun submitList(list: List<DomainAyah>?) {
        Log.d("QuranAdapter", "Submitting list with ${list?.size ?: 0} items")
        super.submitList(list)
    }

    inner class QuranViewHolder(private val binding: ItemQuranBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(verse: DomainAyah) {
            try {
                binding.apply {
                    // Set verse text with null check
                    verseText.text = verse.text ?: "نص الآية غير متوفر"

                    // Set verse number with validation
                    verseNumber.text = if (verse.numberInSurah > 0) {
                        verse.numberInSurah.toString()
                    } else {
                        "1"
                    }

                    Log.d("QuranAdapter", "Bound verse ${verse.numberInSurah}: ${verse.text?.take(50)}...")
                }
            } catch (e: Exception) {
                Log.e("QuranAdapter", "Error in bind method: ${e.message}")
                // Set fallback values
                binding.verseText.text = "خطأ في تحميل الآية"
                binding.verseNumber.text = "?"
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