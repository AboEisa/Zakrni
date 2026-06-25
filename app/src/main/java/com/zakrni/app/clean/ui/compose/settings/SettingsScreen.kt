package com.zakrni.app.clean.ui.compose.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zakrni.app.BuildConfig
import com.zakrni.app.R
import com.zakrni.app.clean.ui.theme.components.ZCard
import com.zakrni.app.clean.ui.theme.components.ZSectionHeader
import com.zakrni.app.clean.ui.theme.components.ZTopBar
import com.zakrni.app.clean.ui.utils.PrayerCalcSettings
import com.zakrni.app.clean.ui.utils.ThemeManager

@Composable
fun SettingsScreen(onBack: () -> Unit, onOpenPaywall: () -> Unit) {
    val context = LocalContext.current
    val subscriptionManager = rememberSubscriptionManager()
    val isPremium by subscriptionManager.isSubscribedState.collectAsStateWithLifecycle()

    var darkMode by remember { mutableStateOf(ThemeManager.isDarkMode(context)) }
    var language by remember { mutableStateOf(ThemeManager.getCurrentAppLanguage(context)) }
    var fontSize by remember { mutableIntStateOf(ThemeManager.getFontSize(context)) }
    var prayerNotif by remember { mutableStateOf(ThemeManager.isPrayerNotificationsEnabled(context)) }
    var azkarNotif by remember { mutableStateOf(ThemeManager.isAzkarRemindersEnabled(context)) }
    var autoPlay by remember { mutableStateOf(ThemeManager.isAutoPlayEnabled(context)) }
    var countSound by remember { mutableStateOf(ThemeManager.isCountSoundEnabled(context)) }
    var calcMethod by remember { mutableIntStateOf(PrayerCalcSettings.method(context)) }
    var madhab by remember { mutableIntStateOf(PrayerCalcSettings.school(context)) }

    Column(modifier = Modifier.fillMaxSize()) {
        ZTopBar(title = stringResource(R.string.set_title), onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Premium card
            if (isPremium) {
                ZCard {
                    Text(stringResource(R.string.set_premium_active), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
            } else {
                ZCard(onClick = onOpenPaywall, color = MaterialTheme.colorScheme.tertiary, contentColor = MaterialTheme.colorScheme.onTertiary) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.set_premium_title), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onTertiary)
                            Text(stringResource(R.string.set_premium_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.9f))
                        }
                        Text(stringResource(R.string.set_premium_cta), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onTertiary)
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiary)
                    }
                }
            }

            // Appearance
            ZSectionHeader(stringResource(R.string.set_section_appearance))
            ZCard {
                SwitchRow(
                    title = stringResource(R.string.set_dark_mode),
                    subtitle = stringResource(R.string.set_dark_mode_subtitle),
                    checked = darkMode,
                    onCheckedChange = { darkMode = it; ThemeManager.setDarkMode(context, it) },
                )
            }
            ZCard {
                Text(stringResource(R.string.set_language), style = MaterialTheme.typography.titleSmall)
                Text(stringResource(R.string.set_language_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                SegmentedChoice(
                    options = listOf(stringResource(R.string.set_language_arabic), stringResource(R.string.set_language_english)),
                    selectedIndex = if (language == ThemeManager.LANGUAGE_ENGLISH) 1 else 0,
                    onSelect = { idx ->
                        val code = if (idx == 1) ThemeManager.LANGUAGE_ENGLISH else ThemeManager.LANGUAGE_ARABIC
                        language = code
                        ThemeManager.setAppLanguage(context, code)
                    },
                )
            }
            ZCard {
                Text(stringResource(R.string.set_font_size), style = MaterialTheme.typography.titleSmall)
                Text(stringResource(R.string.set_font_size_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                SegmentedChoice(
                    options = listOf(stringResource(R.string.set_font_small), stringResource(R.string.set_font_medium), stringResource(R.string.set_font_large)),
                    selectedIndex = fontSize,
                    onSelect = { fontSize = it; ThemeManager.setFontSize(context, it) },
                )
            }

            // Notifications
            ZSectionHeader(stringResource(R.string.set_section_notifications))
            ZCard {
                SwitchRow(stringResource(R.string.set_prayer_notifications), stringResource(R.string.set_prayer_notifications_subtitle), prayerNotif) { prayerNotif = it; ThemeManager.setPrayerNotificationsEnabled(context, it) }
                SwitchRow(stringResource(R.string.set_azkar_reminders), stringResource(R.string.set_azkar_reminders_subtitle), azkarNotif) { azkarNotif = it; ThemeManager.setAzkarRemindersEnabled(context, it) }
            }

            // Prayer calculation
            ZSectionHeader(stringResource(R.string.set_section_prayer_calc))
            ZCard {
                Text(stringResource(R.string.set_calc_method), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 4.dp))
                calcMethods.forEach { m ->
                    MethodRow(
                        label = stringResource(m.labelRes),
                        selected = calcMethod == m.id,
                        onClick = { calcMethod = m.id; PrayerCalcSettings.setMethod(context, m.id) },
                    )
                }
            }
            ZCard {
                Text(stringResource(R.string.set_madhab), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
                SegmentedChoice(
                    options = listOf(stringResource(R.string.set_madhab_standard), stringResource(R.string.set_madhab_hanafi)),
                    selectedIndex = madhab,
                    onSelect = { madhab = it; PrayerCalcSettings.setSchool(context, it) },
                )
            }

            // Audio
            ZSectionHeader(stringResource(R.string.set_section_audio))
            ZCard {
                SwitchRow(stringResource(R.string.set_auto_play), stringResource(R.string.set_auto_play_subtitle), autoPlay) { autoPlay = it; ThemeManager.setAutoPlayEnabled(context, it) }
                SwitchRow(stringResource(R.string.set_count_sound), stringResource(R.string.set_count_sound_subtitle), countSound) { countSound = it; ThemeManager.setCountSoundEnabled(context, it) }
            }

            // About
            ZSectionHeader(stringResource(R.string.set_section_about))
            ZCard {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.set_about_version), style = MaterialTheme.typography.bodyLarge)
                    Text(BuildConfig.VERSION_NAME, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                ActionRow(stringResource(R.string.set_about_rate)) {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}")))
                    } catch (e: Exception) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")))
                    }
                }
                ActionRow(stringResource(R.string.set_about_share)) {
                    context.startActivity(
                        Intent.createChooser(
                            Intent(Intent.ACTION_SEND).setType("text/plain")
                                .putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=${context.packageName}"),
                            null,
                        ),
                    )
                }
                ActionRow(stringResource(R.string.set_restore_purchases)) {
                    subscriptionManager.restorePurchases()
                    Toast.makeText(context, R.string.set_restore_done, Toast.LENGTH_SHORT).show()
                }
            }
            Column(modifier = Modifier.padding(bottom = 24.dp)) {}
        }
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline,
            ),
        )
    }
}

@Composable
private fun ActionRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

private data class CalcMethodOption(val id: Int, val labelRes: Int)

private val calcMethods = listOf(
    CalcMethodOption(5, R.string.set_method_egypt),
    CalcMethodOption(4, R.string.set_method_makkah),
    CalcMethodOption(3, R.string.set_method_mwl),
    CalcMethodOption(1, R.string.set_method_karachi),
    CalcMethodOption(2, R.string.set_method_isna),
    CalcMethodOption(8, R.string.set_method_dubai),
)

@Composable
private fun MethodRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().selectable(selected = selected, onClick = onClick).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 4.dp).weight(1f))
    }
}

@Composable
private fun SegmentedChoice(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            options.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                Surface(
                    modifier = Modifier.weight(1f).selectable(selected = selected, onClick = { onSelect(index) }),
                    shape = RoundedCornerShape(10.dp),
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                ) {
                    Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(vertical = 10.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }
    }
}
