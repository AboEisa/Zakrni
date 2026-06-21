package com.zakrni.app.clean.ui.compose.categories

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zakrni.app.R
import com.zakrni.app.clean.ui.theme.BrandGold
import com.zakrni.app.clean.ui.theme.components.ZCard
import com.zakrni.app.clean.ui.theme.components.ZTopBar
import kotlinx.coroutines.delay

/** Routes opened from the categories hub. */
object CategoryRoutes {
    const val QURAN = "quran_list"
    const val AZKAR = "azkar"
    const val DUA = "dua"
    const val HADITH = "hadith"
    const val ALLAH_NAMES = "allah_names"
    const val TASBIH = "tasbih"
    const val MEDIA = "media"
    const val QIBLA = "qibla"
    const val HIJRI = "hijri"
    const val TRACKER = "tracker"
    const val FASTING = "fasting"
    const val ZAKAT = "zakat"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"
}

private data class CategoryUi(
    @StringRes val title: Int,
    val route: String,
    @DrawableRes val icon: Int? = null,
    val iconVector: ImageVector? = null,
)

private val categories = listOf(
    CategoryUi(R.string.rd_cat_quran, CategoryRoutes.QURAN, iconVector = Icons.AutoMirrored.Filled.MenuBook),
    CategoryUi(R.string.rd_cat_azkar, CategoryRoutes.AZKAR, iconVector = Icons.Filled.Spa),
    CategoryUi(R.string.rd_cat_dua, CategoryRoutes.DUA, iconVector = Icons.Filled.VolunteerActivism),
    CategoryUi(R.string.rd_cat_hadith, CategoryRoutes.HADITH, iconVector = Icons.Filled.FormatQuote),
    CategoryUi(R.string.rd_cat_allah_names, CategoryRoutes.ALLAH_NAMES, iconVector = Icons.Filled.AutoAwesome),
    CategoryUi(R.string.rd_cat_tasbih, CategoryRoutes.TASBIH, iconVector = Icons.Filled.TouchApp),
    CategoryUi(R.string.media_hub_title, CategoryRoutes.MEDIA, iconVector = Icons.Filled.OndemandVideo),
    CategoryUi(R.string.rd_cat_qibla, CategoryRoutes.QIBLA, iconVector = Icons.Filled.Explore),
    CategoryUi(R.string.rd_cat_hijri, CategoryRoutes.HIJRI, iconVector = Icons.Filled.CalendarMonth),
    CategoryUi(R.string.rd_cat_tracker, CategoryRoutes.TRACKER, iconVector = Icons.Filled.LocalFireDepartment),
    CategoryUi(R.string.rd_cat_fasting, CategoryRoutes.FASTING, iconVector = Icons.Filled.DarkMode),
    CategoryUi(R.string.rd_cat_zakat, CategoryRoutes.ZAKAT, iconVector = Icons.Filled.Calculate),
    CategoryUi(R.string.rd_cat_favorites, CategoryRoutes.FAVORITES, iconVector = Icons.Filled.Favorite),
    CategoryUi(R.string.rd_cat_settings, CategoryRoutes.SETTINGS, iconVector = Icons.Filled.Settings),
)

@Composable
fun CategoriesScreen(
    onOpenCategory: (route: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        ZTopBar(title = stringResource(R.string.rd_categories_title))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            itemsIndexed(categories) { index, item ->
                CategoryTile(item = item, index = index, onClick = { onOpenCategory(item.route) })
            }
        }
    }
}

@Composable
private fun CategoryTile(item: CategoryUi, index: Int, onClick: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 55L)
        visible = true
    }
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(350), label = "tile-alpha")
    val translate by animateFloatAsState(if (visible) 0f else 40f, tween(350), label = "tile-translate")

    ZCard(
        onClick = onClick,
        contentPadding = 20.dp,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .graphicsLayer { translationY = translate },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(BrandGold.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    item.icon != null -> Image(
                        painter = painterResource(item.icon),
                        contentDescription = null,
                        modifier = Modifier.size(38.dp),
                    )
                    item.iconVector != null -> Icon(
                        imageVector = item.iconVector,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(34.dp),
                    )
                }
            }
            Text(
                text = stringResource(item.title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}
