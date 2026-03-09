package com.emm.mybest.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.emm.mybest.R

private val AppFontFamily: FontFamily = FontFamily(
    Font(R.font.geist_regular, FontWeight.Normal),
    Font(R.font.geist_medium, FontWeight.Medium),
    Font(R.font.geist_semibold, FontWeight.SemiBold),
    Font(R.font.geist_bold, FontWeight.Bold),
)

/**
 * Sistema tipográfico inspirado directamente en shadcn/ui.
 *
 * Mapeo Material3 → shadcn (equivalencias de clase CSS):
 *
 * | Token M3          | shadcn clase         | Uso en Hello                      |
 * |-------------------|----------------------|-----------------------------------|
 * | headlineLarge     | text-4xl extrabold   | —                                 |
 * | headlineMedium    | text-3xl bold        | —                                 |
 * | headlineSmall     | text-2xl semibold    | Título de word en CardDetail      |
 * | titleLarge        | text-xl semibold     | TopAppBar, sección principal      |
 * | titleMedium       | text-lg medium       | Subtítulos de sección             |
 * | titleSmall        | text-sm medium       | Labels de sección (SectionCard)   |
 * | bodyLarge         | text-base normal     | Cuerpo principal de flashcard     |
 * | bodyMedium        | text-sm normal       | Textos secundarios / onSurfaceVariant |
 * | bodySmall         | text-xs normal       | Helper / supporting text          |
 * | labelLarge        | text-sm medium       | Texto de botones                  |
 * | labelMedium       | text-xs medium       | Badges, chips, conteos            |
 * | labelSmall        | text-[10px] medium   | Micro-labels, fechas de review    |
 */
val Typography = Typography(
    headlineLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.25).sp,
    ),
    headlineSmall = androidx.compose.ui.text.TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    titleSmall = androidx.compose.ui.text.TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    bodyLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
    ),
    bodySmall = androidx.compose.ui.text.TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
    ),
    labelLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    labelMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
    ),
    labelSmall = androidx.compose.ui.text.TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.sp,
    ),
)
