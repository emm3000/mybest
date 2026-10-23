package com.emm.mybest.features.debts.presentation

sealed interface DebtsEffect {
    data object ActionFailed : DebtsEffect
}
