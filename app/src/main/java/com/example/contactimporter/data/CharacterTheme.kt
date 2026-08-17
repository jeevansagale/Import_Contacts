package com.example.contactimporter.data

import kotlinx.serialization.Serializable

@Serializable
data class AnimationConfig(
    val transition: String = "fade",
    val background: String = "none",
    val accent: String = "none"
)

@Serializable
data class CopyrightInfo(
    val owner: String,
    val series: String
)

@Serializable
data class ImageAssets(
    val hero: String,
    val portrait: String,
    val background: String
)

@Serializable
data class AssetAvailability(
    val hero: Boolean = true,
    val portrait: Boolean = true,
    val background: Boolean = true
)

@Serializable
data class Gradient(
    val start: String,
    val end: String
)

@Serializable
data class ThemeStyle(
    val personality: List<String> = emptyList(),
    val motifs: List<String> = emptyList(),
    val accessories: List<String> = emptyList(),
    val animation: AnimationConfig = AnimationConfig(),
    val iconStyle: String = "default",
    val particleStyle: String = "none",
    val heroAlignment: String = "center",
    val themeMood: String = "neutral",
    val blur: Int = 16,
    val backgroundOpacity: Float = 0.22f
)

@Serializable
data class ThemePalette(
    val primary: String,
    val secondary: String,
    val accent: String,
    val glow: String,
    val backgroundOverlay: String,
    val surfaceTint: String,
    val gradient: Gradient
)

@Serializable
data class CharacterMetadata(
    val id: String,
    val name: String,
    val displayOrder: Int = 0,
    val version: Int = 1,
    val source: String,
    val category: String = "",
    val description: String,
    val copyright: CopyrightInfo,
    val supportsTheme: Boolean = true,
    val images: ImageAssets,
    val assets: AssetAvailability = AssetAvailability(),
    val theme: ThemePalette,
    val style: ThemeStyle
)

// The older enums and classes are being phased out in favor of CharacterMetadata
// but we keep them for ThemeEngine/UI interpretation logic.
enum class CardStyle { FLAT, ROUNDED, GLASS, OUTLINED }
enum class BackgroundStyle { NONE, GRADIENT, RIPPLE, VIGNETTE, GLOW, FOG, SCANLINE }
enum class AnimationStyle { NONE, SOFT, ENERGETIC }