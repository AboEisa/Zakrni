package com.zakrni.app.clean.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zakrni.app.R
import com.zakrni.app.clean.ui.utils.LocaleHelper
import com.zakrni.app.databinding.ItemReciterBinding

data class ReciterItem(
    val identifier: String,
    val nameArabic: String,
    val nameEnglish: String,
    val isSelected: Boolean = false
)

class ReciterAdapter(
    private val onReciterSelected: (ReciterItem) -> Unit
) : ListAdapter<ReciterItem, ReciterAdapter.ReciterViewHolder>(ReciterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReciterViewHolder {
        val binding = ItemReciterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReciterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReciterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReciterViewHolder(
        private val binding: ItemReciterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(reciter: ReciterItem) {
            val isArabic = LocaleHelper.isArabic(binding.root.context)
            binding.apply {
                if (isArabic) {
                    reciterNameArabic.text = reciter.nameArabic
                    reciterNameEnglish.text = reciter.nameEnglish
                } else {
                    reciterNameArabic.text = reciter.nameEnglish
                    reciterNameEnglish.text = reciter.nameArabic
                }
                reciterMeta?.text = if (isArabic) "تلاوة صوتية للقرآن الكريم" else "Quran recitation audio"
                selectedBadge?.text = if (isArabic) "محدد" else "Selected"

                // Update selection state
                if (reciter.isSelected) {
                    selectedIndicator.visibility = View.VISIBLE
                    selectedBadge?.visibility = View.VISIBLE
                    reciterCard.strokeColor = root.context.getColor(R.color.primary)
                    reciterCard.strokeWidth = 2
                    reciterCard.setCardBackgroundColor(root.context.getColor(R.color.reciter_item_selected))
                } else {
                    selectedIndicator.visibility = View.GONE
                    selectedBadge?.visibility = View.GONE
                    reciterCard.strokeColor = root.context.getColor(R.color.reciter_item_border)
                    reciterCard.strokeWidth = 1
                    reciterCard.setCardBackgroundColor(root.context.getColor(R.color.reciter_item_background))
                }

                root.setOnClickListener {
                    onReciterSelected(reciter)
                }
            }
        }
    }

    class ReciterDiffCallback : DiffUtil.ItemCallback<ReciterItem>() {
        override fun areItemsTheSame(oldItem: ReciterItem, newItem: ReciterItem): Boolean {
            return oldItem.identifier == newItem.identifier
        }

        override fun areContentsTheSame(oldItem: ReciterItem, newItem: ReciterItem): Boolean {
            return oldItem == newItem
        }
    }
}
