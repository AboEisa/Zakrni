package com.zakrni.app.clean.ui.compose.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zakrni.app.R
import com.zakrni.app.clean.ui.theme.components.ErrorState
import com.zakrni.app.clean.ui.theme.components.ShimmerBox
import com.zakrni.app.clean.ui.theme.components.EmptyState
import com.zakrni.app.clean.ui.theme.components.ZTopBar
import com.zakrni.app.clean.ui.viewmodels.ArticlesViewModel
import kotlinx.coroutines.delay

/**
 * Video list for a single [MediaContentType]. Backed by [ArticlesViewModel];
 * features a debounced search field, shimmer loading, and error/empty states.
 */
@Composable
fun ArticleListScreen(
    contentType: String,
    onBack: () -> Unit,
    onOpenVideo: (videoId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArticlesViewModel = hiltViewModel(),
) {
    val category = remember(contentType) { MediaContentType.fromContentType(contentType) }

    val videos by viewModel.videos.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var query by remember { mutableStateOf("") }

    // Initial load for this content type.
    LaunchedEffect(contentType) {
        viewModel.loadVideosByType(contentType)
    }

    // Debounced search (mirrors the legacy 500ms / 3-char behaviour).
    LaunchedEffect(query) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            // loadVideosByType already ran for empty initial state; reload list on clear.
            return@LaunchedEffect
        }
        if (trimmed.length >= 3) {
            delay(500)
            viewModel.searchVideos(trimmed)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        ZTopBar(title = stringResource(category.listTitle), onBack = onBack)

        MediaSearchField(
            query = query,
            hint = stringResource(category.searchHint),
            onQueryChange = { query = it },
            onClear = {
                query = ""
                viewModel.loadVideosByType(contentType)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> MediaListShimmer()

                error != null && videos.isEmpty() -> ErrorState(
                    message = error ?: stringResource(R.string.media_error_title),
                    onRetry = {
                        if (query.trim().length >= 3) {
                            viewModel.searchVideos(query.trim())
                        } else {
                            viewModel.loadVideosByType(contentType)
                        }
                    },
                    retryLabel = stringResource(R.string.media_retry),
                )

                videos.isEmpty() -> EmptyState(
                    icon = Icons.Outlined.VideoLibrary,
                    title = if (query.isNotBlank()) {
                        stringResource(R.string.media_empty_search)
                    } else {
                        stringResource(R.string.media_empty_title)
                    },
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(videos, key = { it.videoId }) { video ->
                        MediaVideoCard(
                            video = video,
                            onClick = { onOpenVideo(video.videoId) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaSearchField(
    query: String,
    hint: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text(hint, maxLines = 1) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.media_search))
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.media_back))
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
    )
}

@Composable
private fun MediaListShimmer() {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = false,
    ) {
        items(6) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    shape = RoundedCornerShape(16.dp),
                )
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(16.dp),
                )
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(12.dp),
                )
            }
        }
    }
}
