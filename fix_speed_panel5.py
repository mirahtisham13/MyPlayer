import re

with open("app/src/main/java/com/example/ui/screens/SpeedControlPanel.kt", "r") as f:
    content = f.read()

old_update_speed = """                                    fun updateSpeed(x: Float) {
                                        val boundedX = x.coerceIn(thumbRadius, size.width - thumbRadius.toFloat())
                                        val fraction = (boundedX - thumbRadius) / trackWidth
                                        val newValue = 0.25f + fraction * (2.0f - 0.25f)
                                        val snapped = (newValue * 20).roundToInt() / 20f
                                        onSpeedChange(snapped.coerceIn(0.25f, 2.0f))
                                    }"""

new_update_speed = """                                    fun updateSpeed(x: Float) {
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

content = content.replace(old_update_speed, new_update_speed)

with open("app/src/main/java/com/example/ui/screens/SpeedControlPanel.kt", "w") as f:
    f.write(content)
