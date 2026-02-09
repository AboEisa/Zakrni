package com.zakrni.app.data

import com.zakrni.app.clean.data.network.YouTubeSearchResponse
import com.zakrni.app.clean.data.network.YouTubeSearchItem
import com.zakrni.app.clean.data.network.VideoId
import com.zakrni.app.clean.data.network.Snippet
import com.zakrni.app.clean.data.network.Thumbnails
import com.zakrni.app.clean.data.network.ThumbnailInfo
import com.zakrni.app.clean.data.network.PageInfo
import com.zakrni.app.clean.data.network.YouTubeVideoDetailsResponse
import com.zakrni.app.clean.data.network.YouTubeVideoItem
import com.zakrni.app.clean.data.network.ContentDetails
import com.zakrni.app.clean.data.network.Statistics
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for YouTube API Response Models
 */
class YouTubeApiResponseTest {

    @Test
    fun `YouTubeSearchResponse parses items correctly`() {
        // Given
        val items = listOf(
            YouTubeSearchItem(
                id = VideoId(videoId = "abc123"),
                snippet = Snippet(
                    title = "Test Title",
                    description = "Test Description",
                    channelTitle = "Test Channel",
                    publishedAt = "2024-01-01T00:00:00Z",
                    thumbnails = Thumbnails(
                        default = ThumbnailInfo(url = "https://example.com/default.jpg"),
                        medium = ThumbnailInfo(url = "https://example.com/medium.jpg"),
                        high = ThumbnailInfo(url = "https://example.com/high.jpg")
                    )
                )
            )
        )
        val response = YouTubeSearchResponse(
            items = items,
            nextPageToken = "token123",
            pageInfo = PageInfo(totalResults = 100, resultsPerPage = 20)
        )

        // Then
        assertNotNull(response.items)
        assertEquals(1, response.items!!.size)
        assertEquals("token123", response.nextPageToken)
        assertEquals(100, response.pageInfo?.totalResults)
    }

    @Test
    fun `YouTubeSearchItem extracts video ID correctly`() {
        // Given
        val item = YouTubeSearchItem(
            id = VideoId(videoId = "xyz789"),
            snippet = null
        )

        // Then
        assertEquals("xyz789", item.id?.videoId)
    }

    @Test
    fun `Snippet contains all metadata`() {
        // Given
        val snippet = Snippet(
            title = "فيديو إسلامي",
            description = "وصف الفيديو",
            channelTitle = "قناة إسلامية",
            publishedAt = "2024-01-01T12:00:00Z",
            thumbnails = Thumbnails(
                high = ThumbnailInfo(url = "https://img.youtube.com/vi/abc/hqdefault.jpg", width = 480, height = 360)
            )
        )

        // Then
        assertEquals("فيديو إسلامي", snippet.title)
        assertEquals("وصف الفيديو", snippet.description)
        assertEquals("قناة إسلامية", snippet.channelTitle)
        assertEquals("2024-01-01T12:00:00Z", snippet.publishedAt)
        assertNotNull(snippet.thumbnails?.high)
    }

    @Test
    fun `Thumbnails provides fallback options`() {
        // Given
        val thumbnailsWithHigh = Thumbnails(
            high = ThumbnailInfo(url = "https://example.com/high.jpg")
        )
        val thumbnailsWithMedium = Thumbnails(
            medium = ThumbnailInfo(url = "https://example.com/medium.jpg")
        )
        val thumbnailsWithDefault = Thumbnails(
            default = ThumbnailInfo(url = "https://example.com/default.jpg")
        )

        // Then - simulate fallback logic
        val url1 = thumbnailsWithHigh.high?.url 
            ?: thumbnailsWithHigh.medium?.url 
            ?: thumbnailsWithHigh.default?.url
        assertEquals("https://example.com/high.jpg", url1)

        val url2 = thumbnailsWithMedium.high?.url 
            ?: thumbnailsWithMedium.medium?.url 
            ?: thumbnailsWithMedium.default?.url
        assertEquals("https://example.com/medium.jpg", url2)

        val url3 = thumbnailsWithDefault.high?.url 
            ?: thumbnailsWithDefault.medium?.url 
            ?: thumbnailsWithDefault.default?.url
        assertEquals("https://example.com/default.jpg", url3)
    }

    @Test
    fun `YouTubeVideoDetailsResponse contains video details`() {
        // Given
        val response = YouTubeVideoDetailsResponse(
            items = listOf(
                YouTubeVideoItem(
                    id = "video123",
                    snippet = Snippet(title = "Video Title"),
                    contentDetails = ContentDetails(duration = "PT10M30S"),
                    statistics = Statistics(viewCount = "1000000", likeCount = "50000")
                )
            )
        )

        // Then
        assertNotNull(response.items)
        assertEquals(1, response.items!!.size)
        assertEquals("video123", response.items!![0].id)
        assertEquals("PT10M30S", response.items!![0].contentDetails?.duration)
        assertEquals("1000000", response.items!![0].statistics?.viewCount)
    }

    @Test
    fun `ContentDetails parses duration correctly`() {
        // Given
        val contentDetails = ContentDetails(duration = "PT1H30M45S")

        // Then
        assertEquals("PT1H30M45S", contentDetails.duration)
    }

    @Test
    fun `Statistics contains view and like counts`() {
        // Given
        val statistics = Statistics(
            viewCount = "5000000",
            likeCount = "100000"
        )

        // Then
        assertEquals("5000000", statistics.viewCount)
        assertEquals("100000", statistics.likeCount)
    }

    @Test
    fun `PageInfo contains pagination data`() {
        // Given
        val pageInfo = PageInfo(
            totalResults = 500,
            resultsPerPage = 25
        )

        // Then
        assertEquals(500, pageInfo.totalResults)
        assertEquals(25, pageInfo.resultsPerPage)
    }

    @Test
    fun `ThumbnailInfo contains dimensions`() {
        // Given
        val thumbnailInfo = ThumbnailInfo(
            url = "https://example.com/thumb.jpg",
            width = 1280,
            height = 720
        )

        // Then
        assertEquals("https://example.com/thumb.jpg", thumbnailInfo.url)
        assertEquals(1280, thumbnailInfo.width)
        assertEquals(720, thumbnailInfo.height)
    }

    @Test
    fun `YouTubeSearchResponse handles null items`() {
        // Given
        val response = YouTubeSearchResponse(
            items = null,
            nextPageToken = null,
            pageInfo = null
        )

        // Then
        assertNull(response.items)
        assertNull(response.nextPageToken)
        assertNull(response.pageInfo)
    }

    @Test
    fun `YouTubeSearchResponse handles empty items`() {
        // Given
        val response = YouTubeSearchResponse(
            items = emptyList(),
            nextPageToken = null,
            pageInfo = PageInfo(totalResults = 0, resultsPerPage = 0)
        )

        // Then
        assertNotNull(response.items)
        assertTrue(response.items!!.isEmpty())
        assertEquals(0, response.pageInfo?.totalResults)
    }
}
