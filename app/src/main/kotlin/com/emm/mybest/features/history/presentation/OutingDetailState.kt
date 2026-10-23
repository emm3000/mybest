package com.emm.mybest.features.history.presentation

import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.RelapseReason
import com.emm.mybest.ui.components.LimitOutcomeState
import kotlinx.collections.immutable.ImmutableList

data class OutingDetailState(
    val kind: OutingKind,
    val isPendingCheckIn: Boolean,
    val spentLabel: String?,
    val mood: Int?,
    val outcomes: ImmutableList<LimitOutcomeState>,
    val relapseReasons: ImmutableList<RelapseReason>,
)
