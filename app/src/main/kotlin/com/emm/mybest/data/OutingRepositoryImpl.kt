package com.emm.mybest.data

import com.emm.mybest.data.entities.NoOutingAnswerDao
import com.emm.mybest.data.entities.NoOutingAnswerEntity
import com.emm.mybest.data.entities.OutingDao
import com.emm.mybest.data.entities.OutingEntity
import com.emm.mybest.data.entities.WonNightDao
import com.emm.mybest.data.mappers.toDomain
import com.emm.mybest.data.mappers.toEntity
import com.emm.mybest.domain.models.CheckIn
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.WonNight
import com.emm.mybest.domain.repository.OutingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

private const val NEW_OUTING_ID = 0L

@Suppress("TooManyFunctions")
class OutingRepositoryImpl(
    private val outingDao: OutingDao,
    private val wonNightDao: WonNightDao,
    private val noOutingAnswerDao: NoOutingAnswerDao,
) : OutingRepository {

    override fun observeAll(): Flow<List<Outing>> = outingDao.observeAll().toOutings()

    override fun observeRange(from: LocalDate, to: LocalDate): Flow<List<Outing>> =
        outingDao.observeRange(from, to).toOutings()

    override fun observePending(): Flow<List<Outing>> = outingDao.observePending().toOutings()

    override suspend fun recordPlanned(appDay: LocalDate, limits: Limits): Long =
        insert(newOuting(appDay, OutingKind.PLANNED, limits))

    override suspend fun recordUnplanned(appDay: LocalDate): Long =
        insert(newOuting(appDay, OutingKind.UNPLANNED, limits = null))

    override suspend fun recordUnplannedReplacingWonNight(appDay: LocalDate): Long =
        outingDao.insertReplacingWonNight(newOuting(appDay, OutingKind.UNPLANNED, limits = null).toEntity())

    override suspend fun saveCheckIn(outingId: Long, checkIn: CheckIn) {
        val stored = checkNotNull(outingDao.findById(outingId)) { "No outing with id $outingId" }
        outingDao.update(stored.toDomain().copy(checkIn = checkIn).toEntity())
    }

    override fun observeWonNights(from: LocalDate, to: LocalDate): Flow<List<WonNight>> =
        wonNightDao.observeRange(from, to).map { list -> list.map { it.toDomain() } }

    override suspend fun recordWonNight(wonNight: WonNight) {
        wonNightDao.upsert(wonNight.toEntity())
    }

    override suspend fun deleteWonNight(appDay: LocalDate) {
        wonNightDao.deleteByAppDay(appDay)
    }

    override fun observeNoOutingAnswers(from: LocalDate, to: LocalDate): Flow<Set<LocalDate>> =
        noOutingAnswerDao.observeRange(from, to).map { it.toSet() }

    override suspend fun recordNoOuting(appDay: LocalDate) {
        noOutingAnswerDao.insert(NoOutingAnswerEntity(appDay))
    }

    private suspend fun insert(outing: Outing): Long {
        val id = outingDao.insert(outing.toEntity())
        check(id != OutingDao.IGNORED_INSERT_ID) { "App day ${outing.appDay} already has an outing" }
        return id
    }

    private fun newOuting(appDay: LocalDate, kind: OutingKind, limits: Limits?): Outing =
        Outing(id = NEW_OUTING_ID, appDay = appDay, kind = kind, limits = limits, checkIn = null)

    private fun Flow<List<OutingEntity>>.toOutings(): Flow<List<Outing>> = map { list -> list.map { it.toDomain() } }
}
