package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.RenderEffect
import android.os.Build

@Composable
fun Modifier.glossyCard(
    isDark: Boolean,
    cornerRadius: Dp = 24.dp,
    borderWidth: Dp = 1.dp,
    blurRadius: Float = 16f
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    
    // Liquid glass frosted backdrop
    val backgroundBrush = if (isDark) {
        Brush.radialGradient(
            colors = listOf(
                Color.Black.copy(alpha = (0.45f * (blurRadius / 16f)).coerceIn(0.2f, 0.9f)),
                Color.Black.copy(alpha = (0.25f * (blurRadius / 16f)).coerceIn(0.1f, 0.8f))
            ),
            center = Offset(Float.POSITIVE_INFINITY, 0f),
            radius = 2000f
        )
    } else {
        Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = (0.90f * (blurRadius / 16f)).coerceIn(0.5f, 0.98f)),
                Color.White.copy(alpha = (0.60f * (blurRadius / 16f)).coerceIn(0.3f, 0.95f))
            ),
            center = Offset(Float.POSITIVE_INFINITY, 0f),
            radius = 2000f
        )
    }
    
    // Crisp Apple-like thin borders for glass
    val borderBrush = if (isDark) {
        Brush.linearGradient(
            0.0f to Color(0x66FFFFFF),
            0.3f to Color(0x1AFFFFFF),
            0.7f to Color(0x00FFFFFF),
            1.0f to Color(0x33FFFFFF),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    } else {
        Brush.linearGradient(
            0.0f to Color(0xFFFFFFFF),
            0.3f to Color(0x66FFFFFF),
            0.7f to Color(0x00FFFFFF),
            1.0f to Color(0x66FFFFFF),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }

    return this
        .shadow(
            elevation = 16.dp,
            shape = shape,
            ambientColor = if (isDark) Color.Black else Color.Black.copy(alpha = 0.05f),
            spotColor = if (isDark) Color.Black.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.15f)
        )
        // Note: RenderEffect.createBlurEffect is API 31+, but we use a visual approximation for older APIs using graphicsLayer if needed
        // For broad compatibility, the glossy layered approach gives the glass effect
        .clip(shape)
        .background(backgroundBrush)
        .border(borderWidth, borderBrush, shape)
        .drawBehind {
            val w = size.width
            val h = size.height
            
            // Soft inner glow top edge
            drawLine(
                color = if (isDark) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.8f),
                start = Offset(cornerRadius.toPx(), 1.dp.toPx()),
                end = Offset(w - cornerRadius.toPx(), 1.dp.toPx()),
                strokeWidth = 1.dp.toPx()
            )
            
            // Apple-like specular highlight / diagonal glare
            val shineBrush = Brush.linearGradient(
                0.0f to Color.Transparent,
                0.2f to if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.15f),
                0.3f to if (isDark) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.3f),
                0.4f to Color.Transparent,
                1.0f to Color.Transparent,
                start = Offset(0f, 0f),
                end = Offset(w * 1.5f, h * 1.5f)
            )
            drawRect(brush = shineBrush)
        }
}

@Composable
fun Modifier.glossyHeader(
    isDark: Boolean
): Modifier {
    val headerBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xCC000000),
                Color(0x99000000)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xF2FFFFFF),
                Color(0xCCF0F0F5)
            )
        )
    }
    
    return this
        .shadow(
            elevation = 12.dp,
            ambientColor = Color.Black.copy(alpha = 0.03f),
            spotColor = Color.Black.copy(alpha = 0.08f)
        )
        .background(headerBrush)
        .drawBehind {
            val w = size.width
            val h = size.height
            // Frosted bottom accent line
            drawLine(
                color = if (isDark) Color(0x33FFFFFF) else Color(0x1A000000),
                start = Offset(0f, h),
                end = Offset(w, h),
                strokeWidth = 1.dp.toPx()
            )
            // Top inner glow
            drawLine(
                color = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.5f),
                start = Offset(0f, 0f),
                end = Offset(w, 0f),
                strokeWidth = 1.dp.toPx()
            )
        }
}
