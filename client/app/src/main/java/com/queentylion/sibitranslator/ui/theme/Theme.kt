package com.queentylion.sibitranslator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


private val LightColors = lightColorScheme(
//    primary = md_theme_light_primary,
//    onPrimary = Color(0xFF70787c),
//    primaryContainer = md_theme_light_primaryContainer,
//    onPrimaryContainer = Color(0xFF70787c),
//    secondary = md_theme_light_secondary,
//    onSecondary = Color(0xFF70787c),
//    secondaryContainer = md_theme_light_secondaryContainer,
//    onSecondaryContainer = Color(0xFF70787c),
//    tertiary = md_theme_light_tertiary,
//    onTertiary = Color(0xFF70787c),
//    tertiaryContainer = md_theme_light_tertiaryContainer,
//    onTertiaryContainer = Color(0xFF70787c),
//    error = md_theme_light_error,
//    errorContainer = md_theme_light_errorContainer,
//    onError = Color(0xFF70787c),
//    onErrorContainer = Color(0xFF70787c),
//    background = md_theme_light_background,
//    onBackground = Color(0xFF70787c),
//    surface = md_theme_light_surface,
//    onSurface = Color(0xFF70787c),
//    surfaceVariant = md_theme_light_surfaceVariant,
//    onSurfaceVariant = Color(0xFF70787c),
//    outline = md_theme_light_outline,
//    inverseOnSurface = Color(0xFF70787c),
//    inverseSurface = md_theme_light_inverseSurface,
//    inversePrimary = md_theme_light_inversePrimary,
//    surfaceTint = md_theme_light_surfaceTint,
//    outlineVariant = md_theme_light_outlineVariant,
//    scrim = md_theme_light_scrim,
)


private val DarkColors = darkColorScheme(
//    primary = md_theme_light_primary,
//    onPrimary = Color(0xFF70787c),
//    primaryContainer = md_theme_light_primaryContainer,
//    onPrimaryContainer = Color(0xFF70787c),
//    secondary = md_theme_light_secondary,
//    onSecondary = Color(0xFF70787c),
//    secondaryContainer = md_theme_light_secondaryContainer,
//    onSecondaryContainer = Color(0xFF70787c),
//    tertiary = md_theme_light_tertiary,
//    onTertiary = Color(0xFF70787c),
//    tertiaryContainer = md_theme_light_tertiaryContainer,
//    onTertiaryContainer = Color(0xFF70787c),
//    error = md_theme_light_error,
//    errorContainer = md_theme_light_errorContainer,
//    onError = Color(0xFF70787c),
//    onErrorContainer = Color(0xFF70787c),
//    background = md_theme_light_background,
//    onBackground = Color(0xFF70787c),
//    surface = md_theme_light_surface,
//    onSurface = Color(0xFF70787c),
//    surfaceVariant = md_theme_light_surfaceVariant,
//    onSurfaceVariant = Color(0xFF70787c),
//    outline = md_theme_light_outline,
//    inverseOnSurface = Color(0xFF70787c),
//    inverseSurface = md_theme_light_inverseSurface,
//    inversePrimary = md_theme_light_inversePrimary,
//    surfaceTint = md_theme_light_surfaceTint,
//    outlineVariant = md_theme_light_outlineVariant,
//    scrim = md_theme_light_scrim,
)

@Composable
fun SIBITranslatorTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colors = if (!useDarkTheme) {
        LightColors
    } else {
        DarkColors
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}