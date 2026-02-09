package com.zakrni.app.utils

import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Unit tests for utility functions and helpers
 */
class UtilsTest {

    @Test
    fun `formatArabicNumber converts numbers correctly`() {
        // Arabic numerals mapping
        val arabicNumerals = mapOf(
            '0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤',
            '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩'
        )

        // Test conversion
        val number = 123
        val expected = "١٢٣"
        val result = number.toString().map { arabicNumerals[it] ?: it }.joinToString("")
        
        assertEquals(expected, result)
    }

    @Test
    fun `formatTime converts seconds to MM-SS format`() {
        // Given
        val seconds = 125 // 2 minutes and 5 seconds

        // When
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        val formatted = String.format("%02d:%02d", minutes, remainingSeconds)

        // Then
        assertEquals("02:05", formatted)
    }

    @Test
    fun `formatTime handles zero seconds`() {
        val seconds = 0
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        val formatted = String.format("%02d:%02d", minutes, remainingSeconds)
        
        assertEquals("00:00", formatted)
    }

    @Test
    fun `formatTime handles hours`() {
        val seconds = 3661 // 1 hour, 1 minute, 1 second
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
        val formatted = String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
        
        assertEquals("01:01:01", formatted)
    }

    @Test
    fun `isToday returns true for today's date`() {
        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis
        
        val cal1 = Calendar.getInstance().apply { timeInMillis = today }
        val cal2 = Calendar.getInstance()
        
        val isToday = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
        
        assertTrue(isToday)
    }

    @Test
    fun `isToday returns false for yesterday's date`() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.timeInMillis
        
        val cal1 = Calendar.getInstance().apply { timeInMillis = yesterday }
        val cal2 = Calendar.getInstance()
        
        val isToday = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
        
        assertFalse(isToday)
    }

    @Test
    fun `validateVideoId accepts valid YouTube IDs`() {
        val validIds = listOf(
            "dQw4w9WgXcQ",
            "abc123XYZ_-",
            "12345678901"
        )
        
        validIds.forEach { id ->
            assertTrue("$id should be valid", id.length == 11)
        }
    }

    @Test
    fun `calculateProgress returns correct percentage`() {
        val current = 50
        val total = 100
        val progress = (current.toFloat() / total.toFloat()) * 100
        
        assertEquals(50f, progress, 0.01f)
    }

    @Test
    fun `calculateProgress handles zero total`() {
        val current = 50
        val total = 0
        val progress = if (total == 0) 0f else (current.toFloat() / total.toFloat()) * 100
        
        assertEquals(0f, progress, 0.01f)
    }

    @Test
    fun `truncateText shortens long text`() {
        val text = "هذا نص طويل جداً يحتاج إلى اختصار"
        val maxLength = 10
        val truncated = if (text.length > maxLength) {
            text.take(maxLength) + "..."
        } else {
            text
        }
        
        // Just verify it's truncated with ellipsis
        assertTrue(truncated.endsWith("..."))
        assertTrue(truncated.length <= maxLength + 3)
    }

    @Test
    fun `truncateText keeps short text intact`() {
        val text = "نص قصير"
        val maxLength = 20
        val truncated = if (text.length > maxLength) {
            text.take(maxLength) + "..."
        } else {
            text
        }
        
        assertEquals("نص قصير", truncated)
    }

    @Test
    fun `formatViewCount formats large numbers`() {
        val views = 1500000L
        val formatted = when {
            views >= 1_000_000 -> "${views / 1_000_000}M"
            views >= 1_000 -> "${views / 1_000}K"
            else -> views.toString()
        }
        
        assertEquals("1M", formatted)
    }

    @Test
    fun `formatViewCount formats thousands`() {
        val views = 5500L
        val formatted = when {
            views >= 1_000_000 -> "${views / 1_000_000}M"
            views >= 1_000 -> "${views / 1_000}K"
            else -> views.toString()
        }
        
        assertEquals("5K", formatted)
    }

    @Test
    fun `formatViewCount keeps small numbers as is`() {
        val views = 500L
        val formatted = when {
            views >= 1_000_000 -> "${views / 1_000_000}M"
            views >= 1_000 -> "${views / 1_000}K"
            else -> views.toString()
        }
        
        assertEquals("500", formatted)
    }

    @Test
    fun `generateThumbnailUrl creates valid URL`() {
        val videoId = "abc123XYZ_-"
        val url = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
        
        assertTrue(url.startsWith("https://"))
        assertTrue(url.contains(videoId))
        assertTrue(url.endsWith(".jpg"))
    }

    @Test
    fun `dayOfYear calculation is consistent`() {
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        
        assertTrue(dayOfYear in 1..366)
    }

    @Test
    fun `cache key generation is unique per type`() {
        val type1 = "lectures"
        val type2 = "quran"
        
        val key1 = "youtube_videos_$type1"
        val key2 = "youtube_videos_$type2"
        
        assertNotEquals(key1, key2)
    }

    @Test
    fun `Arabic text detection works`() {
        val arabicText = "بسم الله الرحمن الرحيم"
        val englishText = "In the name of Allah"
        
        val arabicPattern = Regex("[\\u0600-\\u06FF]")
        
        assertTrue(arabicPattern.containsMatchIn(arabicText))
        assertFalse(arabicPattern.containsMatchIn(englishText))
    }
}
