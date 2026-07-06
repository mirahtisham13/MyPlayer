import re

with open("app/src/main/java/com/example/ui/screens/SpeedControlPanel.kt", "r") as f:
    content = f.read()

# Make the column padding larger (horizontal = 64.dp)
content = content.replace("Column(modifier = Modifier.weight(1f).padding(horizontal = 48.dp))", "Column(modifier = Modifier.weight(1f).padding(horizontal = 64.dp))")

old_thumb_drawing = """                        val thumbX = thumbRadius + activeWidth
                        drawCircle(
                            color = Color.White,
                            radius = 8.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(thumbX, centerY)
                        )"""

new_thumb_drawing = """                        val thumbX = thumbRadius + activeWidth
                        drawLine(
                            color = Color.White,
                            start = androidx.compose.ui.geometry.Offset(thumbX, centerY - 8.dp.toPx()),
                            end = androidx.compose.ui.geometry.Offset(thumbX, centerY + 8.dp.toPx()),
                            strokeWidth = 3.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )"""

content = content.replace(old_thumb_drawing, new_thumb_drawing)

with open("app/src/main/java/com/example/ui/screens/SpeedControlPanel.kt", "w") as f:
    f.write(content)
