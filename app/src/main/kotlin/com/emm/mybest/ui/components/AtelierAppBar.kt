package com.emm.mybest.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NavigateBefore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.theme.AtelierTheme

private val APP_BAR_HEIGHT = 56.dp
private val APP_BAR_PADDING_START = 8.dp
private val APP_BAR_PADDING_END = 16.dp
private val APP_BAR_PADDING_VERTICAL = 12.dp
private val BACK_BUTTON_SIZE = 44.dp
private val BACK_ICON_SIZE = 20.dp
private val LEADING_SPACER = 12.dp

@Composable
fun AtelierAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = APP_BAR_HEIGHT)
                    .padding(
                        start = APP_BAR_PADDING_START,
                        end = APP_BAR_PADDING_END,
                        top = APP_BAR_PADDING_VERTICAL,
                        bottom = APP_BAR_PADDING_VERTICAL,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LeadingSlot(onBack = onBack, navigationIcon = navigationIcon)
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (actions != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, content = actions)
                }
            }
            Hairline()
        }
    }
}

@Composable
private fun LeadingSlot(
    onBack: (() -> Unit)?,
    navigationIcon: (@Composable () -> Unit)?,
) {
    when {
        navigationIcon != null -> Box(contentAlignment = Alignment.Center) { navigationIcon() }
        onBack != null -> BackChevronButton(onClick = onBack)
        else -> Spacer(modifier = Modifier.width(LEADING_SPACER))
    }
}

@Composable
private fun BackChevronButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(BACK_BUTTON_SIZE),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.NavigateBefore,
            contentDescription = stringResource(id = R.string.action_back),
            modifier = Modifier.size(BACK_ICON_SIZE),
        )
    }
}

@PreviewLightDark
@Composable
private fun AtelierAppBarPreview() {
    AtelierTheme {
        AtelierAppBar(title = "Habit Details", onBack = {})
    }
}
