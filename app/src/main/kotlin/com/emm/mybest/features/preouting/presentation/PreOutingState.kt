package com.emm.mybest.features.preouting.presentation

data class PreOutingState(
    val isLoaded: Boolean = false,
    val spentOnOutingsLabel: String = "",
    val estimatedOutingCostLabel: String? = null,
    val isEstimateAveraged: Boolean = false,
    val debtName: String = "",
    val paymentDay: Int = 0,
    val nightPercent: Int? = null,
    val goalName: String? = null,
    val goalLeftLabel: String? = null,
    val outingsThisMonth: Int = 0,
    val monthlyOutingLimit: Int? = null,
    val isNextOutingOverLimit: Boolean = false,
    val notSpentLabel: String = "",
    val prompts: Set<PreOutingPrompt> = emptySet(),
)
