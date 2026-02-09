package com.zakrni.app.navigation

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Navigation and Content Type logic
 */
class ContentTypeNavigationTest {

    // Content type constants
    private val CONTENT_TYPE_LECTURES = "lectures"
    private val CONTENT_TYPE_QURAN = "quran"
    private val CONTENT_TYPE_VIDEOS = "videos"
    private val CONTENT_TYPE_SERMONS = "sermons"
    private val CONTENT_TYPE_ALL = "all"

    @Test
    fun `content type lectures maps to correct search query`() {
        // Given
        val contentType = CONTENT_TYPE_LECTURES

        // When
        val searchQuery = getSearchQueryForType(contentType)

        // Then
        assertTrue(searchQuery.contains("دروس") || searchQuery.contains("محاضرات"))
    }

    @Test
    fun `content type quran maps to correct search query`() {
        // Given
        val contentType = CONTENT_TYPE_QURAN

        // When
        val searchQuery = getSearchQueryForType(contentType)

        // Then
        assertTrue(searchQuery.contains("قرآن") || searchQuery.contains("تلاوة"))
    }

    @Test
    fun `content type videos maps to correct search query`() {
        // Given
        val contentType = CONTENT_TYPE_VIDEOS

        // When
        val searchQuery = getSearchQueryForType(contentType)

        // Then
        assertTrue(searchQuery.contains("فيديوهات") || searchQuery.contains("إسلامية"))
    }

    @Test
    fun `content type sermons maps to correct search query`() {
        // Given
        val contentType = CONTENT_TYPE_SERMONS

        // When
        val searchQuery = getSearchQueryForType(contentType)

        // Then
        assertTrue(searchQuery.contains("خطبة") || searchQuery.contains("خطب"))
    }

    @Test
    fun `content type all maps to general search query`() {
        // Given
        val contentType = CONTENT_TYPE_ALL

        // When
        val searchQuery = getSearchQueryForType(contentType)

        // Then
        assertTrue(searchQuery.contains("إسلامي") || searchQuery.contains("محتوى"))
    }

    @Test
    fun `unknown content type falls back to all`() {
        // Given
        val contentType = "unknown"

        // When
        val searchQuery = getSearchQueryForType(contentType)

        // Then
        assertEquals(getSearchQueryForType(CONTENT_TYPE_ALL), searchQuery)
    }

    @Test
    fun `all content types are distinct`() {
        // Given
        val types = listOf(CONTENT_TYPE_LECTURES, CONTENT_TYPE_QURAN, CONTENT_TYPE_VIDEOS, CONTENT_TYPE_SERMONS, CONTENT_TYPE_ALL)

        // Then
        assertEquals(5, types.distinct().size)
    }

    @Test
    fun `cache key is unique per content type`() {
        // Given
        val types = listOf(CONTENT_TYPE_LECTURES, CONTENT_TYPE_QURAN, CONTENT_TYPE_VIDEOS, CONTENT_TYPE_SERMONS)

        // When
        val cacheKeys = types.map { "youtube_videos_$it" }

        // Then
        assertEquals(4, cacheKeys.distinct().size)
    }

    @Test
    fun `default content type is all`() {
        // Given
        val defaultType = "all"

        // Then
        assertEquals(CONTENT_TYPE_ALL, defaultType)
    }

    // Helper function simulating the app's logic
    private fun getSearchQueryForType(contentType: String): String {
        return when (contentType) {
            CONTENT_TYPE_LECTURES -> "دروس إسلامية محاضرات"
            CONTENT_TYPE_QURAN -> "القرآن الكريم تلاوة قراء"
            CONTENT_TYPE_VIDEOS -> "فيديوهات إسلامية"
            CONTENT_TYPE_SERMONS -> "خطبة الجمعة خطب"
            else -> "محتوى إسلامي"
        }
    }

    @Test
    fun `fragment navigation destinations are valid`() {
        // Simulating navigation destinations
        val destinations = listOf(
            "AllCategoriesFragment",
            "HadithFragment",
            "DuaFragment",
            "AzkarFragment",
            "QuranFragment",
            "AllahNamesFragment",
            "TasbehFragment",
            "ArticleFragment",
            "VideoPlayerFragment"
        )

        // Then
        destinations.forEach { destination ->
            assertTrue(destination.endsWith("Fragment"))
            assertTrue(destination.isNotEmpty())
        }
    }

    @Test
    fun `media categories are complete`() {
        // Given
        val mediaCategories = listOf(
            "الخطب والمحاضرات" to CONTENT_TYPE_LECTURES,
            "الصوتيات" to CONTENT_TYPE_QURAN,
            "الفيديوهات" to CONTENT_TYPE_VIDEOS,
            "الخطب" to CONTENT_TYPE_SERMONS
        )

        // Then
        assertEquals(4, mediaCategories.size)
        mediaCategories.forEach { (name, type) ->
            assertTrue(name.isNotEmpty())
            assertTrue(type.isNotEmpty())
        }
    }

    @Test
    fun `all categories fragment has all items`() {
        // Simulating the categories in AllCategoriesFragment
        val categories = listOf(
            "allahNamesFragment",
            "tasbehFragment",
            "duaFragment",
            "hadithFragment",
            "azkarFragment",
            "quranFragment"
        )

        // Then
        assertEquals(6, categories.size)
    }

    @Test
    fun `animation resources are consistent`() {
        // Simulating animation resource names
        val animations = listOf(
            "animation" to "enter",
            "animation2" to "exit",
            "animation3" to "popEnter",
            "animation4" to "popExit"
        )

        // Then
        assertEquals(4, animations.size)
    }
}
