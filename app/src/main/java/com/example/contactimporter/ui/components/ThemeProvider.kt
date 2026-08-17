package com.example.contactimporter.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.contactimporter.theme.ThemePresets
import com.example.contactimporter.theme.ThemeTokens

val LocalThemeTokens = staticCompositionLocalOf<ThemeTokens> { ThemePresets.FallbackLight }

@Composable
fun CharacterThemeProvider(
    tokens: ThemeTokens,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = tokens.primary,
            onPrimary = tokens.onPrimary,
            secondary = tokens.secondary,
            onSecondary = tokens.onSecondary,
            background = tokens.background,
            onBackground = tokens.onBackground,
            surface = tokens.surface,
            onSurface = tokens.onSurface,
            surfaceVariant = tokens.surfaceVariant,
            onSurfaceVariant = tokens.onSurfaceVariant,
            error = tokens.error,
            onError = tokens.onError,
            outline = tokens.outline
        )
    } else {
        lightColorScheme(
            primary = tokens.primary,
            onPrimary = tokens.onPrimary,
            secondary = tokens.secondary,
            onSecondary = tokens.onSecondary,
            background = tokens.background,
            onBackground = tokens.onBackground,
            surface = tokens.surface,
            onSurface = tokens.onSurface,
            surfaceVariant = tokens.surfaceVariant,
            onSurfaceVariant = tokens.onSurfaceVariant,
            error = tokens.error,
            onError = tokens.onError,
            outline = tokens.outline
        )
    }

    CompositionLocalProvider(LocalThemeTokens provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
