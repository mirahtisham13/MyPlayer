import re

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "r") as f:
    content = f.read()

# Update FolderListRow thumbnail
content = content.replace(".size(width = 64.dp, height = 48.dp)", ".size(width = 88.dp, height = 72.dp)")
content = content.replace("tint = if (isSelected) Color(0xFF3B82F6) else if (isDark) Color(0xFF6B7280) else Color(0xFF6B7280)\n            )", 
                          "tint = if (isSelected) Color(0xFF3B82F6) else if (isDark) Color(0xFF6B7280) else Color(0xFF6B7280),\n                modifier = Modifier.size(40.dp)\n            )")

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "w") as f:
    f.write(content)
