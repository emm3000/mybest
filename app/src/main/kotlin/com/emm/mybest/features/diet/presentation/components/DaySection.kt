package com.emm.mybest.features.diet.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emm.mybest.domain.models.MealType
import com.emm.mybest.ui.theme.StarlinkTextStyles
import kotlinx.datetime.DayOfWeek

private const val MEAL_LABEL_WEIGHT = 0.35f
private const val MEAL_DESCRIPTION_WEIGHT = 0.65f

@Composable
fun DaySection(
    day: DayOfWeek,
    meals: Map<MealType, String>,
    onMealClick: (MealType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Text(
            text = day.name,
            style = StarlinkTextStyles.sectionLabel,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        MealType.entries.forEach { type ->
            MealRow(
                mealType = type,
                description = meals[type].orEmpty(),
                onClick = { onMealClick(type) },
            )
        }
    }
}

@Composable
private fun MealRow(
    mealType: MealType,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = mealType.name,
            style = StarlinkTextStyles.chipLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(MEAL_LABEL_WEIGHT),
        )
        val hasContent = description.isNotEmpty()
        Text(
            text = if (hasContent) description else "—",
            style = MaterialTheme.typography.bodyLarge,
            color = if (hasContent) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.weight(MEAL_DESCRIPTION_WEIGHT),
        )
    }
}
