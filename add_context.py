import re

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "r") as f:
    content = f.read()

# Add context
content = content.replace("    var isSearchActive by remember { mutableStateOf(false) }", "    val context = LocalContext.current\n    var isSearchActive by remember { mutableStateOf(false) }")

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "w") as f:
    f.write(content)
print("Context added")
