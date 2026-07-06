import re

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "r") as f:
    content = f.read()

# I will find the first "// Audio Dialog" and the end of the file (before IconButtonWithLabel)
# Then I'll rewrite both Audio Dialog and Subtitles Dialog properly.

audio_dialog = """    // Audio Dialog
    if (showAudioDialog) {
        AlertDialog(
            onDismissRequest = { showAudioDialog = false },
            title = { Text("Audio Tracks", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = Color.Transparent,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.9f), RoundedCornerShape(16.dp)),
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    val audioGroups = player.currentTracks.groups.filter { it.type == androidx.media3.common.C.TRACK_TYPE_AUDIO }
                    if (audioGroups.isEmpty()) {
                        Text("No audio tracks available", color = Color.Gray)
                    } else {
                        audioGroups.forEach { group ->
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
            },
            confirmButton = {
                TextButton(onClick = { showAudioDialog = false }) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }"""

subtitles_dialog = """    // Subtitles Dialog
    if (showSubtitleDialog) {
        var subtitleTab by remember { mutableIntStateOf(0) }
        val context = androidx.compose.ui.platform.LocalContext.current
        
        val subtitlePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                val currentItem = player.currentMediaItem
                if (currentItem != null) {
                    val subtitleConfig = androidx.media3.common.MediaItem.SubtitleConfiguration.Builder(uri)
                        .setMimeType(androidx.media3.common.MimeTypes.APPLICATION_SUBRIP)
                        .setLanguage("en")
                        .setSelectionFlags(androidx.media3.common.C.SELECTION_FLAG_DEFAULT)
                        .build()
                        
                    val newItem = currentItem.buildUpon()
                        .setSubtitleConfigurations(currentItem.localConfiguration?.subtitleConfigurations.orEmpty() + subtitleConfig)
                        .build()
                        
                    val pos = player.currentPosition
                    player.setMediaItem(newItem)
                    player.seekTo(pos)
                    player.prepare()
                    player.play()
                }
                showSubtitleDialog = false
            }
        }
        
        AlertDialog(
            onDismissRequest = { showSubtitleDialog = false },
            title = { Text("Subtitles", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = Color.Transparent,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.9f), RoundedCornerShape(16.dp)),
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            text = "Tracks", 
                            color = if (subtitleTab == 0) Color.White else Color.Gray,
                            fontWeight = if (subtitleTab == 0) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.clickable { subtitleTab = 0 }.padding(8.dp)
                        )
                        Text(
                            text = "Sync", 
                            color = if (subtitleTab == 1) Color.White else Color.Gray,
                            fontWeight = if (subtitleTab == 1) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.clickable { subtitleTab = 1 }.padding(8.dp)
                        )
                        Text(
                            text = "Online", 
                            color = if (subtitleTab == 2) Color.White else Color.Gray,
                            fontWeight = if (subtitleTab == 2) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.clickable { subtitleTab = 2 }.padding(8.dp)
                        )
                    }
                    
                    if (subtitleTab == 0) {
                        val textGroups = player.currentTracks.groups.filter { it.type == androidx.media3.common.C.TRACK_TYPE_TEXT }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    player.trackSelectionParameters = player.trackSelectionParameters
                                        .buildUpon()
                                        .setIgnoredTextSelectionFlags(androidx.media3.common.C.SELECTION_FLAG_DEFAULT.inv())
                                        .setTrackTypeDisabled(androidx.media3.common.C.TRACK_TYPE_TEXT, true)
                                        .build()
                                    showSubtitleDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = player.trackSelectionParameters.disabledTrackTypes.contains(androidx.media3.common.C.TRACK_TYPE_TEXT),
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = Color.White, unselectedColor = Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("OFF", color = Color.White)
                        }
                        
                        textGroups.forEach { group ->
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
                                            .setTrackTypeDisabled(androidx.media3.common.C.TRACK_TYPE_TEXT, false)
                                            .setOverrideForType(androidx.media3.common.TrackSelectionOverride(group.mediaTrackGroup, 0))
                                            .build()
                                        showSubtitleDialog = false
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
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        androidx.compose.material3.Button(
                            onClick = { subtitlePickerLauncher.launch("*/*") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = "Import", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import from device")
                        }
                    } else if (subtitleTab == 1) {
                        var audioDelay by remember { mutableFloatStateOf(0f) }
                        var subDelay by remember { mutableFloatStateOf(0f) }
                        
                        Text("Audio Delay", color = Color.Gray, fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Slider(
                                value = audioDelay,
                                onValueChange = { audioDelay = it },
                                valueRange = -5f..5f,
                                modifier = Modifier.weight(1f)
                            )
                            Text(String.format("%.1f s", audioDelay), color = Color.White, modifier = Modifier.width(50.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Subtitle Delay", color = Color.Gray, fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Slider(
                                value = subDelay,
                                onValueChange = { subDelay = it },
                                valueRange = -5f..5f,
                                modifier = Modifier.weight(1f)
                            )
                            Text(String.format("%.1f s", subDelay), color = Color.White, modifier = Modifier.width(50.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                        }
                        
                        Text("Note: Real-time offset adjustments require custom MediaSource implementations which are beyond this basic ExoPlayer integration.", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(top = 16.dp))
                    } else if (subtitleTab == 2) {
                        var searchQuery by remember { mutableStateOf(video.title) }
                        
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search OpenSubtitles", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF4285F4),
                                unfocusedBorderColor = Color.Gray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        androidx.compose.material3.Button(
                            onClick = { /* Mock Search */ },
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Search")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("OpenSubtitles API integration requires a registered API key.", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSubtitleDialog = false }) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }"""

start_idx = content.find("    // Audio Dialog")
end_idx = content.find("}\n\n@Composable\nfun IconButtonWithLabel")

if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + audio_dialog + "\n\n" + subtitles_dialog + "\n" + content[end_idx:]

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "w") as f:
    f.write(content)
