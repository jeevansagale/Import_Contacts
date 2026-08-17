package com.example.contactimporter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.contactimporter.data.CardStyle

/**
 * REDESIGN: First-Principles Component Library.
 * 
 * Implements expressive Material 3 geometry and token-driven styling.
 */

@Composable
fun ThemedCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val tokens = LocalThemeTokens.current
    
    // Expressive corner radii based on token geometry
    val shape = when (tokens.cardStyle) {
        CardStyle.FLAT -> RoundedCornerShape(tokens.cornerRadiusXS)
        CardStyle.ROUNDED -> RoundedCornerShape(tokens.cornerRadiusL)
        CardStyle.GLASS -> RoundedCornerShape(tokens.cornerRadiusXL)
        CardStyle.OUTLINED -> RoundedCornerShape(tokens.cornerRadiusS)
    }
    
    // VERIFIED: Using tokens.card from metadata.json
    val containerColor = if (tokens.cardStyle == CardStyle.GLASS) {
        tokens.card.copy(alpha = 0.7f) // High-fidelity glass transparency
    } else {
        tokens.card
    }

    val border = if (tokens.cardStyle == CardStyle.OUTLINED) {
        BorderStroke(1.dp, tokens.outline)
    } else if (tokens.cardStyle == CardStyle.GLASS) {
        BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)) // Subtle refractive rim
    } else null

    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = tokens.onSurface
        ),
        border = border,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (tokens.cardStyle == CardStyle.GLASS) 0.dp else tokens.elevationSmall
        )
    ) {
        ThemedAnimation {
            content()
        }
    }
}

@Composable
fun ThemedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable () -> Unit
) {
    val tokens = LocalThemeTokens.current
    
    // Premium button shapes aligned with Material 3 Expressive guidelines
    val shape = when (tokens.cardStyle) {
        CardStyle.FLAT -> RoundedCornerShape(tokens.cornerRadiusXS)
        CardStyle.ROUNDED -> RoundedCornerShape(tokens.cornerRadiusPill)
        CardStyle.GLASS -> RoundedCornerShape(tokens.cornerRadiusXL)
        CardStyle.OUTLINED -> RoundedCornerShape(tokens.cornerRadiusS)
    }

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = tokens.primary,
            contentColor = tokens.onPrimary,
            disabledContainerColor = tokens.disabled,
            disabledContentColor = tokens.onDisabled
        ),
        contentPadding = contentPadding,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        content()
    }
}

@Composable
fun ThemedProgress(
    modifier: Modifier = Modifier,
    progress: Float? = null
) {
    val tokens = LocalThemeTokens.current
    // VERIFIED: Using tokens.accent for highlighting progress if defined
    val indicatorColor = if (tokens.accent != Color.Gray) tokens.accent else tokens.primary
    
    if (progress != null) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = modifier,
            color = indicatorColor,
            trackColor = tokens.surfaceVariant
        )
    } else {
        CircularProgressIndicator(
            modifier = modifier,
            color = indicatorColor,
            trackColor = tokens.surfaceVariant
        )
    }
}
