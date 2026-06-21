package com.zakrni.app.clean.ui.compose.zakat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zakrni.app.R
import com.zakrni.app.clean.ui.theme.components.ZCard
import com.zakrni.app.clean.ui.theme.components.ZTopBar
import java.util.Locale

/**
 * Self-contained Zakat calculator. Sums zakatable assets minus debts, checks the gold nisab
 * (85g × entered gold price), and computes 2.5% due. Pure UI + math, no persistence.
 */
@Composable
fun ZakatCalculatorScreen(onBack: () -> Unit) {
    val arabic = isArabicLocale()
    var cash by remember { mutableStateOf("") }
    var gold by remember { mutableStateOf("") }
    var silver by remember { mutableStateOf("") }
    var trade by remember { mutableStateOf("") }
    var receivables by remember { mutableStateOf("") }
    var debts by remember { mutableStateOf("") }
    var goldPrice by remember { mutableStateOf("") }

    val assets = num(cash) + num(gold) + num(silver) + num(trade) + num(receivables)
    val net = (assets - num(debts)).coerceAtLeast(0.0)
    val nisab = num(goldPrice) * 85.0
    val meetsNisab = net > 0.0 && (nisab <= 0.0 || net >= nisab)
    val due = if (meetsNisab) net * 0.025 else 0.0
    val currency = stringResource(R.string.zk_currency)

    Column(modifier = Modifier.fillMaxSize()) {
        ZTopBar(title = stringResource(R.string.rd_cat_zakat), onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ZCard(contentPadding = 14.dp, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.zk_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            ZakatField(R.string.zk_cash, cash) { cash = it }
            ZakatField(R.string.zk_gold, gold) { gold = it }
            ZakatField(R.string.zk_silver, silver) { silver = it }
            ZakatField(R.string.zk_trade, trade) { trade = it }
            ZakatField(R.string.zk_receivables, receivables) { receivables = it }
            ZakatField(R.string.zk_debts, debts) { debts = it }
            ZakatField(R.string.zk_gold_price, goldPrice) { goldPrice = it }

            ZCard(
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                contentPadding = 18.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ResultRow(stringResource(R.string.zk_net), money(net, currency, arabic))
                    if (nisab > 0.0) {
                        Spacer(Modifier.height(6.dp))
                        ResultRow(stringResource(R.string.zk_nisab), money(nisab, currency, arabic))
                    }
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                    Spacer(Modifier.height(12.dp))
                    if (meetsNisab) {
                        Text(
                            text = stringResource(R.string.zk_due),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = money(due, currency, arabic),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.zk_below),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ZakatField(labelRes: Int, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onChange(input.filter { it.isDigit() || it == '.' }) },
        label = { Text(stringResource(labelRes)) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(text = value, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

private fun num(s: String): Double = s.replace(",", "").trim().toDoubleOrNull() ?: 0.0

private fun money(v: Double, currency: String, arabic: Boolean): String {
    val formatted = String.format(Locale.US, "%,.2f", v)
    val digits = if (arabic) {
        formatted.map { c -> if (c in '0'..'9') ('٠' + (c - '0')) else c }.joinToString("")
    } else {
        formatted
    }
    return "$digits $currency"
}

@Composable
private fun isArabicLocale(): Boolean {
    val config = LocalConfiguration.current
    @Suppress("DEPRECATION")
    return config.locale.language == "ar"
}
