package com.emm.mybest.ui.theme

import androidx.compose.ui.graphics.Color

// ----------------------------------------------------------------------------
// Atelier Dark — design tokens for the MyBest visual system.
//
// Dark-only by design. Hierarchy is conveyed by ink opacity steps and
// hairline rules on a near-black ground; the single sage accent is reserved
// for the "cumplido / done" state and the warm tone for the single
// recommendation hint.
//
// Mirror of project/styles.css from the Atelier Dark handoff bundle:
//   --ink, --ink-2..4, --hair, --bg, --bg-2, --bg-3, --done, --done-d, --warm
// ----------------------------------------------------------------------------

/** Primary text — "bone". */
val AtelierInk = Color(0xFFEEEAE2)

/** Secondary text — 58% bone. */
val AtelierInk2 = Color(0x94EEEAE2)

/** Tertiary text / inactive icon — 34% bone. */
val AtelierInk3 = Color(0x57EEEAE2)

/** Muted edge / disabled — 16% bone. */
val AtelierInk4 = Color(0x29EEEAE2)

/** Hairline rule — 8% bone. */
val AtelierHair = Color(0x14EEEAE2)

/** Page background. */
val AtelierBg = Color(0xFF0A0A0C)

/** Sheet / elevated surface 1. */
val AtelierBg2 = Color(0xFF101013)

/** Elevated surface 2 (rare — input fills, popovers). */
val AtelierBg3 = Color(0xFF15151A)

/** Sage accent — used ONLY for the "cumplido / done" state. */
val AtelierDone = Color(0xFFC8E6A8)

/** Dim sage fill — 20% sage. */
val AtelierDoneDim = Color(0x33C8E6A8)

/** Warm hint — single recommendation tag. */
val AtelierWarm = Color(0xFFE8C190)
