package com.emm.mybest.testing

import com.emm.mybest.domain.models.CheckIn
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.WonNight
import com.emm.mybest.domain.repository.OutingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class FakeOutingRepository(
    initialOutings: List<Outing> = emptyList(),
    initialWonNights: List<WonNight> = emptyList(),
) : OutingRepository {

    private val outings = MutableStateFlow(initialOutings)
    private val wonNights = MutableStateFlow(initialWonNights)

    override fun observeAll(): Flow<List<Outing>> = outings

    override fun observeRange(from: LocalDate, to: LocalDate): Flow<List<Outing>> =
        outings.map { all -> all.filter { it.appDay in from..to } }

    override fun observePending(): Flow<List<Outing>> = outings.map { all -> all.filter { it.isPendingCheckIn } }

    override suspend fun recordPlanned(appDay: LocalDate, limits: Limits): Long =
        add(Outing(nextId(), appDay, OutingKind.PLANNED, limits, checkIn = null))

    override suspend fun recordUnplanned(appDay: LocalDate): Long =
        add(Outing(nextId(), appDay, OutingKind.UNPLANNED, limits = null, checkIn = null))

    override suspend fun saveCheckIn(outingId: Long, checkIn: CheckIn) {
        outings.value = outings.value.map { if (it.id == outingId) it.copy(checkIn = checkIn) else it }
    }

    override fun observeWonNights(from: LocalDate, to: LocalDate): Flow<List<WonNight>> =
        wonNights.map { all -> all.filter { it.appDay in from..to } }

    override suspend fun recordWonNight(wonNight: WonNight) {
        wonNights.value = wonNights.value.filterNot { it.appDay == wonNight.appDay } + wonNight
    }

    override suspend fun deleteWonNight(appDay: LocalDate) {
        wonNights.value = wonNights.value.filterNot { it.appDay == appDay }
    }

    override fun observeNoOutingAnswers(from: LocalDate, to: LocalDate): Flow<Set<LocalDate>> =
        MutableStateFlow(emptySet())

    override suspend fun recordNoOuting(appDay: LocalDate) = Unit

    private fun nextId(): Long = outings.value.size + 1L

    private fun add(outing: Outing): Long {
        outings.value = outings.value + outing
        return outing.id
    }
}
