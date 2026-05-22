package com.emm.mybest.features.exercise.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.domain.models.ExercisePlanEntry
import com.emm.mybest.features.exercise.presentation.edit.EditExerciseSheet
import com.emm.mybest.features.exercise.presentation.edit.EditExerciseSheetCallbacks
import com.emm.mybest.ui.components.AtelierAppBar
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierBackground
import kotlinx.datetime.DayOfWeek

@Composable
fun ExercisePlanScreen(
    viewModel: ExercisePlanViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    ExercisePlanContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
private fun ExercisePlanContent(
    state: ExercisePlanState,
    onIntent: (ExercisePlanIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.consumeWindowInsets(WindowInsets.navigationBars),
        containerColor = AtelierBackground,
        topBar = {
            AtelierAppBar(
                title = stringResource(R.string.exercise_plan_app_bar_title),
                actions = {
                    MicroLabel(
                        text = stringResource(R.string.exercise_plan_app_bar_right),
                        style = MicroLabelStyle(tone = MicroLabelTone.Dim),
                    )
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            ExercisePlanHero()
            Hairline()
            DayOfWeek.entries.forEachIndexed { index, day ->
                ExercisePlanDayRow(
                    content = ExercisePlanDayContent(
                        day = day,
                        today = state.today,
                        entry = state.entries[day] ?: ExercisePlanEntry(day),
                    ),
                    onClick = { onIntent(ExercisePlanIntent.StartEdit(day)) },
                )
                if (index < DayOfWeek.entries.lastIndex) {
                    Hairline(inset = 28.dp)
                }
            }
        }
    }
    state.editing?.let { draft ->
        EditExerciseSheet(
            draft = draft,
            callbacks = EditExerciseSheetCallbacks(
                onNameChange = { onIntent(ExercisePlanIntent.UpdateName(it)) },
                onDetailChange = { onIntent(ExercisePlanIntent.UpdateDetail(it)) },
                onVolumeChange = { onIntent(ExercisePlanIntent.UpdateVolume(it)) },
                onSave = { onIntent(ExercisePlanIntent.SaveRoutine) },
                onCancel = { onIntent(ExercisePlanIntent.CancelEdit) },
            ),
        )
    }
}
