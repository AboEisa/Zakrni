package com.zakrni.app.clean.ui.compose.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

/**
 * Registers the Home landing destination on a [androidx.navigation.compose.NavHost]:
 *  - [HomeRoutes.HOME] → [HomeScreen] (the home bottom-nav tab).
 *
 * Quick actions emit plain route strings (e.g. "quran_list", "azkar", "qibla", ...) through
 * [onOpen]; the host wires those to the destinations owned by the other feature graphs.
 *
 * Call from the host `NavHost { homeGraph(navController, onOpen = ::open) }`.
 *
 * Note: [SplashScreen] and [OnboardingScreen] are pre-shell and intentionally NOT registered here;
 * the host shows them as standalone composables before entering the navigation shell.
 */
fun NavGraphBuilder.homeGraph(navController: NavHostController, onOpen: (String) -> Unit) {
    composable(HomeRoutes.HOME) {
        HomeScreen(onOpen = onOpen)
    }
}
