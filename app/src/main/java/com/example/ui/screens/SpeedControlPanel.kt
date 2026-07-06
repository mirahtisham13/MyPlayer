package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedControlPanel(
    speed: Float,
    onSpeedChange: (Float) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Speed Box
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF222222))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = String.format("%.2fx", speed),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Slider Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = { onSpeedChange((speed - 0.05f).coerceAtLeast(1.0f)) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF222222))
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color.White)
            }
            
            Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                val textMeasurer = rememberTextMeasurer()
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val down = awaitFirstDown()
                                    val thumbRadius = 10.dp.toPx()
                                    val trackWidth = size.width - 2 * thumbRadius
                                    
                                    fun updateSpeed(x: Float) {
                                        val boundedX = x.coerceIn(thumbRadius, size.width - thumbRadius.toFloat())
                                        val fraction = (boundedX - thumbRadius) / trackWidth
                                        val newValue = 1.0f + fraction * (2.0f - 1.0f)
                                        
                                        var closestDot = 1.0f
                                        var minDiff = Float.MAX_VALUE
                                        val dots = listOf(1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
                                        for (dot in dots) {
                                            val diff = kotlin.math.abs(newValue - dot)
                                            if (diff < minDiff) {
                                                minDiff = diff
                                                closestDot = dot
                                            }
                                        }
                                        
                                        val finalValue = if (minDiff <= 0.06f) {
                                            closestDot
                                        } else {
                                            (newValue * 20).roundToInt() / 20f
                                        }
                                        
                                        onSpeedChange(finalValue.coerceIn(1.0f, 2.0f))
                                    }
                                    
                                    updateSpeed(down.position.x)
                                    
                                    horizontalDrag(down.id) { change ->
                                        change.consume()
                                        updateSpeed(change.position.x)
                                    }
                                }
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        
                        val thumbRadius = 10.dp.toPx()
                        val trackWidth = width - 2 * thumbRadius
                        val centerY = height / 2f
                        
                        val fraction = ((speed.coerceIn(1.0f, 2.0f)) - 1.0f) / (2.0f - 1.0f)
                        val activeWidth = trackWidth * fraction
                        
                        drawLine(
                            color = Color.Gray,
                            start = androidx.compose.ui.geometry.Offset(thumbRadius, centerY),
                            end = androidx.compose.ui.geometry.Offset(width - thumbRadius, centerY),
                            strokeWidth = 4.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        
                        if (activeWidth > 0) {
                            drawLine(
                                color = Color(0xFF4285F4),
                                start = androidx.compose.ui.geometry.Offset(thumbRadius, centerY),
                                end = androidx.compose.ui.geometry.Offset(thumbRadius + activeWidth, centerY),
                                strokeWidth = 4.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        }
                        
                        val labels = listOf("1.0x" to 1.0f, "1.25x" to 1.25f, "1.5x" to 1.5f, "1.75x" to 1.75f, "2.0x" to 2.0f)
                        labels.forEach { (label, value) ->
                            val dotFraction = (value - 1.0f) / (2.0f - 1.0f)
                            val cx = thumbRadius + trackWidth * dotFraction
                            drawCircle(
                                color = Color.White,
                                radius = 3.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(cx, centerY)
                            )
                            
                            val textLayoutResult = textMeasurer.measure(
                                text = label,
                                style = TextStyle(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            )
                            val tx = cx - textLayoutResult.size.width / 2f
                            val ty = centerY + 12.dp.toPx()
                            
                            drawText(
                                textLayoutResult = textLayoutResult,
                                topLeft = androidx.compose.ui.geometry.Offset(tx, ty)
                            )
                        }
                        
                        val thumbX = thumbRadius + activeWidth
                        drawLine(
                            color = Color.White,
                            start = androidx.compose.ui.geometry.Offset(thumbX, centerY - 8.dp.toPx()),
                            end = androidx.compose.ui.geometry.Offset(thumbX, centerY + 8.dp.toPx()),
                            strokeWidth = 3.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                }
            }
            
            IconButton(
                onClick = { onSpeedChange((speed + 0.05f).coerceAtMost(2.0f)) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF222222))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White)
            }
            

        }
    }
}
