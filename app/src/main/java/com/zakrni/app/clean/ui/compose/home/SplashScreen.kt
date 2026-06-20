package com.zakrni.app.clean.ui.compose.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zakrni.app.R
import com.zakrni.app.clean.ui.theme.BrandGold
import kotlinx.coroutines.delay

/**
 * Pre-shell brand splash. No Lottie asset ships with the app, so this is a fully Compose-native
 * reveal: a gold ring pulses while the logo scales in with an ease-out-back pop, then the app
 * name and tagline fade up. After a short hold [onDone] fires so the host can route onward.
 *
 * Expose & use from the host (this is pre-shell, so it is not in [homeGraph]):
 * ```
 * SplashScreen(onDone = { /* navigate to onboarding or home */ })
 * ```
 */
@Composable
fun SplashScreen(onDone: () -> Unit) {
    val logoScale = remember { Animatable(0.6f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffset = remember { Animatable(24f) }

    // Gentle continuous halo pulse behind the logo.
    val pulse = rememberInfiniteTransition(label = "splash-pulse")
    val haloScale by pulse.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "halo-scale",
    )

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, tween(400))
        logoScale.animateTo(1f, tween(550, easing = EaseOutBack))
        textAlpha.animateTo(1f, tween(450))
        textOffset.animateTo(0f, tween(450))
        delay(900L)
        onDone()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Pulsing halo.
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .graphicsLayer {
                            scaleX = haloScale
                            scaleY = haloScale
                            alpha = logoAlpha.value * 0.25f
                        }
                        .background(BrandGold.copy(alpha = 0.30f), CircleShape),
                )
                // Logo medallion.
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .graphicsLayer {
                            scaleX = logoScale.value
                            scaleY = logoScale.value
                            alpha = logoAlpha.value
                        }
                        .background(BrandGold.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mosque,
                        contentDescription = null,
                        tint = BrandGold,
                        modifier = Modifier.size(56.dp),
                    )
                }
            }

            Spacer(Modifier.size(28.dp))

            Text(
                text = stringResource(R.string.home_app_name),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.graphicsLayer {
                    alpha = textAlpha.value
                    translationY = textOffset.value
                },
            )
            Spacer(Modifier.size(6.dp))
            Text(
                text = stringResource(R.string.home_app_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.graphicsLayer {
                    alpha = textAlpha.value
                    translationY = textOffset.value
                },
            )
        }
    }
}
