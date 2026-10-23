package com.emm.mybest.data.mappers

import com.emm.mybest.core.money.Money
import com.emm.mybest.data.entities.UserSetupEntity
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.Setup
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class SetupMappersTest {

    private val fullSetup = Setup(
        targetWeightKg = 72.5f,
        monthlyOutingLimit = 2,
        typicalOutingCost = Money(18_050),
        weeklyTargets = mapOf(
            DailyCheckType.GYM to 4,
            DailyCheckType.DIET to 6,
            DailyCheckType.ENGLISH to 5,
            DailyCheckType.ALCOHOL to 0,
        ),
        preOutingReminderTime = LocalTime(19, 30),
        checkInReminderTime = LocalTime(10, 0),
        weighInDay = DayOfWeek.SUNDAY,
        weighInReminderTime = LocalTime(7, 15),
        isPhotoReminderEnabled = true,
        debtReminderTime = LocalTime(0, 5),
    )

    @Test
    fun `all-unset setup round-trips through the single setup row`() {
        val entity = Setup().toEntity()

        assertEquals(UserSetupEntity(id = 1), entity)
        assertEquals(Setup(), entity.toDomain())
    }

    @Test
    fun `full setup round-trips through the entity`() {
        assertEquals(fullSetup, fullSetup.toEntity().toDomain())
    }

    @Test
    fun `setup is stored as cents, minutes since midnight and ISO day number`() {
        val expected = UserSetupEntity(
            id = 1,
            targetWeightKg = 72.5f,
            monthlyOutingLimit = 2,
            typicalOutingCostCents = 18_050L,
            gymWeeklyTarget = 4,
            dietWeeklyTarget = 6,
            englishWeeklyTarget = 5,
            alcoholWeeklyTarget = 0,
            preOutingReminderMinutes = 1_170,
            checkInReminderMinutes = 600,
            weighInDay = 7,
            weighInReminderMinutes = 435,
            isPhotoReminderEnabled = true,
            debtReminderMinutes = 5,
        )

        assertEquals(expected, fullSetup.toEntity())
    }

    @Test
    fun `missing weekly target column maps to a missing key`() {
        val entity = UserSetupEntity(gymWeeklyTarget = 3, weighInDay = 1)

        val setup = entity.toDomain()

        assertEquals(mapOf(DailyCheckType.GYM to 3), setup.weeklyTargets)
        assertEquals(DayOfWeek.MONDAY, setup.weighInDay)
    }
}
