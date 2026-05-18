package com.emm.mybest.features.exercise.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emm.mybest.features.exercise.presentation.EditingExercise
import com.emm.mybest.features.exercise.presentation.ExercisePlanIntent
import com.emm.mybest.ui.components.HBottomSheet
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.HIconButton
import com.emm.mybest.ui.components.HInput
import com.emm.mybest.ui.theme.StarlinkTextStyles

@Composable
fun ExerciseEditorSheet(
    editing: EditingExercise,
    onIntent: (ExercisePlanIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    HBottomSheet(
        onDismissRequest = { onIntent(ExercisePlanIntent.CancelEdit) },
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = editing.day.name,
                    style = StarlinkTextStyles.sectionLabel,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                HIconButton(
                    icon = Icons.Rounded.Close,
                    contentDescription = "Cerrar",
                    onClick = { onIntent(ExercisePlanIntent.CancelEdit) },
                )
            }

            HInput(
                value = editing.draftRoutine,
                onValueChange = { onIntent(ExercisePlanIntent.UpdateDraft(it)) },
                placeholder = "Ej: Pecho + tríceps · 4 series · 12 reps",
                singleLine = false,
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            HButton(
                text = "SAVE",
                onClick = { onIntent(ExercisePlanIntent.SaveRoutine) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
