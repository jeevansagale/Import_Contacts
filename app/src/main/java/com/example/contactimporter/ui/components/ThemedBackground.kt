package com.example.contactimporter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.contactimporter.ui.AsyncUriImage
import com.example.contactimporter.theme.LocalThemeTokens

/**
 * REDESIGN: First-Principles Atmospheric Background.
 * 
 * Implements a three-layer depth rendering system:
 * 1. Background Artwork: Full-screen background artwork.
 * 2. Gradient Overlay: Top (Transparent) -> Middle (Slight tint) -> Bottom (Dark Surface).
 * 3. Ambient Character Glow: Large radial gradient behind UI.
 */
@Composable
fun ThemedBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val tokens = LocalThemeTokens.current
    val density = LocalDensity.current
    val glowRadius = with(density) { 250.dp.toPx() }
    
    Box(
        modifier = modifier.fillMaxSize().background(tokens.background)
    ) {
        // LAYER 1: Background Artwork
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
        
        // LAYER 2: Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            tokens.backgroundOverlay.copy(alpha = 0.2f),
                            tokens.backgroundOverlay.copy(alpha = 0.6f)
                        )
                    )
                )
        )

        // LAYER 3: Ambient Character Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(tokens.glow.copy(alpha = 0.12f), Color.Transparent),
                        radius = glowRadius
                    )
                )
        )
        
        content()
    }
}
