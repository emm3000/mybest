package com.emm.mybest.features.exercise.presentation.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.emm.mybest.R
import com.emm.mybest.core.datetime.longEs
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierBackground
import com.emm.mybest.ui.theme.AtelierBackgroundSheet
import com.emm.mybest.ui.theme.AtelierDone
import com.emm.mybest.ui.theme.AtelierInk
import com.emm.mybest.ui.theme.AtelierInkMuted
import com.emm.mybest.ui.theme.AtelierInkSecondary
import com.emm.mybest.ui.theme.AtelierMonoFamily
import com.emm.mybest.ui.theme.AtelierSerifFamily

internal const val MAX_NAME_LENGTH = 40
internal const val MAX_DETAIL_LENGTH = 300
internal const val MAX_VOLUME_LENGTH = 30

private val DRAG_HANDLE_WIDTH = 40.dp
private val DRAG_HANDLE_HEIGHT = 3.dp
private val DRAG_HANDLE_CORNER = 2.dp
private val DRAG_HANDLE_PADDING_TOP = 12.dp
private val DRAG_HANDLE_PADDING_BOTTOM = 22.dp
private val SHEET_HORIZONTAL_PADDING = 28.dp
private val HERO_PADDING_TOP = 10.dp
private val HERO_PADDING_BOTTOM = 22.dp
private val HERO_FONT_SIZE = 38.sp
private val SECTION_MARGIN_TOP = 10.dp
private val TEXTAREA_MIN_HEIGHT = 56.dp
private val TEXTAREA_BORDER_WIDTH = 1.dp
private val BODY_FONT_SIZE = 15.sp
private val ACTIONS_PADDING_HORIZONTAL = 28.dp
private val ACTIONS_PADDING_TOP = 16.dp
private val ACTIONS_PADDING_BOTTOM = 4.dp
private val ACTIONS_GAP = 14.dp
private val BUTTON_PADDING_VERTICAL = 14.dp
private val BUTTON_FONT_SIZE = 11.sp
private val BUTTON_TRACKING = 0.18.em
private const val SAVE_BUTTON_WEIGHT = 1.4f
private const val CANCEL_BUTTON_WEIGHT = 1f
private const val DISABLED_ALPHA = 0.4f

@Composable
fun EditExerciseSheet(
    draft: EditingExerciseDraft,
    callbacks: EditExerciseSheetCallbacks,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = callbacks.onCancel,
        modifier = modifier,
        sheetState = sheetState,
        containerColor = AtelierBackgroundSheet,
        dragHandle = null,
        shape = RectangleShape,
        tonalElevation = 0.dp,
    ) {
        SheetDragHandle()
        SheetHeader(draft)
        SheetHero(draft)
        Hairline()
        SheetSection(
            label = stringResource(R.string.exercise_plan_edit_name_label),
            value = draft.name,
            maxLength = MAX_NAME_LENGTH,
            onValueChange = callbacks.onNameChange,
        )
        Hairline()
        SheetSection(
            label = stringResource(R.string.exercise_plan_edit_detail_label),
            value = draft.detail,
            maxLength = MAX_DETAIL_LENGTH,
            onValueChange = callbacks.onDetailChange,
        )
        Hairline()
        SheetSection(
            label = stringResource(R.string.exercise_plan_edit_volume_label),
            value = draft.volume,
            maxLength = MAX_VOLUME_LENGTH,
            onValueChange = callbacks.onVolumeChange,
        )
        Hairline()
        SheetActions(draft, callbacks)
    }
}

@Composable
private fun SheetDragHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = DRAG_HANDLE_PADDING_TOP, bottom = DRAG_HANDLE_PADDING_BOTTOM),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(DRAG_HANDLE_WIDTH)
                .height(DRAG_HANDLE_HEIGHT)
                .background(AtelierInkMuted, RoundedCornerShape(DRAG_HANDLE_CORNER)),
        )
    }
}

@Composable
private fun SheetHeader(draft: EditingExerciseDraft) {
    val dayLabel = draft.day.longEs().uppercase()
    val nameDisplay = draft.name.ifBlank { "—" }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SHEET_HORIZONTAL_PADDING),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        MicroLabel(
            text = "$dayLabel · $nameDisplay",
            style = MicroLabelStyle(tone = MicroLabelTone.Done),
        )
        MicroLabel(
            text = stringResource(R.string.edit_meal_header_template),
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
        )
    }
}

@Composable
private fun SheetHero(draft: EditingExerciseDraft) {
    val displayText = draft.name.ifBlank { "—" }
    Text(
        text = displayText,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SHEET_HORIZONTAL_PADDING)
            .padding(top = HERO_PADDING_TOP, bottom = HERO_PADDING_BOTTOM),
        style = TextStyle(
            fontFamily = AtelierSerifFamily,
            fontStyle = FontStyle.Italic,
            fontSize = HERO_FONT_SIZE,
            color = AtelierInk,
        ),
    )
}

@Composable
private fun SheetSection(
    label: String,
    value: String,
    maxLength: Int,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SHEET_HORIZONTAL_PADDING)
            .padding(vertical = SECTION_MARGIN_TOP),
    ) {
        MicroLabel(
            text = label,
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
        )
        Spacer(modifier = Modifier.height(SECTION_MARGIN_TOP))
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it.take(maxLength)) },
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = TEXTAREA_MIN_HEIGHT)
                .border(TEXTAREA_BORDER_WIDTH, AtelierInkMuted, RectangleShape),
            textStyle = TextStyle(
                fontFamily = AtelierMonoFamily,
                fontSize = BODY_FONT_SIZE,
                color = AtelierInk,
            ),
            cursorBrush = SolidColor(AtelierDone),
            maxLines = Int.MAX_VALUE,
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.padding(SECTION_MARGIN_TOP)) {
                    if (value.isEmpty()) {
                        Text(
                            text = "—",
                            style = TextStyle(
                                fontFamily = AtelierMonoFamily,
                                fontSize = BODY_FONT_SIZE,
                                color = AtelierInkMuted,
                            ),
                        )
                    }
                    innerTextField()
                }
            },
        )
        Spacer(modifier = Modifier.height(SECTION_MARGIN_TOP))
        MicroLabel(
            text = "${value.length} / $maxLength",
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
        )
    }
}

@Composable
private fun SheetActions(draft: EditingExerciseDraft, callbacks: EditExerciseSheetCallbacks) {
    val allBlank = draft.name.trim().isBlank() &&
        draft.detail.trim().isBlank() &&
        draft.volume.trim().isBlank()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ACTIONS_PADDING_HORIZONTAL)
            .padding(top = ACTIONS_PADDING_TOP, bottom = ACTIONS_PADDING_BOTTOM),
        horizontalArrangement = Arrangement.spacedBy(ACTIONS_GAP),
    ) {
        ExCancelButton(
            onCancel = callbacks.onCancel,
            modifier = Modifier.weight(CANCEL_BUTTON_WEIGHT),
        )
        ExSaveButton(
            isDisabled = allBlank,
            onSave = callbacks.onSave,
            modifier = Modifier.weight(SAVE_BUTTON_WEIGHT),
        )
    }
}

@Composable
private fun ExCancelButton(onCancel: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(TEXTAREA_BORDER_WIDTH, AtelierInkMuted, RectangleShape)
            .clickable(onClick = onCancel)
            .padding(vertical = BUTTON_PADDING_VERTICAL),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.edit_meal_cancel),
            style = TextStyle(
                fontFamily = AtelierMonoFamily,
                fontSize = BUTTON_FONT_SIZE,
                letterSpacing = BUTTON_TRACKING,
                color = AtelierInkSecondary,
            ),
        )
    }
}

@Composable
private fun ExSaveButton(
    isDisabled: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (isDisabled) AtelierInk.copy(alpha = DISABLED_ALPHA) else AtelierInk
    Box(
        modifier = modifier
            .background(bgColor, RectangleShape)
            .clickable(enabled = !isDisabled, onClick = onSave)
            .padding(vertical = BUTTON_PADDING_VERTICAL),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.edit_meal_save),
            style = TextStyle(
                fontFamily = AtelierMonoFamily,
                fontSize = BUTTON_FONT_SIZE,
                letterSpacing = BUTTON_TRACKING,
                color = AtelierBackground,
            ),
        )
    }
}
