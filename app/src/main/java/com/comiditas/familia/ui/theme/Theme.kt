package com.comiditas.familia.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Iris,
    onPrimary = Color.White,
    primaryContainer = Lavender,
    onPrimaryContainer = OnLavender,
    inversePrimary = Color(0xFFCFBDFF),
    secondary = SlateLavender,
    onSecondary = Color.White,
    secondaryContainer = SecondaryLavender,
    onSecondaryContainer = OnSecondaryLavender,
    tertiary = DustyRose,
    onTertiary = Color.White,
    tertiaryContainer = PaleRose,
    onTertiaryContainer = OnPaleRose,
    background = LilacCanvas,
    onBackground = NeutralInk,
    // White resting cards; lavender containers express inset and modal depth.
    surface = Color.White,
    onSurface = NeutralInk,
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = NeutralSlate,
    surfaceTint = Iris,
    inverseSurface = Color(0xFF322F38),
    inverseOnSurface = Color(0xFFF6EEFB),
    outline = NeutralOutline,
    outlineVariant = LavenderOutline,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = PaleError,
    onErrorContainer = OnPaleError,
    scrim = Color.Black,
    surfaceBright = LilacCanvas,
    surfaceDim = Color(0xFFDFD7E4),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8F1FE),
    surfaceContainer = Color(0xFFF3EBF8),
    surfaceContainerHigh = Color(0xFFEDE5F2),
    surfaceContainerHighest = Color(0xFFE7E0EC)
)

@Composable
fun ComiditasFamiliaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
