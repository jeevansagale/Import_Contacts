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

val LocalThemeTokens = staticCompositionLocalOf { ThemePresets.Fallback }

@Composable
fun CharacterThemeProvider(
    tokens: ThemeTokens,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = tokens.primary,
            secondary = tokens.secondary,
            background = tokens.background,
            surface = tokens.surface,
            onBackground = tokens.onBackground,
            onSurface = tokens.onSurface
        )
    } else {
        lightColorScheme(
            primary = tokens.primary,
            secondary = tokens.secondary,
            background = tokens.background,
            surface = tokens.surface,
            onBackground = tokens.onBackground,
            onSurface = tokens.onSurface
        )
    }

    CompositionLocalProvider(LocalThemeTokens provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}