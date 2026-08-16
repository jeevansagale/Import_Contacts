package com.example.contactimporter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.contactimporter.data.BackgroundEffect

@Composable
fun ThemedBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val tokens = LocalThemeTokens.current
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                when (tokens.backgroundEffect) {
                    BackgroundEffect.GRADIENT -> Modifier.background(
                        Brush.linearGradient(
                            colors = listOf(tokens.background, tokens.primary.copy(alpha = 0.2f))
                        )
                    )
                    else -> Modifier.background(tokens.background)
                }
            )
    ) {
        content()
    }
}