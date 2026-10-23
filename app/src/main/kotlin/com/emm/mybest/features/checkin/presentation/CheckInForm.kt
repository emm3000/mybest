package com.emm.mybest.features.checkin.presentation

import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.usecase.checkin.CheckInDraft
import kotlinx.datetime.LocalTime

private val DEFAULT_RETURN_TIME = LocalTime(2, 0)

data class CheckInForm(
    val spentInput: String = "",
    val drinksInput: String = "",
    val returnTime: LocalTime? = null,
    val mood: Int? = null,
    val isTimePickerOpen: Boolean = false,
) {
    val pickerTime: LocalTime get() = returnTime ?: DEFAULT_RETURN_TIME

    fun toDraft(): CheckInDraft = CheckInDraft(
        totalSpent = Money.parseOrNull(spentInput),
        drinks = drinksInput.toIntOrNull(),
        returnTime = returnTime,
        mood = mood,
    )

    fun isCompleteFor(kind: OutingKind): Boolean {
        val draft = toDraft()
        val hasCommonAnswers = draft.totalSpent != null && draft.mood != null
        return hasCommonAnswers && (kind == OutingKind.UNPLANNED || (draft.drinks != null && draft.returnTime != null))
    }
}
