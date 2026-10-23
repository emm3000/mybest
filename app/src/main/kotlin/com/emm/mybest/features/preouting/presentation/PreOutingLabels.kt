package com.emm.mybest.features.preouting.presentation

import androidx.annotation.StringRes
import com.emm.mybest.R

@get:StringRes
internal val PreOutingPrompt.labelRes: Int
    get() = when (this) {
        PreOutingPrompt.TYPICAL_COST -> R.string.pre_outing_prompt_typical_cost
        PreOutingPrompt.DEBTS -> R.string.pre_outing_prompt_debts
        PreOutingPrompt.PURCHASE_GOAL -> R.string.pre_outing_prompt_purchase_goal
        PreOutingPrompt.OUTING_LIMIT -> R.string.pre_outing_prompt_outing_limit
    }

@get:StringRes
internal val PreOutingState.estimateCaptionRes: Int
    get() = if (isEstimateAveraged) R.string.pre_outing_estimate_caption else R.string.pre_outing_estimate_caption_typical
