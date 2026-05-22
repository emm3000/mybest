package com.emm.mybest.domain.validation

object WeightInputValidator {
    const val MIN_WEIGHT_KG = 20f
    const val MAX_WEIGHT_KG = 500f

    private val pattern = Regex("""^\d+([.,]\d{0,2})?$""")

    fun isValidWeightInput(raw: String): Boolean = raw.isNotBlank() && pattern.matches(raw)

    fun isWeightInRange(value: Float): Boolean = value in MIN_WEIGHT_KG..MAX_WEIGHT_KG

    fun parse(raw: String): Float? = raw.replace(',', '.').toFloatOrNull()
}
