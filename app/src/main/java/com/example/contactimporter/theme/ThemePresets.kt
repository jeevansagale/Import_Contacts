package com.example.contactimporter.theme

import androidx.compose.ui.graphics.Color
import com.example.contactimporter.data.AnimationStyle
import com.example.contactimporter.data.BackgroundStyle
import com.example.contactimporter.data.CardStyle

/**
 * Base design tokens that guarantee a professional look even before
 * any character palette is applied.
 *
 * Tokens are defined for light and dark explicitly — character themes
 * derive from these base tokens rather than replacing them wholesale.
 */
object ThemePresets {

    /** Semantic base colors used for deriving states. */
    object Base {
        val LightPrimary = Color(0xFF4A6B99)
        val LightSecondary = Color(0xFF769DCB)
        val LightAccent = Color(0xFFCBA976)
        val LightBackground = Color(0xFFF8FAFD)
        val LightSurface = Color(0xFFFFFFFF)
        val LightText = Color(0xFF1B263B)

        val DarkPrimary = Color(0xFF9FC2E8)
        val DarkSecondary = Color(0xFFA8C7EC)
        val DarkAccent = Color(0xFFE0C08C)
        val DarkBackground = Color(0xFF121823)
        val DarkSurface = Color(0xFF1C2432)
        val DarkText = Color(0xFFF0F4FA)
    }

    private val Light = ThemeTokens(
        primary = Base.LightPrimary,
        onPrimary = Color.White,
        secondary = Base.LightSecondary,
        onSecondary = Color.White,
        accent = Base.LightAccent,
        onAccent = Color.White,
        background = Base.LightBackground,
        onBackground = Base.LightText,
        surface = Base.LightSurface,
        onSurface = Base.LightText,
        surfaceVariant = Color(0xFFEAF0F7),
        onSurfaceVariant = Color(0xFF4E5B70),
        surfaceElevated = Color(0xFFFFFFFF),
        onSurfaceElevated = Base.LightText,
        card = Base.LightSurface,
        scrim = Color(0x66000000),
        inverseSurface = Color(0xFF2C3547),
        surfaceTint = Color.Transparent,
        outline = Color(0xFFB8C4D4),
        outlineVariant = Color(0xFFD8E0EA),
        divider = Color(0xFFE4E9F0),
        success = Color(0xFF2E7D32),
        onSuccess = Color.White,
        successContainer = Color(0xFFE6F4E6),
        onSuccessContainer = Color(0xFF1B5E20),
        warning = Color(0xFFB26A00),
        onWarning = Color.White,
        warningContainer = Color(0xFFFFF0D9),
        onWarningContainer = Color(0xFF7A4A00),
        error = Color(0xFFB3261E),
        onError = Color.White,
        errorContainer = Color(0xFFF9DEDC),
        onErrorContainer = Color(0xFF8B1A1A),
        disabled = Color(0x1A1B263B),
        onDisabled = Color(0x661B263B),
        isDark = false,
        cardStyle = CardStyle.ROUNDED,
        animationStyle = AnimationStyle.NONE,
        backgroundStyle = BackgroundStyle.GRADIENT,
        glow = Base.LightPrimary,
        backgroundOverlay = Color.Black,
        gradientStart = Color.Transparent,
        gradientEnd = Color.Transparent
    )

    private val Dark = ThemeTokens(
        primary = Base.DarkPrimary,
        onPrimary = Color(0xFF1B263B),
        secondary = Base.DarkSecondary,
        onSecondary = Color(0xFF1B263B),
        accent = Base.DarkAccent,
        onAccent = Color(0xFF1B263B),
        background = Base.DarkBackground,
        onBackground = Base.DarkText,
        surface = Base.DarkSurface,
        onSurface = Base.DarkText,
        surfaceVariant = Color(0xFF2A3548),
        onSurfaceVariant = Color(0xFFB8C4D4),
        surfaceElevated = Color(0xFF242F40),
        onSurfaceElevated = Base.DarkText,
        card = Base.DarkSurface,
        scrim = Color(0x99000000),
        inverseSurface = Color(0xFFE8EDF5),
        surfaceTint = Color.Transparent,
        outline = Color(0xFF4E5B70),
        outlineVariant = Color(0xFF3A4558),
        divider = Color(0xFF2A3548),
        success = Color(0xFF81C784),
        onSuccess = Color(0xFF0D3310),
        successContainer = Color(0xFF1E3B22),
        onSuccessContainer = Color(0xFFA5D6A7),
        warning = Color(0xFFFFB74D),
        onWarning = Color(0xFF3D2600),
        warningContainer = Color(0xFF4A3400),
        onWarningContainer = Color(0xFFFFD699),
        error = Color(0xFFF2B8B5),
        onError = Color(0xFF601410),
        errorContainer = Color(0xFF4A2320),
        onErrorContainer = Color(0xFFF9DEDC),
        disabled = Color(0x1AF0F4FA),
        onDisabled = Color(0x66F0F4FA),
        isDark = true,
        cardStyle = CardStyle.ROUNDED,
        animationStyle = AnimationStyle.NONE,
        backgroundStyle = BackgroundStyle.GRADIENT,
        glow = Base.DarkPrimary,
        backgroundOverlay = Color.Black,
        gradientStart = Color.Transparent,
        gradientEnd = Color.Transparent
    )

    /** Fallback light token set — used before a character theme resolves. */
    val FallbackLight: ThemeTokens get() = Light

    /** Fallback dark token set — used before a character theme resolves. */
    val FallbackDark: ThemeTokens get() = Dark
}
