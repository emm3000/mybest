package com.emm.mybest.domain.usecase.history

import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.Debt
import com.emm.mybest.domain.models.DebtPayment
import com.emm.mybest.domain.models.GoalContribution
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.RelapseReason
import com.emm.mybest.domain.models.ReturnTime
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.models.WonNight
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

private data class BitacoraBodyRecords(
    val weights: List<WeightEntry>,
    val photos: List<ProgressPhoto>,
)

private data class BitacoraNightRecords(
    val outings: List<Outing>,
    val reasonsByDay: Map<LocalDate, Set<RelapseReason>>,
    val wonNights: List<WonNight>,
    val stayedInDays: Set<LocalDate>,
)

private data class BitacoraMoneyRecords(
    val debts: List<Debt>,
    val payments: List<DebtPayment>,
    val contributions: List<GoalContribution>,
)

private data class BitacoraMonthRecords(
    val body: BitacoraBodyRecords,
    val nights: BitacoraNightRecords,
    val returns: List<ReturnTime>,
    val money: BitacoraMoneyRecords,
    val notes: List<DatedEntry>,
)

private data class DatedEntry(val appDay: LocalDate, val entry: BitacoraEntry)

class GetBitacoraUseCase(
    private val sources: BitacoraSources,
) {

    /**
     * Emits the days of [month] that have a recorded event, newest app day first. Inside a day the order is fixed:
     * outing, won night, stayed in, return, weigh-in, photos, installments by debt name, contributions by id. A
     * return lands on its return app day, an installment on the payment date of its month, and daily checks are never
     * listed. Notes close the day, newest first, each with the local time it was written.
     */
    operator fun invoke(month: YearMonthValue): Flow<List<BitacoraDay>> {
        val days = month.days()
        return combine(
            bodyRecords(),
            nightRecords(days),
            sources.observeReturnTimes(),
            moneyRecords(),
            noteEntries(days),
            ::BitacoraMonthRecords,
        ).map { it.toDays(days) }
    }

    private fun bodyRecords(): Flow<BitacoraBodyRecords> = combine(
        sources.body.weightRepository.getWeightProgress(),
        sources.body.photoRepository.getAllPhotos(),
        ::BitacoraBodyRecords,
    )

    private fun nightRecords(days: ClosedRange<LocalDate>): Flow<BitacoraNightRecords> {
        val outingRepository = sources.nights.outingRepository
        return combine(
            outingRepository.observeRange(days.start, days.endInclusive),
            sources.nights.observeRelapses().map { relapses -> relapses.associate { it.outing.appDay to it.reasons } },
            outingRepository.observeWonNights(days.start, days.endInclusive),
            outingRepository.observeNoOutingAnswers(days.start, days.endInclusive),
            ::BitacoraNightRecords,
        )
    }

    private fun noteEntries(days: ClosedRange<LocalDate>): Flow<List<DatedEntry>> =
        sources.noteRepository.observeRange(days.start, days.endInclusive).map { notes ->
            notes.map { note ->
                val time = sources.appDayClock.localDateTimeOf(note.createdAt).time
                DatedEntry(note.appDay, BitacoraEntry.NoteEntry(note, time))
            }
        }

    private fun moneyRecords(): Flow<BitacoraMoneyRecords> = combine(
        sources.money.debtRepository.observeDebts(),
        sources.money.debtRepository.observePayments(),
        sources.money.purchaseGoalRepository.observeContributions(),
        ::BitacoraMoneyRecords,
    )
}

private fun BitacoraMonthRecords.toDays(days: ClosedRange<LocalDate>): List<BitacoraDay> =
    datedEntries()
        .filter { it.appDay in days }
        .groupBy({ it.appDay }, { it.entry })
        .map { (appDay, entries) -> BitacoraDay(appDay, entries) }
        .sortedByDescending { it.appDay }

private fun BitacoraMonthRecords.datedEntries(): List<DatedEntry> =
    outingEntries() + wonNightEntries() + stayedInEntries() + returnEntries() +
        weighInEntries() + photoEntries() + installmentEntries() + contributionEntries() +
        notes

private fun BitacoraMonthRecords.outingEntries(): List<DatedEntry> = nights.outings.map {
    DatedEntry(it.appDay, BitacoraEntry.OutingEntry(it, nights.reasonsByDay[it.appDay].orEmpty()))
}

private fun BitacoraMonthRecords.wonNightEntries(): List<DatedEntry> =
    nights.wonNights.map { DatedEntry(it.appDay, BitacoraEntry.WonNightEntry(it)) }

private fun BitacoraMonthRecords.stayedInEntries(): List<DatedEntry> =
    nights.stayedInDays.map { DatedEntry(it, BitacoraEntry.StayedInEntry) }

private fun BitacoraMonthRecords.returnEntries(): List<DatedEntry> = returns.mapNotNull { returnTime ->
    val returnDay = returnTime.returnAppDay ?: return@mapNotNull null
    val days = returnTime.days ?: return@mapNotNull null
    DatedEntry(returnDay, BitacoraEntry.ReturnEntry(days))
}

private fun BitacoraMonthRecords.weighInEntries(): List<DatedEntry> =
    body.weights.map { DatedEntry(it.date, BitacoraEntry.WeighInEntry(it)) }

private fun BitacoraMonthRecords.photoEntries(): List<DatedEntry> =
    body.photos.groupBy { it.date }.map { (date, photos) ->
        DatedEntry(date, BitacoraEntry.PhotosEntry(photos.map { it.type }.sortedBy { it.ordinal }))
    }

private fun BitacoraMonthRecords.installmentEntries(): List<DatedEntry> {
    val debtsById = money.debts.associateBy { it.id }
    return money.payments
        .mapNotNull { payment -> debtsById[payment.debtId]?.let { it to payment } }
        .sortedBy { (debt, _) -> debt.name }
        .map { (debt, payment) ->
            DatedEntry(debt.paymentDateIn(payment.month), BitacoraEntry.InstallmentEntry(debt.name, payment.amount))
        }
}

private fun BitacoraMonthRecords.contributionEntries(): List<DatedEntry> =
    money.contributions.sortedBy { it.id }.map { DatedEntry(it.appDay, BitacoraEntry.ContributionEntry(it.amount)) }
