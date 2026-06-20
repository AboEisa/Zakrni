package com.zakrni.app.clean.ui.compose.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

object SettingsRoutes {
    const val SETTINGS = "settings"
    const val PAYWALL = "paywall"
}

fun NavGraphBuilder.settingsGraph(navController: NavHostController) {
    composable(SettingsRoutes.SETTINGS) {
        SettingsScreen(
            onBack = { navController.popBackStack() },
            onOpenPaywall = { navController.navigate(SettingsRoutes.PAYWALL) },
        )
    }
    composable(SettingsRoutes.PAYWALL) {
        PaywallScreen(onBack = { navController.popBackStack() })
    }
}
