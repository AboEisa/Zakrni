package com.zakrni.app.clean.ui.compose.media

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.ui.graphics.vector.ImageVector
import com.zakrni.app.R

/**
 * The four media categories shown on the hub. [contentType] is the value the
 * [com.zakrni.app.clean.ui.viewmodels.ArticlesViewModel] expects
 * ("lectures", "quran", "videos", "sermons").
 */
enum class MediaContentType(
    val contentType: String,
    @StringRes val hubTitle: Int,
    @StringRes val listTitle: Int,
    @StringRes val searchHint: Int,
    val icon: ImageVector,
) {
    LECTURES(
        contentType = "lectures",
        hubTitle = R.string.media_cat_lectures,
        listTitle = R.string.media_list_lectures,
        searchHint = R.string.media_search_lectures,
        icon = Icons.AutoMirrored.Filled.MenuBook,
    ),
    QURAN(
        contentType = "quran",
        hubTitle = R.string.media_cat_quran,
        listTitle = R.string.media_list_quran,
        searchHint = R.string.media_search_quran,
        icon = Icons.Filled.Mic,
    ),
    VIDEOS(
        contentType = "videos",
        hubTitle = R.string.media_cat_videos,
        listTitle = R.string.media_list_videos,
        searchHint = R.string.media_search_videos,
        icon = Icons.Filled.PlayCircle,
    ),
    SERMONS(
        contentType = "sermons",
        hubTitle = R.string.media_cat_sermons,
        listTitle = R.string.media_list_sermons,
        searchHint = R.string.media_search_sermons,
        icon = Icons.Filled.RecordVoiceOver,
    );

    companion object {
        /** Resolve a content type string back into the enum, defaulting to [VIDEOS]. */
        fun fromContentType(value: String?): MediaContentType =
            entries.firstOrNull { it.contentType == value } ?: VIDEOS
    }
}

/** Route definitions for the Media feature graph. */
object MediaRoutes {
    const val HUB = "media"

    private const val LIST_BASE = "media_list"
    const val LIST_ARG = "contentType"
    const val LIST = "$LIST_BASE/{$LIST_ARG}"
    fun list(contentType: String): String = "$LIST_BASE/$contentType"

    private const val VIDEO_BASE = "media_video"
    const val VIDEO_ARG = "videoId"
    const val VIDEO = "$VIDEO_BASE/{$VIDEO_ARG}"
    fun video(videoId: String): String = "$VIDEO_BASE/$videoId"
}
