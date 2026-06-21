package com.zakrni.app.clean.ui.compose.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zakrni.app.R
import com.zakrni.app.clean.ui.compose.categories.categories
import com.zakrni.app.clean.ui.theme.components.EmptyState
import com.zakrni.app.clean.ui.theme.components.ZCard
import com.zakrni.app.clean.ui.theme.components.ZSectionHeader
import com.zakrni.app.clean.ui.theme.components.ZTopBar
import com.zakrni.app.clean.ui.viewmodels.QuranViewModel

@Composable
fun SearchScreen(
    onOpenRoute: (String) -> Unit,
    onOpenSurah: (Int) -> Unit,
    onBack: () -> Unit,
    viewModel: QuranViewModel = hiltViewModel(),
) {
    val surahs by viewModel.surahs.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { if (surahs.isEmpty()) viewModel.loadAllSurahs() }

    @Suppress("DEPRECATION")
    val arabic = LocalConfiguration.current.locale.language == "ar"
    var query by rememberSaveable { mutableStateOf("") }
    val needle = normalizeSearch(query)

    // Sections (labels resolved here; map is inline so stringResource is allowed).
    val sectionItems = categories.map { it.route to stringResource(it.title) }
    val sectionResults = if (needle.isBlank()) emptyList() else sectionItems.filter { normalizeSearch(it.second).contains(needle) }

    val surahResults = remember(surahs, needle, query) {
        if (needle.isBlank()) {
            emptyList()
        } else {
            surahs.filter { s ->
                s.number.toString() == query.trim() ||
                    normalizeSearch(s.name).contains(needle) ||
                    normalizeSearch(s.englishName).contains(needle) ||
                    normalizeSearch(s.englishNameTranslation).contains(needle)
            }.take(40)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ZTopBar(title = stringResource(R.string.rd_search_title), onBack = onBack)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            placeholder = { Text(stringResource(R.string.rd_search_hint)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Filled.Clear, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        )

        when {
            query.isBlank() -> EmptyState(icon = Icons.Filled.Search, title = stringResource(R.string.rd_search_prompt))
            sectionResults.isEmpty() && surahResults.isEmpty() ->
                EmptyState(icon = Icons.Filled.Search, title = stringResource(R.string.rd_search_empty))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (sectionResults.isNotEmpty()) {
                    item(key = "h_sections") { ZSectionHeader(stringResource(R.string.rd_search_sections)) }
                    items(sectionResults, key = { it.first }) { (route, label) ->
                        ResultRow(icon = Icons.AutoMirrored.Filled.MenuBook, title = label, subtitle = null, onClick = { onOpenRoute(route) })
                    }
                }
                if (surahResults.isNotEmpty()) {
                    item(key = "h_surahs") { ZSectionHeader(stringResource(R.string.rd_search_surahs)) }
                    items(surahResults, key = { it.number }) { s ->
                        val title = if (arabic) s.name else s.englishName.ifBlank { "Surah ${s.number}" }
                        ResultRow(
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            title = title,
                            subtitle = s.number.toString(),
                            onClick = { onOpenSurah(s.number) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String?, onClick: () -> Unit) {
    ZCard(onClick = onClick, contentPadding = 14.dp, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 14.dp).weight(1f),
            )
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private val searchDiacritics = "[ً-ْٰـ]".toRegex()

private fun normalizeSearch(input: String): String =
    input.lowercase()
        .replace(searchDiacritics, "")
        .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا')
        .replace('ى', 'ي').replace('ئ', 'ي')
        .replace('ؤ', 'و').replace('ة', 'ه')
        .trim()
