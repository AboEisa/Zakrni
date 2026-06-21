package com.zakrni.app.clean.ui.compose.azkardua

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zakrni.app.R
import com.zakrni.app.clean.domain.models.DomainHisnDua
import com.zakrni.app.clean.ui.theme.components.EmptyState
import com.zakrni.app.clean.ui.theme.components.ErrorState
import com.zakrni.app.clean.ui.theme.components.AppearStyle
import com.zakrni.app.clean.ui.theme.components.appear
import com.zakrni.app.clean.ui.theme.components.ExpandableSection
import com.zakrni.app.clean.ui.theme.components.LoadingState
import com.zakrni.app.clean.ui.theme.components.ZTopBar
import com.zakrni.app.clean.ui.utils.HisnLocalizationUtils
import com.zakrni.app.clean.ui.viewmodels.DuasViewModel

/**
 * Compose migration of [com.zakrni.app.clean.ui.views.DuasFragment].
 *
 * Shows the Hisn (حصن المسلم) supplication sections pulled from the Duas API. The أذكار
 * sections are filtered out by [DuasViewModel] (they live on the Azkar screen instead). Each
 * section expands/collapses through the shared [ExpandableSection] component, with expansion state
 * owned by the ViewModel. Every dua item shows a number badge plus its large Arabic text.
 */
@Composable
fun DuaScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DuasViewModel = hiltViewModel(),
) {
    val sectionDuasMap by viewModel.sectionDuasMap.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val expandedSections by viewModel.expandedSections.collectAsStateWithLifecycle()

    val isArabic = isArabicUi()

    Column(modifier = modifier.fillMaxSize()) {
        ZTopBar(title = stringResource(R.string.azd_dua_title), onBack = onBack)

        // Build the ordered list of sections from the ViewModel's grouped map.
        val sections = remember(sectionDuasMap, isArabic) {
            buildDuaSections(sectionDuasMap, isArabic)
        }

        when {
            // Hard error with no data to fall back on -> full error state with retry.
            error != null && sections.isEmpty() -> {
                ErrorState(
                    message = error ?: stringResource(R.string.azd_loading_error),
                    onRetry = {
                        viewModel.clearError()
                        viewModel.refresh()
                    },
                    retryLabel = stringResource(R.string.azd_retry),
                )
            }

            isLoading && sections.isEmpty() -> LoadingState()

            sections.isEmpty() -> {
                EmptyState(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    title = stringResource(R.string.azd_empty_dua),
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(sections, key = { it.key }) { section ->
                        ExpandableSection(
                            modifier = Modifier.appear(AppearStyle.FadeUp),
                            title = section.title,
                            expanded = expandedSections.contains(section.sectionKey),
                            onToggle = { viewModel.toggleSection(section.sectionKey) },
                        ) {
                            DuaSectionContent(section = section, isArabic = isArabic)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DuaSectionContent(section: DuaSectionUi, isArabic: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        section.items.forEachIndexed { index, dua ->
            DuaItemCard(
                number = index + 1,
                text = HisnLocalizationUtils.localizeDuaText(dua, isArabic),
            )
        }
    }
}

/** A single supplication entry: a circular number badge above the large Arabic text. */
@Composable
private fun DuaItemCard(number: Int, text: String) {
    AzkarDuaItemSurface {
        Column(modifier = Modifier.fillMaxWidth()) {
            DuaNumberBadge(number = number)
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )
        }
    }
}

/** Small circular badge carrying the dua's position within its section. */
@Composable
private fun DuaNumberBadge(number: Int) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.size(28.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

/** Stable identity + display data for a dua section. */
private data class DuaSectionUi(
    val sectionKey: String,
    val title: String,
    val items: List<DomainHisnDua>,
) {
    val key: String get() = "dua_$sectionKey"
}

/**
 * Mirrors [com.zakrni.app.clean.ui.adapters.DuasAdapter]: one [ExpandableSection] per Hisn
 * section (skipping empties), titles localized the same way the azkar list does it.
 */
private fun buildDuaSections(
    sectionDuasMap: Map<String, List<DomainHisnDua>>,
    isArabic: Boolean,
): List<DuaSectionUi> = buildList {
    sectionDuasMap.entries.forEachIndexed { index, (sectionName, duas) ->
        if (duas.isNotEmpty()) {
            val sectionEnglish = duas.firstOrNull()?.sectionEnglish
            add(
                DuaSectionUi(
                    sectionKey = sectionName,
                    title = HisnLocalizationUtils.localizeSectionTitle(
                        sectionName = sectionName,
                        sectionEnglish = sectionEnglish,
                        isArabic = isArabic,
                        index = index,
                    ),
                    items = duas,
                )
            )
        }
    }
}
