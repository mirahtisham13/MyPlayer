import re

with open("app/src/main/java/com/example/ui/screens/SpeedControlPanel.kt", "r") as f:
    content = f.read()

# Replace decrease button coerceAtLeast to 1.0f
content = content.replace("onSpeedChange((speed - 0.05f).coerceAtLeast(0.25f))", "onSpeedChange((speed - 0.05f).coerceAtLeast(1.0f))")

old_update_speed = """                                    fun updateSpeed(x: Float) {
                                        val boundedX = x.coerceIn(thumbRadius, size.width - thumbRadius.toFloat())
                                        val fraction = (boundedX - thumbRadius) / trackWidth
                                        val newValue = 0.25f + fraction * (2.0f - 0.25f)
                                        
                                        var closestDot = 0.25f
                                        var minDiff = Float.MAX_VALUE
                                        val dots = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
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
                                        
                                        onSpeedChange(finalValue.coerceIn(0.25f, 2.0f))
                                    }"""

new_update_speed = """                                    fun updateSpeed(x: Float) {
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
                                    }"""

content = content.replace(old_update_speed, new_update_speed)

old_canvas_logic = """                        val fraction = (speed - 0.25f) / (2.0f - 0.25f)
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
                            val dotFraction = (value - 0.25f) / (2.0f - 0.25f)"""

new_canvas_logic = """                        val fraction = ((speed.coerceIn(1.0f, 2.0f)) - 1.0f) / (2.0f - 1.0f)
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
                            val dotFraction = (value - 1.0f) / (2.0f - 1.0f)"""

content = content.replace(old_canvas_logic, new_canvas_logic)

with open("app/src/main/java/com/example/ui/screens/SpeedControlPanel.kt", "w") as f:
    f.write(content)
