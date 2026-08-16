package com.example.contactimporter.theme

import androidx.compose.ui.graphics.Color
import com.example.contactimporter.data.AnimationStyle
import com.example.contactimporter.data.BackgroundEffect
import com.example.contactimporter.data.CardStyle

object ThemePresets {
    val Fallback = ThemeTokens(
        primary = Color(0xFF6200EE),
        secondary = Color(0xFF03DAC6),
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        onBackground = Color(0xFFFFFFFF),
        onSurface = Color(0xFFFFFFFF),
        accent = Color(0xFFBB86FC),
        cardStyle = CardStyle.ROUNDED,
        animationStyle = AnimationStyle.NONE,
        backgroundEffect = BackgroundEffect.NONE
    )
}