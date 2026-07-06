import re

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "r") as f:
    content = f.read()

old_lazy_column = """                        LazyColumn {
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
                        }"""

new_column = """                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
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
                        }"""

content = content.replace(old_lazy_column, new_column)

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "w") as f:
    f.write(content)
