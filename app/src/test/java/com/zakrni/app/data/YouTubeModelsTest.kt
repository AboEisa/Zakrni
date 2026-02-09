package com.zakrni.app.data

import com.zakrni.app.clean.data.models.IslamicVideos
import com.zakrni.app.clean.data.models.YouTubeChannel
import com.zakrni.app.clean.data.models.YouTubeVideo
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for YouTube Models
 */
class YouTubeModelsTest {

    @Test
    fun `YouTubeVideo has all required properties`() {
        // Given
        val video = YouTubeVideo(
            videoId = "abc123",
            title = "عنوان الفيديو",
            description = "وصف الفيديو",
            thumbnailUrl = "https://example.com/thumb.jpg",
            channelName = "اسم القناة",
            publishedAt = "2024-01-01",
            viewCount = "1000",
            duration = "10:30"
        )

        // Then
        assertEquals("abc123", video.videoId)
        assertEquals("عنوان الفيديو", video.title)
        assertEquals("وصف الفيديو", video.description)
        assertEquals("https://example.com/thumb.jpg", video.thumbnailUrl)
        assertEquals("اسم القناة", video.channelName)
        assertEquals("2024-01-01", video.publishedAt)
        assertEquals("1000", video.viewCount)
        assertEquals("10:30", video.duration)
    }

    @Test
    fun `YouTubeChannel has all required properties`() {
        // Given
        val channel = YouTubeChannel(
            id = "channel123",
            name = "Channel Name",
            nameAr = "اسم القناة"
        )

        // Then
        assertEquals("channel123", channel.id)
        assertEquals("Channel Name", channel.name)
        assertEquals("اسم القناة", channel.nameAr)
    }

    @Test
    fun `IslamicVideos contains hardcoded videos`() {
        // When
        val videos = IslamicVideos.videos

        // Then
        assertNotNull(videos)
        assertTrue(videos.isNotEmpty())
    }

    @Test
    fun `IslamicVideos contains hardcoded channels`() {
        // When
        val channels = IslamicVideos.channels

        // Then
        assertNotNull(channels)
        assertTrue(channels.isNotEmpty())
    }

    @Test
    fun `IslamicVideos videos have valid video IDs`() {
        // When
        val videos = IslamicVideos.videos

        // Then
        videos.forEach { video ->
            assertTrue("Video ID should not be empty", video.videoId.isNotEmpty())
        }
    }

    @Test
    fun `IslamicVideos videos have valid thumbnail URLs`() {
        // When
        val videos = IslamicVideos.videos

        // Then
        videos.forEach { video ->
            assertTrue("Thumbnail URL should start with https", 
                video.thumbnailUrl.startsWith("https://"))
        }
    }

    @Test
    fun `IslamicVideos channels have Arabic names`() {
        // When
        val channels = IslamicVideos.channels

        // Then
        channels.forEach { channel ->
            assertTrue("Channel should have Arabic name", channel.nameAr.isNotEmpty())
        }
    }

    @Test
    fun `YouTubeVideo equality works correctly`() {
        // Given
        val video1 = YouTubeVideo(
            videoId = "abc123",
            title = "Title",
            description = "Desc",
            thumbnailUrl = "url",
            channelName = "Channel",
            publishedAt = "date",
            viewCount = "100",
            duration = "5:00"
        )
        val video2 = YouTubeVideo(
            videoId = "abc123",
            title = "Title",
            description = "Desc",
            thumbnailUrl = "url",
            channelName = "Channel",
            publishedAt = "date",
            viewCount = "100",
            duration = "5:00"
        )

        // Then
        assertEquals(video1, video2)
    }

    @Test
    fun `YouTubeVideo with different IDs are not equal`() {
        // Given
        val video1 = YouTubeVideo(
            videoId = "abc123",
            title = "Title",
            description = "Desc",
            thumbnailUrl = "url",
            channelName = "Channel",
            publishedAt = "date",
            viewCount = "100",
            duration = "5:00"
        )
        val video2 = YouTubeVideo(
            videoId = "xyz789",
            title = "Title",
            description = "Desc",
            thumbnailUrl = "url",
            channelName = "Channel",
            publishedAt = "date",
            viewCount = "100",
            duration = "5:00"
        )

        // Then
        assertNotEquals(video1, video2)
    }
}
