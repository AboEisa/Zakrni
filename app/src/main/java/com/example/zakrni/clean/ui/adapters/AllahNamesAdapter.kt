package com.example.zakrni.clean.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zakrni.databinding.ItemAllahNamesBinding
import com.example.zakrni.clean.ui.models.PresentationAllahNameData

class AllahNamesAdapter(
) : ListAdapter<PresentationAllahNameData, AllahNamesAdapter.Holder>(DiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Holder {
        val binding = ItemAllahNamesBinding.inflate(
            android.view.LayoutInflater.from(parent.context),
            parent,
            false
        )
        return Holder(binding)
    }

    override fun onBindViewHolder(
        holder: Holder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    inner class Holder(val binding: ItemAllahNamesBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(allahName: PresentationAllahNameData) {
            with(binding) {
                // Set the number
                tvNumber.text = allahName.number.toString()

                // Set Arabic name
                tvArabicName.text = allahName.name

                // Set transliteration
                tvTransliteration.text = allahName.transliteration

                // Set meaning

                tvMeaning.text = allahName.en.meaning




            }
        }
    }

    // DiffUtil for efficient list updates
    class DiffCallback : DiffUtil.ItemCallback<PresentationAllahNameData>() {
        override fun areItemsTheSame(
            oldItem: PresentationAllahNameData,
            newItem: PresentationAllahNameData
        ): Boolean {
            return oldItem.number == newItem.number
        }

        override fun areContentsTheSame(
            oldItem: PresentationAllahNameData,
            newItem: PresentationAllahNameData
        ): Boolean {
            return oldItem == newItem
        }
    }

    // Helper function to update the list
    fun updateList(newList: List<PresentationAllahNameData>) {
        submitList(newList)
    }

    // Function to get item at specific position
    fun getItemAtPosition(position: Int): PresentationAllahNameData? {
        return if (position in 0 until itemCount) {
            getItem(position)
        } else null
    }
}