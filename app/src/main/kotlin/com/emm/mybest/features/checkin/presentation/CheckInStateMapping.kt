package com.emm.mybest.features.checkin.presentation

import com.emm.mybest.core.datetime.MONTH_ABBR_ES
import com.emm.mybest.core.datetime.formatHhMm
import com.emm.mybest.core.datetime.shortEs
import com.emm.mybest.core.money.Money
import com.emm.mybest.core.money.formatPen
import com.emm.mybest.domain.models.LimitKind
import com.emm.mybest.domain.models.LimitResult
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.usecase.checkin.CheckInTask
import kotlinx.datetime.LocalDate

internal fun toCheckInState(task: CheckInTask?, form: CheckInForm, saved: Outing?): CheckInState = when {
    saved != null -> saved.toResultState()
    task == null -> CheckInState()
    else -> task.toState(form)
}

private fun CheckInTask.toState(form: CheckInForm): CheckInState = when (this) {
    is CheckInTask.PendingOuting -> outing.toFormState(form)
    is CheckInTask.AskUnplanned -> CheckInState(
        isLoaded = true,
        step = CheckInStep.QUESTION,
        dayLabel = eveningAppDay.toDayLabel(),
    )
    CheckInTask.NoTask -> CheckInState(isLoaded = true)
}

private fun Outing.toFormState(form: CheckInForm): CheckInState = CheckInState(
    isLoaded = true,
    step = CheckInStep.FORM,
    dayLabel = appDay.toDayLabel(),
    limits = limits?.toState(),
    form = form,
    returnTimeLabel = form.returnTime?.formatHhMm(),
    canSave = form.isCompleteFor(kind),
)

private fun Outing.toResultState(): CheckInState = CheckInState(
    isLoaded = true,
    step = CheckInStep.RESULT,
    dayLabel = appDay.toDayLabel(),
    limits = limits?.toState(),
    outcomes = limitResults().map(LimitResult::toOutcome),
)

private fun Limits.toState(): CheckInLimitsState = CheckInLimitsState(
    moneyLabel = money.formatPen(),
    drinksLabel = drinks.toString(),
    returnTimeLabel = returnTime.formatHhMm(),
)

private fun LimitResult.toOutcome(): LimitOutcomeState = when {
    exceededBy == 0L -> LimitOutcomeState.Kept(kind)
    kind == LimitKind.MONEY -> LimitOutcomeState.MoneyExceeded(Money(exceededBy).formatPen())
    kind == LimitKind.DRINKS -> LimitOutcomeState.DrinksExceeded(exceededBy.toInt())
    else -> LimitOutcomeState.ReturnTimeExceeded(exceededBy.toInt())
}

private fun LocalDate.toDayLabel(): String =
    "${dayOfWeek.shortEs()} · $day ${MONTH_ABBR_ES.getValue(month.ordinal + 1)}"
