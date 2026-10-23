package com.emm.mybest.domain.models

enum class DailyCheckType {
    GYM,
    DIET,
    ENGLISH,

    /**
     * Inverted wording from PRD §2 ("did you drink today?"): `isDone = true` means the user drank today.
     */
    ALCOHOL,
}
