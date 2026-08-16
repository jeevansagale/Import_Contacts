package com.example.contactimporter.theme

import androidx.compose.ui.graphics.Color
import com.example.contactimporter.data.AnimationStyle
import com.example.contactimporter.data.BackgroundEffect
import com.example.contactimporter.data.CardStyle

data class ThemeTokens(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val onBackground: Color,
    val onSurface: Color,
    val accent: Color,
    val cardStyle: CardStyle,
    val animationStyle: AnimationStyle,
    val backgroundEffect: BackgroundEffect,
    val customBackgroundUri: String? = null,
    val customLogoUri: String? = null
)