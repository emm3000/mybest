package com.emm.mybest.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.theme.AtelierTheme
import com.emm.mybest.ui.theme.StarlinkTextStyles

// ─── Variants ──────────────────────────────────────────────────────────────

enum class BadgeVariant { Default, Secondary, Destructive, Outline, Success }

/**
 * Status badge/chip inspired by shadcn/ui.
 *
 * Typical usage: cards count in a Deck, review status,
 * flashcard category, difficulty.
 */
@Composable
fun HBadge(
    label: String,
    modifier: Modifier = Modifier,
    variant: BadgeVariant = BadgeVariant.Default,
) {
    val cs = MaterialTheme.colorScheme
    val dotColor = badgeDotColor(variant)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(100.dp),
        color = Color.Transparent,
        contentColor = cs.onSurface,
        border = BorderStroke(
            width = 1.dp,
            color = cs.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color = dotColor, shape = CircleShape),
            )
            Text(
                text = label.uppercase(),
                style = StarlinkTextStyles.chipLabel,
            )
        }
    }
}

@Composable
private fun badgeDotColor(variant: BadgeVariant): Color {
    val cs = MaterialTheme.colorScheme
    return when (variant) {
        BadgeVariant.Default -> cs.onSurface
        BadgeVariant.Secondary -> cs.onSurface
        BadgeVariant.Destructive -> cs.error
        BadgeVariant.Outline -> cs.onSurface
        BadgeVariant.Success -> cs.tertiary
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

@PreviewLightDark
@Composable
private fun HBadgeVariantsPreview() {
    AtelierTheme {
        androidx.compose.material3.Surface {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BadgeVariant.entries.forEach { variant ->
                    HBadge(
                        label = variant.name,
                        variant = variant,
                        modifier = Modifier.wrapContentWidth(),
                    )
                }
            }
        }
    }
}
