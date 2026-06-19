package com.zakrni.app.clean.ui.compose.hijri

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

/** Route for the Hijri calendar + Islamic events screen. */
const val HIJRI_ROUTE = "hijri"

/**
 * Registers the Hijri calendar destination. Wire into the app graph from `ZakrniApp.kt`
 * (owned by another agent) with:
 *
 *     hijriGraph(navController)
 *
 * and navigate to it via `navController.navigate(HIJRI_ROUTE)`.
 */
fun NavGraphBuilder.hijriGraph(navController: NavHostController) {
    composable(HIJRI_ROUTE) {
        HijriCalendarScreen(onBack = { navController.popBackStack() })
    }
}
