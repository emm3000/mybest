package com.emm.mybest.features.diet.presentation.edit

import com.emm.mybest.domain.models.DailySlot
import com.emm.mybest.domain.models.MealType

fun MealType.toDailySlot(): DailySlot = when (this) {
    MealType.BREAKFAST -> DailySlot.BREAKFAST
    MealType.LUNCH -> DailySlot.LUNCH
    MealType.SNACK -> DailySlot.SNACK
    MealType.DINNER -> DailySlot.DINNER
}
