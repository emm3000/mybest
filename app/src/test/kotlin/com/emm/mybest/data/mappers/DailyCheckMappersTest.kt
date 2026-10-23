package com.emm.mybest.data.mappers

import com.emm.mybest.data.entities.DailyCheckEntity
import com.emm.mybest.domain.models.DailyCheck
import com.emm.mybest.domain.models.DailyCheckType
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyCheckMappersTest {

    private val appDay = LocalDate(2026, 10, 23)

    @Test
    fun `daily check maps to its entity`() {
        val check = DailyCheck(appDay = appDay, type = DailyCheckType.ALCOHOL, isDone = true)

        assertEquals(DailyCheckEntity(appDay = appDay, type = DailyCheckType.ALCOHOL, isDone = true), check.toEntity())
    }

    @Test
    fun `every daily check type round-trips through the entity`() {
        DailyCheckType.entries.forEach { type ->
            val check = DailyCheck(appDay = appDay, type = type, isDone = type.ordinal % 2 == 0)

            assertEquals(check, check.toEntity().toDomain())
        }
    }
}
