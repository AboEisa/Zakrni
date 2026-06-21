package com.zakrni.app.clean.ui.compose.quran

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zakrni.app.R
import com.zakrni.app.clean.domain.models.DomainSurah
import com.zakrni.app.clean.ui.theme.BrandGold
import com.zakrni.app.clean.ui.theme.QuranFamily
import com.zakrni.app.clean.ui.theme.components.EmptyState
import com.zakrni.app.clean.ui.theme.components.ErrorState
import com.zakrni.app.clean.ui.theme.components.ShimmerBox
import com.zakrni.app.clean.ui.theme.components.ZCard
import com.zakrni.app.clean.ui.theme.components.ZTopBar
import com.zakrni.app.clean.ui.utils.QuranUtils
import com.zakrni.app.clean.ui.viewmodels.QuranViewModel

/**
 * Compose migration of [com.zakrni.app.clean.ui.views.Quran2Fragment] — the searchable list of the
 * 114 surahs. Loading shows shimmer skeleton cards; tapping a surah opens the reader.
 */
@Composable
fun SurahListScreen(
    onBack: () -> Unit,
    onOpenSurah: (surahNumber: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuranViewModel = hiltViewModel(),
) {
    val surahs by viewModel.surahs.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val isArabic = isArabicUi()
    var query by rememberSaveable { mutableStateOf("") }

    val filtered = remember(surahs, query, isArabic) {
        filterSurahs(surahs, query)
    }

    Column(modifier = modifier.fillMaxSize()) {
        ZTopBar(title = stringResource(R.string.qrn_list_title), onBack = onBack)

        SurahSearchField(
            query = query,
            onQueryChange = { query = it },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        when {
            // Hard error with nothing to show -> full error state with retry.
            error != null && surahs.isEmpty() && !isLoading -> {
                ErrorState(
                    message = error ?: stringResource(R.string.qrn_loading_error),
                    onRetry = {
                        viewModel.clearError()
                        viewModel.loadAllSurahs()
                    },
                    retryLabel = stringResource(R.string.qrn_retry),
                )
            }

            isLoading && surahs.isEmpty() -> SurahListShimmer()

            surahs.isEmpty() -> {
                EmptyState(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    title = stringResource(R.string.qrn_empty_title),
                )
            }

            filtered.isEmpty() -> {
                EmptyState(
                    icon = Icons.Filled.Search,
                    title = stringResource(R.string.qrn_empty_search),
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(filtered, key = { it.number }) { surah ->
                        SurahCard(
                            surah = surah,
                            isArabic = isArabic,
                            onClick = { onOpenSurah(surah.number) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SurahSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        placeholder = { Text(stringResource(R.string.qrn_search_hint)) },
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Filled.Clear,
                        contentDescription = stringResource(R.string.qrn_search_clear),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    )
}

@Composable
private fun SurahCard(
    surah: DomainSurah,
    isArabic: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ayahCount = QuranUtils.getAyahCount(surah.number)
        .takeIf { it > 0 } ?: surah.ayahs.size
    val revelation = QuranUtils.getRevelationType(surah.number, isArabic)

    val primaryName = if (isArabic) surah.name else surah.englishName.ifBlank { "Surah ${surah.number}" }
    val secondaryName = if (isArabic) {
        surah.englishName
    } else {
        surah.englishNameTranslation.ifBlank { surah.name }
    }

    ZCard(onClick = onClick, contentPadding = 14.dp, modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Decorative gold number badge.
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(BrandGold.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = localizeNumber(surah.number, isArabic),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = primaryName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (secondaryName.isNotBlank()) {
                    Text(
                        text = secondaryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Text(
                    text = "$revelation  •  ${stringResource(R.string.qrn_verses_count, ayahCount)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            Spacer(Modifier.width(8.dp))

            // Arabic surah glyph hint using the Mushaf face.
            Text(
                text = surah.name,
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = QuranFamily),
                color = MaterialTheme.colorScheme.tertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SurahListShimmer() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        repeat(9) { index ->
            val alpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400, delayMillis = index * 40),
                label = "shimmer-row-alpha",
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(alpha),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ShimmerBox(modifier = Modifier.size(46.dp), shape = CircleShape)
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.6f).height(16.dp))
                    Spacer(Modifier.height(8.dp))
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f).height(12.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

/** Filters by Arabic name, English name, translation, or surah number — case/diacritics tolerant. */
private fun filterSurahs(surahs: List<DomainSurah>, query: String): List<DomainSurah> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return surahs
    val needle = normalizeForSearch(trimmed)
    // Match Arabic-Indic digits typed for the number too.
    val asNumber = trimmed.map { ch -> if (ch in '٠'..'٩') ('0' + (ch - '٠')) else ch }.joinToString("")
    return surahs.filter { surah ->
        surah.number.toString() == asNumber ||
            normalizeForSearch(surah.name).contains(needle) ||
            normalizeForSearch(surah.englishName).contains(needle) ||
            normalizeForSearch(surah.englishNameTranslation).contains(needle)
    }
}

private val arabicDiacritics = "[ً-ْٰـ]".toRegex()

/** Lowercase + strip Arabic tashkeel/tatweel and unify alef/hamza/ya/ta-marbuta so search is forgiving. */
private fun normalizeForSearch(input: String): String =
    input.lowercase()
        .replace(arabicDiacritics, "")
        .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا')
        .replace('ى', 'ي').replace('ئ', 'ي')
        .replace('ؤ', 'و').replace('ة', 'ه')
        .trim()
