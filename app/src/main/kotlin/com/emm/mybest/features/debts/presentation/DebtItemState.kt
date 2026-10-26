package com.emm.mybest.features.debts.presentation

data class DebtItemState(
    val id: Long,
    val name: String,
    val balanceLabel: String,
    val installmentLabel: String,
    val paymentDay: Int,
    val isPaidThisMonth: Boolean,
    val canPay: Boolean,
)
