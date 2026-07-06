import re

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "r") as f:
    content = f.read()

# Add Player import if not there
if "import androidx.media3.common.Player" not in content:
    content = content.replace("import androidx.media3.common.MediaItem", "import androidx.media3.common.MediaItem\nimport androidx.media3.common.Player")

# Update onPlaybackStateChanged
old_playback = """            override fun onPlaybackStateChanged(playbackState: Int) {
                duration = player.duration.coerceAtLeast(0L)
            }"""
new_playback = """            override fun onPlaybackStateChanged(playbackState: Int) {
                duration = player.duration.coerceAtLeast(0L)
                if (playbackState == Player.STATE_ENDED) {
                    isPlaying = false
                }
            }"""
content = content.replace(old_playback, new_playback)

# Update onClick of Play/Pause
old_onclick = "onClick = { if (isPlaying) player.pause() else player.play() }"
new_onclick = """onClick = { 
                                        if (player.playbackState == Player.STATE_ENDED) {
                                            player.seekTo(0)
                                            player.play()
                                        } else {
                                            if (isPlaying) player.pause() else player.play() 
                                        }
                                    }"""
content = content.replace(old_onclick, new_onclick)

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "w") as f:
    f.write(content)
