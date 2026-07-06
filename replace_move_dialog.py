import re

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "r") as f:
    content = f.read()

# Replace MoveDialog containerColor
old_str = "            containerColor = Color.Black,"
new_str = "            containerColor = Color.Transparent,"

content = content.replace(old_str, new_str)

# Add glossy background to Scaffold
old_scaffold = "        Scaffold(\n            modifier = Modifier.fillMaxSize(),"
new_scaffold = """        Scaffold(
            modifier = Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    colors = listOf(Color(0xE62A2A3A), Color(0xCC101015), Color(0x99000000)),
                    radius = 2000f,
                    center = Offset(500f, 500f)
                )
            ),"""

content = content.replace(old_scaffold, new_scaffold)

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "w") as f:
    f.write(content)

print("Updated MoveDialog")
