package com.zakrni.app.clean.ui.compose.content

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
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
import java.util.Calendar

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

                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        NameOfDayCard(names = names)
                    }
                    itemsIndexed(names, key = { _, item -> item.number }) { index, item ->
                        FlipNameCard(name = item, index = index)
                    }
                }
            }
        }
    }
}

@Composable
private fun NameOfDayCard(names: List<PresentationAllahNameData>) {
    if (names.isEmpty()) return
    val name = remember(names) { names[Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % names.size] }
    ZCard(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        contentPadding = 18.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.content_name_of_day),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = name.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            if (name.en.meaning.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = name.en.meaning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.content_tap_flip),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
            )
        }
    }
}

/** A 3D flip card: the Arabic name on the front, transliteration + meaning on the back. */
@Composable
private fun FlipNameCard(name: PresentationAllahNameData, index: Int) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(name.number) {
        delay((index % 12) * 40L)
        visible = true
    }
    val appear by animateFloatAsState(if (visible) 1f else 0f, tween(350), label = "name-appear")

    var flipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (flipped) 180f else 0f, tween(480), label = "name-flip")
    val density = LocalDensity.current.density

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .graphicsLayer {
                alpha = appear
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clip(RoundedCornerShape(20.dp))
            .background(if (rotation <= 90f) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(20.dp))
            .clickable { flipped = !flipped }
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (rotation <= 90f) {
            NameFront(name)
        } else {
            Box(modifier = Modifier.graphicsLayer { rotationY = 180f }, contentAlignment = Alignment.Center) {
                NameBack(name)
            }
        }
    }
}

@Composable
private fun NameFront(name: PresentationAllahNameData) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(
            modifier = Modifier.size(34.dp).background(BrandGold.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(name.number.toString(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = name.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun NameBack(name: PresentationAllahNameData) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        if (name.transliteration.isNotBlank()) {
            Text(
                text = name.transliteration,
                style = MaterialTheme.typography.titleSmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
        }
        Text(
            text = name.en.meaning,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
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
