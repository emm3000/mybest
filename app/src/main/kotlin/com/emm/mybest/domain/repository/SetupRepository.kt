package com.emm.mybest.domain.repository

import com.emm.mybest.domain.models.Setup
import kotlinx.coroutines.flow.Flow

interface SetupRepository {
    /**
     * Emits the stored setup, or an all-unset [Setup] while the user has saved nothing yet.
     */
    fun observe(): Flow<Setup>

    suspend fun save(setup: Setup)
}
