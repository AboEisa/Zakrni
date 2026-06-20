package com.zakrni.app.clean.ui.compose.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zakrni.app.R
import com.zakrni.app.clean.ui.theme.BrandGold
import com.zakrni.app.clean.ui.theme.components.ZButton
import com.zakrni.app.clean.ui.theme.components.ZButtonStyle
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

private data class OnboardingPage(
    val icon: ImageVector,
    val titleRes: Int,
    val bodyRes: Int,
)

private val onboardingPages = listOf(
    OnboardingPage(Icons.Filled.Mosque, R.string.home_onb_title_1, R.string.home_onb_body_1),
    OnboardingPage(Icons.Filled.AccessTime, R.string.home_onb_title_2, R.string.home_onb_body_2),
    OnboardingPage(Icons.AutoMirrored.Filled.MenuBook, R.string.home_onb_title_3, R.string.home_onb_body_3),
)

/**
 * Pre-shell welcome flow: three swipeable pages with a parallax icon, page dots, a "Skip" shortcut,
 * and a "Next" → "Get Started" button. [onFinish] fires when the user finishes or skips.
 *
 * Expose & use from the host (pre-shell, so not in [homeGraph]):
 * ```
 * OnboardingScreen(onFinish = { /* mark onboarding complete; navigate to home */ })
 * ```
 */
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == onboardingPages.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
    ) {
        // Skip row.
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onFinish) {
                Text(
                    text = stringResource(R.string.home_onb_skip),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) { page ->
            val offset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            OnboardingPageContent(page = onboardingPages[page], pageOffset = offset)
        }

        Spacer(Modifier.size(24.dp))
        PageDots(count = onboardingPages.size, selected = pagerState.currentPage)
        Spacer(Modifier.size(24.dp))

        ZButton(
            text = stringResource(if (isLastPage) R.string.home_onb_start else R.string.home_onb_next),
            onClick = {
                if (isLastPage) {
                    onFinish()
                } else {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            },
            style = ZButtonStyle.Gold,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage, pageOffset: Float) {
    val fraction = pageOffset.absoluteValue.coerceIn(0f, 1f)
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .graphicsLayer {
                    // Parallax + fade as the page scrolls.
                    translationX = pageOffset * size.width * 0.20f
                    alpha = 1f - 0.5f * fraction
                    val scale = 1f - 0.15f * fraction
                    scaleX = scale
                    scaleY = scale
                }
                .background(BrandGold.copy(alpha = 0.14f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = BrandGold,
                modifier = Modifier.size(80.dp),
            )
        }
        Spacer(Modifier.size(40.dp))
        Text(
            text = stringResource(page.titleRes),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.size(14.dp))
        Text(
            text = stringResource(page.bodyRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
    }
}

@Composable
private fun PageDots(count: Int, selected: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(count) { index ->
            val isSelected = index == selected
            val width by animateDpAsState(if (isSelected) 24.dp else 8.dp, tween(250), label = "onb-dot-width")
            val alpha by animateFloatAsState(if (isSelected) 1f else 0.3f, tween(250), label = "onb-dot-alpha")
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .height(8.dp)
                    .width(width)
                    .graphicsLayer { this.alpha = alpha }
                    .background(BrandGold, CircleShape),
            )
        }
    }
}
