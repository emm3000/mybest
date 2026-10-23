package com.emm.mybest.features.summary.presentation

import com.emm.mybest.R
import com.emm.mybest.core.money.formatPen
import com.emm.mybest.domain.models.GoalProgress
import com.emm.mybest.domain.models.WeekDebt
import com.emm.mybest.domain.models.WeekMoney
import com.emm.mybest.ui.theme.AtelierInk

internal fun WeekMoney.toMoneySection(): SummarySectionState = SummarySectionState(
    titleRes = R.string.summary_section_money,
    rows = debt.toRows() + goal.toRows() + plainLine(R.string.summary_money_not_spent, notSpentEstimated.formatPen()),
)

private fun WeekDebt.toRows(): List<SummaryRowState> {
    if (debtCount == 0) return listOf(SummaryRowState.SetupPrompt(SummaryText(R.string.summary_money_debts_setup)))
    return listOf(
        plainLine(R.string.summary_money_debt_balance, totalBalance.formatPen()),
        plainLine(R.string.summary_money_installments_paid, installmentsPaidThisMonth.toString()),
    )
}

private fun GoalProgress?.toRows(): List<SummaryRowState> {
    if (this == null) return listOf(SummaryRowState.SetupPrompt(SummaryText(R.string.summary_money_goal_setup)))
    return listOf(
        SummaryRowState.Line(
            label = SummaryText(R.string.summary_money_goal_label, listOf(goal.name)),
            value = SummaryText(R.string.summary_money_goal_value, listOf(contributed.formatPen(), percent)),
            valueColor = AtelierInk,
        ),
    )
}
