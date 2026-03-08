package com.emm.mybest.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.theme.MyBestTheme

private const val SELECT_EXPANDED_ROTATION_DEGREES = 180f

/**
 * Dropdown selector inspired by shadcn/ui `<Select />`.
 *
 * Uses an external label like [HInput], with the same visual language:
 * - 1dp outlineVariant border → animated outline on focus/open
 * - Transparent background, minimum height 40dp
 * - Rotated chevron when expanded
 */
@Composable
fun <T> HSelect(
    items: List<T>,
    selectedItem: T?,
    onItemSelect: (T) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    itemLabel: (T) -> String = { it.toString() },
    placeholder: String = "Seleccionar…",
) {
    val (isExpanded, setIsExpanded) = remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme

    val borderColor by animateColorAsState(
        targetValue = if (isExpanded) cs.outline else cs.outlineVariant,
        animationSpec = tween(150),
        label = "select_border",
    )

    Column(modifier = modifier) {
        // ── Label ─────────────────────────────────────────────────────────────
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = cs.onSurface,
        )
        Spacer(Modifier.height(6.dp))

        // ── Dropdown ──────────────────────────────────────────────────────────
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { if (enabled) setIsExpanded(it) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            val displayText = selectedItem?.let { itemLabel(it) }
            val displayColor = selectDisplayColor(
                enabled = enabled,
                hasSelectedValue = displayText != null,
            )
            Box(
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 40.dp)
                    .clip(MaterialTheme.shapes.small)
                    .border(1.dp, borderColor, MaterialTheme.shapes.small)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = displayText ?: placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = displayColor,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(if (isExpanded) SELECT_EXPANDED_ROTATION_DEGREES else 0f),
                        tint = cs.onSurfaceVariant,
                    )
                }
            }

            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { setIsExpanded(false) },
            ) {
                items.forEach { option ->
                    SelectDropdownItem(
                        option = option,
                        itemLabel = itemLabel,
                        isSelected = selectedItem == option,
                        onClick = {
                            onItemSelect(option)
                            setIsExpanded(false)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> SelectDropdownItem(
    option: T,
    itemLabel: (T) -> String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    DropdownMenuItem(
        text = {
            Text(
                text = itemLabel(option),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) cs.primary else cs.onSurface,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            )
        },
        onClick = onClick,
    )
}

@Composable
private fun selectDisplayColor(
    enabled: Boolean,
    hasSelectedValue: Boolean,
): androidx.compose.ui.graphics.Color {
    val cs = MaterialTheme.colorScheme
    return when {
        !hasSelectedValue -> cs.onSurfaceVariant.copy(alpha = 0.6f)
        enabled -> cs.onSurface
        else -> cs.onSurface.copy(alpha = 0.38f)
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

private val demoItems = listOf("Vocabulario B2", "Phrasal Verbs", "Idioms", "Gramática")

@PreviewLightDark
@Composable
private fun HSelectEmptyPreview() {
    MyBestTheme {
        Surface {
            HSelect(
                items = demoItems,
                selectedItem = null,
                onItemSelect = {},
                label = "Mazo",
                placeholder = "Seleccionar mazo…",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun HSelectWithValuePreview() {
    MyBestTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                var selected1 by remember { mutableStateOf<String?>(demoItems.first()) }
                HSelect(
                    items = demoItems,
                    selectedItem = selected1,
                    onItemSelect = { selected1 = it },
                    label = "Mazo seleccionado",
                )

                var selected2 by remember { mutableStateOf<String?>(null) }
                HSelect(
                    items = demoItems,
                    selectedItem = selected2,
                    onItemSelect = { selected2 = it },
                    label = "Dificultad",
                    enabled = false,
                    placeholder = "Desactivado",
                )
            }
        }
    }
}
