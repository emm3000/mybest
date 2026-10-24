package com.emm.mybest.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.theme.AtelierTheme

private const val RESTING_BORDER_ALPHA = 0.40f
private const val FOCUSED_BORDER_ALPHA = 0.45f
private val RESTING_BORDER_WIDTH = 1.dp
private val FOCUSED_BORDER_WIDTH = 2.dp

/**
 * Text input in the shadcn/ui style: external label above the field, no fill, no floating label.
 *
 * The field boundary is always drawn so it keeps at least 3:1 contrast (WCAG 2.2 SC 1.4.11):
 * - Rest: 1dp `outline` at alpha 0.40
 * - Focused: 2dp `outline` at alpha 0.45
 * - Error, rest or focused: `error` at full alpha, 1dp at rest and 2dp when focused
 *
 * The color change is animated. The error message replaces the supporting text and turns the
 * label and helper red. The field is at least 40dp tall.
 */
@Composable
fun HInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    errorMessage: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val isError = errorMessage != null
    val isFocused by interactionSource.collectIsFocusedAsState()

    val cs = MaterialTheme.colorScheme
    val border = inputBorder(isError = isError, isFocused = isFocused)
    val textColor = inputTextColor(enabled = enabled)

    Column(modifier = modifier) {
        InputLabelSection(label = label, isError = isError)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .border(
                    width = border.width,
                    color = border.color,
                    shape = MaterialTheme.shapes.small,
                )
                .padding(2.dp),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 40.dp),
                enabled = enabled,
                readOnly = readOnly,
                singleLine = singleLine,
                minLines = minLines,
                maxLines = maxLines,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                visualTransformation = visualTransformation,
                interactionSource = interactionSource,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                cursorBrush = SolidColor(cs.onSurface),
                decorationBox = { innerTextField ->
                    HInputDecoration(
                        content = InputDecorationContent(
                            value = value,
                            placeholder = placeholder,
                            leadingIcon = leadingIcon,
                            trailingIcon = trailingIcon,
                        ),
                        singleLine = singleLine,
                        innerTextField = innerTextField,
                    )
                },
            )
        }

        val helperText = errorMessage ?: supportingText
        InputHelperSection(helperText = helperText, isError = isError)
    }
}

private data class InputBorder(val width: Dp, val color: Color)

@Composable
private fun inputBorder(isError: Boolean, isFocused: Boolean): InputBorder {
    val cs = MaterialTheme.colorScheme
    val targetColor = when {
        isError -> cs.error
        isFocused -> cs.outline.copy(alpha = FOCUSED_BORDER_ALPHA)
        else -> cs.outline.copy(alpha = RESTING_BORDER_ALPHA)
    }
    val color = animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 120),
        label = "input_ring",
    ).value
    return InputBorder(width = if (isFocused) FOCUSED_BORDER_WIDTH else RESTING_BORDER_WIDTH, color = color)
}

@Composable
private fun inputTextColor(enabled: Boolean): androidx.compose.ui.graphics.Color {
    val cs = MaterialTheme.colorScheme
    return if (enabled) cs.onSurface else cs.onSurface.copy(alpha = 0.38f)
}

@Composable
private fun HInputDecoration(
    content: InputDecorationContent,
    singleLine: Boolean,
    innerTextField: @Composable () -> Unit,
) {
    val boxAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
    val trailingAlignment = if (singleLine) Alignment.CenterEnd else Alignment.TopEnd
    val layout = InputSlotLayout(
        alignment = boxAlignment,
        hasLeadingIcon = content.leadingIcon != null,
        hasTrailingIcon = content.trailingIcon != null,
    )

    Box(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = boxAlignment,
    ) {
        LeadingIconSlot(icon = content.leadingIcon, alignment = boxAlignment)
        PlaceholderText(
            placeholder = content.placeholder.takeIf { content.value.isEmpty() },
            layout = layout,
        )
        InputTextFieldSlot(layout = layout, innerTextField = innerTextField)
        TrailingIconSlot(icon = content.trailingIcon, alignment = trailingAlignment)
    }
}

@Composable
private fun BoxScope.LeadingIconSlot(icon: (@Composable () -> Unit)?, alignment: Alignment) {
    if (icon == null) return
    Box(
        modifier = Modifier
            .align(alignment)
            .padding(end = 36.dp),
    ) { icon() }
}

@Composable
private fun BoxScope.PlaceholderText(placeholder: String?, layout: InputSlotLayout) {
    if (placeholder == null) return
    Text(
        text = placeholder,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier
            .align(layout.alignment)
            .then(if (layout.hasLeadingIcon) Modifier.padding(start = 28.dp) else Modifier),
    )
}

@Composable
private fun BoxScope.InputTextFieldSlot(
    layout: InputSlotLayout,
    innerTextField: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .align(layout.alignment)
            .padding(
                start = if (layout.hasLeadingIcon) 28.dp else 0.dp,
                end = if (layout.hasTrailingIcon) 40.dp else 0.dp,
            )
            .fillMaxWidth(),
    ) { innerTextField() }
}

@Composable
private fun BoxScope.TrailingIconSlot(icon: (@Composable () -> Unit)?, alignment: Alignment) {
    if (icon == null) return
    Box(Modifier.align(alignment)) { icon() }
}

@PreviewLightDark
@Composable
private fun HInputPreview() {
    AtelierTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                var name by remember { mutableStateOf("") }
                HInput(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre del mazo",
                    placeholder = "Ej: Vocabulario de Inglés B2",
                )

                var word by remember { mutableStateOf("serendipity") }
                HInput(
                    value = word,
                    onValueChange = { word = it },
                    label = "Palabra",
                    supportingText = "Escribe la palabra en inglés",
                )

                var broken by remember { mutableStateOf("") }
                HInput(
                    value = broken,
                    onValueChange = { broken = it },
                    label = "Campo requerido",
                    placeholder = "Este campo es obligatorio",
                    errorMessage = "Este campo es obligatorio",
                )

                var notes by remember { mutableStateOf("") }
                HInput(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notas (multiline)",
                    singleLine = false,
                    minLines = 4,
                    placeholder = "Escribe tus notas aquí…",
                )

                HInput(
                    value = "Campo desactivado",
                    onValueChange = {},
                    label = "Desactivado",
                    enabled = false,
                )
            }
        }
    }
}
