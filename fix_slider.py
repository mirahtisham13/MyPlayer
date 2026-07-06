import re

with open("app/src/main/java/com/example/ui/screens/SpeedControlPanel.kt", "r") as f:
    content = f.read()

# add Canvas import if missing
if "import androidx.compose.foundation.Canvas" not in content:
    content = content.replace("import androidx.compose.foundation.background", "import androidx.compose.foundation.background\nimport androidx.compose.foundation.Canvas")

slider_box_old = """            Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Slider(
                    value = speed,
                    onValueChange = { 
                        // Snap to 0.25 intervals roughly
                        val snapped = (it * 4).roundToInt() / 4f
                        onSpeedChange(snapped.coerceIn(0.25f, 4.0f))
                    },
                    valueRange = 0.25f..4.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color(0xFF4285F4),
                        inactiveTrackColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }"""

slider_box_new = """            Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxWidth().height(4.dp)) {
                    val width = size.width
                    val height = size.height
                    
                    val thumbRadius = 10.dp.toPx()
                    val trackWidth = width - 2 * thumbRadius
                    
                    val fraction = (speed - 0.25f) / (4.0f - 0.25f)
                    val activeWidth = trackWidth * fraction
                    
                    drawLine(
                        color = Color.Gray,
                        start = androidx.compose.ui.geometry.Offset(thumbRadius, height / 2f),
                        end = androidx.compose.ui.geometry.Offset(width - thumbRadius, height / 2f),
                        strokeWidth = 2.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    
                    if (activeWidth > 0) {
                        drawLine(
                            color = Color(0xFF4285F4),
                            start = androidx.compose.ui.geometry.Offset(thumbRadius, height / 2f),
                            end = androidx.compose.ui.geometry.Offset(thumbRadius + activeWidth, height / 2f),
                            strokeWidth = 2.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                    
                    val labels = listOf(0.25f, 1.0f, 2.0f, 3.0f, 4.0f)
                    labels.forEach { value ->
                        val dotFraction = (value - 0.25f) / (4.0f - 0.25f)
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
                        val snapped = (it * 4).roundToInt() / 4f
                        onSpeedChange(snapped.coerceIn(0.25f, 4.0f))
                    },
                    valueRange = 0.25f..4.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent,
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }"""

content = content.replace(slider_box_old, slider_box_new)

with open("app/src/main/java/com/example/ui/screens/SpeedControlPanel.kt", "w") as f:
    f.write(content)
