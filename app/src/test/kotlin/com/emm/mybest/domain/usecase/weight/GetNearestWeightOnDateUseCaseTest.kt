package com.emm.mybest.domain.usecase.weight

import com.emm.mybest.domain.models.WeightEntry
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private fun makeEntry(id: String, day: Int, weight: Float): WeightEntry = WeightEntry(
    id = id,
    date = LocalDate(2026, 1, day),
    weight = weight,
)

class GetNearestWeightOnDateUseCaseTest {

    @Test
    fun `returns null when entry list is empty`() {
        val lookup = NearestWeightLookup(emptyList())
        assertNull(lookup.nearest(LocalDate(2026, 1, 15)))
    }

    @Test
    fun `returns exact date match`() {
        val entry = makeEntry("e1", 15, 80f)
        val lookup = NearestWeightLookup(listOf(entry))
        assertEquals(80f, lookup.nearest(LocalDate(2026, 1, 15)))
    }

    @Test
    fun `returns weight within 7 day window`() {
        val entry = makeEntry("e1", 15, 80f)
        val lookup = NearestWeightLookup(listOf(entry))
        assertEquals(80f, lookup.nearest(LocalDate(2026, 1, 22)))
    }

    @Test
    fun `returns null outside 7 day window`() {
        val entry = makeEntry("e1", 1, 80f)
        val lookup = NearestWeightLookup(listOf(entry))
        assertNull(lookup.nearest(LocalDate(2026, 1, 9)))
    }

    @Test
    fun `returns closest entry when multiple entries exist`() {
        val entries = listOf(
            makeEntry("e1", 10, 82f),
            makeEntry("e2", 15, 80f),
            makeEntry("e3", 20, 78f),
        )
        val lookup = NearestWeightLookup(entries)
        assertEquals(80f, lookup.nearest(LocalDate(2026, 1, 16)))
    }

    @Test
    fun `boundary condition — exactly 7 days away returns value`() {
        val entry = makeEntry("e1", 15, 80f)
        val lookup = NearestWeightLookup(listOf(entry))
        assertEquals(80f, lookup.nearest(LocalDate(2026, 1, 8)))
    }

    @Test
    fun `boundary condition — 8 days away returns null`() {
        val entry = makeEntry("e1", 15, 80f)
        val lookup = NearestWeightLookup(listOf(entry))
        assertNull(lookup.nearest(LocalDate(2026, 1, 7)))
    }
}
