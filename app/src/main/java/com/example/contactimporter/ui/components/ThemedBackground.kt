package com.example.contactimporter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.contactimporter.data.BackgroundStyle
import com.example.contactimporter.ui.AsyncUriImage

/**
 * REDESIGN: First-Principles Atmospheric Background.
 * 
 * Implements a three-layer depth rendering system:
 * 1. Palette Foundation: Thematic gradients grounded in character colors.
 * 2. Gaussian Atmosphere: Heavy Gaussian blur (80.dp) of assets for character immersion.
 * 3. Legibility Scrim: Adaptive contrast control for Material 3 expressive legibility.
 */
@Composable
fun ThemedBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val tokens = LocalThemeTokens.current
    val isDark = isSystemInDarkTheme()
    
    // LAYER 1: Foundation (Mood Mesh)
    val baseModifier = when (tokens.backgroundStyle) {
        BackgroundStyle.GRADIENT -> {
            Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(tokens.gradientStart, tokens.gradientEnd)
                )
            )
        }
        BackgroundStyle.RIPPLE -> {
            Modifier.background(
                brush = Brush.radialGradient(
                    colors = listOf(tokens.primary.copy(alpha = 0.15f), tokens.background)
                )
            )
        }
        BackgroundStyle.VIGNETTE -> {
            Modifier.background(
                brush = Brush.radialGradient(
                    radius = 2.5f,
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                )
            ).background(tokens.background)
        }
        BackgroundStyle.GLOW -> {
            Modifier.background(
                brush = Brush.radialGradient(
                    center = Offset(0.5f, 0.25f), // Backlight aligned with Carousel
                    radius = 1.0f,
                    colors = listOf(tokens.primary.copy(alpha = 0.22f), tokens.background)
                )
            )
        }
        else -> Modifier.background(tokens.background)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(baseModifier)
    ) {
        // LAYER 2: Atmosphere (Immersive Atmospheric Depth)
        val bgPath = tokens.backgroundAssetPath ?: tokens.customBackgroundUri
        if (bgPath != null) {
            AsyncUriImage(
                uriString = bgPath,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(tokens.blurStrength.dp)
                    .alpha(tokens.backgroundOpacity),
                contentScale = ContentScale.Crop
            )
        }
        
        // LAYER 3: Adaptive Scrim (Dark Gradient Overlay)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(tokens.surfaceTint)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            tokens.backgroundOverlay.copy(alpha = 0.2f),
                            tokens.backgroundOverlay.copy(alpha = 0.6f)
                        )
                    )
                )
        )
        
        content()
    }
}
