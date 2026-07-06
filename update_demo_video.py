import re

with open("app/src/main/java/com/example/viewmodel/VideoPlayerViewModel.kt", "r") as f:
    content = f.read()

new_samples = """        val samples = listOf(
            VideoItem(
                id = -1,
                path = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                title = "Demo Video.mp4",
                durationMs = 15000L, // 00:15
                sizeBytes = 15000000L, // 15 MB
                resolution = "1080p",
                hasSubtitle = false,
                subtitlePath = null,
                isOnline = true
            )
        )"""

content = re.sub(r'val samples = listOf\([\s\S]*?isOnline = true\n            \)\n        \)', new_samples, content)

with open("app/src/main/java/com/example/viewmodel/VideoPlayerViewModel.kt", "w") as f:
    f.write(content)
