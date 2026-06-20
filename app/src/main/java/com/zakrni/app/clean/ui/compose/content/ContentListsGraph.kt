package com.zakrni.app.clean.ui.compose.content

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

/** Route constants for the content-list (Hadith + Names of Allah) Compose screens. */
object ContentRoutes {
    const val HADITH = "hadith"
    const val ALLAH_NAMES = "allah_names"
}

/**
 * Registers the content-list destinations onto the host [NavHost].
 * Both screens are simple back-stacked lists driven by their own Hilt ViewModels.
 *
 * [navController] is accepted for parity with other feature graphs (and future
 * cross-screen navigation); today both screens only need [onBack].
 */
fun NavGraphBuilder.contentListsGraph(
    navController: NavHostController,
    onBack: () -> Unit,
) {
    composable(ContentRoutes.HADITH) {
        HadithScreen(onBack = onBack)
    }
    composable(ContentRoutes.ALLAH_NAMES) {
        AllahNamesScreen(onBack = onBack)
    }
}
