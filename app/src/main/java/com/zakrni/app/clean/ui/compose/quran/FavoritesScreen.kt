package com.zakrni.app.clean.ui.compose.quran

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zakrni.app.R
import com.zakrni.app.clean.ui.theme.AyahTextStyle
import com.zakrni.app.clean.ui.theme.QuranFamily
import com.zakrni.app.clean.ui.theme.components.EmptyState
import com.zakrni.app.clean.ui.theme.components.ZCard
import com.zakrni.app.clean.ui.theme.components.ZTopBar

@Composable
fun FavoritesScreen(onOpenSurah: (Int) -> Unit, onBack: (() -> Unit)? = null) {
    val context = LocalContext.current
    val arabic = isArabicUi()
    var favorites by remember { mutableStateOf(readFavorites(context)) }

    Column(modifier = Modifier.fillMaxSize()) {
        ZTopBar(title = stringResource(R.string.rd_cat_favorites), onBack = onBack)
        if (favorites.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.FavoriteBorder,
                title = stringResource(R.string.rd_fav_empty),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(favorites, key = { it.id() }) { fav ->
                    FavoriteRow(
                        fav = fav,
                        arabic = arabic,
                        onOpen = { onOpenSurah(fav.surah) },
                        onRemove = {
                            removeFavorite(context, fav.surah, fav.ayahInSurah)
                            favorites = readFavorites(context)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteRow(
    fav: FavoriteAyah,
    arabic: Boolean,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
) {
    ZCard(onClick = onOpen, contentPadding = 16.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = fav.text,
                style = AyahTextStyle.copy(fontFamily = QuranFamily),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${fav.surahName} • ${localizeNumber(fav.ayahInSurah, arabic)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = stringResource(R.string.rd_fav_remove),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(38.dp)
                        .clickable(onClick = onRemove)
                        .padding(7.dp),
                )
            }
        }
    }
}
