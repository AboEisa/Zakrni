package com.zakrni.app.clean.ui.compose.tasbih

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

const val TASBIH_ROUTE = "tasbih"

fun NavGraphBuilder.tasbihGraph(navController: NavHostController, onBack: () -> Unit) {
    composable(TASBIH_ROUTE) {
        TasbihScreen(onBack = onBack)
    }
}
