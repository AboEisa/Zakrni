package com.zakrni.app.clean.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zakrni.app.R
import com.zakrni.app.clean.ui.compose.categories.CategoriesScreen
import com.zakrni.app.clean.ui.compose.categories.CategoryRoutes
import com.zakrni.app.clean.ui.theme.components.ZBottomBar
import com.zakrni.app.clean.ui.theme.components.ZBottomNavItem

object Routes {
    const val CATEGORIES = "categories"
    const val MEDIA = "media"
    const val PRAYER = "prayer"
}

/**
 * Root of the Compose UI: a Scaffold with the custom animated bottom bar and a NavHost.
 * Foundation skeleton — real screens replace the placeholders group-by-group.
 */
@Composable
fun ZakrniApp() {
    val navController = rememberNavController()

    val items = listOf(
        ZBottomNavItem(stringResource(R.string.player_times), Icons.Filled.AccessTime, Routes.PRAYER),
        ZBottomNavItem(stringResource(R.string.all_media), Icons.Filled.PlayCircle, Routes.MEDIA),
        ZBottomNavItem(stringResource(R.string.all_categories), Icons.Filled.GridView, Routes.CATEGORIES),
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    Scaffold(
        bottomBar = {
            ZBottomBar(
                items = items,
                selectedIndex = selectedIndex,
                onSelect = { index ->
                    navController.navigate(items[index].route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.CATEGORIES,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            composable(Routes.CATEGORIES) {
                CategoriesScreen(onOpenCategory = { route -> navController.navigate(route) })
            }
            composable(Routes.MEDIA) { PlaceholderScreen(stringResource(R.string.all_media)) }
            composable(Routes.PRAYER) { PlaceholderScreen(stringResource(R.string.player_times)) }

            // Category sub-screens — placeholders until migrated by the feature agents.
            composable(CategoryRoutes.QURAN) { PlaceholderScreen(stringResource(R.string.rd_cat_quran)) }
            composable(CategoryRoutes.AZKAR) { PlaceholderScreen(stringResource(R.string.rd_cat_azkar)) }
            composable(CategoryRoutes.DUA) { PlaceholderScreen(stringResource(R.string.rd_cat_dua)) }
            composable(CategoryRoutes.HADITH) { PlaceholderScreen(stringResource(R.string.rd_cat_hadith)) }
            composable(CategoryRoutes.ALLAH_NAMES) { PlaceholderScreen(stringResource(R.string.rd_cat_allah_names)) }
            composable(CategoryRoutes.TASBIH) { PlaceholderScreen(stringResource(R.string.rd_cat_tasbih)) }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
    }
}
