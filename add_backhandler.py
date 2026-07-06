import re

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "r") as f:
    content = f.read()

imports = "import androidx.activity.compose.BackHandler\nimport androidx.compose.animation.AnimatedVisibility\n"
if "import androidx.activity.compose.BackHandler" not in content:
    content = content.replace("import androidx.compose.animation.AnimatedVisibility", imports)

back_handler_code = """
    BackHandler(enabled = isSelectionMode || selectedFolder != null) {
        if (isSelectionMode) {
            selectedFolderPaths = emptySet()
            selectedVideoIds = emptySet()
        } else if (selectedFolder != null) {
            onFolderSelect(null)
        }
    }
"""

# Find where to put BackHandler
# Put it right after remembering states
content = content.replace("    var isSelectionMode = selectedFolderPaths.isNotEmpty() || selectedVideoIds.isNotEmpty()", 
                          "    var isSelectionMode = selectedFolderPaths.isNotEmpty() || selectedVideoIds.isNotEmpty()\n" + back_handler_code)

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "w") as f:
    f.write(content)
print("Added BackHandler")
