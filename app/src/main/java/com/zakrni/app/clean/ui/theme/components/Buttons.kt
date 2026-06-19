package com.zakrni.app.clean.ui.theme.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.zakrni.app.clean.ui.theme.BrandGold

enum class ZButtonStyle { Primary, Gold, Outline }

/**
 * Brand button with a subtle press-scale micro-interaction shared across the app.
 */
@Composable
fun ZButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ZButtonStyle = ZButtonStyle.Primary,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, label = "z-button-scale")

    val content: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(20.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }

    when (style) {
        ZButtonStyle.Outline -> OutlinedButton(
            onClick = onClick,
            modifier = modifier.scale(scale),
            enabled = enabled,
            interactionSource = interaction,
        ) { content() }

        else -> Button(
            onClick = onClick,
            modifier = modifier.scale(scale),
            enabled = enabled,
            interactionSource = interaction,
            colors = if (style == ZButtonStyle.Gold) {
                ButtonDefaults.buttonColors(
                    containerColor = BrandGold,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                )
            } else {
                ButtonDefaults.buttonColors()
            },
        ) { content() }
    }
}
