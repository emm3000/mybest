package com.emm.mybest.data

import com.emm.mybest.data.entities.UserSetupDao
import com.emm.mybest.data.mappers.toDomain
import com.emm.mybest.data.mappers.toEntity
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.repository.SetupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SetupRepositoryImpl(
    private val dao: UserSetupDao,
) : SetupRepository {

    override fun observe(): Flow<Setup> = dao.observe().map { it?.toDomain() ?: Setup() }

    override suspend fun save(setup: Setup) {
        dao.upsert(setup.toEntity())
    }
}
