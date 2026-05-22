package com.emm.mybest.features.diet.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.emm.mybest.core.datetime.shortEs
import com.emm.mybest.domain.models.MealType
import com.emm.mybest.features.home.presentation.labelEs
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierDone
import com.emm.mybest.ui.theme.AtelierInk
import com.emm.mybest.ui.theme.AtelierInkTertiary
import com.emm.mybest.ui.theme.AtelierMonoFamily
import com.emm.mybest.ui.theme.AtelierSerifFamily

private val DAY_COL_WIDTH = 38.dp
private val ROW_PADDING_HORIZONTAL = 28.dp
private val ROW_PADDING_VERTICAL = 16.dp
private val ROW_GAP = 18.dp
private val MEAL_ROWS_GAP = 8.dp
private val MEAL_LABEL_WIDTH = 64.dp
private val MEAL_LABEL_GAP = 12.dp
private val DAY_ABBR_FONT_SIZE = 22.sp
private val DAY_ABBR_LINE_HEIGHT = 22.sp
private val DAY_INDEX_FONT_SIZE = 9.5.sp
private val DAY_INDEX_MARGIN_TOP = 4.dp
private val DAY_INDEX_LETTER_SPACING = 0.18.em
private val MEAL_DESC_FONT_SIZE = 12.5.sp
private val MEAL_DESC_LINE_HEIGHT = 17.5.sp
private val MEAL_LABEL_FONT_SIZE = 9.sp
private val DAY_COL_PADDING_TOP = 4.dp

private val MEAL_TYPE_ORDER = listOf(
    MealType.BREAKFAST,
    MealType.LUNCH,
    MealType.SNACK,
    MealType.DINNER,
)

@Composable
internal fun MealPlanDayRow(
    content: MealPlanDayContent,
    onMealClick: (MealType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ROW_PADDING_HORIZONTAL, vertical = ROW_PADDING_VERTICAL),
        horizontalArrangement = Arrangement.spacedBy(ROW_GAP),
    ) {
        DayIndexColumn(content)
        MealsColumn(
            meals = content.meals,
            onMealClick = onMealClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DayIndexColumn(content: MealPlanDayContent) {
    val isToday = content.day == content.today.dayOfWeek
    val dayColor = if (isToday) AtelierDone else AtelierInk
    val indexLabel = (content.index + 1).toString().padStart(2, '0')
    Column(
        modifier = Modifier
            .width(DAY_COL_WIDTH)
            .padding(top = DAY_COL_PADDING_TOP),
    ) {
        Text(
            text = content.day.shortEs(),
            style = TextStyle(
                fontFamily = AtelierSerifFamily,
                fontSize = DAY_ABBR_FONT_SIZE,
                lineHeight = DAY_ABBR_LINE_HEIGHT,
                color = dayColor,
            ),
        )
        Text(
            text = indexLabel,
            modifier = Modifier.padding(top = DAY_INDEX_MARGIN_TOP),
            style = TextStyle(
                fontFamily = AtelierMonoFamily,
                fontSize = DAY_INDEX_FONT_SIZE,
                letterSpacing = DAY_INDEX_LETTER_SPACING,
                color = AtelierInkTertiary,
            ),
        )
    }
}

@Composable
private fun MealsColumn(
    meals: Map<MealType, String>,
    onMealClick: (MealType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MEAL_ROWS_GAP),
    ) {
        MEAL_TYPE_ORDER.forEach { type ->
            MealRow(
                type = type,
                description = meals[type].orEmpty(),
                onMealClick = onMealClick,
            )
        }
    }
}

@Composable
private fun MealRow(
    type: MealType,
    description: String,
    onMealClick: (MealType) -> Unit,
) {
    val hasDescription = description.isNotBlank()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMealClick(type) },
        horizontalArrangement = Arrangement.spacedBy(MEAL_LABEL_GAP),
    ) {
        MicroLabel(
            text = type.labelEs(),
            modifier = Modifier.width(MEAL_LABEL_WIDTH).alignByBaseline(),
            style = MicroLabelStyle(tone = MicroLabelTone.Dim, size = MEAL_LABEL_FONT_SIZE),
        )
        Text(
            text = if (hasDescription) description else "—",
            modifier = Modifier.weight(1f).alignByBaseline(),
            style = TextStyle(
                fontSize = MEAL_DESC_FONT_SIZE,
                lineHeight = MEAL_DESC_LINE_HEIGHT,
                color = if (hasDescription) AtelierInk else AtelierInkTertiary,
            ),
        )
    }
}
