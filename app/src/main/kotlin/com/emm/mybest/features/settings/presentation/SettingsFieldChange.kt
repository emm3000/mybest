package com.emm.mybest.features.settings.presentation

import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.Setup

internal fun SettingsField.inputFor(setup: Setup): String = when (this) {
    SettingsField.TARGET_WEIGHT -> setup.targetWeightKg?.toString()?.removeSuffix(".0")
    SettingsField.MONTHLY_OUTING_LIMIT -> setup.monthlyOutingLimit?.toString()
    SettingsField.TYPICAL_OUTING_COST -> setup.typicalOutingCost?.toInput()
    else -> setup.weeklyTargets[checkType]?.toString()
}.orEmpty()

internal fun SettingsField.cleared(setup: Setup): Setup = when (this) {
    SettingsField.TARGET_WEIGHT -> setup.copy(targetWeightKg = null)
    SettingsField.MONTHLY_OUTING_LIMIT -> setup.copy(monthlyOutingLimit = null)
    SettingsField.TYPICAL_OUTING_COST -> setup.copy(typicalOutingCost = null)
    else -> checkType?.let { setup.copy(weeklyTargets = setup.weeklyTargets - it) } ?: setup
}

internal fun SettingsField.withInput(setup: Setup, input: String): Setup? = when (this) {
    SettingsField.TARGET_WEIGHT -> setup.withWeight(input)
    SettingsField.MONTHLY_OUTING_LIMIT -> setup.withLimit(input)
    SettingsField.TYPICAL_OUTING_COST -> setup.withCost(input)
    else -> checkType?.let { setup.withWeeklyTarget(it, input) }
}

private fun Setup.withWeight(input: String): Setup? =
    input.trim().replace(',', '.').toFloatOrNull()?.let { copy(targetWeightKg = it) }

private fun Setup.withLimit(input: String): Setup? = input.trim().toIntOrNull()?.let { copy(monthlyOutingLimit = it) }

private fun Setup.withCost(input: String): Setup? = Money.parseOrNull(input)?.let { copy(typicalOutingCost = it) }

private fun Setup.withWeeklyTarget(type: DailyCheckType, input: String): Setup? =
    input.trim().toIntOrNull()?.let { copy(weeklyTargets = weeklyTargets + (type to it)) }

private fun Money.toInput(): String {
    val soles = cents / Money.CENTS_PER_SOL
    val fraction = cents % Money.CENTS_PER_SOL
    return if (fraction == 0L) "$soles" else "$soles.%02d".format(fraction)
}
