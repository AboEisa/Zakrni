package com.zakrni.app.clean.ui.compose.azkardua

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

/**
 * Routes owned by the Azkar / Dua Compose feature. Kept as constants so callers register and
 * navigate without stringly-typed duplication.
 */
object AzkarDuaRoutes {
    const val AZKAR = "azkar"
    const val DUA = "dua"
}

/**
 * Registers the Azkar and Dua destinations on a host [androidx.navigation.compose.NavHost].
 *
 * Usage:
 * ```
 * NavHost(navController, startDestination = AzkarDuaRoutes.AZKAR) {
 *     azkarDuaGraph(navController, onBack = { navController.popBackStack() })
 * }
 * ```
 *
 * @param navController kept in the signature for parity with sibling graphs and to allow future
 *   cross-screen navigation (e.g. azkar -> dua) without changing the call site.
 * @param onBack invoked by each screen's back affordance.
 */
fun NavGraphBuilder.azkarDuaGraph(
    navController: NavHostController,
    onBack: () -> Unit,
) {
    composable(AzkarDuaRoutes.AZKAR) {
        AzkarScreen(onBack = onBack)
    }
    composable(AzkarDuaRoutes.DUA) {
        DuaScreen(onBack = onBack)
    }
}
