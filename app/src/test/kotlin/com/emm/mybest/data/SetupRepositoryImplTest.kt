package com.emm.mybest.data

import com.emm.mybest.data.entities.UserSetupDao
import com.emm.mybest.data.entities.UserSetupEntity
import com.emm.mybest.domain.models.Setup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SetupRepositoryImplTest {

    private val storedRow = MutableStateFlow<UserSetupEntity?>(null)
    private val dao = object : UserSetupDao {
        override suspend fun upsert(setup: UserSetupEntity) {
            storedRow.value = setup
        }

        override fun observe(): Flow<UserSetupEntity?> = storedRow
    }
    private val repository = SetupRepositoryImpl(dao)

    @Test
    fun `observe falls back to an all-unset setup when no row exists`() = runTest {
        assertEquals(Setup(), repository.observe().first())
    }

    @Test
    fun `observe returns the setup saved through the repository`() = runTest {
        val setup = Setup(targetWeightKg = 72.5f, monthlyOutingLimit = 3)

        repository.save(setup)

        assertEquals(setup, repository.observe().first())
    }
}
