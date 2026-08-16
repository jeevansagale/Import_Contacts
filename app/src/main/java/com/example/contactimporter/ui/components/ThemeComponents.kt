package com.example.contactimporter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.contactimporter.data.CardStyle

@Composable
fun ThemedCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val tokens = LocalThemeTokens.current
    val shape = when (tokens.cardStyle) {
        CardStyle.FLAT -> RoundedCornerShape(0.dp)
        CardStyle.ROUNDED -> RoundedCornerShape(16.dp)
        CardStyle.GLASS -> RoundedCornerShape(24.dp)
        CardStyle.OUTLINED -> RoundedCornerShape(8.dp)
    }
    
    val containerColor = if (tokens.cardStyle == CardStyle.GLASS) {
        tokens.surface.copy(alpha = 0.7f)
    } else {
        tokens.surface
    }

    val border = if (tokens.cardStyle == CardStyle.OUTLINED) {
        BorderStroke(1.dp, tokens.primary.copy(alpha = 0.5f))
    } else null

    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = tokens.onSurface
        ),
        border = border
    ) {
        content()
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
    val shape = when (tokens.cardStyle) {
        CardStyle.FLAT -> RoundedCornerShape(0.dp)
        CardStyle.ROUNDED -> RoundedCornerShape(24.dp)
        CardStyle.GLASS -> RoundedCornerShape(32.dp)
        CardStyle.OUTLINED -> RoundedCornerShape(4.dp)
    }

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = tokens.primary,
            contentColor = Color.White
        ),
        contentPadding = contentPadding
    ) {
        content()
    }
}