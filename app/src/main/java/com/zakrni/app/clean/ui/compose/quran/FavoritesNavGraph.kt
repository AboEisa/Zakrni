package com.zakrni.app.clean.ui.compose.quran

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

const val FAVORITES_ROUTE = "favorites"

fun NavGraphBuilder.favoritesGraph(navController: NavHostController, onBack: () -> Unit) {
    composable(FAVORITES_ROUTE) {
        FavoritesScreen(
            onBack = onBack,
            onOpenSurah = { surahNumber -> navController.navigate(QuranRoutes.reader(surahNumber)) },
        )
    }
}
