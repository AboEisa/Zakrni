package com.zakrni.app.clean.domain.usecase

import com.zakrni.app.BuildConfig
import com.zakrni.app.clean.data.local.CacheManager
import com.zakrni.app.clean.data.models.IslamicVideos
import com.zakrni.app.clean.data.models.YouTubeVideo
import com.zakrni.app.clean.data.network.YouTubeApiService
import javax.inject.Inject

class GetIslamicVideosUseCase @Inject constructor(
    private val youTubeApiService: YouTubeApiService,
    private val cacheManager: CacheManager
) {
    
    companion object {
        private const val CACHE_KEY_VIDEOS = "youtube_islamic_videos"
        
        // Arabic search queries to fetch diverse content
        private val ARABIC_SEARCH_QUERIES = listOf(
            "محمد راتب النابلسي",
            "عمر عبد الكافي",
            "عمرو خالد",
            "مصطفى حسني",
            "القرآن الكريم تلاوة",
            "دروس إسلامية",
            "خطب الجمعة"
        )

        // English search queries to fetch diverse content
        private val ENGLISH_SEARCH_QUERIES = listOf(
            "Islamic lecture",
            "Quran recitation",
            "Friday sermon",
            "Islamic reminder",
            "Prophetic hadith explanation",
            "Islamic lessons",
            "Dua and adhkar"
        )
    }
    
    private val apiKey: String
        get() = BuildConfig.YOUTUBE_API_KEY
    
    /**
     * Get Islamic videos from YouTube API (with caching)
     */
    suspend operator fun invoke(isArabic: Boolean = true): Result<List<YouTubeVideo>> {
        val languageTag = if (isArabic) "ar" else "en"
        val cacheKey = "${CACHE_KEY_VIDEOS}_$languageTag"

        // Try to get from cache first
        val cachedVideos = cacheManager.getListFromCache<YouTubeVideo>(cacheKey)
        if (cachedVideos != null && cachedVideos.isNotEmpty()) {
            android.util.Log.d("GetIslamicVideosUseCase", "Returning ${cachedVideos.size} cached videos")
            return Result.success(cachedVideos.shuffled())
        }
        
        return try {
            val allVideos = mutableListOf<YouTubeVideo>()
            val queries = if (isArabic) ARABIC_SEARCH_QUERIES else ENGLISH_SEARCH_QUERIES
            
            // Fetch videos from multiple search queries for diverse content
            for (query in queries.take(3)) { // Limit to 3 to save API quota
                try {
                    val response = youTubeApiService.searchVideos(
                        query = query,
                        maxResults = 10,
                        language = languageTag,
                        apiKey = apiKey
                    )
                    
                    response.items?.forEach { item ->
                        val videoId = item.id?.videoId ?: return@forEach
                        val snippet = item.snippet ?: return@forEach
                        
                        allVideos.add(
                            YouTubeVideo(
                                videoId = videoId,
                                title = snippet.title ?: "",
                                description = snippet.description ?: "",
                                thumbnailUrl = snippet.thumbnails?.high?.url 
                                    ?: snippet.thumbnails?.medium?.url
                                    ?: snippet.thumbnails?.default?.url
                                    ?: "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
                                channelName = snippet.channelTitle ?: "",
                                publishedAt = snippet.publishedAt ?: "",
                                viewCount = "",
                                duration = ""
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Continue with next query if one fails
                    android.util.Log.e("GetIslamicVideosUseCase", "Error fetching query: $query", e)
                }
            }
            
            if (allVideos.isEmpty()) {
                // Fallback to hardcoded videos if API fails
                Result.success(IslamicVideos.getAllVideos(isArabic))
            } else {
                // Remove duplicates, cache, and shuffle
                val uniqueVideos = allVideos.distinctBy { it.videoId }
                cacheManager.saveToCache(cacheKey, uniqueVideos)
                android.util.Log.d("GetIslamicVideosUseCase", "Cached ${uniqueVideos.size} videos from YouTube API")
                Result.success(uniqueVideos.shuffled())
            }
        } catch (e: Exception) {
            android.util.Log.e("GetIslamicVideosUseCase", "Error fetching videos", e)
            // Fallback to hardcoded videos
            Result.success(IslamicVideos.getAllVideos(isArabic))
        }
    }

    /**
     * Search for Islamic videos by query
     */
    suspend fun search(query: String, isArabic: Boolean = true): Result<List<YouTubeVideo>> {
        val languageTag = if (isArabic) "ar" else "en"
        val scopedQuery = if (isArabic) "$query إسلامي" else "$query islamic"

        return try {
            val response = youTubeApiService.searchVideos(
                query = scopedQuery,
                maxResults = 20,
                language = languageTag,
                apiKey = apiKey
            )
            
            val videos = response.items?.mapNotNull { item ->
                val videoId = item.id?.videoId ?: return@mapNotNull null
                val snippet = item.snippet ?: return@mapNotNull null
                
                YouTubeVideo(
                    videoId = videoId,
                    title = snippet.title ?: "",
                    description = snippet.description ?: "",
                    thumbnailUrl = snippet.thumbnails?.high?.url 
                        ?: snippet.thumbnails?.medium?.url
                        ?: snippet.thumbnails?.default?.url
                        ?: "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
                    channelName = snippet.channelTitle ?: "",
                    publishedAt = snippet.publishedAt ?: "",
                    viewCount = "",
                    duration = ""
                )
            } ?: emptyList()
            
            if (videos.isEmpty()) {
                // Fallback to local search
                val filteredVideos = IslamicVideos.getAllVideos(isArabic).filter { video ->
                    video.title.contains(query, ignoreCase = true) ||
                    video.channelName.contains(query, ignoreCase = true) ||
                    video.description.contains(query, ignoreCase = true)
                }
                Result.success(filteredVideos)
            } else {
                Result.success(videos)
            }
        } catch (e: Exception) {
            android.util.Log.e("GetIslamicVideosUseCase", "Error searching videos", e)
            // Fallback to local search
            val filteredVideos = IslamicVideos.getAllVideos(isArabic).filter { video ->
                video.title.contains(query, ignoreCase = true) ||
                video.channelName.contains(query, ignoreCase = true) ||
                video.description.contains(query, ignoreCase = true)
            }
            Result.success(filteredVideos)
        }
    }

    /**
     * Get videos from a specific channel by searching their name
     */
    suspend fun getChannelVideos(channelName: String, isArabic: Boolean = true): Result<List<YouTubeVideo>> {
        val languageTag = if (isArabic) "ar" else "en"
        return try {
            val response = youTubeApiService.searchVideos(
                query = channelName,
                maxResults = 20,
                language = languageTag,
                apiKey = apiKey
            )
            
            val videos = response.items?.mapNotNull { item ->
                val videoId = item.id?.videoId ?: return@mapNotNull null
                val snippet = item.snippet ?: return@mapNotNull null
                
                YouTubeVideo(
                    videoId = videoId,
                    title = snippet.title ?: "",
                    description = snippet.description ?: "",
                    thumbnailUrl = snippet.thumbnails?.high?.url 
                        ?: snippet.thumbnails?.medium?.url
                        ?: snippet.thumbnails?.default?.url
                        ?: "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
                    channelName = snippet.channelTitle ?: "",
                    publishedAt = snippet.publishedAt ?: "",
                    viewCount = "",
                    duration = ""
                )
            } ?: emptyList()
            
            if (videos.isEmpty()) {
                // Fallback to hardcoded
                val channelVideos = IslamicVideos.getAllVideos(isArabic).filter { video ->
                    video.channelName.contains(channelName, ignoreCase = true)
                }
                Result.success(channelVideos)
            } else {
                Result.success(videos)
            }
        } catch (e: Exception) {
            android.util.Log.e("GetIslamicVideosUseCase", "Error getting channel videos", e)
            // Fallback
            val channelVideos = IslamicVideos.getAllVideos(isArabic).filter { video ->
                video.channelName.contains(channelName, ignoreCase = true)
            }
            Result.success(channelVideos)
        }
    }
    
    /**
     * Get videos by content type
     * Types: lectures, quran, videos, sermons, all
     */
    suspend fun getVideosByType(contentType: String, isArabic: Boolean = true): Result<List<YouTubeVideo>> {
        val languageTag = if (isArabic) "ar" else "en"
        val searchQuery = when (contentType) {
            "lectures" -> if (isArabic) "دروس إسلامية محاضرات" else "Islamic lectures"
            "quran" -> if (isArabic) "القرآن الكريم تلاوة قراء" else "Quran recitation"
            "videos" -> if (isArabic) "فيديوهات إسلامية" else "Islamic videos"
            "sermons" -> if (isArabic) "خطبة الجمعة خطب" else "Friday sermon khutbah"
            else -> if (isArabic) "محتوى إسلامي" else "Islamic content"
        }
        
        val cacheKey = "youtube_videos_${contentType}_$languageTag"
        
        // Try to get from cache first
        val cachedVideos = cacheManager.getListFromCache<YouTubeVideo>(cacheKey)
        if (cachedVideos != null && cachedVideos.isNotEmpty()) {
            android.util.Log.d("GetIslamicVideosUseCase", "Returning ${cachedVideos.size} cached videos for $contentType")
            return Result.success(cachedVideos.shuffled())
        }
        
        return try {
            val response = youTubeApiService.searchVideos(
                query = searchQuery,
                maxResults = 25,
                language = languageTag,
                apiKey = apiKey
            )
            
            val videos = response.items?.mapNotNull { item ->
                val videoId = item.id?.videoId ?: return@mapNotNull null
                val snippet = item.snippet ?: return@mapNotNull null
                
                YouTubeVideo(
                    videoId = videoId,
                    title = snippet.title ?: "",
                    description = snippet.description ?: "",
                    thumbnailUrl = snippet.thumbnails?.high?.url 
                        ?: snippet.thumbnails?.medium?.url
                        ?: snippet.thumbnails?.default?.url
                        ?: "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
                    channelName = snippet.channelTitle ?: "",
                    publishedAt = snippet.publishedAt ?: "",
                    viewCount = "",
                    duration = ""
                )
            } ?: emptyList()
            
            if (videos.isNotEmpty()) {
                cacheManager.saveToCache(cacheKey, videos)
                android.util.Log.d("GetIslamicVideosUseCase", "Cached ${videos.size} videos for $contentType")
            }
            
            if (videos.isEmpty()) {
                // Fallback to type-specific hardcoded videos
                Result.success(IslamicVideos.getVideosByType(contentType, isArabic))
            } else {
                Result.success(videos.shuffled())
            }
        } catch (e: Exception) {
            android.util.Log.e("GetIslamicVideosUseCase", "Error fetching videos by type: $contentType", e)
            Result.success(IslamicVideos.getVideosByType(contentType, isArabic))
        }
    }
    
    /**
     * Get list of available channels
     */
    fun getChannels() = IslamicVideos.channels
}
