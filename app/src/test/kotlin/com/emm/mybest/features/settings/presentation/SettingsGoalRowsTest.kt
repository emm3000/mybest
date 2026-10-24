package com.emm.mybest.features.settings.presentation

import com.emm.mybest.R
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.Setup
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsGoalRowsTest {

    private val unset = SettingsRowValue.Resource(R.string.settings_goal_unset)

    @Test
    fun `every goal of a fresh setup reads unset`() {
        assertEquals(List(SettingsField.entries.size) { unset }, Setup().toGoalRows().map { it.value })
    }

    @Test
    fun `rows follow the field order with their labels`() {
        val rows = Setup().toGoalRows()

        assertEquals(SettingsField.entries, rows.map { it.field })
        assertEquals(R.string.settings_goal_target_weight_label, rows.first().labelRes)
    }

    @Test
    fun `set goals are formatted for display`() {
        val setup = Setup(
            targetWeightKg = 72.5f,
            monthlyOutingLimit = 3,
            typicalOutingCost = Money(12_050),
            weeklyTargets = mapOf(DailyCheckType.GYM to 4),
        )

        assertEquals(
            listOf(
                SettingsRowValue.Resource(R.string.settings_goal_target_weight_value, "72.5"),
                SettingsRowValue.Text("3"),
                SettingsRowValue.Text("S/ 120.50"),
                SettingsRowValue.Plural(R.plurals.settings_goal_days_per_week, 4),
                unset,
                unset,
                unset,
            ),
            setup.toGoalRows().map { it.value },
        )
    }

    @Test
    fun `a section keeps only its own rows in its own order`() {
        val rows = Setup().toGoalRows()

        assertEquals(
            SettingsSetupSection.OUTINGS.fields,
            SettingsSetupSection.OUTINGS.goalRowsIn(rows).map { it.field },
        )
    }
}
