package com.emm.mybest.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.theme.MyBestTheme

private const val SWITCH_DISABLED_ALPHA = 0.5f
private const val SWITCH_ANIMATION_MS = 180

@Composable
fun HSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
    width: Dp = 44.dp,
    height: Dp = 24.dp,
    thumbSize: Dp = 18.dp,
) {
    val cs = MaterialTheme.colorScheme
    val trackColor by animateColorAsState(
        targetValue = if (checked) cs.primary else cs.surfaceContainerHighest,
        animationSpec = tween(SWITCH_ANIMATION_MS),
        label = "switch_track",
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) cs.primary else cs.outlineVariant,
        animationSpec = tween(SWITCH_ANIMATION_MS),
        label = "switch_border",
    )
    val thumbColor by animateColorAsState(
        targetValue = if (checked) cs.onPrimary else cs.onSurfaceVariant,
        animationSpec = tween(SWITCH_ANIMATION_MS),
        label = "switch_thumb",
    )

    val horizontalOffset = ((width - thumbSize) / 2f) - 2.dp
    val thumbOffset = if (checked) horizontalOffset else -horizontalOffset

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .alpha(if (enabled) 1f else SWITCH_DISABLED_ALPHA)
            .semantics {
                role = Role.Switch
                stateDescription = if (checked) "Activado" else "Desactivado"
                if (contentDescription != null) this.contentDescription = contentDescription
            }
            .clickable(
                enabled = enabled,
                role = Role.Switch,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onCheckedChange(!checked) }
            .width(width)
            .height(height)
            .border(1.dp, borderColor, RoundedCornerShape(999.dp))
            .background(trackColor, RoundedCornerShape(999.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .background(thumbColor, CircleShape),
        )
    }
}

@PreviewLightDark
@Composable
private fun HSwitchPreview() {
    MyBestTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                var enabledSwitch by remember { mutableStateOf(true) }
                var disabledSwitch by remember { mutableStateOf(false) }

                Text("Recordatorios")
                HSwitch(
                    checked = enabledSwitch,
                    onCheckedChange = { enabledSwitch = it },
                    contentDescription = "Toggle recordatorios",
                    modifier = Modifier.padding(top = 8.dp),
                )

                Text(
                    text = "Deshabilitado",
                    modifier = Modifier.padding(top = 16.dp),
                )
                HSwitch(
                    checked = disabledSwitch,
                    onCheckedChange = { disabledSwitch = it },
                    enabled = false,
                    contentDescription = "Toggle deshabilitado",
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
