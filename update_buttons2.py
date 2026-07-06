import re

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "r") as f:
    content = f.read()

# Replace the inner part of BottomAppBar row
pattern = re.compile(r'Row\(\s*modifier = Modifier\.fillMaxWidth\(\)\.padding\(start = 16\.dp, end = 16\.dp, top = 4\.dp, bottom = 12\.dp\),\s*horizontalArrangement = Arrangement\.SpaceEvenly,\s*verticalAlignment = Alignment\.CenterVertically\s*\)\s*\{.*?\n\s*\}\s*\}\s*\}\s*\},', re.DOTALL)

new_row = """Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                        )
                        IconButton(onClick = { showMoveDialog = true }) {
                            Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = "Move")
                        }
                        
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                        
                        if (selectedFolderPaths.size + selectedVideoIds.size == 1) {
                            IconButton(onClick = { showRenameDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Rename")
                            }
                            IconButton(onClick = { showInfoDialog = true }) {
                                Icon(Icons.Default.Info, contentDescription = "Info")
                            }
                        }
                    }
                }
            }
        },"""

if pattern.search(content):
    content = pattern.sub(new_row, content)
    print("Replaced!")
else:
    print("Not found")

content = content.replace("import androidx.compose.material.icons.filled.DriveFileMove", "import androidx.compose.material.icons.automirrored.filled.DriveFileMove")

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "w") as f:
    f.write(content)
