package com.emm.mybest.data.mappers

import com.emm.mybest.core.money.Money
import com.emm.mybest.data.entities.OutingEntity
import com.emm.mybest.domain.models.CheckIn
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

private val FRIDAY = LocalDate(2026, 10, 23)

class OutingMappersTest {

    @Test
    fun `a planned outing with its check-in round-trips through the entity`() {
        val outing = Outing(
            id = 7,
            appDay = FRIDAY,
            kind = OutingKind.PLANNED,
            limits = Limits(money = Money(12_000), drinks = 4, returnTime = LocalTime(2, 0)),
            checkIn = CheckIn(totalSpent = Money(15_050), drinks = 5, returnTime = LocalTime(0, 15), mood = 2),
        )

        assertEquals(outing, outing.toEntity().toDomain())
    }

    @Test
    fun `a planned outing stores limits and check-in times as minutes since midnight`() {
        val outing = Outing(
            id = 7,
            appDay = FRIDAY,
            kind = OutingKind.PLANNED,
            limits = Limits(money = Money(12_000), drinks = 4, returnTime = LocalTime(2, 0)),
            checkIn = CheckIn(totalSpent = Money(15_050), drinks = null, returnTime = LocalTime(23, 30), mood = 2),
        )

        assertEquals(
            OutingEntity(
                id = 7,
                appDay = FRIDAY,
                kind = OutingKind.PLANNED,
                limitMoneyCents = 12_000,
                limitDrinks = 4,
                limitReturnMinutes = 120,
                spentCents = 15_050,
                drinks = null,
                returnMinutes = 1_410,
                mood = 2,
            ),
            outing.toEntity(),
        )
    }

    @Test
    fun `an unplanned outing pending its check-in round-trips through the entity`() {
        val outing = Outing(id = 3, appDay = FRIDAY, kind = OutingKind.UNPLANNED, limits = null, checkIn = null)

        assertEquals(outing, outing.toEntity().toDomain())
    }
}
