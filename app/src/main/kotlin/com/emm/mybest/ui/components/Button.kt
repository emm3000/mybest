package com.emm.mybest.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.theme.MyBestTheme

// ─── Variants ──────────────────────────────────────────────────────────────

enum class ButtonVariant { Default, Destructive, Outline, Secondary, Ghost, Link }

private const val BUTTON_DISABLED_ALPHA = 0.5f

/**
 * Main button inspired by shadcn/ui.
 *
 * Supports variants: Default, Destructive, Outline, Secondary, Ghost, Link.
 * Has support for loading state with [isLoading].
 */
@Composable
fun HButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Default,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
    content: @Composable RowScope.() -> Unit,
) {
    HButtonContainer(
        variant = variant,
        onClick = onClick,
        enabled = enabled && !isLoading,
        contentPadding = contentPadding,
        modifier = modifier.defaultMinSize(minHeight = 40.dp),
    ) {
        ButtonContent(leadingIcon = leadingIcon, isLoading = isLoading, content = content)
    }
}

@Composable
private fun HButtonContainer(
    variant: ButtonVariant,
    onClick: () -> Unit,
    enabled: Boolean,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val (containerColor, contentColor, borderColor) = buttonTokens(variant)
    val finalContentPadding = if (variant == ButtonVariant.Link) {
        PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    } else {
        contentPadding
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.alpha(if (enabled) 1f else BUTTON_DISABLED_ALPHA),
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small,
        border = borderColor?.let { BorderStroke(1.dp, it) },
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(finalContentPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
private fun buttonTokens(variant: ButtonVariant): Triple<Color, Color, Color?> {
    val cs = MaterialTheme.colorScheme
    return when (variant) {
        ButtonVariant.Default -> Triple(cs.primary, cs.onPrimary, null)
        ButtonVariant.Destructive -> Triple(cs.error, cs.onError, null)
        ButtonVariant.Secondary -> Triple(cs.secondaryContainer, cs.onSecondaryContainer, null)
        ButtonVariant.Outline -> Triple(Color.Transparent, cs.onSurface, cs.outlineVariant)
        ButtonVariant.Ghost -> Triple(Color.Transparent, cs.onSurface, null)
        ButtonVariant.Link -> Triple(Color.Transparent, cs.primary, null)
    }
}

@Composable
private fun RowScope.ButtonContent(
    leadingIcon: ImageVector?,
    isLoading: Boolean,
    content: @Composable RowScope.() -> Unit,
) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            color = LocalContentColor.current,
            strokeWidth = 2.dp,
        )
        Spacer(Modifier.width(8.dp))
    } else if (leadingIcon != null) {
        Icon(
            imageVector = leadingIcon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(8.dp))
    }
    content()
}

// ─── Convenience overloads ───────────────────────────────────────────────────

@Composable
fun HButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Default,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
) {
    HButton(
        onClick = onClick,
        modifier = modifier,
        variant = variant,
        enabled = enabled,
        isLoading = isLoading,
        leadingIcon = leadingIcon,
    ) {
        Text(
            text = if (isLoading) "Loading…" else text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

@PreviewLightDark
@Composable
private fun HButtonVariantsPreview() {
    MyBestTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ButtonVariant.entries.forEach { variant ->
                    HButton(
                        text = variant.name,
                        onClick = {},
                        variant = variant,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun HButtonWithIconPreview() {
    MyBestTheme {
        Surface(Modifier.padding(16.dp)) {
            HButton(
                text = "Nueva tarjeta",
                onClick = {},
                leadingIcon = Icons.Default.Add,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun HButtonLoadingPreview() {
    MyBestTheme {
        Surface(Modifier.padding(16.dp)) {
            HButton(
                text = "Generar",
                onClick = {},
                isLoading = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
