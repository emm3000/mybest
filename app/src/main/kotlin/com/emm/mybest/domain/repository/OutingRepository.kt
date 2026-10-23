package com.emm.mybest.domain.repository

import com.emm.mybest.domain.models.CheckIn
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.WonNight
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

/**
 * Stores outings with their check-ins, won nights and the "did you go out last night? no" answers, all keyed by app
 * day. Every range is inclusive of both ends.
 */
@Suppress("TooManyFunctions")
interface OutingRepository {
    /**
     * Emits every outing, oldest app day first.
     */
    fun observeAll(): Flow<List<Outing>>

    /**
     * Emits the outings from [from] to [to], both app days included, oldest first.
     */
    fun observeRange(from: LocalDate, to: LocalDate): Flow<List<Outing>>

    /**
     * Emits the outings still pending their check-in, oldest first.
     */
    fun observePending(): Flow<List<Outing>>

    /**
     * Records a planned outing on [appDay] with its [limits] and returns its id.
     *
     * @throws IllegalStateException when [appDay] already has an outing.
     */
    suspend fun recordPlanned(appDay: LocalDate, limits: Limits): Long

    /**
     * Records an unplanned outing on [appDay], pending its check-in, and returns its id.
     *
     * @throws IllegalStateException when [appDay] already has an outing.
     */
    suspend fun recordUnplanned(appDay: LocalDate): Long

    /**
     * Records an unplanned outing on [appDay], pending its check-in, and removes the won night on [appDay] in one
     * step, so either both happen or neither does. Returns the outing id.
     *
     * @throws IllegalStateException when [appDay] already has an outing; the won night is then kept.
     */
    suspend fun recordUnplannedReplacingWonNight(appDay: LocalDate): Long

    /**
     * Saves [checkIn] on the outing with [outingId], replacing any earlier one.
     *
     * @throws IllegalStateException when no outing has [outingId].
     */
    suspend fun saveCheckIn(outingId: Long, checkIn: CheckIn)

    /**
     * Emits the won nights from [from] to [to], both app days included, oldest first.
     */
    fun observeWonNights(from: LocalDate, to: LocalDate): Flow<List<WonNight>>

    /**
     * Records [wonNight], replacing any won night already on its app day.
     */
    suspend fun recordWonNight(wonNight: WonNight)

    /**
     * Removes the won night on [appDay], if any.
     */
    suspend fun deleteWonNight(appDay: LocalDate)

    /**
     * Emits the app days from [from] to [to], both included, on which the user answered that they did not go out.
     */
    fun observeNoOutingAnswers(from: LocalDate, to: LocalDate): Flow<Set<LocalDate>>

    /**
     * Records that the user did not go out on the night of [appDay]; recording it again changes nothing.
     */
    suspend fun recordNoOuting(appDay: LocalDate)

    /**
     * Whether [appDay] already holds an outing or a won night.
     */
    suspend fun hasDecisionOn(appDay: LocalDate): Boolean =
        observeRange(appDay, appDay).first().isNotEmpty() || observeWonNights(appDay, appDay).first().isNotEmpty()
}
