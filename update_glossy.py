import re

with open("app/src/main/java/com/example/ui/theme/Glossy.kt", "r") as f:
    content = f.read()

# Add blurRadius parameter
content = content.replace("    borderWidth: Dp = 1.dp\n): Modifier {", "    borderWidth: Dp = 1.dp,\n    blurRadius: Float = 16f\n): Modifier {")

# Update colors to use blurRadius logic
old_dark = """                Color(0x73333333), // 45% white-ish gray
                Color(0x401A1A1A)  // 25% darker center"""

new_dark = """                Color(0x333333).copy(alpha = (0.45f * (blurRadius / 16f)).coerceIn(0.2f, 0.9f)),
                Color(0x1A1A1A).copy(alpha = (0.25f * (blurRadius / 16f)).coerceIn(0.1f, 0.8f))"""

old_light = """                Color(0xE6FFFFFF), // 90% white
                Color(0x99FFFFFF)  // 60% white"""

new_light = """                Color.White.copy(alpha = (0.90f * (blurRadius / 16f)).coerceIn(0.5f, 0.98f)),
                Color.White.copy(alpha = (0.60f * (blurRadius / 16f)).coerceIn(0.3f, 0.95f))"""

content = content.replace(old_dark, new_dark)
content = content.replace(old_light, new_light)

with open("app/src/main/java/com/example/ui/theme/Glossy.kt", "w") as f:
    f.write(content)
print("Updated Glossy.kt")
