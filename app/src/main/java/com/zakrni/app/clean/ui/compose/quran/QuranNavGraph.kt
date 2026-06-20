package com.zakrni.app.clean.ui.compose.quran

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

/**
 * Routes owned by the Quran Compose feature. Kept as constants so callers register and navigate
 * without stringly-typed duplication.
 */
object QuranRoutes {
    /** Searchable list of the 114 surahs. */
    const val LIST = "quran_list"

    /** Argument name for the surah number passed to the reader. */
    const val ARG_SURAH_NUMBER = "surahNumber"

    /** Pattern for the reader destination, e.g. `quran_reader/18`. */
    const val READER_PATTERN = "quran_reader/{$ARG_SURAH_NUMBER}"

    /** Builds a concrete reader route for [surahNumber]. */
    fun reader(surahNumber: Int): String = "quran_reader/$surahNumber"
}

/**
 * Registers the Quran list + reader destinations on a host
 * [androidx.navigation.compose.NavHost].
 *
 * Usage:
 * ```
 * NavHost(navController, startDestination = QuranRoutes.LIST) {
 *     quranGraph(navController)
 * }
 * ```
 *
 * The reader argument is an [NavType.IntType] (default `1`, i.e. Al-Fatiha) so a malformed or
 * missing argument still opens a valid surah.
 */
fun NavGraphBuilder.quranGraph(navController: NavHostController) {
    composable(QuranRoutes.LIST) {
        SurahListScreen(
            onBack = { navController.popBackStack() },
            onOpenSurah = { surahNumber ->
                navController.navigate(QuranRoutes.reader(surahNumber))
            },
        )
    }

    composable(
        route = QuranRoutes.READER_PATTERN,
        arguments = listOf(
            navArgument(QuranRoutes.ARG_SURAH_NUMBER) {
                type = NavType.IntType
                defaultValue = 1
            },
        ),
    ) { backStackEntry ->
        val surahNumber = backStackEntry.arguments
            ?.getInt(QuranRoutes.ARG_SURAH_NUMBER, 1)
            ?.coerceIn(1, 114)
            ?: 1
        QuranReaderScreen(
            surahNumber = surahNumber,
            onBack = { navController.popBackStack() },
        )
    }
}
