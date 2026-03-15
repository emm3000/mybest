package com.emm.mybest.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.theme.MyBestTheme

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

@Preview(showBackground = true)
@Composable
private fun HIconButtonPreview() {
    MyBestTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                HIconButton(
                    icon = Icons.Default.Add,
                    contentDescription = "Ghost",
                    onClick = {},
                    variant = IconButtonVariant.Ghost,
                )
                HIconButton(
                    icon = Icons.Default.Add,
                    contentDescription = "Outline",
                    onClick = {},
                    variant = IconButtonVariant.Outline,
                )
                HIconButton(
                    icon = Icons.Default.Add,
                    contentDescription = "Destructive",
                    onClick = {},
                    variant = IconButtonVariant.Destructive,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                HIconButton(
                    icon = Icons.Default.Add,
                    contentDescription = "Ghost Disabled",
                    onClick = {},
                    variant = IconButtonVariant.Ghost,
                    enabled = false,
                )
                HIconButton(
                    icon = Icons.Default.Add,
                    contentDescription = "Outline Disabled",
                    onClick = {},
                    variant = IconButtonVariant.Outline,
                    enabled = false,
                )
                HIconButton(
                    icon = Icons.Default.Add,
                    contentDescription = "Destructive Disabled",
                    onClick = {},
                    variant = IconButtonVariant.Destructive,
                    enabled = false,
                )
            }
        }
    }
}
