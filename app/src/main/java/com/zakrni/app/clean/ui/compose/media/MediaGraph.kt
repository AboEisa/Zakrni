package com.zakrni.app.clean.ui.compose.media

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

/**
 * Registers the Media feature destinations on a [androidx.navigation.compose.NavHost]:
 *  - [MediaRoutes.HUB]   ("media")                       -> [MediaHubScreen]
 *  - [MediaRoutes.LIST]  ("media_list/{contentType}")    -> [ArticleListScreen]
 *  - [MediaRoutes.VIDEO] ("media_video/{videoId}")       -> [VideoDetailScreen]
 *
 * Both sub-routes take a single [NavType.StringType] argument.
 */
fun NavGraphBuilder.mediaGraph(navController: NavHostController) {
    composable(MediaRoutes.HUB) {
        MediaHubScreen(
            onOpenCategory = { contentType ->
                navController.navigate(MediaRoutes.list(contentType))
            },
        )
    }

    composable(
        route = MediaRoutes.LIST,
        arguments = listOf(navArgument(MediaRoutes.LIST_ARG) { type = NavType.StringType }),
    ) { backStackEntry ->
        val contentType = backStackEntry.arguments?.getString(MediaRoutes.LIST_ARG).orEmpty()
        ArticleListScreen(
            contentType = contentType,
            onBack = { navController.navigateUp() },
            onOpenVideo = { videoId ->
                navController.navigate(MediaRoutes.video(videoId))
            },
        )
    }

    composable(
        route = MediaRoutes.VIDEO,
        arguments = listOf(navArgument(MediaRoutes.VIDEO_ARG) { type = NavType.StringType }),
    ) { backStackEntry ->
        val videoId = backStackEntry.arguments?.getString(MediaRoutes.VIDEO_ARG).orEmpty()
        VideoDetailScreen(
            videoId = videoId,
            onBack = { navController.navigateUp() },
        )
    }
}
