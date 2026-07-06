import re

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "r") as f:
    content = f.read()

start_marker = "                // Header (Back + Video Title)"
end_marker = "            }\n        }\n\n        // Gesture Action floating HUD"

new_content = """                if (!isControlsLocked) {
                    // Header (Back + Video Title)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = video.displayTitle,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { /* Audio Tracks */ }) {
                            Icon(Icons.Default.MusicNote, contentDescription = "Audio Tracks", tint = Color.White)
                        }
                        IconButton(onClick = { showSubtitleDialog = true }) {
                            Icon(Icons.Default.Subtitles, contentDescription = "Subtitles", tint = Color.White)
                        }
                        IconButton(onClick = { /* More options */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                        }
                    }

                    // Left Controls (Rotation, Speed)
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 80.dp, start = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(onClick = {
                            activity?.let { act ->
                                if (act.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || act.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                                    act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                } else {
                                    act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                }
                            }
                        }) {
                            Icon(Icons.Default.ScreenRotation, contentDescription = "Rotation", tint = Color.White)
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF3B5998), CircleShape)
                                .clickable { showSpeedDialog = true }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${speed}X", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Bottom Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
                ) {
                    if (!isControlsLocked) {
                        // Seek Bar Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatTime(currentPosition),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Slider(
                                value = currentPosition.toFloat(),
                                onValueChange = {
                                    player.seekTo(it.toLong())
                                    currentPosition = it.toLong()
                                },
                                valueRange = 0f..(duration.toFloat().coerceAtLeast(1f)),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = Color.White,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp)
                                    .testTag("player_seek_slider")
                            )
                            Text(
                                text = "-" + formatTime(duration - currentPosition),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
    
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Media Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left
                        IconButton(onClick = { isControlsLocked = !isControlsLocked }) {
                            Icon(if (isControlsLocked) Icons.Default.Lock else Icons.Default.LockOpen, contentDescription = "Lock", tint = Color.White)
                        }

                        if (!isControlsLocked) {
                            // Center
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                                IconButton(onClick = { player.seekTo((player.currentPosition - 10000).coerceAtLeast(0L)) }) {
                                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(32.dp))
                                }
                                IconButton(
                                    onClick = { if (isPlaying) player.pause() else player.play() }
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        tint = Color.White,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                                IconButton(onClick = { player.seekTo((player.currentPosition + 10000).coerceAtMost(duration)) }) {
                                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(32.dp))
                                }
                            }
    
                            // Right
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    activity?.let { act ->
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                            act.enterPictureInPictureMode(android.app.PictureInPictureParams.Builder().build())
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.PictureInPictureAlt, contentDescription = "PiP", tint = Color.White)
                                }
                                IconButton(
                                    onClick = {
                                        aspectMode = when (aspectMode) {
                                            AspectMode.FIT -> AspectMode.ZOOM
                                            AspectMode.ZOOM -> AspectMode.FILL
                                            AspectMode.FILL -> AspectMode.FIT
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Fullscreen, contentDescription = "Aspect Ratio", tint = Color.White)
                                }
                            }
                        }
                    }
                }
"""

start_idx = content.find(start_marker)
end_idx = content.find(end_marker)

if start_idx != -1 and end_idx != -1:
    new_text = content[:start_idx] + new_content + content[end_idx:]
    with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "w") as f:
        f.write(new_text)
    print("Replaced successfully")
else:
    print(f"Could not find markers. start: {start_idx}, end: {end_idx}")

