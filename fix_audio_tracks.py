import re

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "r") as f:
    content = f.read()

# Add showAudioDialog state
content = content.replace("var showSubtitleDialog by remember { mutableStateOf(false) }", "var showSubtitleDialog by remember { mutableStateOf(false) }\n    var showAudioDialog by remember { mutableStateOf(false) }")

# Update IconButton
old_icon_button = """                        IconButton(onClick = { /* Audio Tracks */ }) {
                            Icon(Icons.Default.MusicNote, contentDescription = "Audio Tracks", tint = Color.White)
                        }"""
new_icon_button = """                        IconButton(onClick = { showAudioDialog = true }) {
                            Icon(Icons.Default.MusicNote, contentDescription = "Audio Tracks", tint = Color.White)
                        }"""
content = content.replace(old_icon_button, new_icon_button)

# Add Audio Track Dialog
audio_dialog_code = """    // Audio Dialog
    if (showAudioDialog) {
        AlertDialog(
            onDismissRequest = { showAudioDialog = false },
            title = { Text("Audio Tracks", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = Color.Transparent,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.9f), RoundedCornerShape(16.dp)),
            text = {
                Column {
                    val audioGroups = player.currentTracks.groups.filter { it.type == androidx.media3.common.C.TRACK_TYPE_AUDIO }
                    if (audioGroups.isEmpty()) {
                        Text("No audio tracks available", color = Color.Gray)
                    } else {
                        LazyColumn {
                            items(audioGroups.size) { groupIndex ->
                                val group = audioGroups[groupIndex]
                                val isSelected = group.isSelected
                                val trackName = group.mediaTrackGroup.getFormat(0).let { format ->
                                    val lang = format.language ?: "Unknown"
                                    val label = format.label ?: ""
                                    if (label.isNotEmpty()) "$label ($lang)" else lang
                                }
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            player.trackSelectionParameters = player.trackSelectionParameters
                                                .buildUpon()
                                                .setOverrideForType(androidx.media3.common.TrackSelectionOverride(group.mediaTrackGroup, 0))
                                                .build()
                                            showAudioDialog = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = null,
                                        colors = RadioButtonDefaults.colors(selectedColor = Color.White, unselectedColor = Color.Gray)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(trackName.uppercase(), color = if (isSelected) Color.White else Color.Gray)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAudioDialog = false }) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }
"""

# Insert audio dialog before Subtitles Dialog
content = content.replace("    // Subtitles Dialog", audio_dialog_code + "\n    // Subtitles Dialog")

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "w") as f:
    f.write(content)
