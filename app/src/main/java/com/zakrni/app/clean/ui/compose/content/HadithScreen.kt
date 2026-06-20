package com.zakrni.app.clean.ui.compose.content

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zakrni.app.R
import com.zakrni.app.clean.ui.models.PresentationHadith
import com.zakrni.app.clean.ui.theme.BrandGold
import com.zakrni.app.clean.ui.theme.components.ErrorState
import com.zakrni.app.clean.ui.theme.components.ShimmerBox
import com.zakrni.app.clean.ui.theme.components.ZCard
import com.zakrni.app.clean.ui.theme.components.ZTopBar
import com.zakrni.app.clean.ui.viewmodels.HadithViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HadithViewModel = hiltViewModel(),
) {
    val hadiths by viewModel.hadithsList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    // Pagination: load the next page when the user nears the end of the list.
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val total = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastVisible >= total - 3
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadNextPage()
    }

    Column(modifier = modifier.fillMaxSize()) {
        ZTopBar(
            title = stringResource(R.string.content_hadith_title),
            onBack = onBack,
        )

        val pullState = rememberPullToRefreshState()
        // Only the first-page load drives the swipe-refresh spinner.
        PullToRefreshBox(
            isRefreshing = isLoading && hadiths.isNotEmpty(),
            onRefresh = { viewModel.refresh() },
            state = pullState,
            modifier = Modifier.fillMaxSize(),
        ) {
            when {
                error != null && hadiths.isEmpty() -> ErrorState(
                    message = error ?: stringResource(R.string.content_hadith_error),
                    onRetry = { viewModel.refresh() },
                    retryLabel = stringResource(R.string.content_retry),
                )

                isLoading && hadiths.isEmpty() -> HadithShimmerList()

                else -> LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    itemsIndexed(hadiths, key = { _, item -> item.id }) { index, item ->
                        HadithCard(hadith = item, index = index)
                    }
                    if (isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HadithCard(hadith: PresentationHadith, index: Int) {
    // Tasteful one-shot entrance animation per item.
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(hadith.id) {
        delay((index % 10) * 45L)
        visible = true
    }
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(350), label = "hadith-alpha")
    val translate by animateFloatAsState(if (visible) 0f else 32f, tween(350), label = "hadith-translate")

    ZCard(
        contentPadding = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .graphicsLayer { translationY = translate },
    ) {
        // Header: book name + hadith number badge.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = hadith.book?.bookName.orEmpty(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f, fill = false),
            )
            val number = hadith.hadithNumber
            if (!number.isNullOrBlank()) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(50),
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
        }

        // Body.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            val narrator = hadith.englishNarrator ?: hadith.urduNarrator
            if (!narrator.isNullOrBlank()) {
                Text(
                    text = narrator,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            BrandDivider(Modifier.padding(top = if (!narrator.isNullOrBlank()) 16.dp else 0.dp))

            Text(
                text = hadith.hadithArabic
                    ?: hadith.hadithEnglish
                    ?: hadith.hadithUrdu.orEmpty(),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
            )

            BrandDivider()

            val chapter = hadith.chapter?.chapterArabic ?: hadith.chapter?.chapterEnglish
            if (!chapter.isNullOrBlank()) {
                Text(
                    text = chapter,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                )
            }
        }
    }
}

@Composable
private fun BrandDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(2.dp)
                .background(BrandGold, RoundedCornerShape(1.dp)),
        )
    }
}

@Composable
private fun HadithShimmerList() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(5) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(20.dp),
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    ShimmerBox(modifier = Modifier.width(120.dp).height(18.dp))
                    ShimmerBox(modifier = Modifier.size(40.dp, 18.dp), shape = CircleShape)
                }
                ShimmerBox(modifier = Modifier.fillMaxWidth().height(16.dp))
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.9f).height(16.dp))
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.7f).height(16.dp))
            }
        }
    }
}
