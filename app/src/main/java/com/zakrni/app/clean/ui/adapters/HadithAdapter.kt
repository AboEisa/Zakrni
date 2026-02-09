package com.zakrni.app.clean.ui.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zakrni.app.clean.ui.models.PresentationHadith
import com.zakrni.app.clean.ui.utils.LocaleHelper
import com.zakrni.app.databinding.ItemHadithBinding

class HadithAdapter : ListAdapter<PresentationHadith, HadithAdapter.HadithViewHolder>(HadithDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HadithViewHolder {
        val binding = ItemHadithBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HadithViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HadithViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HadithViewHolder(private val binding: ItemHadithBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.shareIcon?.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    shareHadith(getItem(position))
                }
            }
        }

        fun bind(hadith: PresentationHadith) {
            val context = binding.root.context
            val isArabic = LocaleHelper.isArabic(context)
            val fontScale = com.zakrni.app.clean.ui.utils.ThemeManager.getFontScale(context)

            with(binding) {
                bookName?.text = getLocalizedBookName(hadith.book?.bookName, isArabic)
                bookName?.textSize = 16f * fontScale

                hadithNumber?.text = formatHadithNumber(hadith.hadithNumber, isArabic)
                hadithNumber?.textSize = 14f * fontScale

                narrator?.text = buildNarratorText(hadith, isArabic)
                narrator?.textSize = 15f * fontScale

                hadithText?.text = formatHadithText(hadith, isArabic)
                hadithText?.textSize = 20f * fontScale

                chapterInfo?.text = buildChapterText(hadith, isArabic)
                chapterInfo?.textSize = 14f * fontScale

                root.contentDescription = buildContentDescription(hadith, isArabic)
            }
        }

        private fun getLocalizedBookName(bookName: String?, isArabic: Boolean): String {
            if (bookName.isNullOrBlank()) {
                return if (isArabic) "كتاب الحديث" else "Hadith Collection"
            }

            if (!isArabic) return bookName

            return when {
                bookName.contains("Bukhari", ignoreCase = true) -> "صحيح البخاري"
                bookName.contains("Muslim", ignoreCase = true) -> "صحيح مسلم"
                bookName.contains("Tirmidhi", ignoreCase = true) -> "سنن الترمذي"
                bookName.contains("Abu Dawud", ignoreCase = true) -> "سنن أبي داود"
                bookName.contains("Nasai", ignoreCase = true) -> "سنن النسائي"
                bookName.contains("Ibn Majah", ignoreCase = true) -> "سنن ابن ماجه"
                bookName.contains("Malik", ignoreCase = true) -> "موطأ مالك"
                bookName.contains("Ahmad", ignoreCase = true) -> "مسند أحمد"
                bookName.contains("Darimi", ignoreCase = true) -> "سنن الدارمي"
                bookName.contains("Hakim", ignoreCase = true) -> "المستدرك"
                else -> bookName
            }
        }

        private fun formatHadithNumber(number: String?, isArabic: Boolean): String {
            val resolved = number?.takeIf { it.isNotBlank() } ?: "1"
            return if (isArabic) convertToArabicNumerals(resolved) else resolved
        }

        private fun convertToArabicNumerals(number: String): String {
            val arabicNumerals = mapOf(
                '0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤',
                '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩'
            )
            return number.map { arabicNumerals[it] ?: it }.joinToString("")
        }

        private fun buildNarratorText(hadith: PresentationHadith, isArabic: Boolean): String {
            val englishNarrator = hadith.englishNarrator?.trim().orEmpty()
            return if (isArabic) {
                val narratorName = if (englishNarrator.isNotBlank()) {
                    translateToArabic(englishNarrator)
                } else {
                    "راوي الحديث"
                }
                "عن $narratorName قال:"
            } else {
                val narratorName = if (englishNarrator.isNotBlank()) {
                    englishNarrator
                        .removePrefix("Narrated by ")
                        .removePrefix("Narrated ")
                        .trim()
                        .trim(':')
                } else {
                    "Unknown narrator"
                }
                "Narrated by $narratorName:"
            }
        }

        private fun formatHadithText(hadith: PresentationHadith, isArabic: Boolean): String {
            val rawText = if (isArabic) {
                hadith.hadithArabic?.takeIf { it.isNotBlank() }
                    ?: hadith.hadithEnglish?.takeIf { it.isNotBlank() }
            } else {
                hadith.hadithEnglish?.takeIf { it.isNotBlank() }
                    ?: hadith.hadithArabic?.takeIf { it.isNotBlank() }
            }

            return cleanText(rawText).ifBlank {
                if (isArabic) "نص الحديث غير متوفر" else "Hadith text is not available"
            }
        }

        private fun buildChapterText(hadith: PresentationHadith, isArabic: Boolean): String {
            return if (isArabic) {
                hadith.headingArabic?.takeIf { it.isNotBlank() }
                    ?: hadith.chapter?.chapterArabic?.takeIf { it.isNotBlank() }
                    ?: hadith.headingEnglish?.takeIf { it.isNotBlank() }
                    ?: "باب الحديث"
            } else {
                hadith.headingEnglish?.takeIf { it.isNotBlank() }
                    ?: hadith.chapter?.chapterEnglish?.takeIf { it.isNotBlank() }
                    ?: hadith.headingArabic?.takeIf { it.isNotBlank() }
                    ?: "Hadith chapter"
            }
        }

        private fun buildContentDescription(hadith: PresentationHadith, isArabic: Boolean): String {
            val bookName = getLocalizedBookName(hadith.book?.bookName, isArabic)
            val narrator = buildNarratorText(hadith, isArabic)
            val text = formatHadithText(hadith, isArabic)
            val number = hadith.hadithNumber?.takeIf { it.isNotBlank() } ?: if (isArabic) "غير محدد" else "Unknown"

            return if (isArabic) {
                "حديث رقم $number من $bookName. $narrator $text"
            } else {
                "Hadith number $number from $bookName. $narrator $text"
            }
        }

        private fun shareHadith(hadith: PresentationHadith) {
            try {
                val context = binding.root.context
                val isArabic = LocaleHelper.isArabic(context)
                val shareText = buildShareText(hadith, isArabic)

                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    putExtra(Intent.EXTRA_SUBJECT, if (isArabic) "حديث شريف" else "Prophetic Hadith")
                }

                val chooserIntent = Intent.createChooser(
                    shareIntent,
                    if (isArabic) "مشاركة الحديث" else "Share hadith"
                )
                if (chooserIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(chooserIntent)
                }
            } catch (_: Exception) {
                // Ignore share failures silently to avoid breaking UX
            }
        }

        private fun buildShareText(hadith: PresentationHadith, isArabic: Boolean): String {
            val bookName = getLocalizedBookName(hadith.book?.bookName, isArabic)
            val chapter = buildChapterText(hadith, isArabic)
            val narrator = buildNarratorText(hadith, isArabic)
            val text = formatHadithText(hadith, isArabic)
            val number = formatHadithNumber(hadith.hadithNumber, isArabic)

            return if (isArabic) {
                buildString {
                    appendLine("📿 حديث شريف")
                    appendLine()
                    appendLine("📚 $bookName - حديث رقم $number")
                    appendLine()
                    appendLine("👤 $narrator")
                    appendLine()
                    appendLine("❝ $text ❞")
                    if (chapter.isNotBlank() && chapter != "باب الحديث") {
                        appendLine()
                        appendLine("📖 $chapter")
                    }
                    appendLine()
                    append("🌙 تم المشاركة من تطبيق ذكرني")
                }
            } else {
                buildString {
                    appendLine("📿 Prophetic Hadith")
                    appendLine()
                    appendLine("📚 $bookName - Hadith #$number")
                    appendLine()
                    appendLine("👤 $narrator")
                    appendLine()
                    appendLine("❝ $text ❞")
                    if (chapter.isNotBlank() && chapter != "Hadith chapter") {
                        appendLine()
                        appendLine("📖 $chapter")
                    }
                    appendLine()
                    append("🌙 Shared via Zakrni App")
                }
            }
        }

        private fun cleanText(text: String?): String {
            return text?.trim()
                ?.replace(Regex("\\s+"), " ")
                ?.replace("\"", "")
                ?.replace("'", "")
                ?.replace(Regex("\\[.*?\\]"), "")
                ?.trim()
                .orEmpty()
        }

        private fun translateToArabic(englishNarrator: String): String {
            val narratorMap = mapOf(
                "Abu Hurairah" to "أبو هريرة رضي الله عنه",
                "Abu Huraira" to "أبو هريرة رضي الله عنه",
                "Aisha" to "عائشة رضي الله عنها",
                "Aishah" to "عائشة رضي الله عنها",
                "Umar" to "عمر بن الخطاب رضي الله عنه",
                "Ali" to "علي بن أبي طالب رضي الله عنه",
                "Ibn Abbas" to "ابن عباس رضي الله عنهما",
                "Anas" to "أنس بن مالك رضي الله عنه"
            )

            narratorMap[englishNarrator]?.let { return it }
            narratorMap.forEach { (english, arabic) ->
                if (englishNarrator.contains(english, ignoreCase = true)) {
                    return arabic
                }
            }
            return englishNarrator
        }
    }

    private class HadithDiffCallback : DiffUtil.ItemCallback<PresentationHadith>() {
        override fun areItemsTheSame(oldItem: PresentationHadith, newItem: PresentationHadith): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PresentationHadith, newItem: PresentationHadith): Boolean {
            return oldItem == newItem
        }
    }
}
