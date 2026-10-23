package com.emm.mybest.testing

import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.repository.SetupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSetupRepository(initial: Setup = Setup()) : SetupRepository {
    private val current = MutableStateFlow(initial)
    val saved = mutableListOf<Setup>()

    override fun observe(): Flow<Setup> = current

    override suspend fun save(setup: Setup) {
        saved += setup
        current.value = setup
    }
}
