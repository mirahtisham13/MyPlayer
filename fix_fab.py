import re

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "r") as f:
    content = f.read()

content = content.replace("containerColor = Color.White,", "containerColor = if (isDarkTheme) Color.White else Color.Black,")
content = content.replace("contentColor = Color.Black,", "contentColor = if (isDarkTheme) Color.Black else Color.White,")

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "w") as f:
    f.write(content)
