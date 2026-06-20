package com.zakrni.app.clean.ui.compose.prayer

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

/** Stable route constants for the prayer-times feature. */
object PrayerRoutes {
    const val PRAYER = "prayer"
    const val ALL_PRAYER_TIMES = "all_prayer_times"
}

/**
 * Registers the prayer-times destinations on a [NavHost]:
 *  - [PrayerRoutes.PRAYER] → [PrayerTimesScreen] (the bottom-nav tab)
 *  - [PrayerRoutes.ALL_PRAYER_TIMES] → [AllPrayerTimesScreen] (the full day list)
 *
 * Call from the host `NavHost { prayerGraph(navController) }`.
 */
fun NavGraphBuilder.prayerGraph(navController: NavHostController) {
    composable(PrayerRoutes.PRAYER) {
        PrayerTimesScreen(
            onViewAll = { navController.navigate(PrayerRoutes.ALL_PRAYER_TIMES) },
        )
    }
    composable(PrayerRoutes.ALL_PRAYER_TIMES) {
        AllPrayerTimesScreen(
            onBack = { navController.popBackStack() },
        )
    }
}
