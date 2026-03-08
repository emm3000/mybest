package com.emm.mybest.domain.validation

import com.emm.mybest.domain.models.HabitType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitValidatorTest {

    @Test
    fun `validateName should return false when name is blank`() {
        val result = HabitValidator.validateName("  ")
        assertFalse(result.isValid)
    }

    @Test
    fun `validateName should return false when name is too short`() {
        val result = HabitValidator.validateName("Ab")
        assertFalse(result.isValid)
    }

    @Test
    fun `validateName should return true when name is valid`() {
        val result = HabitValidator.validateName("Beber agua")
        assertTrue(result.isValid)
    }

    @Test
    fun `validateGoal should return false when value is 0 and type is METRIC`() {
        val result = HabitValidator.validateGoal(HabitType.METRIC, 0f)
        assertFalse(result.isValid)
    }

    @Test
    fun `validateGoal should return true when type is BOOLEAN regardless of value`() {
        val result = HabitValidator.validateGoal(HabitType.BOOLEAN, 0f)
        assertTrue(result.isValid)
    }
}
