import re

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "r") as f:
    content = f.read()

checkbox_code = """                        // Select All Checkbox
                        Checkbox(
                            checked = allSelected,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    if (selectedFolder == null) {
                                        selectedFolderPaths = folders.map { it.path }.toSet()
                                    }
                                    selectedVideoIds = videos.map { it.id }.toSet()
                                } else {
                                    selectedFolderPaths = emptySet()
                                    selectedVideoIds = emptySet()
                                }
                            }
                        )"""

# Remove from topBar
content = content.replace(checkbox_code, "")

new_checkbox_code = """                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Checkbox(
                                checked = allSelected,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (selectedFolder == null) {
                                            selectedFolderPaths = folders.map { it.path }.toSet()
                                        }
                                        selectedVideoIds = videos.map { it.id }.toSet()
                                    } else {
                                        selectedFolderPaths = emptySet()
                                        selectedVideoIds = emptySet()
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Select All", fontSize = 12.sp)
                        }"""

# Insert into bottomBar before Move
content = content.replace(
    'Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { showMoveDialog = true }) {',
    new_checkbox_code + '\n                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { showMoveDialog = true }) {'
)

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "w") as f:
    f.write(content)

print("Moved checkbox")
