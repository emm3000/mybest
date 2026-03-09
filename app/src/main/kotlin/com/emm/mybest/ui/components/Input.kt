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
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.theme.MyBestTheme

/**
 * Input de texto inspirado en shadcn/ui — look & feel exacto.
 *
 * Características:
 * - Label externo sobre el campo (como shadcn `<Label />` + `<Input />`)
 * - Borde `outlineVariant` en reposo → `outline` (ring) al focus (animado)
 * - Fondo transparente (sin fill)
 * - Sin trailing icon / label flotante de Material 3
 * - Error: borde rojo, helper text rojo abajo
 * - Supporting text en `muted-foreground` cuando no hay error
 * - Altura mínima fija de 40 dp (shadcn h-10)
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
    val borderColor = inputBorderColor(isError = isError, isFocused = isFocused)
    val ringColor = inputRingColor(isError = isError, isFocused = isFocused)
    val textColor = inputTextColor(enabled = enabled)

    Column(modifier = modifier) {
        InputLabelSection(label = label, isError = isError)

        // ── Field box ─────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .border(
                    width = 2.dp,
                    color = ringColor,
                    shape = MaterialTheme.shapes.small,
                )
                .padding(2.dp),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 40.dp)
                    .clip(MaterialTheme.shapes.small)
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = MaterialTheme.shapes.small,
                    ),
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
                        value = value,
                        placeholder = placeholder,
                        leadingIcon = leadingIcon,
                        trailingIcon = trailingIcon,
                        singleLine = singleLine,
                        innerTextField = innerTextField,
                    )
                },
            )
        }

        // ── Helper / error text ───────────────────────────────────────────────
        val helperText = errorMessage ?: supportingText
        InputHelperSection(helperText = helperText, isError = isError)
    }
}

@Composable
private fun inputBorderColor(isError: Boolean, isFocused: Boolean): androidx.compose.ui.graphics.Color {
    val cs = MaterialTheme.colorScheme
    val targetColor = when {
        isError -> cs.error
        isFocused -> cs.outline
        else -> cs.outlineVariant
    }
    return animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 150),
        label = "input_border",
    ).value
}

@Composable
private fun inputRingColor(isError: Boolean, isFocused: Boolean): androidx.compose.ui.graphics.Color {
    val cs = MaterialTheme.colorScheme
    val targetColor = when {
        isFocused && isError -> cs.error.copy(alpha = 0.25f)
        isFocused -> cs.outline.copy(alpha = 0.45f)
        else -> Color.Transparent
    }
    return animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 120),
        label = "input_ring",
    ).value
}

@Composable
private fun inputTextColor(enabled: Boolean): androidx.compose.ui.graphics.Color {
    val cs = MaterialTheme.colorScheme
    return if (enabled) cs.onSurface else cs.onSurface.copy(alpha = 0.38f)
}

@Composable
private fun HInputDecoration(
    value: String,
    placeholder: String?,
    leadingIcon: (@Composable () -> Unit)?,
    trailingIcon: (@Composable () -> Unit)?,
    singleLine: Boolean,
    innerTextField: @Composable () -> Unit,
) {
    val boxAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
    val trailingAlignment = if (singleLine) Alignment.CenterEnd else Alignment.TopEnd
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    Box(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = boxAlignment,
    ) {
        LeadingIconSlot(icon = leadingIcon, alignment = boxAlignment)
        PlaceholderText(
            value = value,
            placeholder = placeholder,
            hasLeadingIcon = leadingIcon != null,
            alignment = boxAlignment,
            placeholderColor = placeholderColor,
        )
        InputTextFieldSlot(
            hasLeadingIcon = leadingIcon != null,
            hasTrailingIcon = trailingIcon != null,
            alignment = boxAlignment,
            innerTextField = innerTextField,
        )
        TrailingIconSlot(icon = trailingIcon, alignment = trailingAlignment)
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
private fun BoxScope.PlaceholderText(
    value: String,
    placeholder: String?,
    hasLeadingIcon: Boolean,
    alignment: Alignment,
    placeholderColor: androidx.compose.ui.graphics.Color,
) {
    if (!value.isEmpty() || placeholder == null) return
    Text(
        text = placeholder,
        style = MaterialTheme.typography.bodyMedium,
        color = placeholderColor,
        modifier = Modifier
            .align(alignment)
            .then(if (hasLeadingIcon) Modifier.padding(start = 28.dp) else Modifier),
    )
}

@Composable
private fun BoxScope.InputTextFieldSlot(
    hasLeadingIcon: Boolean,
    hasTrailingIcon: Boolean,
    alignment: Alignment,
    innerTextField: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .align(alignment)
            .padding(
                start = if (hasLeadingIcon) 28.dp else 0.dp,
                end = if (hasTrailingIcon) 40.dp else 0.dp,
            )
            .fillMaxWidth(),
    ) { innerTextField() }
}

@Composable
private fun BoxScope.TrailingIconSlot(icon: (@Composable () -> Unit)?, alignment: Alignment) {
    if (icon == null) return
    Box(Modifier.align(alignment)) { icon() }
}

// ─── Previews ────────────────────────────────────────────────────────────────

@PreviewLightDark
@Composable
private fun HInputPreview() {
    MyBestTheme {
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
