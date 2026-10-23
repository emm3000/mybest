package com.emm.mybest.domain.usecase.checkin

import com.emm.mybest.domain.models.CHECK_IN_MOOD_RANGE
import com.emm.mybest.domain.models.CheckIn
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.repository.OutingRepository

class SaveCheckInUseCase(
    private val outingRepository: OutingRepository,
) {

    /**
     * Stores [draft] as the check-in of [outing]. Nothing is stored when the mood is outside 1 to 5, the spend is
     * missing or negative, or a planned outing lacks its drinks or return time. An unplanned outing keeps only the
     * spend and the mood.
     */
    suspend operator fun invoke(outing: Outing, draft: CheckInDraft): SaveCheckInResult {
        rejection(outing.kind, draft)?.let { return it }
        val checkIn = draft.toCheckIn(outing.kind) ?: return SaveCheckInResult.MissingSpend
        outingRepository.saveCheckIn(outing.id, checkIn)
        return SaveCheckInResult.Saved(outing.copy(checkIn = checkIn))
    }

    private fun rejection(kind: OutingKind, draft: CheckInDraft): SaveCheckInResult? =
        commonRejection(draft) ?: plannedRejection(kind, draft)

    private fun commonRejection(draft: CheckInDraft): SaveCheckInResult? = when {
        draft.mood !in CHECK_IN_MOOD_RANGE -> SaveCheckInResult.InvalidMood
        draft.totalSpent == null -> SaveCheckInResult.MissingSpend
        draft.totalSpent.isNegative -> SaveCheckInResult.NegativeSpend
        else -> null
    }

    private fun plannedRejection(kind: OutingKind, draft: CheckInDraft): SaveCheckInResult? = when {
        kind != OutingKind.PLANNED -> null
        draft.drinks == null -> SaveCheckInResult.MissingDrinks
        draft.returnTime == null -> SaveCheckInResult.MissingReturnTime
        else -> null
    }

    private fun CheckInDraft.toCheckIn(kind: OutingKind): CheckIn? {
        val spent = totalSpent ?: return null
        val mood = mood ?: return null
        val isPlanned = kind == OutingKind.PLANNED
        return CheckIn(spent, drinks.takeIf { isPlanned }, returnTime.takeIf { isPlanned }, mood)
    }
}
