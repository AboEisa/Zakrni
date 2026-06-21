package com.zakrni.app.clean.ui.compose.zakat

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

const val ZAKAT_ROUTE = "zakat"

fun NavGraphBuilder.zakatGraph(navController: NavHostController, onBack: () -> Unit) {
    composable(ZAKAT_ROUTE) {
        ZakatCalculatorScreen(onBack = onBack)
    }
}
