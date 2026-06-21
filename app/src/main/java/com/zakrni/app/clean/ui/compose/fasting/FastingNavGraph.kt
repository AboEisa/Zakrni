package com.zakrni.app.clean.ui.compose.fasting

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

const val FASTING_ROUTE = "fasting"

fun NavGraphBuilder.fastingGraph(navController: NavHostController, onBack: () -> Unit) {
    composable(FASTING_ROUTE) {
        FastingKhatmaScreen(onBack = onBack)
    }
}
