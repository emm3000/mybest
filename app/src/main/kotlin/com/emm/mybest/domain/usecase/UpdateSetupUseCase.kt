package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.repository.SetupRepository
import com.emm.mybest.domain.validation.SetupValidationError
import com.emm.mybest.domain.validation.SetupValidator

class UpdateSetupUseCase(
    private val setupRepository: SetupRepository,
) {
    /**
     * Saves [setup] when [SetupValidator] accepts it; otherwise saves nothing and returns the errors.
     */
    suspend operator fun invoke(setup: Setup): List<SetupValidationError> {
        val errors = SetupValidator.validate(setup)
        if (errors.isEmpty()) setupRepository.save(setup)
        return errors
    }
}
