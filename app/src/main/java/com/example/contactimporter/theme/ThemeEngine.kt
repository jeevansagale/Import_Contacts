package com.example.contactimporter.theme

import androidx.compose.ui.graphics.Color
import com.example.contactimporter.data.CharacterMetadata
import com.example.contactimporter.data.ThemeConfig

object ThemeEngine {
    fun render(metadata: CharacterMetadata?, config: ThemeConfig, isDark: Boolean): ThemeTokens {
        // Fallback tokens based on system theme
        val baseTokens = if (isDark) ThemePresets.FallbackDark else ThemePresets.FallbackLight

        // If theme is disabled or no metadata, return professional monochrome Material 3 theme
        if (!config.themeEnabled || metadata == null) {
            val neutralGray = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF5F5F5)
            val textPrimary = if (isDark) Color.White else Color.Black
            val textSecondary = if (isDark) Color.LightGray else Color.DarkGray
            
            return baseTokens.copy(
                primary = textPrimary,
                onPrimary = if (isDark) Color.Black else Color.White,
                secondary = textSecondary,
                onSecondary = if (isDark) Color.Black else Color.White,
                accent = Color.Gray,
                background = if (isDark) Color(0xFF121212) else Color.White,
                surface = neutralGray,
                onSurface = textPrimary,
                surfaceVariant = if (isDark) Color(0xFF2C2C2C) else Color(0xFFE0E0E0),
                onSurfaceVariant = textSecondary,
                card = neutralGray,
                outline = if (isDark) Color(0xFF424242) else Color(0xFFBDBDBD),
                onBackground = textPrimary,
                customBackgroundUri = config.customBackgroundUri,
                customLogoUri = config.customLogoUri,
                heroAssetPath = null,
                portraitAssetPath = null,
                backgroundAssetPath = null,
                isDark = isDark
            )
        }

        // Parse colors from metadata.json
        val theme = metadata.theme
        val primary = safeParseColor(theme.primary, baseTokens.primary)
        val secondary = safeParseColor(theme.secondary, baseTokens.secondary)
        val accent = safeParseColor(theme.accent, baseTokens.accent)
        val surface = safeParseColor(theme.surface, baseTokens.surface)
        val background = safeParseColor(theme.background, baseTokens.background)
        val text = safeParseColor(theme.text, baseTokens.onSurface)
        val card = safeParseColor(theme.card, baseTokens.surface)

        // Map metadata colors to ThemeTokens
        return baseTokens.copy(
            primary = primary,
            onPrimary = if (primary.luminance() > 0.5f) Color.Black else Color.White,
            secondary = secondary,
            onSecondary = if (secondary.luminance() > 0.5f) Color.Black else Color.White,
            accent = accent,
            onAccent = if (accent.luminance() > 0.5f) Color.Black else Color.White,
            background = background,
            onBackground = text,
            surface = surface,
            onSurface = text,
            surfaceVariant = card.copy(alpha = 0.7f),
            onSurfaceVariant = text.copy(alpha = 0.8f),
            outline = primary.copy(alpha = 0.4f),
            card = card,
            animationConfig = metadata.style.animation,
            iconStyle = metadata.style.iconStyle,
            customBackgroundUri = config.customBackgroundUri,
            customLogoUri = config.customLogoUri,
            heroAssetPath = metadata.images.hero,
            portraitAssetPath = metadata.images.portrait,
            backgroundAssetPath = metadata.images.background,
            isDark = isDark
        )
    }

    private fun safeParseColor(colorStr: String, fallback: Color): Color {
        return try {
            Color(android.graphics.Color.parseColor(colorStr))
        } catch (e: Exception) {
            fallback
        }
    }

    // Helper to determine if we should use black or white text on a background color
    private fun Color.luminance(): Float {
        return 0.2126f * red + 0.7152f * green + 0.0722f * blue
    }
}
