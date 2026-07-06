import re

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "r") as f:
    content = f.read()

content = content.replace("containerColor = if (isDarkTheme) Color.Black else Color.White,", "containerColor = if (isDarkTheme) Color.White else Color.Black,")
content = content.replace("contentColor = if (isDarkTheme) Color.White else Color.Black,", "contentColor = if (isDarkTheme) Color.Black else Color.White,")

content = content.replace("""            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.size(28.dp)
            )""", """            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.size(40.dp)
            )""")

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "w") as f:
    f.write(content)
