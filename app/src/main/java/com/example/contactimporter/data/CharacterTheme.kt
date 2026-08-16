package com.example.contactimporter.data

enum class CharacterCategory { ANIME, GAME }

enum class ThemeFamily { SOFT_FANTASY, DARK_ELEGANT, ENERGETIC, FUTURISTIC, MILITARY_SERIOUS, CUSTOM }

enum class AnimationStyle { NONE, SOFT, ELEGANT, PLAYFUL, FUTURISTIC, ENERGETIC, DARK }

enum class CardStyle { FLAT, ROUNDED, GLASS, OUTLINED }

enum class BackgroundEffect { NONE, GRADIENT, PARTICLES, SNOW, RIPPLE, GLOW, HUD, VIGNETTE }

enum class HeroAlignment { LEFT, CENTER, RIGHT }

data class ThemePalette(
    val primary: Long,
    val secondary: Long,
    val background: Long,
    val surface: Long,
    val onBackground: Long,
    val onSurface: Long,
    val accent: Long,
)

data class CharacterTheme(
    val id: String,
    val name: String,
    val franchise: String,
    val category: CharacterCategory,
    val themeFamily: ThemeFamily,
    val portraitAsset: String,
    val heroAsset: String,
    val palette: ThemePalette,
    val animationStyle: AnimationStyle,
    val cardStyle: CardStyle,
    val backgroundEffect: BackgroundEffect,
    val heroAlignment: HeroAlignment = HeroAlignment.CENTER,
)