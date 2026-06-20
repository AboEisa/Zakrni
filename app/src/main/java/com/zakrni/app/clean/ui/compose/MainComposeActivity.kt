package com.zakrni.app.clean.ui.compose

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.zakrni.app.clean.ui.compose.home.OnboardingScreen
import com.zakrni.app.clean.ui.compose.home.SplashScreen
import com.zakrni.app.clean.ui.theme.ZakrniTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single Compose host for the redesigned app: Splash → (Onboarding on first launch) → main shell.
 *
 * Extends [AppCompatActivity] so the app-wide language (AppCompatDelegate per-app locales) and
 * dark-mode night mode set by ThemeManager apply here; Compose then follows the configuration
 * for both strings and RTL/LTR layout direction.
 */
@AndroidEntryPoint
class MainComposeActivity : AppCompatActivity() {

    private enum class Stage { Splash, Onboarding, App }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("zakrni_settings", Context.MODE_PRIVATE)

        setContent {
            ZakrniTheme {
                var stage by remember { mutableStateOf(Stage.Splash) }

                when (stage) {
                    Stage.Splash -> SplashScreen(onDone = {
                        stage = if (prefs.getBoolean(KEY_ONBOARDING_DONE, false)) Stage.App else Stage.Onboarding
                    })

                    Stage.Onboarding -> OnboardingScreen(onFinish = {
                        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
                        stage = Stage.App
                    })

                    Stage.App -> ZakrniApp()
                }
            }
        }
    }

    private companion object {
        const val KEY_ONBOARDING_DONE = "compose_onboarding_done"
    }
}
