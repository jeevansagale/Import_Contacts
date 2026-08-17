package com.example.contactimporter.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale

@Composable
fun ThemedAnimation(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val tokens = LocalThemeTokens.current
    val config = tokens.animationConfig
    
    // Task 7: Animation Hints from Metadata
    // Handling transitions
    val transitionModifier = when (config.transition) {
        "scale" -> {
            val infiniteTransition = rememberInfiniteTransition(label = "scale")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.98f,
                targetValue = 1.02f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "scale"
            )
            Modifier.scale(scale)
        }
        "spring" -> {
            // Simplified spring-like effect
            val infiniteTransition = rememberInfiniteTransition(label = "spring")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = LinearOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "spring"
            )
            Modifier.scale(scale)
        }
        "fade" -> {
            val infiniteTransition = rememberInfiniteTransition(label = "fade")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.85f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "alpha"
            )
            Modifier.alpha(alpha)
        }
        else -> Modifier
    }

    // Task 7: Background & Accent animations would typically be handled by specialized components 
    // or overlayed here. For brevity, we apply the transition to the main content.
    Box(modifier = modifier.then(transitionModifier)) {
        content()
    }
}
