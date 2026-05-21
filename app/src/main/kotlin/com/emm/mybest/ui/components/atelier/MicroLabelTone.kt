package com.emm.mybest.ui.components.atelier

/** Color treatment options for [MicroLabel]. */
enum class MicroLabelTone {
    /** Secondary bone (58% opacity) — the most common label tone. */
    Default,

    /** Tertiary bone (34%) — inactive sections, deemphasised captions. */
    Dim,

    /** Sage — "cumplido / done" state. Used only in the completion context. */
    Done,

    /** Warm — single-recommendation hint on Insights. */
    Warm,
}
