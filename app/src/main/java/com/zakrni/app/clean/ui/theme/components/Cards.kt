package com.zakrni.app.clean.ui.theme.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zakrni.app.clean.ui.theme.glassBorder
import com.zakrni.app.clean.ui.theme.glassChild
import com.zakrni.app.clean.ui.theme.glassSurface

/**
 * Standard brand surface card. When [onClick] is supplied it gets a press-scale micro-interaction.
 */
@Composable
fun ZCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(20.dp),
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.glassSurface,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 2.dp,
    contentPadding: Dp = 16.dp,
    border: BorderStroke? = BorderStroke(1.dp, MaterialTheme.colorScheme.glassBorder),
    content: @Composable ColumnScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && onClick != null) 0.98f else 1f, label = "z-card-scale")

    val clickModifier = if (onClick != null) {
        Modifier.clickable(interactionSource = interaction, indication = null, onClick = onClick)
    } else Modifier

    Surface(
        modifier = modifier.scale(scale).then(clickModifier).glassChild(shape),
        shape = shape,
        color = color,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = border,
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}
