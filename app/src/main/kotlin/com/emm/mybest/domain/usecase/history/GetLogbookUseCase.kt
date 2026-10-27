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

private data class LogbookBodyRecords(
    val weights: List<WeightEntry>,
    val photos: List<ProgressPhoto>,
)

private data class LogbookNightRecords(
    val outings: List<Outing>,
    val reasonsByDay: Map<LocalDate, Set<RelapseReason>>,
    val wonNights: List<WonNight>,
    val stayedInDays: Set<LocalDate>,
)

private data class LogbookMoneyRecords(
    val debts: List<Debt>,
    val payments: List<DebtPayment>,
    val contributions: List<GoalContribution>,
)

private data class LogbookMonthRecords(
    val body: LogbookBodyRecords,
    val nights: LogbookNightRecords,
    val returns: List<ReturnTime>,
    val money: LogbookMoneyRecords,
    val notes: List<DatedEntry>,
)

private data class DatedEntry(val appDay: LocalDate, val entry: LogbookEntry)

class GetLogbookUseCase(
    private val sources: LogbookSources,
) {

    /**
     * Emits the days of [month] that have a recorded event, newest app day first. Inside a day the order is fixed:
     * outing, won night, stayed in, return, weigh-in, photos, installments by debt name, contributions by id. A
     * return lands on its return app day, an installment on the payment date of its month, and daily checks are never
     * listed. Notes close the day, newest first, each with the local time it was written.
     */
    operator fun invoke(month: YearMonthValue): Flow<List<LogbookDay>> {
        val days = month.days()
        return combine(
            bodyRecords(),
            nightRecords(days),
            sources.observeReturnTimes(),
            moneyRecords(),
            noteEntries(days),
            ::LogbookMonthRecords,
        ).map { it.toDays(days) }
    }

    private fun bodyRecords(): Flow<LogbookBodyRecords> = combine(
        sources.body.weightRepository.getWeightProgress(),
        sources.body.photoRepository.getAllPhotos(),
        ::LogbookBodyRecords,
    )

    private fun nightRecords(days: ClosedRange<LocalDate>): Flow<LogbookNightRecords> {
        val outingRepository = sources.nights.outingRepository
        return combine(
            outingRepository.observeRange(days.start, days.endInclusive),
            sources.nights.observeRelapses().map { relapses -> relapses.associate { it.outing.appDay to it.reasons } },
            outingRepository.observeWonNights(days.start, days.endInclusive),
            outingRepository.observeNoOutingAnswers(days.start, days.endInclusive),
            ::LogbookNightRecords,
        )
    }

    private fun noteEntries(days: ClosedRange<LocalDate>): Flow<List<DatedEntry>> =
        sources.noteRepository.observeRange(days.start, days.endInclusive).map { notes ->
            notes.map { note ->
                val time = sources.appDayClock.localDateTimeOf(note.createdAt).time
                DatedEntry(note.appDay, LogbookEntry.NoteEntry(note, time))
            }
        }

    private fun moneyRecords(): Flow<LogbookMoneyRecords> = combine(
        sources.money.debtRepository.observeDebts(),
        sources.money.debtRepository.observePayments(),
        sources.money.purchaseGoalRepository.observeContributions(),
        ::LogbookMoneyRecords,
    )
}

private fun LogbookMonthRecords.toDays(days: ClosedRange<LocalDate>): List<LogbookDay> =
    datedEntries()
        .filter { it.appDay in days }
        .groupBy({ it.appDay }, { it.entry })
        .map { (appDay, entries) -> LogbookDay(appDay, entries) }
        .sortedByDescending { it.appDay }

private fun LogbookMonthRecords.datedEntries(): List<DatedEntry> =
    outingEntries() + wonNightEntries() + stayedInEntries() + returnEntries() +
        weighInEntries() + photoEntries() + installmentEntries() + contributionEntries() +
        notes

private fun LogbookMonthRecords.outingEntries(): List<DatedEntry> = nights.outings.map {
    DatedEntry(it.appDay, LogbookEntry.OutingEntry(it, nights.reasonsByDay[it.appDay].orEmpty()))
}

private fun LogbookMonthRecords.wonNightEntries(): List<DatedEntry> =
    nights.wonNights.map { DatedEntry(it.appDay, LogbookEntry.WonNightEntry(it)) }

private fun LogbookMonthRecords.stayedInEntries(): List<DatedEntry> =
    nights.stayedInDays.map { DatedEntry(it, LogbookEntry.StayedInEntry) }

private fun LogbookMonthRecords.returnEntries(): List<DatedEntry> = returns.mapNotNull { returnTime ->
    val returnDay = returnTime.returnAppDay ?: return@mapNotNull null
    val days = returnTime.days ?: return@mapNotNull null
    DatedEntry(returnDay, LogbookEntry.ReturnEntry(days))
}

private fun LogbookMonthRecords.weighInEntries(): List<DatedEntry> =
    body.weights.map { DatedEntry(it.date, LogbookEntry.WeighInEntry(it)) }

private fun LogbookMonthRecords.photoEntries(): List<DatedEntry> =
    body.photos.groupBy { it.date }.map { (date, photos) ->
        DatedEntry(date, LogbookEntry.PhotosEntry(photos.map { it.type }.sortedBy { it.ordinal }))
    }

private fun LogbookMonthRecords.installmentEntries(): List<DatedEntry> {
    val debtsById = money.debts.associateBy { it.id }
    return money.payments
        .mapNotNull { payment -> debtsById[payment.debtId]?.let { it to payment } }
        .sortedBy { (debt, _) -> debt.name }
        .map { (debt, payment) ->
            DatedEntry(debt.paymentDateIn(payment.month), LogbookEntry.InstallmentEntry(debt.name, payment.amount))
        }
}

private fun LogbookMonthRecords.contributionEntries(): List<DatedEntry> =
    money.contributions.sortedBy { it.id }.map { DatedEntry(it.appDay, LogbookEntry.ContributionEntry(it.amount)) }
