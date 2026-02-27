package com.example.aighostextractor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ElectricPurple,
    background = SpaceBlack,
    surface = SurfaceDark,
    onPrimary = SoftWhite,
    onBackground = SoftWhite,
    onSurface = SoftWhite
)

@Composable
fun AIGhostExtractorTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
