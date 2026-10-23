package com.emm.mybest.ui.components

import com.emm.mybest.domain.models.LimitKind

sealed interface LimitOutcomeState {
    val kind: LimitKind

    data class Kept(override val kind: LimitKind) : LimitOutcomeState

    data class MoneyExceeded(val amountLabel: String) : LimitOutcomeState {
        override val kind: LimitKind = LimitKind.MONEY
    }

    data class DrinksExceeded(val count: Int) : LimitOutcomeState {
        override val kind: LimitKind = LimitKind.DRINKS
    }

    data class ReturnTimeExceeded(val minutes: Int) : LimitOutcomeState {
        override val kind: LimitKind = LimitKind.RETURN_TIME
    }
}
