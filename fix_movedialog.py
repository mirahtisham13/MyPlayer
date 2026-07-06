import re

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "r") as f:
    content = f.read()

# Update MoveDialog items loop
old_lazy_column = """                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(folders) { (name, count) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { }.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp, 48.dp)
                                    .background(Color(0x4D000000), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFF1E2328), modifier = Modifier.size(32.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Text(count, color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    }
                }"""

new_lazy_column = """                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(folders) { folder ->
                        val isSelected = selectedTargetFolder == folder
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable { selectedTargetFolder = folder }
                                .background(if (isSelected) Color(0x333B82F6) else Color.Transparent)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp, 48.dp)
                                    .background(Color(0x4D000000), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(folder.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Text("${folder.videoCount} videos", color = Color.Gray, fontSize = 14.sp)
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = Color(0xFF3B82F6))
                            }
                        }
                    }
                }"""

content = content.replace(old_lazy_column, new_lazy_column)

# Update onClick = onMoveHere
content = content.replace("onClick = onMoveHere,", "onClick = { selectedTargetFolder?.let { onMoveHere(it.path) } },\n                        enabled = selectedTargetFolder != null,")

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "w") as f:
    f.write(content)
