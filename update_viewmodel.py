import re

with open("app/src/main/java/com/example/viewmodel/VideoPlayerViewModel.kt", "r") as f:
    content = f.read()

funcs = """
    fun playNextVideo() {
        val currentVideo = _activeVideo.value ?: return
        val currentList = videosInSelectedFolder.value
        if (currentList.isEmpty()) return
        val currentIndex = currentList.indexOfFirst { it.id == currentVideo.id }
        if (currentIndex != -1 && currentIndex < currentList.size - 1) {
            _activeVideo.value = currentList[currentIndex + 1]
        }
    }

    fun playPreviousVideo() {
        val currentVideo = _activeVideo.value ?: return
        val currentList = videosInSelectedFolder.value
        if (currentList.isEmpty()) return
        val currentIndex = currentList.indexOfFirst { it.id == currentVideo.id }
        if (currentIndex > 0) {
            _activeVideo.value = currentList[currentIndex - 1]
        }
    }
}"""

content = content.rstrip()
if content.endswith("}"):
    content = content[:-1] + funcs

with open("app/src/main/java/com/example/viewmodel/VideoPlayerViewModel.kt", "w") as f:
    f.write(content)
