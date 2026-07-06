import re

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "r") as f:
    content = f.read()

content = content.replace("containerColor = if (isDarkTheme) Color.White else Color.Black,", "containerColor = if (isDarkTheme) Color.Black else Color.White,")
content = content.replace("contentColor = if (isDarkTheme) Color.Black else Color.White,", "contentColor = if (isDarkTheme) Color.White else Color.Black,")

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "w") as f:
    f.write(content)
