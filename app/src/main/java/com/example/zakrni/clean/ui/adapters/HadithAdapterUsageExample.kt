package com.example.zakrni.clean.ui.adapters

import android.view.View
import com.example.zakrni.clean.ui.models.PresentationBook
import com.example.zakrni.clean.ui.models.PresentationChapter
import com.example.zakrni.clean.ui.models.PresentationHadith

/**
 * Example usage of HadithAdapter demonstrating null safety
 * This class shows how to use the HadithAdapter safely with various null scenarios
 */
class HadithAdapterUsageExample {

    /**
     * Example of how to use the adapter with proper null handling
     */
    fun demonstrateNullSafeUsage() {
        val adapter = HadithAdapter()

        // Example 1: Complete hadith with all data
        val completeHadith = PresentationHadith(
            id = "1",
            book = PresentationBook(
                bookName = "Sahih Bukhari",
                bookNameArabic = "صحيح البخاري",
                bookNameUrdu = "صحیح بخاری",
                bookId = "bukhari"
            ),
            chapter = PresentationChapter(
                chapterNumber = "1",
                chapterName = "How the Divine Inspiration started",
                chapterNameArabic = "كيف بدأ الوحي",
                chapterNameUrdu = "وحی کی ابتداء کیسے ہوئی",
                chapterId = "ch1"
            ),
            hadithNumber = "1",
            hadithArabic = "إنما الأعمال بالنيات",
            hadithEnglish = "Actions are judged by intentions",
            hadithUrdu = "اعمال کا دارومدار نیتوں پر ہے",
            urduNarrator = "عمر بن خطاب",
            englishNarrator = "Umar ibn al-Khattab",
            headingArabic = "باب النية",
            headingUrdu = "نیت کا باب",
            headingEnglish = "Chapter on Intention",
            status = "sahih"
        )

        // Example 2: Hadith with null book and chapter (this was causing the original NPE)
        val hadithWithNullBookAndChapter = PresentationHadith(
            id = "2",
            book = null, // This will be safely handled by the adapter
            chapter = null, // This will be safely handled by the adapter
            hadithNumber = "2",
            hadithArabic = "بُنِيَ الإِسْلاَمُ عَلَى خَمْسٍ",
            hadithEnglish = "Islam is built upon five pillars",
            hadithUrdu = "اسلام پانچ بنیادوں پر قائم ہے",
            urduNarrator = null, // This will be safely handled
            englishNarrator = null, // This will be safely handled
            headingArabic = null, // This will be safely handled
            headingUrdu = null, // This will be safely handled
            headingEnglish = null, // This will be safely handled
            status = "sahih"
        )

        // Example 3: Hadith with empty strings (also handled safely)
        val hadithWithEmptyStrings = PresentationHadith(
            id = "3",
            book = PresentationBook(
                bookName = "", // Empty string will be handled safely
                bookNameArabic = "",
                bookNameUrdu = "",
                bookId = ""
            ),
            chapter = PresentationChapter(
                chapterNumber = "", // Empty string will be handled safely
                chapterName = "",
                chapterNameArabic = "",
                chapterNameUrdu = "",
                chapterId = ""
            ),
            hadithNumber = "3",
            hadithArabic = "خَيْرُ النَّاسِ أَنْفَعُهُمْ لِلنَّاسِ",
            hadithEnglish = "The best of people are those who are most beneficial to others",
            hadithUrdu = "بہترین لوگ وہ ہیں جو دوسروں کے لیے زیادہ فائدہ مند ہیں",
            urduNarrator = "",
            englishNarrator = "",
            headingArabic = "",
            headingUrdu = "",
            headingEnglish = "",
            status = "sahih"
        )

        // Example 4: Completely null hadith (extreme case)
        val completelyNullHadith = PresentationHadith(
            id = null,
            book = null,
            chapter = null,
            hadithNumber = null,
            hadithArabic = null,
            hadithEnglish = null,
            hadithUrdu = null,
            urduNarrator = null,
            englishNarrator = null,
            headingArabic = null,
            headingUrdu = null,
            headingEnglish = null,
            status = null
        )

        // Update the adapter with various null scenarios
        // This demonstrates that the adapter will handle all these cases without crashing
        val hadithList = listOf(
            completeHadith,
            hadithWithNullBookAndChapter,
            hadithWithEmptyStrings,
            completelyNullHadith
        )

        adapter.updateData(hadithList)

        // The adapter now contains 4 items and won't crash when binding any of them
        // Line 48 in HadithAdapter.bind() safely handles hadith.book?.bookName
        // All other null checks prevent NPEs for the properties mentioned in the problem statement
    }
}