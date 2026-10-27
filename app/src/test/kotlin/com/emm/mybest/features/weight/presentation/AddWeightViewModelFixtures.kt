package com.emm.mybest.features.weight.presentation

import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.models.WeighInTrend
import com.emm.mybest.domain.usecase.weight.ObserveWeighInTrendUseCase
import com.emm.mybest.domain.usecase.weight.SaveWeightUseCase
import com.emm.mybest.testing.FakeSetupRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

internal class AddWeightViewModelFixture {

    val saveWeightUseCase = mockk<SaveWeightUseCase>(relaxed = true)
    private val observeWeighInTrendUseCase = mockk<ObserveWeighInTrendUseCase>(relaxed = true)

    fun buildViewModel(
        trend: WeighInTrend = WeighInTrend(),
        setup: Setup = Setup(),
    ): AddWeightViewModel {
        every { observeWeighInTrendUseCase() } returns flowOf(trend)
        return AddWeightViewModel(saveWeightUseCase, observeWeighInTrendUseCase, FakeSetupRepository(setup))
    }
}

internal fun newAddWeightFixture() = AddWeightViewModelFixture()
