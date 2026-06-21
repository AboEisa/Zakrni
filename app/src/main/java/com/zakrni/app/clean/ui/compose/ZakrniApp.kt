package com.zakrni.app.clean.ui.compose

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zakrni.app.R
import com.zakrni.app.clean.ui.compose.azkardua.azkarDuaGraph
import com.zakrni.app.clean.ui.compose.categories.CategoriesScreen
import com.zakrni.app.clean.ui.compose.content.contentListsGraph
import com.zakrni.app.clean.ui.compose.hijri.hijriGraph
import com.zakrni.app.clean.ui.compose.home.HomeRoutes
import com.zakrni.app.clean.ui.compose.home.homeGraph
import com.zakrni.app.clean.ui.compose.media.mediaGraph
import com.zakrni.app.clean.ui.compose.prayer.prayerGraph
import com.zakrni.app.clean.ui.compose.fasting.fastingGraph
import com.zakrni.app.clean.ui.compose.qibla.qiblaGraph
import com.zakrni.app.clean.ui.compose.quran.favoritesGraph
import com.zakrni.app.clean.ui.compose.search.searchGraph
import com.zakrni.app.clean.ui.compose.zakat.zakatGraph
import com.zakrni.app.clean.ui.compose.quran.quranGraph
import com.zakrni.app.clean.ui.compose.settings.settingsGraph
import com.zakrni.app.clean.ui.compose.tasbih.tasbihGraph
import com.zakrni.app.clean.ui.compose.tracker.trackerGraph
import com.zakrni.app.clean.ui.theme.components.ZBottomBar
import com.zakrni.app.clean.ui.theme.components.ZBottomNavItem

object Routes {
    const val HOME = HomeRoutes.HOME
    const val PRAYER = "prayer"
    const val QURAN = "quran_list"
    const val CATEGORIES = "categories"
    const val FAVORITES = "favorites"
}

private val topLevelRoutes = setOf(Routes.HOME, Routes.PRAYER, Routes.QURAN, Routes.FAVORITES)

/** Root of the redesigned Compose app: animated bottom bar + the full NavHost. */
@Composable
fun ZakrniApp() {
    val navController = rememberNavController()

    val items = listOf(
        ZBottomNavItem(stringResource(R.string.rd_nav_home), Icons.Filled.Home, Routes.HOME),
        ZBottomNavItem(stringResource(R.string.player_times), Icons.Filled.AccessTime, Routes.PRAYER),
        ZBottomNavItem(stringResource(R.string.rd_nav_quran), Icons.AutoMirrored.Filled.MenuBook, Routes.QURAN),
        ZBottomNavItem(stringResource(R.string.rd_cat_favorites), Icons.Filled.Favorite, Routes.FAVORITES),
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    val showBottomBar = currentRoute in topLevelRoutes

    val open: (String) -> Unit = { route -> navController.navigate(route) }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
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
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            // Fast, light crossfade — no slide/size animation (no stretch, no jank).
            enterTransition = { fadeIn(tween(120)) },
            exitTransition = { fadeOut(tween(90)) },
            popEnterTransition = { fadeIn(tween(120)) },
            popExitTransition = { fadeOut(tween(90)) },
        ) {
            // Landing + main tabs
            homeGraph(navController, onOpen = open)
            prayerGraph(navController)
            quranGraph(navController)
            mediaGraph(navController)
            composable(Routes.CATEGORIES) {
                CategoriesScreen(onOpenCategory = open, onBack = { navController.popBackStack() })
            }

            // Content + tools + features
            contentListsGraph(navController, onBack = { navController.popBackStack() })
            azkarDuaGraph(navController, onBack = { navController.popBackStack() })
            tasbihGraph(navController, onBack = { navController.popBackStack() })
            trackerGraph(navController)
            fastingGraph(navController, onBack = { navController.popBackStack() })
            zakatGraph(navController, onBack = { navController.popBackStack() })
            favoritesGraph(navController, onBack = { navController.popBackStack() })
            searchGraph(navController)
            qiblaGraph(navController)
            hijriGraph(navController)
            settingsGraph(navController)
        }
    }
}
