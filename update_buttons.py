import re

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "r") as f:
    content = f.read()

old_row = """                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { showMoveDialog = true }) {
                            Icon(Icons.Default.DriveFileMove, contentDescription = "Move")
                            Text("Move", fontSize = 12.sp)
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                            Text("Delete", fontSize = 12.sp)
                        }
                        
                        if (selectedFolderPaths.size + selectedVideoIds.size == 1) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { showRenameDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Rename")
                                Text("Rename", fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { showInfoDialog = true }) {
                                Icon(Icons.Default.Info, contentDescription = "Info")
                                Text("Info", fontSize = 12.sp)
                            }
                        }
                    }"""

new_row = """                    Row(
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
                    }"""

content = content.replace(old_row, new_row)
content = content.replace("import androidx.compose.material.icons.filled.DriveFileMove", "import androidx.compose.material.icons.automirrored.filled.DriveFileMove")

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "w") as f:
    f.write(content)

print("Updated buttons")
