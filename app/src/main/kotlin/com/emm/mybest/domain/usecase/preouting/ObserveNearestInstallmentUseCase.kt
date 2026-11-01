package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.domain.models.InstallmentShare
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveNearestInstallmentUseCase(
    private val observeInstallmentInputs: ObserveInstallmentInputsUseCase,
    private val getEstimatedOutingCost: GetEstimatedOutingCostUseCase,
) {

    /**
     * Emits the installment with the earliest payment date on or after today among the debts with a balance, or null
     * without them. A debt's installment falls on its due month, or one month later when that month is already paid;
     * a payment day past the end of a month falls on its last day.
     */
    operator fun invoke(): Flow<InstallmentShare?> = combine(
        observeInstallmentInputs(),
        getEstimatedOutingCost(),
    ) { inputs, estimate ->
        inputs.nearestShare(estimate.cost)
    }
}
