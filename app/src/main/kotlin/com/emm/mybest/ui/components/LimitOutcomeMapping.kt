package com.emm.mybest.ui.components

import com.emm.mybest.core.money.Money
import com.emm.mybest.core.money.formatPen
import com.emm.mybest.domain.models.LimitKind
import com.emm.mybest.domain.models.LimitResult

fun LimitResult.toOutcome(): LimitOutcomeState = when {
    exceededBy == 0L -> LimitOutcomeState.Kept(kind)
    kind == LimitKind.MONEY -> LimitOutcomeState.MoneyExceeded(Money(exceededBy).formatPen())
    kind == LimitKind.DRINKS -> LimitOutcomeState.DrinksExceeded(exceededBy.toInt())
    else -> LimitOutcomeState.ReturnTimeExceeded(exceededBy.toInt())
}
