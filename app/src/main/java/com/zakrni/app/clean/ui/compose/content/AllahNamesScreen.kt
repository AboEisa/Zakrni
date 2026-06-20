package com.zakrni.app.clean.ui.compose.content

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zakrni.app.R
import com.zakrni.app.clean.ui.models.PresentationAllahNameData
import com.zakrni.app.clean.ui.theme.BrandGold
import com.zakrni.app.clean.ui.theme.components.EmptyState
import com.zakrni.app.clean.ui.theme.components.ErrorState
import com.zakrni.app.clean.ui.theme.components.ShimmerBox
import com.zakrni.app.clean.ui.theme.components.ZCard
import com.zakrni.app.clean.ui.theme.components.ZTopBar
import com.zakrni.app.clean.ui.viewmodels.AllahNamesViewModel
import kotlinx.coroutines.delay

/**
 * The 99 Names of Allah (Asma al-Husna). Each name is shown in an elegant card:
 * a gold-tinted number circle, the Arabic name in bold, its transliteration, and meaning.
 */
@Composable
fun AllahNamesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AllahNamesViewModel = hiltViewModel(),
) {
    val names by viewModel.allahNamesList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.errorMessage.collectAsStateWithLifecycle()
    val isEmpty by viewModel.isEmpty.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        ZTopBar(
            title = stringResource(R.string.content_allah_names_title),
            onBack = onBack,
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                error != null && names.isEmpty() -> ErrorState(
                    message = error ?: stringResource(R.string.content_allah_names_error),
                    onRetry = { viewModel.retryLoading() },
                    retryLabel = stringResource(R.string.content_retry),
                )

                isLoading && names.isEmpty() -> AllahNamesShimmerList()

                isEmpty && names.isEmpty() -> EmptyState(
                    icon = Icons.Outlined.Star,
                    title = stringResource(R.string.content_allah_names_empty),
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    itemsIndexed(names, key = { _, item -> item.number }) { index, item ->
                        AllahNameCard(name = item, index = index)
                    }
                }
            }
        }
    }
}

@Composable
private fun AllahNameCard(name: PresentationAllahNameData, index: Int) {
    // Tasteful one-shot entrance animation, staggered by position.
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(name.number) {
        delay((index % 12) * 45L)
        visible = true
    }
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(350), label = "name-alpha")
    val translate by animateFloatAsState(if (visible) 0f else 32f, tween(350), label = "name-translate")

    ZCard(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .graphicsLayer { translationY = translate },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Number circle.
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(BrandGold.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = name.number.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Name details — Arabic name, transliteration, meaning.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = name.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (name.transliteration.isNotBlank()) {
                    Text(
                        text = name.transliteration,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                    )
                }
                if (name.en.meaning.isNotBlank()) {
                    Text(
                        text = name.en.meaning,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AllahNamesShimmerList() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(7) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(20.dp),
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ShimmerBox(modifier = Modifier.size(44.dp), shape = CircleShape)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ShimmerBox(modifier = Modifier.width(120.dp).height(20.dp))
                    ShimmerBox(modifier = Modifier.width(90.dp).height(14.dp))
                    Spacer(modifier = Modifier.height(2.dp))
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.8f).height(14.dp))
                }
            }
        }
    }
}
