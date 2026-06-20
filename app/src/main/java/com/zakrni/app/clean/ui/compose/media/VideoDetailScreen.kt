package com.zakrni.app.clean.ui.compose.media

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.zakrni.app.R
import com.zakrni.app.clean.data.models.IslamicVideos
import com.zakrni.app.clean.ui.theme.components.ZButton
import com.zakrni.app.clean.ui.theme.components.ZButtonStyle
import com.zakrni.app.clean.ui.theme.components.ZCard
import com.zakrni.app.clean.ui.theme.components.ZTopBar

/**
 * Video detail with an in-app YouTube player (embedded via WebView iframe) so playback
 * happens inside the app. Plays purely from [videoId] — metadata (title/desc) is shown
 * when we can resolve it locally, but the player always works regardless.
 */
@Composable
fun VideoDetailScreen(
    videoId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val video = remember(videoId) {
        IslamicVideos.getAllVideos(isArabic = true).firstOrNull { it.videoId == videoId }
    }

    val openInYouTube: () -> Unit = remember(videoId) {
        {
            try {
                val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId"))
                    .setPackage("com.google.android.youtube")
                context.startActivity(appIntent)
            } catch (e: Exception) {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId")),
                )
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        ZTopBar(
            title = video?.title?.takeIf { it.isNotBlank() } ?: stringResource(R.string.media_hub_title),
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // The player always works — it only needs the videoId.
            YouTubePlayer(
                videoId = videoId,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black),
            )

            if (video != null && video.title.isNotBlank()) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            if (video?.channelName?.isNotBlank() == true) {
                Text(
                    text = video.channelName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (video != null && (video.viewCount.isNotBlank() || video.publishedAt.isNotBlank())) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (video.viewCount.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.media_views, video.viewCount),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (video.publishedAt.isNotBlank()) {
                        Text(
                            text = video.publishedAt,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            ZButton(
                text = stringResource(R.string.media_play),
                onClick = openInYouTube,
                style = ZButtonStyle.Outline,
                leadingIcon = Icons.AutoMirrored.Filled.OpenInNew,
                modifier = Modifier.fillMaxWidth(),
            )

            if (video?.description?.isNotBlank() == true) {
                ZCard(shape = RoundedCornerShape(16.dp)) {
                    Text(
                        text = stringResource(R.string.media_description_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = video.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

/** In-app YouTube playback via a WebView iframe embed. */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun YouTubePlayer(videoId: String, modifier: Modifier = Modifier) {
    val html = remember(videoId) {
        """
        <!DOCTYPE html><html><head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>html,body{margin:0;padding:0;height:100%;background:#000;}
        .wrap{position:relative;width:100%;height:100%;}
        iframe{position:absolute;top:0;left:0;width:100%;height:100%;border:0;}</style>
        </head><body><div class="wrap">
        <iframe src="https://www.youtube.com/embed/$videoId?playsinline=1&rel=0&modestbranding=1"
            allow="autoplay; encrypted-media; picture-in-picture; fullscreen" allowfullscreen></iframe>
        </div></body></html>
        """.trimIndent()
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                setBackgroundColor(android.graphics.Color.BLACK)
                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
                with(settings) {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    loadWithOverviewMode = true
                    useWideViewPort = true
                }
                loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "utf-8", null)
            }
        },
        onRelease = { it.destroy() },
    )
}
