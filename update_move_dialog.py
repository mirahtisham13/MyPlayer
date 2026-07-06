import re

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "r") as f:
    content = f.read()

# Update MoveDialog signature
content = content.replace("fun MoveDialog(\n    selectedCount: Int,\n    onDismiss: () -> Unit,\n    onMoveHere: () -> Unit\n) {", 
                          "fun MoveDialog(\n    folders: List<com.example.model.FolderItem>,\n    selectedCount: Int,\n    onDismiss: () -> Unit,\n    onMoveHere: (String) -> Unit\n) {")

# Remove mock folders
mock_folders = """    // Mock folders based on user screenshot
    val folders = listOf(
        "Android" to "4 folders",
        "DCIM" to "1 folder",
        "Download" to "2 folders",
        "Movies" to "1 folder",
        "Pictures" to "1 folder",
        "Recordings" to "1 folder",
        "Screenshots" to "0 folders",
        "Subtitles" to "0 folders"
    )"""

content = content.replace(mock_folders, "    var selectedTargetFolder by remember { mutableStateOf<com.example.model.FolderItem?>(null) }")

# Update MoveDialog usage
content = content.replace("MoveDialog(\n            selectedCount = selectedFolderPaths.size + selectedVideoIds.size,",
                          "MoveDialog(\n            folders = folders,\n            selectedCount = selectedFolderPaths.size + selectedVideoIds.size,")

content = content.replace("onMoveHere = {\n                // Implement move logic later",
                          "onMoveHere = { targetPath ->\n                // Implement move logic later")

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "w") as f:
    f.write(content)
