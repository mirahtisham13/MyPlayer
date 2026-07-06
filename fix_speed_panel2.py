import re

with open("app/src/main/java/com/example/ui/screens/SpeedControlPanel.kt", "r") as f:
    content = f.read()

# 1. Remove Edit Icon and the Spacer before it
content = content.replace("""                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(16.dp))""", "")

# 2. Update Slider values and logic
old_slider_box = """            Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
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

new_slider_box = """            Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
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
                    
                    val dots = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
                    dots.forEach { value ->
                        val dotFraction = (value - 0.25f) / (2.0f - 0.25f)
                        val cx = thumbRadius + trackWidth * dotFraction
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(cx, height / 2f)
                        )
                    }
                }
                
                Slider(
                    value = speed,
                    onValueChange = { 
                        val snapped = (it * 4).roundToInt() / 4f
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
            }"""

content = content.replace(old_slider_box, new_slider_box)

old_labels_box = """        // Labels
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 72.dp)
        ) {
            val labels = listOf("0.25x" to 0.25f, "1.0x" to 1.0f, "2.0x" to 2.0f, "3.0x" to 3.0f, "4.0x" to 4.0f)
            labels.forEach { (label, value) ->
                val fraction = (value - 0.25f) / (4.0f - 0.25f)
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(androidx.compose.ui.BiasAlignment(horizontalBias = -1f + 2f * fraction, verticalBias = 0f))
                )
            }
        }"""

new_labels_box = """        // Labels
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 72.dp)
        ) {
            val labels = listOf("0.25x" to 0.25f, "1.0x" to 1.0f, "1.5x" to 1.5f, "2.0x" to 2.0f)
            labels.forEach { (label, value) ->
                val fraction = (value - 0.25f) / (2.0f - 0.25f)
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(androidx.compose.ui.BiasAlignment(horizontalBias = -1f + 2f * fraction, verticalBias = 0f))
                )
            }
        }"""

content = content.replace(old_labels_box, new_labels_box)

# Need to fix the + and - buttons coerce values
content = content.replace("onSpeedChange((speed + 0.25f).coerceAtMost(4.0f))", "onSpeedChange((speed + 0.25f).coerceAtMost(2.0f))")

with open("app/src/main/java/com/example/ui/screens/SpeedControlPanel.kt", "w") as f:
    f.write(content)
