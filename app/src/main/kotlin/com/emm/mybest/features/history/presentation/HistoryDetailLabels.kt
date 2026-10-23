package com.emm.mybest.features.history.presentation

import androidx.annotation.StringRes
import com.emm.mybest.R
import com.emm.mybest.domain.models.LimitKind
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.RelapseReason

@StringRes
internal fun HistoryMarkKind.legendRes(): Int = when (this) {
    HistoryMarkKind.CHECK -> R.string.history_legend_check
    HistoryMarkKind.OUTING -> R.string.history_legend_outing
    HistoryMarkKind.RELAPSE -> R.string.history_legend_relapse
    HistoryMarkKind.WON_NIGHT -> R.string.history_legend_won_night
    HistoryMarkKind.WEIGHT -> R.string.history_legend_weight
    HistoryMarkKind.PHOTO -> R.string.history_legend_photo
}

@StringRes
internal fun OutingKind.labelRes(): Int = when (this) {
    OutingKind.PLANNED -> R.string.history_detail_outing_planned
    OutingKind.UNPLANNED -> R.string.history_detail_outing_unplanned
}

@StringRes
internal fun RelapseReason.labelRes(): Int = when (this) {
    RelapseReason.BROKE_LIMIT -> R.string.history_detail_reason_broke_limit
    RelapseReason.OVER_MONTHLY_LIMIT -> R.string.history_detail_reason_over_monthly_limit
    RelapseReason.UNPLANNED -> R.string.history_detail_reason_unplanned
}

@StringRes
internal fun LimitKind.labelRes(): Int = when (this) {
    LimitKind.MONEY -> R.string.history_detail_limit_money
    LimitKind.DRINKS -> R.string.history_detail_limit_drinks
    LimitKind.RETURN_TIME -> R.string.history_detail_limit_return_time
}

@StringRes
internal fun checkAnswerRes(answer: CheckAnswer): Int = when (answer) {
    CheckAnswer.YES -> R.string.history_detail_check_yes
    CheckAnswer.NO -> R.string.history_detail_check_no
    CheckAnswer.UNRECORDED -> R.string.history_detail_check_unrecorded
}
