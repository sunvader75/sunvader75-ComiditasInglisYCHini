package com.comiditas.familia.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// System fonts respect user font scaling; all sizes and line heights are in sp.
private fun foodTextStyle(
    size: Int,
    height: Int,
    weight: FontWeight = FontWeight.Normal,
    tracking: Double = 0.0
) = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = height.sp,
    letterSpacing = tracking.sp
)

val Typography = Typography(
    displayLarge = foodTextStyle(57, 64, tracking = -0.25),
    displayMedium = foodTextStyle(45, 52),
    displaySmall = foodTextStyle(36, 44, tracking = -0.25),
    headlineLarge = foodTextStyle(32, 40),
    headlineMedium = foodTextStyle(28, 36),
    headlineSmall = foodTextStyle(24, 32),
    titleLarge = foodTextStyle(22, 28),
    titleMedium = foodTextStyle(16, 24, FontWeight.Medium, 0.15),
    titleSmall = foodTextStyle(14, 20, FontWeight.Medium, 0.1),
    bodyLarge = foodTextStyle(16, 24, tracking = 0.5),
    bodyMedium = foodTextStyle(14, 20, tracking = 0.25),
    bodySmall = foodTextStyle(12, 16, tracking = 0.4),
    labelLarge = foodTextStyle(14, 20, FontWeight.Medium, 0.1),
    labelMedium = foodTextStyle(12, 16, FontWeight.Medium, 0.5),
    labelSmall = foodTextStyle(11, 16, FontWeight.Medium, 0.5)
)
