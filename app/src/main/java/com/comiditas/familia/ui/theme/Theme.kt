package com.comiditas.familia.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Terracotta,
    onPrimary = Color.White,
    primaryContainer = Peach,
    onPrimaryContainer = Cocoa,
    inversePrimary = Peach,
    secondary = Olive,
    onSecondary = Color.White,
    secondaryContainer = PaleOlive,
    onSecondaryContainer = Cocoa,
    tertiary = MutedCocoa,
    onTertiary = Color.White,
    tertiaryContainer = SoftBorder,
    onTertiaryContainer = Cocoa,
    background = Cream,
    onBackground = Cocoa,
    surface = WarmWhite,
    onSurface = Cocoa,
    surfaceVariant = Color(0xFFF3E8DD),
    onSurfaceVariant = MutedCocoa,
    surfaceTint = Terracotta,
    inverseSurface = Cocoa,
    inverseOnSurface = Cream,
    outline = WarmBorder,
    outlineVariant = SoftBorder,
    error = WarmError,
    onError = Color.White,
    errorContainer = PaleError,
    onErrorContainer = Color(0xFF410E0B),
    scrim = Color.Black,
    surfaceBright = WarmWhite,
    surfaceDim = Color(0xFFE8DCD0),
    surfaceContainerLowest = WarmWhite,
    surfaceContainerLow = Cream,
    surfaceContainer = Color(0xFFF7EDE3),
    surfaceContainerHigh = Color(0xFFF1E5D9),
    surfaceContainerHighest = Color(0xFFEADDD0)
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
