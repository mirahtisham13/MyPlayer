import re

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "r") as f:
    content = f.read()

content = content.replace("val timeUs = player.currentPosition * 1000L", "val timeUs = withContext(kotlinx.coroutines.Dispatchers.Main) { player.currentPosition * 1000L }")

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "w") as f:
    f.write(content)
