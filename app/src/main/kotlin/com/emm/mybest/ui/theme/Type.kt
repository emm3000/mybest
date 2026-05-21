package com.emm.mybest.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Atelier Dark type scale.
 *
 * | Token M3          | Family          | Style    | Use                                        |
 * |-------------------|-----------------|----------|--------------------------------------------|
 * | displayLarge      | Instrument Serif| Italic   | Hero number (−4.2 kg, 30/5 ratio)          |
 * | displayMedium     | Instrument Serif| Italic   | Secondary metric (streak counter)          |
 * | displaySmall      | Instrument Serif| Italic   | Auxiliary metric                           |
 * | headlineLarge     | Instrument Serif| Regular  | Section hero headline                      |
 * | headlineMedium    | Instrument Serif| Regular  | Sub-title ("Mantén la rutina.")            |
 * | headlineSmall     | Instrument Serif| Regular  | Card display sub-title                     |
 * | titleLarge        | Geist Sans      | SemiBold | TopAppBar, primary section label           |
 * | titleMedium       | Geist Sans      | Medium   | Section sub-label                          |
 * | titleSmall        | Geist Sans      | Medium   | SectionCard label                          |
 * | bodyLarge         | Geist Sans      | Normal   | Primary body copy                          |
 * | bodyMedium        | Geist Sans      | Normal   | Secondary body / onSurfaceVariant          |
 * | bodySmall         | Geist Sans      | Normal   | Helper / supporting text                   |
 * | labelLarge        | Geist Sans      | Medium   | Button text                                |
 * | labelMedium       | Geist Mono      | Medium   | Uppercase tracking label (PROGRESO)        |
 * | labelSmall        | Geist Mono      | Medium   | Micro uppercase label (DELTA PESO)         |
 */
val AtelierTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = AtelierSerifFamily,
        fontStyle = FontStyle.Italic,
        fontSize = 128.sp,
        letterSpacing = (-2).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = AtelierSerifFamily,
        fontStyle = FontStyle.Italic,
        fontSize = 96.sp,
        letterSpacing = (-1.5).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = AtelierSerifFamily,
        fontStyle = FontStyle.Italic,
        fontSize = 56.sp,
        letterSpacing = (-1).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = AtelierSerifFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = (36 * 1.2).sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = AtelierSerifFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = (28 * 1.2).sp,
        letterSpacing = (-0.25).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = AtelierSerifFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = (22 * 1.2).sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = AtelierSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = AtelierSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = AtelierSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = AtelierSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = (15 * 1.45).sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = AtelierSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = (13 * 1.45).sp,
    ),
    bodySmall = TextStyle(
        fontFamily = AtelierSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = (12 * 1.45).sp,
    ),
    labelLarge = TextStyle(
        fontFamily = AtelierSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = AtelierMonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 1.8.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = AtelierMonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 2.0.sp,
    ),
)
