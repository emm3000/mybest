package com.emm.mybest.testing

import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.repository.SetupRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class SlowSetupRepository(private val saveDelayMs: Long) : SetupRepository {
    private val current = MutableStateFlow(Setup())

    override fun observe(): Flow<Setup> = current

    override suspend fun save(setup: Setup) {
        delay(saveDelayMs)
        current.value = setup
    }
}
