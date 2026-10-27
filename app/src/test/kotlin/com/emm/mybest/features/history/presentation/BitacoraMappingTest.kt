package com.emm.mybest.features.history.presentation

import com.emm.mybest.R
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.CheckIn
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.RelapseReason
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.models.WonNight
import com.emm.mybest.domain.usecase.history.BitacoraDay
import com.emm.mybest.domain.usecase.history.BitacoraEntry
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private val TODAY = LocalDate(2026, 11, 10)
private val FRIDAY = LocalDate(2026, 11, 6)
private val LIMITS = Limits(money = Money(10_000), drinks = 3, returnTime = LocalTime(1, 0))

private fun res(resId: Int, vararg args: Any) = BitacoraTextPart.Res(resId, args.toList())

private fun checkIn(spent: Long, mood: Int) = CheckIn(Money(spent), drinks = 2, returnTime = null, mood = mood)

private fun planned(checkIn: CheckIn?) = Outing(1, FRIDAY, OutingKind.PLANNED, LIMITS, checkIn)

private fun unplanned(checkIn: CheckIn?) = Outing(2, FRIDAY, OutingKind.UNPLANNED, null, checkIn)

private fun rowOf(entry: BitacoraEntry): BitacoraRowState =
    BitacoraDay(FRIDAY, listOf(entry)).toState(TODAY).rows.single()

class BitacoraMappingTest {

    @Test
    fun `a kept planned outing is an outing row with the limits kept`() {
        val row = rowOf(BitacoraEntry.OutingEntry(planned(checkIn(9_000, mood = 4)), emptySet()))

        assertEquals(R.string.bitacora_kind_outing, row.labelRes)
        assertFalse(row.isRelapse)
        assertEquals(
            listOf(
                res(R.string.history_detail_outing_planned),
                res(R.string.bitacora_spent, "S/ 90.00"),
                res(R.string.bitacora_limits_kept),
                res(R.string.bitacora_mood, 4),
            ),
            row.parts,
        )
    }

    @Test
    fun `a planned outing that broke a limit is a relapse row`() {
        val outing = planned(checkIn(15_000, mood = 2))

        val row = rowOf(BitacoraEntry.OutingEntry(outing, setOf(RelapseReason.BROKE_LIMIT)))

        assertEquals(R.string.bitacora_kind_relapse, row.labelRes)
        assertTrue(row.isRelapse)
        assertEquals(
            listOf(
                res(R.string.history_detail_outing_planned),
                res(R.string.bitacora_spent, "S/ 150.00"),
                res(R.string.history_detail_reason_broke_limit),
                res(R.string.bitacora_mood, 2),
            ),
            row.parts,
        )
    }

    @Test
    fun `an unplanned relapse adds no unplanned reason part`() {
        val row = rowOf(BitacoraEntry.OutingEntry(unplanned(checkIn(20_000, mood = 3)), setOf(RelapseReason.UNPLANNED)))

        assertEquals(
            listOf(
                res(R.string.history_detail_outing_unplanned),
                res(R.string.bitacora_spent, "S/ 200.00"),
                res(R.string.bitacora_mood, 3),
            ),
            row.parts,
        )
    }

    @Test
    fun `a pending planned outing shows the pending check-in and the monthly limit reason`() {
        val pending = rowOf(BitacoraEntry.OutingEntry(planned(checkIn = null), emptySet()))
        val overLimit =
            rowOf(BitacoraEntry.OutingEntry(planned(checkIn = null), setOf(RelapseReason.OVER_MONTHLY_LIMIT)))

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
        val estimated = rowOf(BitacoraEntry.WonNightEntry(WonNight(FRIDAY, Money(10_000))))
        val unestimated = rowOf(BitacoraEntry.WonNightEntry(WonNight(FRIDAY, null)))

        assertEquals(R.string.bitacora_kind_won_night, estimated.labelRes)
        assertEquals(
            listOf(res(R.string.bitacora_won_night_stayed), res(R.string.bitacora_won_night_estimate, "S/ 100.00")),
            estimated.parts,
        )
        assertEquals(
            listOf(res(R.string.bitacora_won_night_stayed), res(R.string.history_detail_won_night_no_estimate)),
            unestimated.parts,
        )
    }

    @Test
    fun `stayed in, return, weigh-in, photos, installment and contribution rows`() {
        assertEquals(listOf(res(R.string.bitacora_stayed_in)), rowOf(BitacoraEntry.StayedInEntry).parts)
        assertEquals(
            listOf(BitacoraTextPart.Plural(R.plurals.bitacora_return_days, 3)),
            rowOf(BitacoraEntry.ReturnEntry(3)).parts,
        )
        assertEquals(
            listOf(BitacoraTextPart.Text("79.9 kg · ayuno")),
            rowOf(BitacoraEntry.WeighInEntry(WeightEntry("w", FRIDAY, 79.9f, note = "ayuno"))).parts,
        )
        assertEquals(
            listOf(res(R.string.photo_type_trunk), res(R.string.photo_type_face)),
            rowOf(BitacoraEntry.PhotosEntry(listOf(PhotoType.TRUNK, PhotoType.FACE))).parts,
        )
        assertEquals(
            listOf(BitacoraTextPart.Text("Banco"), BitacoraTextPart.Text("S/ 400.00")),
            rowOf(BitacoraEntry.InstallmentEntry("Banco", Money(40_000))).parts,
        )
        assertEquals(
            listOf(res(R.string.bitacora_contribution, "S/ 100.00")),
            rowOf(BitacoraEntry.ContributionEntry(Money(10_000))).parts,
        )
    }

    @Test
    fun `non-outing rows carry their own label`() {
        assertEquals(R.string.bitacora_kind_stayed_in, rowOf(BitacoraEntry.StayedInEntry).labelRes)
        assertEquals(R.string.bitacora_kind_return, rowOf(BitacoraEntry.ReturnEntry(1)).labelRes)
        assertEquals(
            R.string.bitacora_kind_installment,
            rowOf(BitacoraEntry.InstallmentEntry("Banco", Money(1))).labelRes,
        )
        assertEquals(R.string.bitacora_kind_contribution, rowOf(BitacoraEntry.ContributionEntry(Money(1))).labelRes)
    }

    @Test
    fun `the date label and the today flag come from the app day`() {
        val friday = BitacoraDay(FRIDAY, emptyList()).toState(TODAY)
        val today = BitacoraDay(TODAY, emptyList()).toState(TODAY)

        assertEquals("06 NOV · VIE", friday.dateLabel)
        assertFalse(friday.isToday)
        assertTrue(today.isToday)
    }
}
