import re

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "r") as f:
    content = f.read()

# Replace hardcoded solid colors with translucent Apple Liquid Glass colors
content = content.replace("Color(0xFF1E1E1E)", "Color(0x33FFFFFF)") # Header background
content = content.replace("Color(0xFF2C3238)", "Color(0x4D000000)") # Move folder item
content = content.replace("Color(0xFF121212)", "Color.Transparent") # VideoThumbnail box
content = content.replace("Color(0xFF0A0A0A)", "Color(0x1AFFFFFF)") # VideoThumbnail placeholder
content = content.replace("Color.Black.copy(alpha = 0.75f)", "Color(0x80000000)") # Duration badges

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "w") as f:
    f.write(content)
print("Updated colors")
