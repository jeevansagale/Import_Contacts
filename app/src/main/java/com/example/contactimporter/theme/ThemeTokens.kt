package com.example.contactimporter.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.contactimporter.data.AnimationStyle
import com.example.contactimporter.data.BackgroundStyle
import com.example.contactimporter.data.CardStyle
import com.example.contactimporter.data.AnimationConfig

/**
 * Full design-token architecture for the application.
 */
data class ThemeTokens(
    // ── Core palette ──────────────────────────────────────────────
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val accent: Color,
    val onAccent: Color,

    // ── Surfaces ──────────────────────────────────────────────────
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val surfaceElevated: Color,
    val onSurfaceElevated: Color,
    val card: Color, // Mapped from theme.card in metadata.json
    val scrim: Color,
    val inverseSurface: Color,
    val surfaceTint: Color,

    // ── Outlines & dividers ───────────────────────────────────────
    val outline: Color,
    val outlineVariant: Color,
    val divider: Color,

    // ── Semantic states ───────────────────────────────────────────
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val disabled: Color,
    val onDisabled: Color,

    // ── Geometry ──────────────────────────────────────────────────
    val cornerRadiusXS: Dp = 4.dp,
    val cornerRadiusS: Dp = 8.dp,
    val cornerRadiusM: Dp = 12.dp,
    val cornerRadiusL: Dp = 16.dp,
    val cornerRadiusXL: Dp = 24.dp,
    val cornerRadiusPill: Dp = 100.dp,
    val elevationNone: Dp = 0.dp,
    val elevationTiny: Dp = 1.dp,
    val elevationSmall: Dp = 2.dp,
    val elevationMedium: Dp = 6.dp,
    val elevationLarge: Dp = 12.dp,
    val spacingXS: Dp = 4.dp,
    val spacingS: Dp = 8.dp,
    val spacingM: Dp = 16.dp,
    val spacingL: Dp = 24.dp,
    val spacingXL: Dp = 32.dp,

    // ── Visual style ──────────────────────────────────────────────
    val isDark: Boolean,
    val cardStyle: CardStyle,
    val animationStyle: AnimationStyle,
    val backgroundStyle: BackgroundStyle,
    val animationConfig: AnimationConfig = AnimationConfig(),
    val iconStyle: String = "default",
    val customBackgroundUri: String? = null,
    val customLogoUri: String? = null,
    val heroAssetPath: String? = null,
    val portraitAssetPath: String? = null,
    val backgroundAssetPath: String? = null,
    
    // ── Premium Atmosphere ────────────────────────────────────────
    val backgroundGradient: List<Color> = emptyList(),
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val ambientColor: Color,
    val backgroundOverlay: Color,
    val backgroundOpacity: Float = 0.40f,
    val heroAlignment: String = "center",
    val blurStrength: Int = 15,
    val themeMood: String = "neutral",
    val gradientStart: Color,
    val gradientEnd: Color
)
