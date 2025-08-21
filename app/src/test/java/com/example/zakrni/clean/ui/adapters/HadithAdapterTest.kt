package com.example.zakrni.clean.ui.adapters

import com.example.zakrni.clean.ui.models.PresentationBook
import com.example.zakrni.clean.ui.models.PresentationChapter
import com.example.zakrni.clean.ui.models.PresentationHadith
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for HadithAdapter to ensure null safety
 */
class HadithAdapterTest {

    @Test
    fun `test adapter handles null hadith objects gracefully`() {
        val adapter = HadithAdapter()
        
        // Test with empty list
        adapter.updateData(emptyList())
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `test adapter handles hadith with all null properties`() {
        val adapter = HadithAdapter()
        
        val hadithWithNulls = PresentationHadith(
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
        
        adapter.updateData(listOf(hadithWithNulls))
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `test adapter handles hadith with null book and chapter`() {
        val adapter = HadithAdapter()
        
        val hadithWithNullBookAndChapter = PresentationHadith(
            id = "1",
            book = null, // This should not cause NPE
            chapter = null, // This should not cause NPE  
            hadithNumber = "1",
            hadithArabic = "حديث عربي",
            hadithEnglish = "English hadith",
            hadithUrdu = "اردو حديث",
            urduNarrator = null, // This should not cause NPE
            englishNarrator = null, // This should not cause NPE
            headingArabic = null, // This should not cause NPE
            headingUrdu = null, // This should not cause NPE
            headingEnglish = null, // This should not cause NPE
            status = "sahih"
        )
        
        adapter.updateData(listOf(hadithWithNullBookAndChapter))
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `test adapter handles hadith with empty book properties`() {
        val adapter = HadithAdapter()
        
        val emptyBook = PresentationBook(
            bookName = null, // This should not cause NPE
            bookNameArabic = "",
            bookNameUrdu = "",
            bookId = null
        )
        
        val emptyChapter = PresentationChapter(
            chapterNumber = null, // This should not cause NPE
            chapterName = "",
            chapterNameArabic = "",
            chapterNameUrdu = "",
            chapterId = null
        )
        
        val hadithWithEmptyBookAndChapter = PresentationHadith(
            id = "1",
            book = emptyBook,
            chapter = emptyChapter,
            hadithNumber = "1",
            hadithArabic = "حديث عربي",
            hadithEnglish = "English hadith",
            hadithUrdu = "اردو حديث",
            urduNarrator = "", // Empty string should not cause NPE
            englishNarrator = "", // Empty string should not cause NPE
            headingArabic = "", // Empty string should not cause NPE
            headingUrdu = "", // Empty string should not cause NPE
            headingEnglish = "", // Empty string should not cause NPE
            status = "sahih"
        )
        
        adapter.updateData(listOf(hadithWithEmptyBookAndChapter))
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `test adapter handles mixed null and valid data`() {
        val adapter = HadithAdapter()
        
        val validBook = PresentationBook(
            bookName = "Sahih Bukhari",
            bookNameArabic = "صحيح البخاري",
            bookNameUrdu = "صحیح بخاری",
            bookId = "bukhari"
        )
        
        val validChapter = PresentationChapter(
            chapterNumber = "1",
            chapterName = "How the Divine Inspiration started",
            chapterNameArabic = "كيف بدأ الوحي",
            chapterNameUrdu = "وحی کی ابتداء کیسے ہوئی",
            chapterId = "ch1"
        )
        
        val hadithWithValidData = PresentationHadith(
            id = "1",
            book = validBook,
            chapter = validChapter,
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
        
        val hadithWithNulls = PresentationHadith(
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
        
        adapter.updateData(listOf(hadithWithValidData, hadithWithNulls))
        assertEquals(2, adapter.itemCount)
    }
}