package com.example.zakrni.clean.ui.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zakrni.clean.ui.models.PresentationHadith
import com.example.zakrni.databinding.ItemHadithBinding

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
        val hadith = getItem(position)
        holder.bind(hadith)
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
            with(binding) {
                // Book name
                bookName?.text = getLocalizedBookName(hadith.book?.bookName)
                // hadith number text
                hadithNumber?.text = formatHadithNumber(hadith.hadithNumber ?: 1)
                // Narrator with proper Arabic formatting
                narrator?.text = buildNarratorText(hadith)
                // Main hadith text with cleaning and formatting
                hadithText?.text = formatArabicText(hadith.hadithArabic)
                // Chapter info
                chapterInfo?.text = buildChapterText(hadith)
                // Set content description for accessibility
                binding.root.contentDescription = buildContentDescription(hadith)
            }
        }

        private fun getLocalizedBookName(bookName: String?): String {
            return bookName?.let { name ->
                when {
                    name.contains("Bukhari", ignoreCase = true) -> "صحيح البخاري"
                    name.contains("Muslim", ignoreCase = true) -> "صحيح مسلم"
                    name.contains("Tirmidhi", ignoreCase = true) -> "سنن الترمذي"
                    name.contains("Abu Dawud", ignoreCase = true) -> "سنن أبي داود"
                    name.contains("Nasai", ignoreCase = true) -> "سنن النسائي"
                    name.contains("Ibn Majah", ignoreCase = true) -> "سنن ابن ماجه"
                    name.contains("Malik", ignoreCase = true) -> "موطأ مالك"
                    name.contains("Ahmad", ignoreCase = true) -> "مسند أحمد"
                    name.contains("Darimi", ignoreCase = true) -> "سنن الدارمي"
                    name.contains("Hakim", ignoreCase = true) -> "المستدرك"
                    else -> name
                }
            } ?: "كتاب الحديث"
        }

        private fun formatHadithNumber(number: Any): String {
            return number?.let {
                convertToArabicNumerals(it.toString())
            } ?: "١"
        }

        private fun convertToArabicNumerals(number: String): String {
            val arabicNumerals = mapOf(
                '0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤',
                '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩'
            )
            return number.map { arabicNumerals[it] ?: it }.joinToString("")
        }

        private fun buildNarratorText(hadith: PresentationHadith): String {
            val arabicNarrator = when {
                !hadith.englishNarrator.isNullOrEmpty() -> translateToArabic(hadith.englishNarrator)
                else -> "راوي الحديث"
            }
            return "عن $arabicNarrator قال:"
        }

        private fun formatArabicText(text: String?): String {
            return text?.let { arabicText ->
                arabicText
                    .trim()
                    .replace(Regex("\\s+"), " ") // Normalize spaces
                    .replace("\"", "") // Remove quotes
                    .replace("'", "") // Remove apostrophes
                    .replace(Regex("\\[.*?\\]"), "") // Remove brackets with references
                    .trim()
            } ?: "النص العربي غير متوفر"
        }

        private fun buildChapterText(hadith: PresentationHadith): String {
            return when {
                !hadith.headingArabic.isNullOrEmpty() -> hadith.headingArabic!!
                !hadith.chapter?.chapterArabic.isNullOrEmpty() -> hadith.chapter!!.chapterArabic!!
                !hadith.headingEnglish.isNullOrEmpty() -> hadith.headingEnglish!!
                else -> "باب الحديث"
            }
        }

        private fun buildContentDescription(hadith: PresentationHadith): String {
            return buildString {
                append("حديث رقم ")
                append(hadith.hadithNumber ?: "غير محدد")
                append(" من ")
                append(getLocalizedBookName(hadith.book?.bookName))
                append(". ")
                append("عن ")
                append(translateToArabic(hadith.englishNarrator))
                append(". ")
                append(formatArabicText(hadith.hadithArabic))
            }
        }

        private fun shareHadith(hadith: PresentationHadith) {
            try {
                val context = binding.root.context
                val shareText = buildShareText(hadith)

                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    putExtra(Intent.EXTRA_SUBJECT, "حديث شريف")
                }

                val chooserIntent = Intent.createChooser(shareIntent, "مشاركة الحديث")
                if (chooserIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(chooserIntent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun buildShareText(hadith: PresentationHadith): String {
            return buildString {
                appendLine("📿 حديث شريف")
                appendLine("بسم الله الرحمن الرحيم")
                appendLine()

                append("📚 ")
                append(getLocalizedBookName(hadith.book?.bookName))
                hadith.hadithNumber?.let {
                    append(" - حديث رقم ")
                    append(convertToArabicNumerals(it.toString()))
                }
                appendLine()
                appendLine()

                append("👤 ")
                append(buildNarratorText(hadith))
                appendLine()
                appendLine()

                append("❝ ")
                append(formatArabicText(hadith.hadithArabic))
                append(" ❞")
                appendLine()
                appendLine()

                val chapterText = buildChapterText(hadith)
                if (chapterText != "باب الحديث" && chapterText.isNotBlank()) {
                    append("📖 ")
                    append(chapterText)
                    appendLine()
                    appendLine()
                }

                appendLine("صدق رسول الله ﷺ")
                appendLine()
                append("🌙 تم المشاركة من تطبيق ذكرني")
            }
        }

        private fun translateToArabic(englishNarrator: String?): String {
            if (englishNarrator.isNullOrEmpty()) return "راوي الحديث"
            val narratorMap = mapOf(

                "Abu Hurairah" to "أبو هريرة رضي الله عنه",
                "Abu Huraira" to "أبو هريرة رضي الله عنه",
                "Abu Hurraira" to "أبو هريرة رضي الله عنه",
                "Abu Hurayrah" to "أبو هريرة رضي الله عنه",
                "Aisha" to "عائشة رضي الله عنها",
                "A'isha" to "عائشة رضي الله عنها",
                "Aishah" to "عائشة رضي الله عنها",
                "A'ishah" to "عائشة رضي الله عنها",
                "Abdullah ibn Umar" to "عبد الله بن عمر رضي الله عنهما",
                "Abdullah bin Umar" to "عبد الله بن عمر رضي الله عنهما",
                "Ibn Umar" to "ابن عمر رضي الله عنهما",
                "Anas ibn Malik" to "أنس بن مالك رضي الله عنه",
                "Anas bin Malik" to "أنس بن مالك رضي الله عنه",
                "Anas" to "أنس بن مالك رضي الله عنه",
                "Abu Bakr" to "أبو بكر الصديق رضي الله عنه",
                "Umar ibn al-Khattab" to "عمر بن الخطاب رضي الله عنه",
                "Umar bin al-Khattab" to "عمر بن الخطاب رضي الله عنه",
                "Umar" to "عمر بن الخطاب رضي الله عنه",
                "Ali ibn Abi Talib" to "علي بن أبي طالب رضي الله عنه",
                "Ali bin Abi Talib" to "علي بن أبي طالب رضي الله عنه",
                "Ali" to "علي بن أبي طالب رضي الله عنه",
                "Jabir ibn Abdullah" to "جابر بن عبد الله رضي الله عنهما",
                "Jabir bin Abdullah" to "جابر بن عبد الله رضي الله عنهما",
                "Jabir" to "جابر بن عبد الله رضي الله عنهما",

                "Abu Sa'id al-Khudri" to "أبو سعيد الخدري رضي الله عنه",
                "Abu Said al-Khudri" to "أبو سعيد الخدري رضي الله عنه",
                "Abu Saeed al-Khudri" to "أبو سعيد الخدري رضي الله عنه",
                "Ibn Abbas" to "ابن عباس رضي الله عنهما",
                "Abdullah ibn Abbas" to "عبد الله بن عباس رضي الله عنهما",
                "Abdullah bin Abbas" to "عبد الله بن عباس رضي الله عنهما",
                "Abdullah ibn Amr" to "عبد الله بن عمرو رضي الله عنهما",
                "Abdullah ibn Mas'ud" to "عبد الله بن مسعود رضي الله عنه",
                "Ibn Mas'ud" to "ابن مسعود رضي الله عنه",
                "Salman al-Farisi" to "سلمان الفارسي رضي الله عنه",
                "Bilal ibn Rabah" to "بلال بن رباح رضي الله عنه",
                "Bilal" to "بلال بن رباح رضي الله عنه",
                "Khadijah" to "خديجة رضي الله عنها",
                "Fatimah" to "فاطمة رضي الله عنها",
                "Umm Salama" to "أم سلمة رضي الله عنها",
                "Hafsah" to "حفصة رضي الله عنها",
                "Zainab" to "زينب رضي الله عنها",
                "Umm Habiba" to "أم حبيبة رضي الله عنها",
                "Maymuna" to "ميمونة رضي الله عنها",
                "Juwayriya" to "جويرية رضي الله عنها",
                "Sawda" to "سودة رضي الله عنها"
            )
            // Try exact match first
            narratorMap[englishNarrator]?.let { return it }

            // Try case-insensitive match
            narratorMap.forEach { (english, arabic) ->
                if (english.equals(englishNarrator, ignoreCase = true)) {
                    return arabic
                }
            }

            // Try partial matches for complex names
            narratorMap.forEach { (english, arabic) ->
                if (englishNarrator.contains(english, ignoreCase = true) ||
                    english.contains(englishNarrator, ignoreCase = true)) {
                    return arabic
                }
            }

            // If no match found, add appropriate blessing if not already present
            return when {
                englishNarrator.contains("رضي الله عنه") ||
                        englishNarrator.contains("رضي الله عنها") ||
                        englishNarrator.contains("رضي الله عنهما") -> englishNarrator

                // Check if it's likely a female name for proper blessing
                englishNarrator.lowercase().let { name ->
                    name.contains("aisha") || name.contains("fatima") || name.contains("khadijah") ||
                            name.contains("umm") || name.contains("zainab") || name.contains("hafsah")
                } -> "$englishNarrator رضي الله عنها"

                else -> "$englishNarrator رضي الله عنه"
            }
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