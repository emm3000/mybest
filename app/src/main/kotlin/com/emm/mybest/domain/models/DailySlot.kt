package com.emm.mybest.domain.models

/**
 * The five rows shown in the daily diary on Home — four meals plus one
 * exercise slot. Distinct from [MealType] because the meal-plan domain only
 * tracks food slots; EXERCISE belongs to the exercise plan but renders in the
 * same daily list at presentation time.
 */
enum class DailySlot { BREAKFAST, LUNCH, SNACK, DINNER, EXERCISE }
