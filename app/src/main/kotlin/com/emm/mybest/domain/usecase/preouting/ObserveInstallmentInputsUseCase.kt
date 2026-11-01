package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.domain.models.InstallmentInputs
import com.emm.mybest.domain.repository.DebtRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

class ObserveInstallmentInputsUseCase(
    private val debtRepository: DebtRepository,
    private val appDayClock: AppDayClock,
) {

    /**
     * Emits every stored debt and installment payment together with today's app day, reading each source once.
     */
    operator fun invoke(): Flow<InstallmentInputs> = appDayClock.observeToday().flatMapLatest { today ->
        combine(debtRepository.observeDebts(), debtRepository.observePayments()) { debts, payments ->
            InstallmentInputs(debts, payments, today)
        }
    }
}
