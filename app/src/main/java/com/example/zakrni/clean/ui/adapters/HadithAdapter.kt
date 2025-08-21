package com.example.zakrni.clean.ui.adapters

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

        fun bind(hadith: PresentationHadith) {
            with(binding) {
                // Hadith number and book info - Handle nulls safely
                hadithNumber.text = buildString {
                    append("حديث رقم ")
                    append(hadith.hadithNumber ?: "غير محدد")
                }

                // Book info
                bookInfo.text = buildString {
                    val bookName = hadith.book?.bookName ?: "كتاب غير محدد"
                    val volume = hadith.volume ?: "غير محدد"
                    append("$bookName - المجلد $volume")
                }

                // Narrator
                val narratorText = when {

                    !hadith.englishNarrator.isNullOrEmpty() -> translateToArabic(hadith.englishNarrator)
                    else -> "راوي غير محدد"
                }
                narrator.text = narratorText

                // Arabic hadith text
                hadithArabic.text = hadith.hadithArabic ?: "النص العربي غير متوفر"

                // Heading - prioritize Arabic then others, handle nulls
                val headingText = when {
                    !hadith.headingArabic.isNullOrEmpty() -> hadith.headingArabic
                    !hadith.headingEnglish.isNullOrEmpty() -> hadith.headingEnglish
                    else -> null
                }

                if (!headingText.isNullOrEmpty()) {
                    hadithHeading.text = headingText
                    hadithHeading.visibility = android.view.View.VISIBLE
                } else {
                    hadithHeading.visibility = android.view.View.GONE
                }

                // Chapter info
                chapterInfo.text = buildString {
                    val chapterNumber = hadith.chapter?.chapterNumber ?: "غير محدد"
                    val chapterArabic = hadith.chapter?.chapterArabic ?: "باب غير محدد"
                    append("الباب $chapterNumber - $chapterArabic")
                }
            }
        }

        private fun translateToArabic(englishNarrator: String?): String {
            if (englishNarrator.isNullOrEmpty()) return "راوي غير محدد"

            val narratorMap = mapOf(
                "Abu Hurairah" to "أبو هريرة",
                "Abu Huraira" to "أبو هريرة",
                "Abu Hurraira" to "أبو هريرة",
                "Aisha" to "عائشة",
                "A'isha" to "عائشة",
                "Aishah" to "عائشة",
                "Abdullah ibn Umar" to "عبد الله بن عمر",
                "Ibn Umar" to "ابن عمر",
                "Anas ibn Malik" to "أنس بن مالك",
                "Anas" to "أنس بن مالك",
                "Abu Bakr" to "أبو بكر",
                "Umar ibn al-Khattab" to "عمر بن الخطاب",
                "Umar" to "عمر بن الخطاب",
                "Ali ibn Abi Talib" to "علي بن أبي طالب",
                "Ali" to "علي بن أبي طالب",
                "Jabir ibn Abdullah" to "جابر بن عبد الله",
                "Jabir" to "جابر بن عبد الله",
                "Abu Sa'id al-Khudri" to "أبو سعيد الخدري",
                "Abu Said al-Khudri" to "أبو سعيد الخدري",
                "Ibn Abbas" to "ابن عباس",
                "Abdullah ibn Abbas" to "عبد الله بن عباس",
                "Abdullah bin Abbas" to "عبد الله بن عباس",
                "Abdullah ibn Amr" to "عبد الله بن عمرو",
                "Abdullah bin Amr" to "عبد الله بن عمرو",
                "Abdullah ibn Amr ibn al-As" to "عبد الله بن عمرو بن العاص",
                "Ibn Amr" to "ابن عمرو",
                "Salman al-Farisi" to "سلمان الفارسي",
                "Salman al-Farsi" to "سلمان الفارسي",
                "Bilal ibn Rabah" to "بلال بن رباح",
                "Bilal" to "بلال بن رباح",
                "Khadijah" to "خديجة",
                "Fatimah" to "فاطمة",
                "Fatima" to "فاطمة",
                "Zaid ibn Thabit" to "زيد بن ثابت",
                "Zayd ibn Thabit" to "زيد بن ثابت",
                "Abdullah ibn Mas'ud" to "عبد الله بن مسعود",
                "Ibn Mas'ud" to "ابن مسعود",
                "Abu Musa al-Ash'ari" to "أبو موسى الأشعري",
                "Abu Musa" to "أبو موسى الأشعري",
                "Mu'adh ibn Jabal" to "معاذ بن جبل",
                "Muadh ibn Jabal" to "معاذ بن جبل",
                "Ubadah ibn as-Samit" to "عبادة بن الصامت",
                "Abu Dharr" to "أبو ذر",
                "Abu Dhar" to "أبو ذر",
                "Usama ibn Zaid" to "أسامة بن زيد",
                "Ammar ibn Yasir" to "عمار بن ياسر",
                "Hudhaifa" to "حذيفة",
                "Hudhaifah" to "حذيفة",
                "Ka'b ibn Malik" to "كعب بن مالك",
                "Abu Talha" to "أبو طلحة",
                "Safiyya" to "صفية",
                "Umm Salama" to "أم سلمة",
                "Hafsah" to "حفصة",
                "Zainab" to "زينب",
                "Umm Habiba" to "أم حبيبة",
                "Maymuna" to "ميمونة",
                "Juwayriya" to "جويرية",
                "Sawda" to "سودة",
                "Saeed ibn Jubair" to "سعيد بن جبير",
                "Said ibn Jubayr" to "سعيد بن جبير",
                "Saeed ibn al-Musayyib" to "سعيد بن المسيب",
                "Ibn al-Zubayr" to "ابن الزبير",
                "Abdullah ibn al-Zubayr" to "عبد الله بن الزبير",
                "Al-Bara ibn Azib" to "البراء بن عازب",
                "Ibn Shihab al-Zuhri" to "ابن شهاب الزهري",
                "Abu Umamah" to "أبو أمامة",
                "Abu Darda" to "أبو الدرداء",
                "Abu Ayyub al-Ansari" to "أبو أيوب الأنصاري",
                "Abu Bakrah" to "أبو بكرة",
                "Abu Malik al-Ash'ari" to "أبو مالك الأشعري",
                "Abu Sufyan" to "أبو سفيان"
            )



            // Try exact match first
            narratorMap[englishNarrator]?.let { return it }

            // Try case-insensitive match
            narratorMap.forEach { (english, arabic) ->
                if (english.equals(englishNarrator, ignoreCase = true)) {
                    return arabic
                }
            }

            // Try partial matches
            narratorMap.forEach { (english, arabic) ->
                if (englishNarrator.contains(english, ignoreCase = true) ||
                    english.contains(englishNarrator, ignoreCase = true)) {
                    return arabic
                }
            }

            // Return original if no match found
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