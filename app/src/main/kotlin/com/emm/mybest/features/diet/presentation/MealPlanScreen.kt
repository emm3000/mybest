package com.emm.mybest.features.diet.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emm.mybest.R
import com.emm.mybest.features.diet.presentation.edit.EditMealSheet
import com.emm.mybest.features.diet.presentation.edit.EditMealSheetCallbacks
import com.emm.mybest.ui.components.AtelierAppBar
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierBackground
import com.emm.mybest.ui.theme.AtelierInk
import com.emm.mybest.ui.theme.AtelierInkTertiary
import com.emm.mybest.ui.theme.AtelierSerifFamily
import kotlinx.datetime.DayOfWeek

private val HERO_PADDING_TOP = 24.dp
private val HERO_PADDING_BOTTOM = 22.dp
private val HERO_MARGIN_TOP = 4.dp
private val HERO_PADDING_HORIZONTAL = 28.dp
private val HERO_FONT_SIZE = 56.sp
private val HERO_LINE_HEIGHT = 53.2.sp

@Composable
fun MealPlanScreen(
    viewModel: MealPlanViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MealPlanContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
private fun MealPlanContent(
    state: MealPlanState,
    onIntent: (MealPlanIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AtelierBackground,
        topBar = {
            AtelierAppBar(
                title = stringResource(R.string.meal_plan_app_bar_title),
                actions = {
                    MicroLabel(
                        text = stringResource(R.string.meal_plan_app_bar_right),
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
            MealPlanHero()
            Hairline()
            DayOfWeek.entries.forEachIndexed { index, day ->
                MealPlanDayRow(
                    content = MealPlanDayContent(
                        day = day,
                        index = index,
                        today = state.today,
                        meals = state.entries[day] ?: emptyMap(),
                    ),
                    onMealClick = { type -> onIntent(MealPlanIntent.StartEdit(day, type)) },
                )
                if (index < DayOfWeek.entries.lastIndex) {
                    Hairline(inset = 28.dp)
                }
            }
        }
    }
    state.editing?.let { draft ->
        EditMealSheet(
            draft = draft,
            callbacks = EditMealSheetCallbacks(
                onDescriptionChange = { onIntent(MealPlanIntent.UpdateDraft(it)) },
                onSave = { onIntent(MealPlanIntent.SaveMeal) },
                onCancel = { onIntent(MealPlanIntent.CancelEdit) },
            ),
        )
    }
}

@Composable
private fun MealPlanHero() {
    Column(
        modifier = Modifier.padding(
            start = HERO_PADDING_HORIZONTAL,
            end = HERO_PADDING_HORIZONTAL,
            top = HERO_PADDING_TOP,
            bottom = HERO_PADDING_BOTTOM,
        ),
    ) {
        MicroLabel(
            text = stringResource(R.string.meal_plan_hero_label),
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
        )
        Text(
            text = stringResource(R.string.meal_plan_hero_line1),
            modifier = Modifier.padding(top = HERO_MARGIN_TOP),
            style = TextStyle(
                fontFamily = AtelierSerifFamily,
                fontStyle = FontStyle.Italic,
                fontSize = HERO_FONT_SIZE,
                lineHeight = HERO_LINE_HEIGHT,
                color = AtelierInk,
            ),
        )
        Text(
            text = stringResource(R.string.meal_plan_hero_line2),
            style = TextStyle(
                fontFamily = AtelierSerifFamily,
                fontSize = HERO_FONT_SIZE,
                lineHeight = HERO_LINE_HEIGHT,
                color = AtelierInkTertiary,
            ),
        )
    }
}
