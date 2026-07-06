import re

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "r") as f:
    content = f.read()

# Replace MoreVert with Speed Text
header_icons_old = """                        IconButton(onClick = { showSubtitleDialog = true }) {
                            Icon(Icons.Default.Subtitles, contentDescription = "Subtitles", tint = Color.White)
                        }
                        IconButton(onClick = { /* More options */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                        }
                    }"""
                    
header_icons_new = """                        IconButton(onClick = { showSubtitleDialog = true }) {
                            Icon(Icons.Default.Subtitles, contentDescription = "Subtitles", tint = Color.White)
                        }
                        Box(
                            modifier = Modifier
                                .clickable { showSpeedDialog = true }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${speed}X", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }"""

content = content.replace(header_icons_old, header_icons_new)

# Remove the old Speed box from the Left Controls
left_controls_old = """                        IconButton(onClick = {
                            videoRotation = (videoRotation + 90f) % 360f
                        }) {
                            Icon(Icons.Default.ScreenRotation, contentDescription = "Rotation", tint = Color.White)
                        }
                        Box(
                            modifier = Modifier
                                .clickable { showSpeedDialog = true }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${speed}X", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }"""

left_controls_new = """                        IconButton(onClick = {
                            videoRotation = (videoRotation + 90f) % 360f
                        }) {
                            Icon(Icons.Default.ScreenRotation, contentDescription = "Rotation", tint = Color.White)
                        }
                    }"""

content = content.replace(left_controls_old, left_controls_new)

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "w") as f:
    f.write(content)
