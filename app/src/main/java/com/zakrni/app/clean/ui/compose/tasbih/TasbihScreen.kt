package com.zakrni.app.clean.ui.compose.tasbih

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zakrni.app.R
import com.zakrni.app.clean.ui.theme.BrandGold
import com.zakrni.app.clean.ui.theme.components.ZTopBar

@Composable
fun TasbihScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val vm: TasbihViewModel = viewModel(factory = TasbihViewModel.factory(context))
    val state by vm.state.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current

    var showAdd by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        ZTopBar(
            title = stringResource(R.string.tsb_title),
            onBack = onBack,
            action = {
                IconButton(onClick = { showAdd = true }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.tsb_add))
                }
            },
        )

        // Dhikr selector
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(state.dhikrs) { index, dhikr ->
                val selected = index == state.currentIndex
                Surface(
                    onClick = { vm.selectDhikr(index) },
                    shape = CircleShape,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    Text(
                        text = dhikr.text,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val current = state.current
                Text(
                    text = current?.text.orEmpty(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                )

                CounterCircle(
                    count = current?.count ?: 0,
                    target = current?.target ?: 0,
                    progress = current?.progress ?: 0f,
                    onTap = {
                        if (vm.increment()) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    },
                    modifier = Modifier.padding(top = 24.dp),
                )

                Text(
                    text = stringResource(R.string.tsb_of, current?.count ?: 0, current?.target ?: 0),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 20.dp),
                )

                TextButton(onClick = { showResetConfirm = true }, modifier = Modifier.padding(top = 8.dp)) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        text = stringResource(R.string.tsb_reset),
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
            }
        }
    }

    if (showAdd) {
        AddDhikrDialog(
            onAdd = { text, target -> vm.addDhikr(text, target); showAdd = false },
            onDismiss = { showAdd = false },
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text(stringResource(R.string.tsb_reset_confirm_title)) },
            text = { Text(stringResource(R.string.tsb_reset_confirm_message)) },
            confirmButton = {
                TextButton(onClick = { vm.resetCurrent(); showResetConfirm = false }) {
                    Text(stringResource(R.string.tsb_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text(stringResource(R.string.tsb_cancel))
                }
            },
        )
    }
}

@Composable
private fun CounterCircle(
    count: Int,
    target: Int,
    progress: Float,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, spring(), label = "tap-scale")
    val animatedProgress by animateFloatAsState(progress, label = "tap-progress")

    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = BrandGold
    val ringColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .size(240.dp)
            .scale(scale)
            .clickable(interactionSource = interaction, indication = null, onClick = onTap),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 18.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Surface(
            shape = CircleShape,
            color = ringColor.copy(alpha = 0.06f),
            modifier = Modifier.size(170.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = count.toString(),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun AddDhikrDialog(onAdd: (String, Int) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    var countText by remember { mutableStateOf("33") }
    val target = countText.toIntOrNull() ?: 0
    val valid = text.isNotBlank() && text.length <= 120 && target in 1..10000

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.tsb_add_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.tsb_add_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(R.string.tsb_add_text_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )
                OutlinedTextField(
                    value = countText,
                    onValueChange = { countText = it.filter { ch -> ch.isDigit() }.take(5) },
                    label = { Text(stringResource(R.string.tsb_add_count_label)) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = { onAdd(text.trim(), target) }) {
                Text(stringResource(R.string.tsb_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.tsb_cancel)) }
        },
    )
}
