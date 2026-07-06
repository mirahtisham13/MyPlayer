import re

with open("app/src/main/java/com/example/ui/screens/SpeedControlPanel.kt", "r") as f:
    content = f.read()

# Make sure imports exist
if "import androidx.compose.ui.input.pointer.pointerInput" not in content:
    content = content.replace("import androidx.compose.ui.text.TextStyle", "import androidx.compose.ui.text.TextStyle\nimport androidx.compose.ui.input.pointer.pointerInput\nimport androidx.compose.foundation.gestures.awaitFirstDown\nimport androidx.compose.foundation.gestures.horizontalDrag\nimport androidx.compose.foundation.gestures.awaitEachGesture")

old_slider_section = """            Column(modifier = Modifier.weight(1f).padding(horizontal = 32.dp)) {
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

new_slider_section = """            Column(modifier = Modifier.weight(1f).padding(horizontal = 48.dp)) {
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
                                        val newValue = 0.25f + fraction * (2.0f - 0.25f)
                                        val snapped = (newValue * 20).roundToInt() / 20f
                                        onSpeedChange(snapped.coerceIn(0.25f, 2.0f))
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
                        
                        val fraction = (speed - 0.25f) / (2.0f - 0.25f)
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
                        
                        val labels = listOf("0.25x" to 0.25f, "0.5x" to 0.5f, "0.75x" to 0.75f, "1.0x" to 1.0f, "1.25x" to 1.25f, "1.5x" to 1.5f, "1.75x" to 1.75f, "2.0x" to 2.0f)
                        labels.forEach { (label, value) ->
                            val dotFraction = (value - 0.25f) / (2.0f - 0.25f)
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
                        drawCircle(
                            color = Color.White,
                            radius = 8.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(thumbX, centerY)
                        )
                    }
                }
            }"""

content = content.replace(old_slider_section, new_slider_section)

with open("app/src/main/java/com/example/ui/screens/SpeedControlPanel.kt", "w") as f:
    f.write(content)
