package com.emm.mybest.features.goal.presentation

import com.emm.mybest.core.datetime.formatEsWeekdayDayMonth
import com.emm.mybest.core.money.Money
import com.emm.mybest.core.money.formatPen
import com.emm.mybest.domain.models.GoalContribution
import com.emm.mybest.domain.models.GoalProgress
import com.emm.mybest.domain.validation.PurchaseGoalDraft

private const val PERCENT_SCALE = 100f

internal fun GoalProgress.toSummaryState(): GoalSummaryState = GoalSummaryState(
    name = goal.name,
    nameLabel = goal.name.uppercase(),
    priceLabel = goal.price.formatPen(),
    contributedLabel = contributed.formatPen(),
    leftLabel = left.formatPen(),
    percent = percent,
    progressFraction = percent / PERCENT_SCALE,
    contributions = contributions.map { it.toItemState() },
    priceInput = goal.price.toInput(),
)

internal fun GoalContribution.toItemState(): ContributionItemState = ContributionItemState(
    id = id,
    dayLabel = appDay.formatEsWeekdayDayMonth(),
    amountLabel = amount.formatPen(),
)

internal fun GoalFormState.toDraft(): PurchaseGoalDraft = PurchaseGoalDraft(
    name = name,
    price = Money.parseOrNull(price),
)

internal fun GoalFormState.withValue(field: GoalFormField, value: String): GoalFormState {
    val edited = when (field) {
        GoalFormField.NAME -> copy(name = value)
        GoalFormField.PRICE -> copy(price = value)
    }
    return edited.copy(errors = emptySet())
}

internal fun GoalSummaryState.toEditFormState(): GoalFormState = GoalFormState(
    name = name,
    price = priceInput,
    isEditing = true,
)

private fun Money.toInput(): String {
    val soles = cents / Money.CENTS_PER_SOL
    val fraction = cents % Money.CENTS_PER_SOL
    return if (fraction == 0L) "$soles" else "$soles.%02d".format(fraction)
}
