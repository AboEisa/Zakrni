package com.zakrni.app.clean.data.network

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * YouTube Data API v3 Service
 * Used to fetch Islamic videos from YouTube
 */
interface YouTubeApiService {

    @GET("search")
    suspend fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 20,
        @Query("order") order: String = "relevance",
        @Query("regionCode") regionCode: String = "EG",
        @Query("relevanceLanguage") language: String = "ar",
        @Query("key") apiKey: String
    ): YouTubeSearchResponse

    @GET("search")
    suspend fun getChannelVideos(
        @Query("part") part: String = "snippet",
        @Query("channelId") channelId: String,
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 10,
        @Query("order") order: String = "date",
        @Query("key") apiKey: String
    ): YouTubeSearchResponse

    @GET("videos")
    suspend fun getVideoDetails(
        @Query("part") part: String = "snippet,contentDetails,statistics",
        @Query("id") videoIds: String,
        @Query("key") apiKey: String
    ): YouTubeVideoDetailsResponse
}

/**
 * YouTube Search Response
 */
data class YouTubeSearchResponse(
    val items: List<YouTubeSearchItem>? = null,
    val nextPageToken: String? = null,
    val pageInfo: PageInfo? = null
)

data class YouTubeSearchItem(
    val id: VideoId? = null,
    val snippet: Snippet? = null
)

data class VideoId(
    val videoId: String? = null
)

data class Snippet(
    val title: String? = null,
    val description: String? = null,
    val channelTitle: String? = null,
    val publishedAt: String? = null,
    val thumbnails: Thumbnails? = null
)

data class Thumbnails(
    val default: ThumbnailInfo? = null,
    val medium: ThumbnailInfo? = null,
    val high: ThumbnailInfo? = null,
    val maxres: ThumbnailInfo? = null
)

data class ThumbnailInfo(
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null
)

data class PageInfo(
    val totalResults: Int? = null,
    val resultsPerPage: Int? = null
)

/**
 * YouTube Video Details Response
 */
data class YouTubeVideoDetailsResponse(
    val items: List<YouTubeVideoItem>? = null
)

data class YouTubeVideoItem(
    val id: String? = null,
    val snippet: Snippet? = null,
    val contentDetails: ContentDetails? = null,
    val statistics: Statistics? = null
)

data class ContentDetails(
    val duration: String? = null
)

data class Statistics(
    val viewCount: String? = null,
    val likeCount: String? = null
)
