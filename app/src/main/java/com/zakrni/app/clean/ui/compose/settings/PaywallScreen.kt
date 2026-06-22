package com.zakrni.app.clean.ui.compose.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.billingclient.api.ProductDetails
import com.zakrni.app.R
import com.zakrni.app.clean.ads.SubscriptionManager
import com.zakrni.app.clean.ui.theme.BrandGold
import com.zakrni.app.clean.ui.theme.components.ZButton
import com.zakrni.app.clean.ui.theme.components.ZButtonStyle
import com.zakrni.app.clean.ui.theme.components.ZCard
import com.zakrni.app.clean.ui.theme.components.ZTopBar

private fun ProductDetails.subPrice(): String? =
    subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice

private fun ProductDetails.subMicros(): Long? =
    subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.priceAmountMicros

private fun ProductDetails.inAppPrice(): String? = oneTimePurchaseOfferDetails?.formattedPrice

@Composable
fun PaywallScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val sm = rememberSubscriptionManager()
    val isPremium by sm.isSubscribedState.collectAsStateWithLifecycle()
    val subs by sm.subscriptionProducts.collectAsStateWithLifecycle()
    val inApps by sm.inAppProducts.collectAsStateWithLifecycle()

    val monthly = subs.firstOrNull { it.productId == SubscriptionManager.MONTHLY_SUB_ID }
    val yearly = subs.firstOrNull { it.productId == SubscriptionManager.YEARLY_SUB_ID }
    val lifetime = inApps.firstOrNull { it.productId == SubscriptionManager.LIFETIME_INAPP_ID }
    val hasAnyPlan = monthly != null || yearly != null || lifetime != null

    Column(modifier = Modifier.fillMaxSize()) {
        ZTopBar(title = stringResource(R.string.set_paywall_title), onBack = onBack)

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            when {
                isPremium -> StateCard(
                    title = stringResource(R.string.set_paywall_already_premium_title),
                    message = stringResource(R.string.set_paywall_already_premium_message),
                )

                !hasAnyPlan -> {
                    Hero()
                    FeatureComparison()
                    StateCard(
                        title = stringResource(R.string.set_paywall_unavailable_title),
                        message = stringResource(R.string.set_paywall_unavailable_message),
                    )
                    ZButton(text = stringResource(R.string.set_paywall_retry), onClick = { sm.refreshProducts() }, style = ZButtonStyle.Outline, modifier = Modifier.fillMaxWidth())
                }

                else -> {
                    Hero()
                    FeatureComparison()

                    // Yearly savings vs 12× monthly
                    val savePercent = run {
                        val m = monthly?.subMicros(); val y = yearly?.subMicros()
                        if (m != null && y != null && m > 0) (((m * 12 - y).toDouble() / (m * 12)) * 100).toInt() else null
                    }

                    yearly?.let {
                        PlanCard(
                            name = stringResource(R.string.set_plan_yearly),
                            price = it.subPrice(),
                            period = stringResource(R.string.set_plan_yearly_period),
                            badge = stringResource(R.string.set_plan_best_value),
                            saveText = savePercent?.let { p -> stringResource(R.string.set_plan_save_format, p) },
                            ctaText = stringResource(R.string.set_plan_subscribe),
                            highlight = true,
                            onClick = { context.findActivity()?.let { a -> sm.launchSubscriptionFlow(a, it) } },
                        )
                    }
                    monthly?.let {
                        PlanCard(
                            name = stringResource(R.string.set_plan_monthly),
                            price = it.subPrice(),
                            period = stringResource(R.string.set_plan_monthly_period),
                            ctaText = stringResource(R.string.set_plan_subscribe),
                            onClick = { context.findActivity()?.let { a -> sm.launchSubscriptionFlow(a, it) } },
                        )
                    }
                    lifetime?.let {
                        PlanCard(
                            name = stringResource(R.string.set_plan_lifetime),
                            price = it.inAppPrice(),
                            period = stringResource(R.string.set_plan_lifetime_period),
                            ctaText = stringResource(R.string.set_plan_buy),
                            onClick = { context.findActivity()?.let { a -> sm.launchInAppPurchaseFlow(a, it) } },
                        )
                    }
                }
            }

            // Restore + legal (always visible)
            TextButton(onClick = { sm.restorePurchases() }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.set_restore_purchases))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.set_terms), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("  ${stringResource(R.string.set_legal_separator)}  ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.set_privacy), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun Hero() {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(shape = RoundedCornerShape(50), color = BrandGold.copy(alpha = 0.15f), modifier = Modifier.size(72.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = BrandGold, modifier = Modifier.size(40.dp))
            }
        }
        Text(stringResource(R.string.set_paywall_hero_title), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 12.dp))
        Text(stringResource(R.string.set_paywall_hero_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun FeatureComparison() {
    val items = listOf(
        stringResource(R.string.set_feature_all_content),
        stringResource(R.string.set_feature_prayer_times),
        stringResource(R.string.set_feature_no_ads),
        stringResource(R.string.set_feature_support),
    )
    ZCard {
        Text(
            text = stringResource(R.string.set_support_note),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        items.forEach { label ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 10.dp).weight(1f))
            }
        }
    }
}

@Composable
private fun Mark(on: Boolean) {
    if (on) Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
    else Icon(Icons.Filled.Close, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
}

@Composable
private fun PlanCard(
    name: String,
    price: String?,
    period: String,
    ctaText: String,
    modifier: Modifier = Modifier,
    badge: String? = null,
    saveText: String? = null,
    highlight: Boolean = false,
    onClick: () -> Unit,
) {
    AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically()) {
        ZCard(
            modifier = modifier.fillMaxWidth(),
            color = if (highlight) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (badge != null) {
                            Surface(shape = RoundedCornerShape(50), color = BrandGold, modifier = Modifier.padding(start = 8.dp)) {
                                Text(badge, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(price ?: stringResource(R.string.set_plan_unavailable), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        Text(" $period", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (saveText != null) {
                        Text(saveText, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
                ZButton(text = ctaText, onClick = onClick, style = if (highlight) ZButtonStyle.Gold else ZButtonStyle.Primary)
            }
        }
    }
}

@Composable
private fun StateCard(title: String, message: String) {
    ZCard(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
    }
}
