package com.zakrni.app.clean.ui.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.zakrni.app.clean.ui.theme.ZakrniTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * New Compose host for the redesigned app. Not yet the launcher — it will replace the
 * legacy Activity/Fragment flow once the screen migration (Phase 1+) is complete.
 *
 * Extends [AppCompatActivity] (not ComponentActivity) so the app-wide language
 * (AppCompatDelegate per-app locales) and dark-mode night mode set by ThemeManager apply here;
 * Compose then follows the configuration for both strings and RTL/LTR layout direction.
 */
@AndroidEntryPoint
class MainComposeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZakrniTheme {
                ZakrniApp()
            }
        }
    }
}
