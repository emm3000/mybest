package com.emm.mybest.data.mappers

import com.emm.mybest.core.money.Money
import com.emm.mybest.data.entities.DebtEntity
import com.emm.mybest.data.entities.DebtPaymentEntity
import com.emm.mybest.domain.models.Debt
import com.emm.mybest.domain.models.DebtPayment

fun Debt.toEntity(): DebtEntity = DebtEntity(
    id = id,
    name = name,
    balanceCents = balance.cents,
    installmentCents = installment.cents,
    paymentDay = paymentDay,
)

fun DebtEntity.toDomain(): Debt = Debt(
    id = id,
    name = name,
    balance = Money(balanceCents),
    installment = Money(installmentCents),
    paymentDay = paymentDay,
)

fun DebtPayment.toEntity(): DebtPaymentEntity = DebtPaymentEntity(
    debtId = debtId,
    month = month,
    amountCents = amount.cents,
)

fun DebtPaymentEntity.toDomain(): DebtPayment = DebtPayment(
    debtId = debtId,
    month = month,
    amount = Money(amountCents),
)
