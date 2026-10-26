package com.emm.mybest.features.summary.presentation

import androidx.annotation.StringRes
import com.emm.mybest.R
import com.emm.mybest.core.money.formatPen
import com.emm.mybest.domain.models.GoalProgress
import com.emm.mybest.domain.models.WeekDebt
import com.emm.mybest.domain.models.WeekMoney
import com.emm.mybest.ui.theme.AtelierInk

internal fun WeekMoney.toMoneySection(isCurrentWeek: Boolean): SummarySectionState = SummarySectionState(
    titleRes = R.string.summary_section_money,
    rows = debt.toRows(isCurrentWeek) + goal.toRows() +
        plainLine(R.string.summary_money_not_spent, notSpentEstimated.formatPen()),
)

private val DEBTS_SETUP_PROMPT = SummaryRowState.SetupPrompt(
    SummaryText(R.string.summary_money_debts_setup),
    SummaryDestination.DEBTS,
)

private val GOAL_SETUP_PROMPT = SummaryRowState.SetupPrompt(
    SummaryText(R.string.summary_money_goal_setup),
    SummaryDestination.PURCHASE_GOAL,
)

private fun WeekDebt.toRows(isCurrentWeek: Boolean): List<SummaryRowState> {
    if (debtCount == 0) return listOf(DEBTS_SETUP_PROMPT)
    return listOf(
        plainLine(balanceLabelOf(isCurrentWeek), totalBalance.formatPen()),
        plainLine(R.string.summary_money_installments_paid, installmentsPaidThisMonth.toString()),
    )
}

@StringRes
private fun balanceLabelOf(isCurrentWeek: Boolean): Int =
    if (isCurrentWeek) R.string.summary_money_debt_balance else R.string.summary_money_debt_balance_today

private fun GoalProgress?.toRows(): List<SummaryRowState> {
    if (this == null) return listOf(GOAL_SETUP_PROMPT)
    return listOf(
        SummaryRowState.Line(
            label = SummaryText(R.string.summary_money_goal_label, listOf(goal.name)),
            value = SummaryText(R.string.summary_money_goal_value, listOf(contributed.formatPen(), percent)),
            valueColor = AtelierInk,
            destination = SummaryDestination.PURCHASE_GOAL,
        ),
    )
}
