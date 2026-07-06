import re

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "r") as f:
    content = f.read()

content = content.replace("override fun onIsPlayingChanged(playing: Boolean) {", "override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {")
content = content.replace("isPlaying = playing", "isPlaying = playWhenReady")

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "w") as f:
    f.write(content)
