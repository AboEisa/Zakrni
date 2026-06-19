package com.zakrni.app.clean.ui.compose.azkardua

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zakrni.app.R
import com.zakrni.app.clean.domain.models.DomainHisnDua
import com.zakrni.app.clean.ui.models.PresentationAzkar
import com.zakrni.app.clean.ui.theme.components.EmptyState
import com.zakrni.app.clean.ui.theme.components.ErrorState
import com.zakrni.app.clean.ui.theme.components.ExpandableSection
import com.zakrni.app.clean.ui.theme.components.LoadingState
import com.zakrni.app.clean.ui.theme.components.ZTopBar
import com.zakrni.app.clean.ui.utils.AzkarType
import com.zakrni.app.clean.ui.utils.HisnLocalizationUtils
import com.zakrni.app.clean.ui.viewmodels.AzkarViewModel

/**
 * Compose migration of [com.zakrni.app.clean.ui.views.AzkarFragment].
 *
 * Shows the general azkar sections (morning / evening) followed by the Hisn أذكار
 * sections pulled from the Duas API. Each section expands/collapses through the shared
 * [ExpandableSection] component, with state owned by [AzkarViewModel].
 */
@Composable
fun AzkarScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AzkarViewModel = hiltViewModel(),
) {
    val azkarData by viewModel.azkarData.collectAsStateWithLifecycle()
    val hisnSections by viewModel.hisnAzkarSections.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val expandedSections by viewModel.expandedSections.collectAsStateWithLifecycle()
    val expandedHisnSections by viewModel.expandedHisnSections.collectAsStateWithLifecycle()

    val isArabic = isArabicUi()

    Column(modifier = modifier.fillMaxSize()) {
        ZTopBar(title = stringResource(R.string.azd_azkar_title), onBack = onBack)

        // Build the ordered list of sections from the ViewModel state.
        val sections = remember(azkarData, hisnSections, isArabic) {
            buildAzkarSections(azkarData, hisnSections, isArabic)
        }

        when {
            // Hard error: no data at all -> full error state with retry.
            error != null && azkarData == null && sections.isEmpty() -> {
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
                    title = stringResource(R.string.azd_empty_azkar),
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(sections, key = { it.key }) { section ->
                        val expanded = when (section) {
                            is AzkarSectionUi.General ->
                                expandedSections.contains(section.type)
                            is AzkarSectionUi.Hisn ->
                                expandedHisnSections.contains(section.sectionKey)
                        }
                        ExpandableSection(
                            title = section.title,
                            expanded = expanded,
                            onToggle = {
                                when (section) {
                                    is AzkarSectionUi.General -> viewModel.toggleSection(section.type)
                                    is AzkarSectionUi.Hisn -> viewModel.toggleHisnSection(section.sectionKey)
                                }
                            },
                        ) {
                            AzkarSectionContent(section = section, isArabic = isArabic)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AzkarSectionContent(section: AzkarSectionUi, isArabic: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when (section) {
            is AzkarSectionUi.General ->
                section.items.forEach { item ->
                    AzkarItemCard(
                        text = HisnLocalizationUtils.localizeGenericAzkarText(item.text, isArabic),
                        count = item.count,
                    )
                }

            is AzkarSectionUi.Hisn ->
                section.items.forEach { dua ->
                    AzkarItemCard(
                        text = HisnLocalizationUtils.localizeDuaText(dua, isArabic),
                        count = dua.count,
                    )
                }
        }
    }
}

/** A single azkar entry: repetition count above the large Arabic text. */
@Composable
private fun AzkarItemCard(text: String, count: Int) {
    AzkarDuaItemSurface {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = countLabel(count),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )
        }
    }
}

/** "N times" / "N مرات" label, mirroring the legacy adapter. */
@Composable
private fun countLabel(count: Int): String =
    if (count > 1) {
        stringResource(R.string.azd_count_times, count)
    } else {
        stringResource(R.string.azd_count_once)
    }

/** Stable identity + display data for an azkar section. */
private sealed interface AzkarSectionUi {
    val key: String
    val title: String

    data class General(
        val type: AzkarType,
        override val title: String,
        val items: List<PresentationAzkar>,
    ) : AzkarSectionUi {
        override val key: String get() = "general_${type.name}"
    }

    data class Hisn(
        val sectionKey: String,
        override val title: String,
        val items: List<DomainHisnDua>,
    ) : AzkarSectionUi {
        override val key: String get() = "hisn_$sectionKey"
    }
}

/**
 * Mirrors [com.zakrni.app.clean.ui.adapters.AzkarAdapter.submitList]: morning, evening, then the
 * Hisn azkar sections (skipping empties). Section titles are localized the same way.
 */
private fun buildAzkarSections(
    azkarData: com.zakrni.app.clean.ui.models.PresentationAzkarResponse?,
    hisnSections: Map<String, List<DomainHisnDua>>,
    isArabic: Boolean,
): List<AzkarSectionUi> = buildList {
    val morningTitle = if (isArabic) "أذكار الصباح" else "Morning adhkar"
    val eveningTitle = if (isArabic) "أذكار المساء" else "Evening adhkar"

    add(AzkarSectionUi.General(AzkarType.MORNING, morningTitle, azkarData?.morning_azkar ?: emptyList()))
    add(AzkarSectionUi.General(AzkarType.EVENING, eveningTitle, azkarData?.evening_azkar ?: emptyList()))

    hisnSections.entries.forEachIndexed { index, (sectionName, duas) ->
        if (duas.isNotEmpty()) {
            val sectionEnglish = duas.firstOrNull()?.sectionEnglish
            add(
                AzkarSectionUi.Hisn(
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
