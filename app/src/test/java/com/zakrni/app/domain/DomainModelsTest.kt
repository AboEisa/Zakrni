package com.zakrni.app.domain

import com.zakrni.app.clean.domain.models.DomainSurah
import com.zakrni.app.clean.domain.models.DomainAyah
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Domain Models
 */
class DomainModelsTest {

    @Test
    fun `DomainSurah contains all required properties`() {
        // Given
        val surah = DomainSurah(
            ayahs = emptyList(),
            englishName = "Al-Fatihah",
            englishNameTranslation = "The Opening",
            name = "سُورَةُ ٱلْفَاتِحَةِ",
            number = 1,
            revelationType = "Meccan"
        )

        // Then
        assertEquals(1, surah.number)
        assertEquals("سُورَةُ ٱلْفَاتِحَةِ", surah.name)
        assertEquals("Al-Fatihah", surah.englishName)
        assertEquals("The Opening", surah.englishNameTranslation)
        assertEquals("Meccan", surah.revelationType)
        assertTrue(surah.ayahs.isEmpty())
    }

    @Test
    fun `DomainAyah contains all required properties`() {
        // Given
        val ayah = DomainAyah(
            hizbQuarter = 1,
            juz = 1,
            manzil = 1,
            number = 1,
            numberInSurah = 1,
            page = 1,
            ruku = 1,
            sajda = false,
            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
        )

        // Then
        assertEquals(1, ayah.number)
        assertEquals("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", ayah.text)
        assertEquals(1, ayah.numberInSurah)
        assertEquals(1, ayah.juz)
        assertEquals(1, ayah.page)
    }

    @Test
    fun `DomainSurah with ayahs list`() {
        // Given
        val ayahs = listOf(
            DomainAyah(1, 1, 1, 1, 1, 1, 1, null, "الآية الأولى"),
            DomainAyah(1, 1, 1, 2, 2, 1, 1, null, "الآية الثانية"),
            DomainAyah(1, 1, 1, 3, 3, 1, 1, null, "الآية الثالثة")
        )
        val surah = DomainSurah(
            ayahs = ayahs,
            englishName = "Al-Fatihah",
            englishNameTranslation = "The Opening",
            name = "الفاتحة",
            number = 1,
            revelationType = "Meccan"
        )

        // Then
        assertEquals(3, surah.ayahs.size)
        assertEquals("الآية الأولى", surah.ayahs[0].text)
    }

    @Test
    fun `Meccan vs Medinan revelation types`() {
        // Given
        val meccanSurah = DomainSurah(emptyList(), "Al-Fatihah", "The Opening", "الفاتحة", 1, "Meccan")
        val medinanSurah = DomainSurah(emptyList(), "Al-Baqarah", "The Cow", "البقرة", 2, "Medinan")

        // Then
        assertEquals("Meccan", meccanSurah.revelationType)
        assertEquals("Medinan", medinanSurah.revelationType)
    }

    @Test
    fun `Surah numbers are valid range`() {
        // Quran has 114 surahs
        val validRange = 1..114
        
        assertTrue(1 in validRange)
        assertTrue(114 in validRange)
        assertFalse(0 in validRange)
        assertFalse(115 in validRange)
    }

    @Test
    fun `Juz numbers are valid range`() {
        // Quran has 30 juz
        val validRange = 1..30
        
        assertTrue(1 in validRange)
        assertTrue(30 in validRange)
        assertFalse(0 in validRange)
        assertFalse(31 in validRange)
    }

    @Test
    fun `Page numbers are valid range`() {
        // Mushaf has approximately 604 pages
        val validRange = 1..604
        
        assertTrue(1 in validRange)
        assertTrue(604 in validRange)
        assertFalse(0 in validRange)
        assertFalse(605 in validRange)
    }

    @Test
    fun `Surah equality based on properties`() {
        // Given
        val surah1 = DomainSurah(emptyList(), "Al-Fatihah", "The Opening", "الفاتحة", 1, "Meccan")
        val surah2 = DomainSurah(emptyList(), "Al-Fatihah", "The Opening", "الفاتحة", 1, "Meccan")
        val surah3 = DomainSurah(emptyList(), "Al-Baqarah", "The Cow", "البقرة", 2, "Medinan")

        // Then
        assertEquals(surah1, surah2)
        assertNotEquals(surah1, surah3)
    }

    @Test
    fun `Ayah text is not empty`() {
        // Given
        val ayah = DomainAyah(1, 1, 1, 1, 1, 1, 1, null, "بِسْمِ اللَّهِ")

        // Then
        assertTrue(ayah.text.isNotEmpty())
        assertTrue(ayah.text.isNotBlank())
    }

    @Test
    fun `Ayah sajda can be null or boolean`() {
        // Given
        val ayahNoSajda = DomainAyah(1, 1, 1, 1, 1, 1, 1, null, "آية عادية")
        val ayahWithSajda = DomainAyah(1, 1, 1, 1, 1, 1, 1, true, "آية سجدة")

        // Then
        assertNull(ayahNoSajda.sajda)
        assertEquals(true, ayahWithSajda.sajda)
    }
}
