package com.example.zakrni.clean.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zakrni.R
import com.example.zakrni.clean.ui.models.PresentationHadith

class HadithAdapter(
    private var hadithList: List<PresentationHadith> = emptyList()
) : RecyclerView.Adapter<HadithAdapter.HadithViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HadithViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hadith, parent, false)
        return HadithViewHolder(view)
    }

    override fun onBindViewHolder(holder: HadithViewHolder, position: Int) {
        val hadith = hadithList[position]
        holder.bind(hadith)
    }

    override fun getItemCount(): Int = hadithList.size

    fun updateData(newHadithList: List<PresentationHadith>) {
        hadithList = newHadithList
        notifyDataSetChanged()
    }

    inner class HadithViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvBookName: TextView = itemView.findViewById(R.id.tv_book_name)
        private val tvHadithNumber: TextView = itemView.findViewById(R.id.tv_hadith_number)
        private val tvChapterNumber: TextView = itemView.findViewById(R.id.tv_chapter_number)
        private val tvHadithArabic: TextView = itemView.findViewById(R.id.tv_hadith_arabic)
        private val tvHadithEnglish: TextView = itemView.findViewById(R.id.tv_hadith_english)
        private val tvHadithUrdu: TextView = itemView.findViewById(R.id.tv_hadith_urdu)
        private val tvUrduNarrator: TextView = itemView.findViewById(R.id.tv_urdu_narrator)
        private val tvEnglishNarrator: TextView = itemView.findViewById(R.id.tv_english_narrator)
        private val tvHeadingArabic: TextView = itemView.findViewById(R.id.tv_heading_arabic)
        private val tvHeadingUrdu: TextView = itemView.findViewById(R.id.tv_heading_urdu)
        private val tvHeadingEnglish: TextView = itemView.findViewById(R.id.tv_heading_english)

        fun bind(hadith: PresentationHadith) {
            // Safely access book properties with null checks
            val bookName = hadith.book?.bookName
            if (!bookName.isNullOrEmpty()) {
                tvBookName.text = bookName
                tvBookName.visibility = View.VISIBLE
            } else {
                tvBookName.visibility = View.GONE
            }

            // Safely access narrator properties with null checks  
            val urduNarrator = hadith.urduNarrator
            if (!urduNarrator.isNullOrEmpty()) {
                tvUrduNarrator.text = urduNarrator
                tvUrduNarrator.visibility = View.VISIBLE
            } else {
                tvUrduNarrator.visibility = View.GONE
            }

            val englishNarrator = hadith.englishNarrator
            if (!englishNarrator.isNullOrEmpty()) {
                tvEnglishNarrator.text = englishNarrator
                tvEnglishNarrator.visibility = View.VISIBLE
            } else {
                tvEnglishNarrator.visibility = View.GONE
            }

            // Safely access heading properties with null checks
            val headingArabic = hadith.headingArabic
            if (!headingArabic.isNullOrEmpty()) {
                tvHeadingArabic.text = headingArabic
                tvHeadingArabic.visibility = View.VISIBLE
            } else {
                tvHeadingArabic.visibility = View.GONE
            }

            val headingUrdu = hadith.headingUrdu
            if (!headingUrdu.isNullOrEmpty()) {
                tvHeadingUrdu.text = headingUrdu
                tvHeadingUrdu.visibility = View.VISIBLE
            } else {
                tvHeadingUrdu.visibility = View.GONE
            }

            val headingEnglish = hadith.headingEnglish
            if (!headingEnglish.isNullOrEmpty()) {
                tvHeadingEnglish.text = headingEnglish
                tvHeadingEnglish.visibility = View.VISIBLE
            } else {
                tvHeadingEnglish.visibility = View.GONE
            }

            // Safely access chapter properties with null checks
            val chapterNumber = hadith.chapter?.chapterNumber
            if (!chapterNumber.isNullOrEmpty()) {
                tvChapterNumber.text = "Chapter: $chapterNumber"
                tvChapterNumber.visibility = View.VISIBLE
            } else {
                tvChapterNumber.visibility = View.GONE
            }

            // Handle other hadith properties safely
            val hadithNumber = hadith.hadithNumber
            if (!hadithNumber.isNullOrEmpty()) {
                tvHadithNumber.text = "Hadith: $hadithNumber"
                tvHadithNumber.visibility = View.VISIBLE
            } else {
                tvHadithNumber.visibility = View.GONE
            }

            val hadithArabic = hadith.hadithArabic
            if (!hadithArabic.isNullOrEmpty()) {
                tvHadithArabic.text = hadithArabic
                tvHadithArabic.visibility = View.VISIBLE
            } else {
                tvHadithArabic.visibility = View.GONE
            }

            val hadithEnglish = hadith.hadithEnglish
            if (!hadithEnglish.isNullOrEmpty()) {
                tvHadithEnglish.text = hadithEnglish
                tvHadithEnglish.visibility = View.VISIBLE
            } else {
                tvHadithEnglish.visibility = View.GONE
            }

            val hadithUrdu = hadith.hadithUrdu
            if (!hadithUrdu.isNullOrEmpty()) {
                tvHadithUrdu.text = hadithUrdu
                tvHadithUrdu.visibility = View.VISIBLE
            } else {
                tvHadithUrdu.visibility = View.GONE
            }
        }
    }
}