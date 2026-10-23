package com.emm.mybest.features.capture.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.theme.AtelierInkSecondary
import com.emm.mybest.ui.theme.AtelierTheme

private val CONTENT_GUTTER = 24.dp
private val SECTION_GAP = 28.dp
private val LABEL_BOTTOM = 4.dp
private val ACTION_VERTICAL = 10.dp
private val EMPTY_TOP = 14.dp

@Composable
fun NotificationCaptureContent(
    state: NotificationCaptureState,
    onIntent: (NotificationCaptureIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(CONTENT_GUTTER),
    ) {
        if (state.isLoaded) {
            introItems(state, onIntent)
            appItems(state.apps, onIntent)
            actionItems(state, onIntent)
            notificationItems(state.notifications)
        }
    }
}

private fun LazyListScope.introItems(
    state: NotificationCaptureState,
    onIntent: (NotificationCaptureIntent) -> Unit,
) {
    item {
        Text(
            text = stringResource(R.string.capture_privacy),
            style = MaterialTheme.typography.bodyMedium,
            color = AtelierInkSecondary,
        )
    }
    if (!state.isAccessGranted) {
        item {
            CaptureAccessSection(
                onOpenSettings = { onIntent(NotificationCaptureIntent.OnOpenAccessSettingsClick) },
                modifier = Modifier.padding(top = SECTION_GAP),
            )
        }
    }
}

private fun LazyListScope.appItems(
    apps: List<CaptureAppItemState>,
    onIntent: (NotificationCaptureIntent) -> Unit,
) {
    item {
        Column(modifier = Modifier.padding(top = SECTION_GAP)) {
            MicroLabel(
                text = stringResource(R.string.capture_apps_label),
                modifier = Modifier.padding(bottom = LABEL_BOTTOM),
            )
            Hairline()
        }
    }
    items(apps, key = { it.packageName }) { app ->
        CaptureAppRow(app = app, onToggle = { onIntent(NotificationCaptureIntent.OnAppToggle(app.packageName, it)) })
        Hairline()
    }
}

private fun LazyListScope.actionItems(
    state: NotificationCaptureState,
    onIntent: (NotificationCaptureIntent) -> Unit,
) {
    if (state.isCaptureActive) {
        item {
            CaptureAction(
                labelRes = R.string.capture_turn_off,
                onClick = { onIntent(NotificationCaptureIntent.OnTurnOffClick) },
                modifier = Modifier.padding(top = SECTION_GAP),
            )
        }
    }
    if (state.notifications.isNotEmpty()) {
        item {
            CaptureAction(
                labelRes = R.string.capture_delete_all,
                onClick = { onIntent(NotificationCaptureIntent.OnDeleteAllClick) },
                modifier = Modifier.padding(top = SECTION_GAP),
            )
        }
    }
}

private fun LazyListScope.notificationItems(notifications: List<CapturedItemState>) {
    item {
        Column(modifier = Modifier.padding(top = SECTION_GAP)) {
            MicroLabel(
                text = stringResource(R.string.capture_list_label),
                modifier = Modifier.padding(bottom = LABEL_BOTTOM),
            )
            Hairline()
            if (notifications.isEmpty()) {
                Text(
                    text = stringResource(R.string.capture_list_empty),
                    modifier = Modifier.padding(top = EMPTY_TOP),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AtelierInkSecondary,
                )
            }
        }
    }
    items(notifications, key = { it.id }) { item ->
        CapturedNotificationRow(item = item)
        Hairline()
    }
}

@Composable
private fun CaptureAction(
    @StringRes labelRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MicroLabel(
        text = stringResource(labelRes),
        modifier = modifier.clickable(onClick = onClick).padding(vertical = ACTION_VERTICAL),
    )
}

private val PREVIEW_STATE = NotificationCaptureState(
    isLoaded = true,
    isCaptureActive = true,
    apps = listOf(
        CaptureAppItemState("com.bcp.innovacxion.yapeapp", "Yape", isSelected = true),
        CaptureAppItemState("com.interbank.plin", "Plin", isSelected = false),
    ),
    notifications = listOf(
        CapturedItemState(
            id = 1,
            appLabel = "YAPE",
            title = "Yape",
            text = "Te yapearon S/ 25.00",
            postedAtDateLabel = "VIERNES 23 OCTUBRE",
            postedAtTimeLabel = "18:15",
        ),
    ),
)

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun NotificationCaptureContentPreview() {
    AtelierTheme {
        NotificationCaptureContent(state = PREVIEW_STATE, onIntent = {})
    }
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun NotificationCaptureContentEmptyPreview() {
    AtelierTheme {
        NotificationCaptureContent(
            state = PREVIEW_STATE.copy(isCaptureActive = false, notifications = emptyList(), isAccessGranted = false),
            onIntent = {},
        )
    }
}
