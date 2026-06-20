package com.zakrni.app.clean.ui.compose.media

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.zakrni.app.R
import com.zakrni.app.clean.data.models.IslamicVideos
import com.zakrni.app.clean.data.models.YouTubeVideo
import com.zakrni.app.clean.ui.theme.BrandGold
import com.zakrni.app.clean.ui.theme.components.EmptyState
import com.zakrni.app.clean.ui.theme.components.ZButton
import com.zakrni.app.clean.ui.theme.components.ZButtonStyle
import com.zakrni.app.clean.ui.theme.components.ZCard
import com.zakrni.app.clean.ui.theme.components.ZTopBar
import androidx.compose.material.icons.outlined.VideoLibrary

/**
 * Video detail: thumbnail + title/channel/views/date/description + a Play button
 * that opens the video in the YouTube app (falling back to the browser).
 * Replicates the legacy VideoPlayerFragment.
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
                appIntent.setPackage("com.google.android.youtube")
                context.startActivity(appIntent)
            } catch (e: Exception) {
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/watch?v=$videoId"),
                )
                context.startActivity(webIntent)
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        ZTopBar(title = stringResource(R.string.media_hub_title), onBack = onBack)

        if (video == null) {
            EmptyState(
                icon = Icons.Outlined.VideoLibrary,
                title = stringResource(R.string.media_empty_title),
            )
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            VideoThumbnail(video = video, onPlay = openInYouTube)

            Text(
                text = video.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )

            if (video.channelName.isNotBlank()) {
                Text(
                    text = video.channelName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

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

            ZButton(
                text = stringResource(R.string.media_play),
                onClick = openInYouTube,
                style = ZButtonStyle.Gold,
                leadingIcon = Icons.Filled.PlayArrow,
                modifier = Modifier.fillMaxWidth(),
            )

            if (video.description.isNotBlank()) {
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

@Composable
private fun VideoThumbnail(video: YouTubeVideo, onPlay: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://img.youtube.com/vi/${video.videoId}/maxresdefault.jpg")
                .crossfade(true)
                .build(),
            contentDescription = video.title,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.placeholder_video),
            error = painterResource(R.drawable.placeholder_video),
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(64.dp)
                .background(BrandGold.copy(alpha = 0.9f), CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = stringResource(R.string.media_play),
                tint = Color.White,
                modifier = Modifier.size(40.dp),
            )
        }
        // Tap thumbnail to play.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickableNoRipple(onPlay),
        )
    }
}

/** Full-surface clickable overlay without ripple, so the thumbnail acts as a play button. */
@Composable
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    return this.clickable(
        interactionSource = interaction,
        indication = null,
        onClick = onClick,
    )
}
