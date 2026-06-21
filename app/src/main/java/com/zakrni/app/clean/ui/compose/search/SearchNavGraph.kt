package com.zakrni.app.clean.ui.compose.search

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.zakrni.app.clean.ui.compose.quran.QuranRoutes

const val SEARCH_ROUTE = "search"

fun NavGraphBuilder.searchGraph(navController: NavHostController) {
    composable(SEARCH_ROUTE) {
        SearchScreen(
            onOpenRoute = { route -> navController.navigate(route) },
            onOpenSurah = { surahNumber -> navController.navigate(QuranRoutes.reader(surahNumber)) },
            onBack = { navController.popBackStack() },
        )
    }
}
