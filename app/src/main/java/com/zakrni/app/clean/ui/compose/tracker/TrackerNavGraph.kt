package com.zakrni.app.clean.ui.compose.tracker

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

/** Route for the worship streaks / habit tracker feature. */
const val TRACKER_ROUTE = "tracker"

/**
 * Registers the worship-tracker destination on an existing [NavHostController]'s graph.
 *
 * Usage inside a `NavHost { ... }` block:
 * ```
 * trackerGraph(navController)
 * ```
 * The screen resolves its [TrackerViewModel] via Hilt, so no arguments are threaded through nav.
 */
fun NavGraphBuilder.trackerGraph(navController: NavHostController) {
    composable(TRACKER_ROUTE) {
        TrackerDashboardScreen(
            onBack = { navController.popBackStack() },
        )
    }
}
