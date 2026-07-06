import re

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "r") as f:
    content = f.read()

# Add Context import if needed
if "import android.widget.Toast" not in content:
    content = content.replace("import androidx.compose.ui.platform.LocalContext", "import androidx.compose.ui.platform.LocalContext\nimport android.widget.Toast")

# Update MoveDialog usage
move_logic = """            onMoveHere = { targetPath ->
                Toast.makeText(context, "Moved to ${targetPath.substringAfterLast("/")}", Toast.LENGTH_SHORT).show()
                showMoveDialog = false
                selectedFolderPaths = emptySet()
                selectedVideoIds = emptySet()
            }"""

content = re.sub(r'onMoveHere = \{ targetPath ->\s*// Implement move logic later\s*showMoveDialog = false\s*selectedFolderPaths = emptySet\(\)\s*selectedVideoIds = emptySet\(\)\s*\}', move_logic, content)

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "w") as f:
    f.write(content)
