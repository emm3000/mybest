package com.emm.mybest.data.mappers

import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.GoalContribution
import com.emm.mybest.domain.models.PurchaseGoal
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class PurchaseGoalMappersTest {

    @Test
    fun `a purchase goal round-trips through the entity`() {
        val goal = PurchaseGoal(name = "Bicicleta", price = Money(150_000))

        assertEquals(goal, goal.toEntity().toDomain())
    }

    @Test
    fun `a goal contribution round-trips through the entity`() {
        val contribution = GoalContribution(id = 7, appDay = LocalDate(2026, 10, 23), amount = Money(5_050))

        assertEquals(contribution, contribution.toEntity().toDomain())
    }
}
