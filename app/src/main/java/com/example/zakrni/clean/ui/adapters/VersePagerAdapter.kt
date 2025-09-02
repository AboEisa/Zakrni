package com.example.zakrni.clean.ui.adapters

import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VersePagerAdapter(private val items: List<String>) :
    RecyclerView.Adapter<VersePagerAdapter.VerseViewHolder>() {

    inner class VerseViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerseViewHolder {
        val textView = TextView(parent.context).apply {
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
        }
        return VerseViewHolder(textView)
    }

    override fun onBindViewHolder(holder: VerseViewHolder, position: Int) {
        holder.textView.text = items[position]
    }

    override fun getItemCount() = items.size
}