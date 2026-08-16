package com.example.contactimporter.theme

import androidx.compose.ui.graphics.Color
import com.example.contactimporter.data.CharacterTheme
import com.example.contactimporter.data.ThemeConfig

object ThemeEngine {
    fun render(character: CharacterTheme?, config: ThemeConfig): ThemeTokens {
        if (character == null) return ThemePresets.Fallback

        val resolvedPrimary = config.customPrimaryColor?.let { Color(it) } ?: Color(character.palette.primary)
        val resolvedSecondary = config.customSecondaryColor?.let { Color(it) } ?: Color(character.palette.secondary)
        val resolvedCardStyle = config.selectedCardStyle ?: character.cardStyle
        val resolvedAnimationStyle = config.selectedAnimationStyle ?: character.animationStyle

        return ThemeTokens(
            primary = resolvedPrimary,
            secondary = resolvedSecondary,
            background = Color(character.palette.background),
            surface = Color(character.palette.surface),
            onBackground = Color(character.palette.onBackground),
            onSurface = Color(character.palette.onSurface),
            accent = Color(character.palette.accent),
            cardStyle = resolvedCardStyle,
            animationStyle = resolvedAnimationStyle,
            backgroundEffect = character.backgroundEffect,
            customBackgroundUri = config.customBackgroundUri,
            customLogoUri = config.customLogoUri
        )
    }
}