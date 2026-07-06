import re

with open("app/src/main/java/com/example/ui/screens/SpeedControlPanel.kt", "r") as f:
    content = f.read()

new_labels = """        // Labels
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

content = re.sub(r'        // Labels\n        Row\([\s\S]*?\}', new_labels, content)

with open("app/src/main/java/com/example/ui/screens/SpeedControlPanel.kt", "w") as f:
    f.write(content)
