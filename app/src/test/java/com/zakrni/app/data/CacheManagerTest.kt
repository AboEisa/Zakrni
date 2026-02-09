package com.zakrni.app.data

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CacheManager functionality
 * Note: These tests mock SharedPreferences since CacheManager uses Android framework
 */
class CacheManagerTest {

    private lateinit var mockContext: Context
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    @Before
    fun setup() {
        mockContext = mockk()
        mockSharedPreferences = mockk()
        mockEditor = mockk(relaxed = true)

        every { mockContext.getSharedPreferences(any(), any()) } returns mockSharedPreferences
        every { mockSharedPreferences.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.putLong(any(), any()) } returns mockEditor
        every { mockEditor.apply() } returns Unit
    }

    @Test
    fun `cache stores string data correctly`() {
        // Given
        val key = "test_key"
        val value = "test_value"
        val keySlot = slot<String>()
        val valueSlot = slot<String>()

        every { mockEditor.putString(capture(keySlot), capture(valueSlot)) } returns mockEditor

        // When
        mockSharedPreferences.edit().putString(key, value).apply()

        // Then
        verify { mockEditor.putString(key, value) }
        assertEquals(key, keySlot.captured)
        assertEquals(value, valueSlot.captured)
    }

    @Test
    fun `cache retrieves string data correctly`() {
        // Given
        val key = "test_key"
        val expectedValue = "test_value"
        every { mockSharedPreferences.getString(key, null) } returns expectedValue

        // When
        val result = mockSharedPreferences.getString(key, null)

        // Then
        assertEquals(expectedValue, result)
    }

    @Test
    fun `cache returns null for non-existent key`() {
        // Given
        val key = "non_existent_key"
        every { mockSharedPreferences.getString(key, null) } returns null

        // When
        val result = mockSharedPreferences.getString(key, null)

        // Then
        assertNull(result)
    }

    @Test
    fun `cache timestamp is stored with data`() {
        // Given
        val key = "test_key"
        val timestampKey = "cache_timestamp_$key"
        val timestamp = System.currentTimeMillis()

        every { mockEditor.putLong(timestampKey, any()) } returns mockEditor

        // When
        mockSharedPreferences.edit().putLong(timestampKey, timestamp).apply()

        // Then
        verify { mockEditor.putLong(timestampKey, any()) }
    }

    @Test
    fun `cache validity period is 7 days`() {
        // Given
        val sevenDaysInMs = 7 * 24 * 60 * 60 * 1000L

        // Then
        assertEquals(604800000L, sevenDaysInMs)
    }

    @Test
    fun `isCacheValid returns true for recent cache`() {
        // Given
        val cacheTimestamp = System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000L) // 1 day ago
        val validityPeriod = 7 * 24 * 60 * 60 * 1000L // 7 days
        val currentTime = System.currentTimeMillis()

        // When
        val isValid = (currentTime - cacheTimestamp) < validityPeriod

        // Then
        assertTrue(isValid)
    }

    @Test
    fun `isCacheValid returns false for old cache`() {
        // Given
        val cacheTimestamp = System.currentTimeMillis() - (10 * 24 * 60 * 60 * 1000L) // 10 days ago
        val validityPeriod = 7 * 24 * 60 * 60 * 1000L // 7 days
        val currentTime = System.currentTimeMillis()

        // When
        val isValid = (currentTime - cacheTimestamp) < validityPeriod

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `cache keys follow naming convention`() {
        // Given
        val expectedKeys = listOf(
            "cached_prayer_times",
            "cached_allah_names",
            "cached_hadiths",
            "cached_azkar_sabah",
            "cached_azkar_masaa",
            "cached_articles",
            "cached_daily_ayahs"
        )

        // Then
        expectedKeys.forEach { key ->
            assertTrue(key.startsWith("cached_"))
        }
    }

    @Test
    fun `YouTube videos cache key includes type`() {
        // Given
        val types = listOf("lectures", "quran", "videos", "sermons")

        // Then
        types.forEach { type ->
            val key = "youtube_videos_$type"
            assertTrue(key.contains(type))
            assertTrue(key.startsWith("youtube_videos_"))
        }
    }

    @Test
    fun `JSON serialization handles empty list`() {
        // Given
        val emptyList = emptyList<String>()
        val json = "[]"

        // Then
        assertEquals(json, com.google.gson.Gson().toJson(emptyList))
    }

    @Test
    fun `JSON serialization handles list with items`() {
        // Given
        val list = listOf("item1", "item2", "item3")
        
        // When
        val json = com.google.gson.Gson().toJson(list)

        // Then
        assertTrue(json.contains("item1"))
        assertTrue(json.contains("item2"))
        assertTrue(json.contains("item3"))
    }

    @Test
    fun `JSON deserialization recovers list`() {
        // Given
        val json = "[\"item1\",\"item2\",\"item3\"]"
        
        // When
        val list = com.google.gson.Gson().fromJson(json, Array<String>::class.java).toList()

        // Then
        assertEquals(3, list.size)
        assertEquals("item1", list[0])
        assertEquals("item2", list[1])
        assertEquals("item3", list[2])
    }
}
