package com.emm.mybest.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.theme.MyBestTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val TIME_PICKER_VISIBLE_ITEM_COUNT = 5
private const val TIME_PICKER_CENTER_PADDING_ITEMS = TIME_PICKER_VISIBLE_ITEM_COUNT / 2
private val TIME_PICKER_ITEM_HEIGHT = 44.dp

@Composable
fun ReminderTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    HTimePickerDialog(
        initialHour = initialHour,
        initialMinute = initialMinute,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Composable
internal fun HTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedHour by rememberSaveable(initialHour) {
        mutableIntStateOf(initialHour.coerceIn(0, 23))
    }
    var selectedMinute by rememberSaveable(initialMinute) {
        mutableIntStateOf(initialMinute.coerceIn(0, 59))
    }

    val hours = remember { (0..23).toList() }
    val minutes = remember { (0..59).toList() }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            tonalElevation = 0.dp,
            shadowElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                TimePickerHeader(
                    hour = selectedHour,
                    minute = selectedMinute,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TimePickerColumn(
                        label = stringResource(R.string.time_picker_hour_label),
                        values = hours,
                        selectedValue = selectedHour,
                        onValueChange = { selectedHour = it },
                        modifier = Modifier.weight(1f),
                    )

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                    )

                    TimePickerColumn(
                        label = stringResource(R.string.time_picker_minute_label),
                        values = minutes,
                        selectedValue = selectedMinute,
                        onValueChange = { selectedMinute = it },
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    HButton(
                        text = stringResource(R.string.action_cancel),
                        onClick = onDismiss,
                        variant = ButtonVariant.Ghost,
                    )
                    Spacer(Modifier.width(8.dp))
                    HButton(
                        text = stringResource(R.string.action_accept),
                        onClick = { onConfirm(selectedHour, selectedMinute) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TimePickerHeader(
    hour: Int,
    minute: Int,
) {
    val cs = MaterialTheme.colorScheme
    val timeText = remember(hour, minute) { formatTime(hour, minute) }
    val selectedDescription = stringResource(R.string.time_picker_selected_description, timeText)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.time_picker_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = cs.onSurface,
            )
            Text(
                text = stringResource(R.string.time_picker_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onSurfaceVariant,
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = cs.surfaceContainerLow,
            border = BorderStroke(1.dp, cs.outlineVariant),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Text(
                text = timeText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .semantics { contentDescription = selectedDescription },
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = cs.onSurface,
            )
        }
    }
}

@Composable
private fun TimePickerColumn(
    label: String,
    values: List<Int>,
    selectedValue: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = values.indexOf(selectedValue).coerceAtLeast(0),
    )
    val scope = rememberCoroutineScope()
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val itemContainerShape = remember { RoundedCornerShape(10.dp) }
    val flingBehavior = rememberSnapFlingBehavior(
        lazyListState = listState,
        snapPosition = SnapPosition.Center,
    )
    val verticalContentPadding = TIME_PICKER_ITEM_HEIGHT * TIME_PICKER_CENTER_PADDING_ITEMS

    var isAnimatingScroll by remember { mutableStateOf(false) }

    LaunchedEffect(listState, values) {
        snapshotFlow { values[centeredItemIndex(listState)] }
            .distinctUntilChanged()
            .collect { value ->
                if (!isAnimatingScroll) {
                    currentOnValueChange(value)
                }
            }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(TIME_PICKER_ITEM_HEIGHT * TIME_PICKER_VISIBLE_ITEM_COUNT)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.shapes.medium)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TIME_PICKER_ITEM_HEIGHT)
                    .align(Alignment.Center)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, itemContainerShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, itemContainerShape),
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = label },
                contentPadding = PaddingValues(vertical = verticalContentPadding),
                flingBehavior = flingBehavior,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items(count = values.size) { index ->
                    val value = values[index]
                    TimePickerCell(
                        value = value,
                        isSelected = value == selectedValue,
                        distanceFromSelection = abs(value - selectedValue),
                        cellContentDescription = "$label $value",
                        onClick = {
                            currentOnValueChange(value)
                            scope.launch {
                                isAnimatingScroll = true
                                listState.animateScrollToItem(index)
                                isAnimatingScroll = false
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun TimePickerCell(
    value: Int,
    isSelected: Boolean,
    distanceFromSelection: Int,
    cellContentDescription: String,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val textColor by animateColorAsState(
        targetValue = when {
            isSelected -> cs.onSurface
            distanceFromSelection == 1 -> cs.onSurface.copy(alpha = 0.72f)
            else -> cs.onSurfaceVariant.copy(alpha = 0.72f)
        },
        animationSpec = tween(durationMillis = 150),
        label = "time_picker_text_color",
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(TIME_PICKER_ITEM_HEIGHT)
            .semantics(mergeDescendants = true) {
                contentDescription = cellContentDescription
                selected = isSelected
            },
        color = Color.Transparent,
        contentColor = cs.onSurface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = formatTwoDigits(value),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                ),
                color = textColor,
            )
        }
    }
}

private fun centeredItemIndex(listState: LazyListState): Int {
    val layoutInfo = listState.layoutInfo
    val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
    return layoutInfo.visibleItemsInfo
        .minByOrNull { item -> abs((item.offset + (item.size / 2)) - viewportCenter) }
        ?.index ?: 0
}

private fun formatTime(hour: Int, minute: Int): String = "${formatTwoDigits(hour)}:${formatTwoDigits(minute)}"

private fun formatTwoDigits(value: Int): String = value.toString().padStart(2, '0')

@PreviewLightDark
@Composable
private fun ReminderTimePickerDialogPreview() {
    MyBestTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HTimePickerDialog(
                initialHour = 8,
                initialMinute = 30,
                onConfirm = { _, _ -> },
                onDismiss = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ReminderTimePickerDialogEdgeCasePreview() {
    MyBestTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HTimePickerDialog(
                initialHour = 23,
                initialMinute = 59,
                onConfirm = { _, _ -> },
                onDismiss = {},
            )
        }
    }
}
