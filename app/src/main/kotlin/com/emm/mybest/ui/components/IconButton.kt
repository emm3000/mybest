package com.emm.mybest.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class IconButtonVariant { Ghost, Outline, Destructive }

private const val ICON_BUTTON_DISABLED_ALPHA = 0.5f

@Composable
fun HIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: IconButtonVariant = IconButtonVariant.Ghost,
) {
    val cs = MaterialTheme.colorScheme
    val (containerColor, iconColor, borderColor) = when (variant) {
        IconButtonVariant.Ghost -> Triple(Color.Transparent, cs.onSurface, null)
        IconButtonVariant.Outline -> Triple(Color.Transparent, cs.onSurface, cs.outlineVariant)
        IconButtonVariant.Destructive -> Triple(
            cs.errorContainer,
            cs.onErrorContainer,
            cs.error.copy(alpha = 0.5f),
        )
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .defaultMinSize(minWidth = 40.dp, minHeight = 40.dp)
            .alpha(if (enabled) 1f else ICON_BUTTON_DISABLED_ALPHA),
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        border = borderColor?.let { BorderStroke(1.dp, it) },
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconColor,
            )
        }
    }
}
