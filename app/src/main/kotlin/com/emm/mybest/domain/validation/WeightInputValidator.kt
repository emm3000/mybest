package com.emm.mybest.domain.validation

object WeightInputValidator {
    private val pattern = Regex("""^\d+([.,]\d{0,2})?$""")

    fun isValidWeightInput(raw: String): Boolean = raw.isNotBlank() && pattern.matches(raw)

    fun parse(raw: String): Float? = raw.replace(',', '.').toFloatOrNull()
}
