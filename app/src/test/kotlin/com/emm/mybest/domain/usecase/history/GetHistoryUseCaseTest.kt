package com.emm.mybest.domain.usecase.history

import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.WeightEntry
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetHistoryUseCaseTest {

    private val month = YearMonthValue(2026, 5)

    private val date1 = LocalDate(2026, 5, 10)
    private val date2 = LocalDate(2026, 5, 7)
    private val date3 = LocalDate(2026, 5, 1)
    private val outOfMonthDate = LocalDate(2026, 4, 30)

    private val weight1 = WeightEntry(id = "w1", date = date1, weight = 78.2f)
    private val weight2 = WeightEntry(id = "w2", date = date2, weight = 78.5f)
    private val photo1 = ProgressPhoto(
        id = "p1",
        date = date2,
        type = PhotoType.TRUNK,
        photoPath = "/tmp/trunk.jpg",
        createdAt = 1L,
    )
    private val outOfMonthWeight = WeightEntry(id = "wout", date = outOfMonthDate, weight = 80f)

    @Test
    fun `buildDaySummaryMap returns empty map for no data`() {
        val result = buildDaySummaryMap(emptyList(), emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `buildDaySummaryMap groups weight correctly`() {
        val result = buildDaySummaryMap(listOf(weight1), emptyList())
        assertEquals(1, result.size)
        assertEquals(weight1, result[date1]?.weight)
    }

    @Test
    fun `buildDaySummaryMap groups photo correctly`() {
        val result = buildDaySummaryMap(emptyList(), listOf(photo1))
        assertEquals(1, result.size)
        assertEquals(listOf(photo1), result[date2]?.photos)
    }

    @Test
    fun `buildDaySummaryMap merges weight and photo on same date`() {
        val sameDate = date2
        val result = buildDaySummaryMap(listOf(weight2), listOf(photo1))
        val summary = result[sameDate]
        assertEquals(weight2, summary?.weight)
        assertEquals(listOf(photo1), summary?.photos)
    }

    @Test
    fun `monthWeightCount counts only weights in month`() {
        val weights = listOf(weight1, weight2, outOfMonthWeight)
        val monthlyData = buildDaySummaryMap(weights, emptyList())
        val monthDates = (1..month.lengthOfMonth()).map { month.atDay(it) }.toSet()
        val count = weights.count { it.date in monthDates }
        assertEquals(2, count)
    }

    @Test
    fun `buildRecentEntries are ordered descending by date`() {
        val weights = listOf(weight1, weight2)
        val monthlyData = buildDaySummaryMap(weights, emptyList())
        val monthDates = (1..month.lengthOfMonth()).map { month.atDay(it) }.toSet()

        val entries = monthDates
            .mapNotNull { date ->
                val summary = monthlyData[date] ?: return@mapNotNull null
                if (!summary.hasActivity) return@mapNotNull null
                HistoryRecentEntry(
                    date = date,
                    weight = summary.weight?.weight,
                    photoTypes = summary.photos.map { it.type }.sortedBy(PhotoType::ordinal),
                )
            }
            .sortedByDescending { it.date }

        assertEquals(2, entries.size)
        assertTrue(entries[0].date > entries[1].date)
        assertEquals(date1, entries[0].date)
        assertEquals(date2, entries[1].date)
    }

    @Test
    fun `buildRecentEntries excludes days with no activity`() {
        val weights = listOf(weight1)
        val monthlyData = buildDaySummaryMap(weights, emptyList())
        val monthDates = (1..month.lengthOfMonth()).map { month.atDay(it) }.toSet()

        val entries = monthDates
            .mapNotNull { date ->
                val summary = monthlyData[date] ?: return@mapNotNull null
                if (!summary.hasActivity) return@mapNotNull null
                HistoryRecentEntry(
                    date = date,
                    weight = summary.weight?.weight,
                    photoTypes = emptyList(),
                )
            }
            .sortedByDescending { it.date }

        assertEquals(1, entries.size)
        assertEquals(date1, entries[0].date)
    }

    @Test
    fun `HistoryRecentEntry weight is null when day has only photo`() {
        val monthlyData = buildDaySummaryMap(emptyList(), listOf(photo1))
        val summary = monthlyData[date2]
        val entry = HistoryRecentEntry(
            date = date2,
            weight = summary?.weight?.weight,
            photoTypes = summary?.photos?.map { it.type } ?: emptyList(),
        )
        assertNull(entry.weight)
        assertEquals(listOf(PhotoType.TRUNK), entry.photoTypes)
    }

    @Test
    fun `DaySummary hasActivity is true when weight and photo present`() {
        val summary = DaySummary(date = date1, weight = weight1, photos = listOf(photo1))
        assertTrue(summary.hasActivity)
    }

    @Test
    fun `DaySummary hasActivity is false for empty summary`() {
        val summary = DaySummary(date = date3)
        assertTrue(!summary.hasActivity)
    }
}
