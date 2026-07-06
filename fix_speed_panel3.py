import re

with open("app/src/main/java/com/example/ui/screens/SpeedControlPanel.kt", "r") as f:
    content = f.read()

# Make sure imports exist
if "import androidx.compose.ui.text.drawText" not in content:
    content = content.replace("import androidx.compose.ui.text.font.FontWeight", "import androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.text.drawText\nimport androidx.compose.ui.text.rememberTextMeasurer\nimport androidx.compose.ui.text.TextStyle")

old_column = """            Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(4.dp)) {
                        val width = size.width
                        val height = size.height
                        
                        val thumbRadius = 10.dp.toPx()
                        val trackWidth = width - 2 * thumbRadius
                        
                        val fraction = (speed - 0.25f) / (2.0f - 0.25f)
                        val activeWidth = trackWidth * fraction
                        
                        drawLine(
                            color = Color.Gray,
                            start = androidx.compose.ui.geometry.Offset(thumbRadius, height / 2f),
                            end = androidx.compose.ui.geometry.Offset(width - thumbRadius, height / 2f),
                            strokeWidth = 4.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        
                        if (activeWidth > 0) {
                            drawLine(
                                color = Color(0xFF4285F4),
                                start = androidx.compose.ui.geometry.Offset(thumbRadius, height / 2f),
                                end = androidx.compose.ui.geometry.Offset(thumbRadius + activeWidth, height / 2f),
                                strokeWidth = 4.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        }
                        
                        val dots = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
                        dots.forEach { value ->
                            val dotFraction = (value - 0.25f) / (2.0f - 0.25f)
                            val cx = thumbRadius + trackWidth * dotFraction
                            drawCircle(
                                color = Color.White,
                                radius = 3.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(cx, height / 2f)
                            )
                        }
                    }
                    
                    Slider(
                        value = speed,
                        onValueChange = { 
                            val snapped = (it * 20).roundToInt() / 20f
                            onSpeedChange(snapped.coerceIn(0.25f, 2.0f))
                        },
                        valueRange = 0.25f..2.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.Transparent,
                            inactiveTrackColor = Color.Transparent,
                            activeTickColor = Color.Transparent,
                            inactiveTickColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                ) {
                    val labels = listOf("0.25x" to 0.25f, "0.5x" to 0.5f, "0.75x" to 0.75f, "1.0x" to 1.0f, "1.25x" to 1.25f, "1.5x" to 1.5f, "1.75x" to 1.75f, "2.0x" to 2.0f)
                    labels.forEach { (label, value) ->
                        val fraction = (value - 0.25f) / (2.0f - 0.25f)
                        Text(
                            text = label,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(androidx.compose.ui.BiasAlignment(horizontalBias = -1f + 2f * fraction, verticalBias = 0f))
                        )
                    }
                }
            }"""

new_column = """            Column(modifier = Modifier.weight(1f).padding(horizontal = 32.dp)) {
                val textMeasurer = rememberTextMeasurer()
                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(64.dp)) {
                        val width = size.width
                        val height = size.height
                        
                        val thumbRadius = 10.dp.toPx()
                        val trackWidth = width - 2 * thumbRadius
                        val centerY = height / 2f
                        
                        val fraction = (speed - 0.25f) / (2.0f - 0.25f)
                        val activeWidth = trackWidth * fraction
                        
                        drawLine(
                            color = Color.Gray,
                            start = androidx.compose.ui.geometry.Offset(thumbRadius, centerY),
                            end = androidx.compose.ui.geometry.Offset(width - thumbRadius, centerY),
                            strokeWidth = 6.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        
                        if (activeWidth > 0) {
                            drawLine(
                                color = Color(0xFF4285F4),
                                start = androidx.compose.ui.geometry.Offset(thumbRadius, centerY),
                                end = androidx.compose.ui.geometry.Offset(thumbRadius + activeWidth, centerY),
                                strokeWidth = 6.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        }
                        
                        val labels = listOf("0.25x" to 0.25f, "0.5x" to 0.5f, "0.75x" to 0.75f, "1.0x" to 1.0f, "1.25x" to 1.25f, "1.5x" to 1.5f, "1.75x" to 1.75f, "2.0x" to 2.0f)
                        labels.forEach { (label, value) ->
                            val dotFraction = (value - 0.25f) / (2.0f - 0.25f)
                            val cx = thumbRadius + trackWidth * dotFraction
                            drawCircle(
                                color = Color.White,
                                radius = 4.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(cx, centerY)
                            )
                            
                            val textLayoutResult = textMeasurer.measure(
                                text = label,
                                style = TextStyle(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            )
                            val tx = cx - textLayoutResult.size.width / 2f
                            val ty = centerY + 14.dp.toPx() // text closely below the dot
                            
                            drawText(
                                textLayoutResult = textLayoutResult,
                                topLeft = androidx.compose.ui.geometry.Offset(tx, ty)
                            )
                        }
                    }
                    
                    Slider(
                        value = speed,
                        onValueChange = { 
                            val snapped = (it * 20).roundToInt() / 20f
                            onSpeedChange(snapped.coerceIn(0.25f, 2.0f))
                        },
                        valueRange = 0.25f..2.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.Transparent,
                            inactiveTrackColor = Color.Transparent,
                            activeTickColor = Color.Transparent,
                            inactiveTickColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }"""

content = content.replace(old_column, new_column)

with open("app/src/main/java/com/example/ui/screens/SpeedControlPanel.kt", "w") as f:
    f.write(content)
