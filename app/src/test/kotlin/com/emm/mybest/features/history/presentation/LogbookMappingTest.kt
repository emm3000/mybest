package com.emm.mybest.features.history.presentation

import com.emm.mybest.R
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.CheckIn
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.models.Note
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.RelapseReason
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.models.WonNight
import com.emm.mybest.domain.usecase.history.LogbookDay
import com.emm.mybest.domain.usecase.history.LogbookEntry
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

private val TODAY = LocalDate(2026, 11, 10)
private val FRIDAY = LocalDate(2026, 11, 6)
private val LIMITS = Limits(money = Money(10_000), drinks = 3, returnTime = LocalTime(1, 0))

private fun res(resId: Int, vararg args: Any) = LogbookTextPart.Res(resId, args.toList())

private fun checkIn(spent: Long, mood: Int) = CheckIn(Money(spent), drinks = 2, returnTime = null, mood = mood)

private fun planned(checkIn: CheckIn?) = Outing(1, FRIDAY, OutingKind.PLANNED, LIMITS, checkIn)

private fun unplanned(checkIn: CheckIn?) = Outing(2, FRIDAY, OutingKind.UNPLANNED, null, checkIn)

private fun rowOf(entry: LogbookEntry): LogbookRowState =
    LogbookDay(FRIDAY, listOf(entry)).toState(TODAY).rows.single()

class LogbookMappingTest {

    @Test
    fun `a kept planned outing is an outing row with the limits kept`() {
        val row = rowOf(LogbookEntry.OutingEntry(planned(checkIn(9_000, mood = 4)), emptySet()))

        assertEquals(res(R.string.logbook_kind_outing), row.label)
        assertFalse(row.isRelapse)
        assertEquals(
            listOf(
                res(R.string.history_detail_outing_planned),
                res(R.string.logbook_spent, "S/ 90.00"),
                res(R.string.logbook_limits_kept),
                res(R.string.logbook_mood, 4),
            ),
            row.parts,
        )
    }

    @Test
    fun `a planned outing that broke a limit is a relapse row`() {
        val outing = planned(checkIn(15_000, mood = 2))

        val row = rowOf(LogbookEntry.OutingEntry(outing, setOf(RelapseReason.BROKE_LIMIT)))

        assertEquals(res(R.string.logbook_kind_relapse), row.label)
        assertTrue(row.isRelapse)
        assertEquals(
            listOf(
                res(R.string.history_detail_outing_planned),
                res(R.string.logbook_spent, "S/ 150.00"),
                res(R.string.history_detail_reason_broke_limit),
                res(R.string.logbook_mood, 2),
            ),
            row.parts,
        )
    }

    @Test
    fun `an unplanned relapse adds no unplanned reason part`() {
        val row = rowOf(LogbookEntry.OutingEntry(unplanned(checkIn(20_000, mood = 3)), setOf(RelapseReason.UNPLANNED)))

        assertEquals(
            listOf(
                res(R.string.history_detail_outing_unplanned),
                res(R.string.logbook_spent, "S/ 200.00"),
                res(R.string.logbook_mood, 3),
            ),
            row.parts,
        )
    }

    @Test
    fun `a pending planned outing shows the pending check-in and the monthly limit reason`() {
        val pending = rowOf(LogbookEntry.OutingEntry(planned(checkIn = null), emptySet()))
        val overLimit =
            rowOf(LogbookEntry.OutingEntry(planned(checkIn = null), setOf(RelapseReason.OVER_MONTHLY_LIMIT)))

        assertEquals(
            listOf(res(R.string.history_detail_outing_planned), res(R.string.history_detail_check_in_pending)),
            pending.parts,
        )
        assertEquals(
            listOf(
                res(R.string.history_detail_outing_planned),
                res(R.string.history_detail_check_in_pending),
                res(R.string.history_detail_reason_over_monthly_limit),
            ),
            overLimit.parts,
        )
    }

    @Test
    fun `a won night shows its estimate or says there is none`() {
        val estimated = rowOf(LogbookEntry.WonNightEntry(WonNight(FRIDAY, Money(10_000))))
        val unestimated = rowOf(LogbookEntry.WonNightEntry(WonNight(FRIDAY, null)))

        assertEquals(res(R.string.logbook_kind_won_night), estimated.label)
        assertEquals(
            listOf(res(R.string.logbook_won_night_stayed), res(R.string.logbook_won_night_estimate, "S/ 100.00")),
            estimated.parts,
        )
        assertEquals(
            listOf(res(R.string.logbook_won_night_stayed), res(R.string.history_detail_won_night_no_estimate)),
            unestimated.parts,
        )
    }

    @Test
    fun `stayed in, return, weigh-in, photos, installment and contribution rows`() {
        assertEquals(listOf(res(R.string.logbook_stayed_in)), rowOf(LogbookEntry.StayedInEntry).parts)
        assertEquals(
            listOf(LogbookTextPart.Plural(R.plurals.logbook_return_days, 3)),
            rowOf(LogbookEntry.ReturnEntry(3)).parts,
        )
        assertEquals(
            listOf(LogbookTextPart.Text("79.9 kg · ayuno")),
            rowOf(LogbookEntry.WeighInEntry(WeightEntry("w", FRIDAY, 79.9f, note = "ayuno"))).parts,
        )
        assertEquals(
            listOf(res(R.string.photo_type_trunk), res(R.string.photo_type_face)),
            rowOf(LogbookEntry.PhotosEntry(listOf(PhotoType.TRUNK, PhotoType.FACE))).parts,
        )
        assertEquals(
            listOf(LogbookTextPart.Text("Banco"), LogbookTextPart.Text("S/ 400.00")),
            rowOf(LogbookEntry.InstallmentEntry("Banco", Money(40_000))).parts,
        )
        assertEquals(
            listOf(res(R.string.logbook_contribution, "S/ 100.00")),
            rowOf(LogbookEntry.ContributionEntry(Money(10_000))).parts,
        )
    }

    @Test
    fun `non-outing rows carry their own label`() {
        assertEquals(res(R.string.logbook_kind_stayed_in), rowOf(LogbookEntry.StayedInEntry).label)
        assertEquals(res(R.string.logbook_kind_return), rowOf(LogbookEntry.ReturnEntry(1)).label)
        assertEquals(
            res(R.string.logbook_kind_installment),
            rowOf(LogbookEntry.InstallmentEntry("Banco", Money(1))).label,
        )
        assertEquals(res(R.string.logbook_kind_contribution), rowOf(LogbookEntry.ContributionEntry(Money(1))).label)
    }

    @Test
    fun `the date label and the today flag come from the app day`() {
        val friday = LogbookDay(FRIDAY, emptyList()).toState(TODAY)
        val today = LogbookDay(TODAY, emptyList()).toState(TODAY)

        assertEquals("06 NOV · VIE", friday.dateLabel)
        assertFalse(friday.isToday)
        assertTrue(today.isToday)
    }

    @Test
    fun `a note row has the time as label, the full text and is not a relapse`() {
        val text = "Volví a las 2\ny dormí bien"
        val note = Note(7, FRIDAY, Instant.fromEpochSeconds(0), text)

        val row = rowOf(LogbookEntry.NoteEntry(note, LocalTime(2, 30)))

        assertEquals(7L, row.noteId)
        assertEquals(LogbookTextPart.Text("02:30"), row.label)
        assertEquals(listOf(LogbookTextPart.Text(text)), row.parts)
        assertFalse(row.isRelapse)
    }

    @Test
    fun `event rows have no note id`() {
        assertEquals(null, rowOf(LogbookEntry.StayedInEntry).noteId)
    }
}
