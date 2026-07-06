import re

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "r") as f:
    content = f.read()

# Add to arguments
content = content.replace("    onBack: () -> Unit,\n    modifier: Modifier = Modifier", "    onBack: () -> Unit,\n    onNextVideo: () -> Unit,\n    onPreviousVideo: () -> Unit,\n    modifier: Modifier = Modifier")

# Change Previous button logic
content = content.replace('IconButton(onClick = { player.seekTo((player.currentPosition - 10000).coerceAtLeast(0L)) }) {\n                                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous"', 'IconButton(onClick = onPreviousVideo) {\n                                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous"')

# Change Next button logic
content = content.replace('IconButton(onClick = { player.seekTo((player.currentPosition + 10000).coerceAtMost(duration)) }) {\n                                    Icon(Icons.Default.SkipNext, contentDescription = "Next"', 'IconButton(onClick = onNextVideo) {\n                                    Icon(Icons.Default.SkipNext, contentDescription = "Next"')

# PiP is already implemented via enterPictureInPictureMode! But let's check it:
# activity?.enterPictureInPictureMode(android.app.PictureInPictureParams.Builder().build())

# Aspect Ratio Change button logic is already implemented! Let's check it.
#                                IconButton(
#                                    onClick = {
#                                        aspectMode = when (aspectMode) {
#                                            AspectMode.FIT -> AspectMode.ZOOM
#                                            AspectMode.ZOOM -> AspectMode.FILL
#                                            AspectMode.FILL -> AspectMode.FIT
#                                        }
#                                        ...
# This changes aspect mode.

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("                                        onBack = { viewModel.setScreen(Screen.Explorer) }", "                                        onBack = { viewModel.setScreen(Screen.Explorer) },\n                                        onNextVideo = { viewModel.playNextVideo() },\n                                        onPreviousVideo = { viewModel.playPreviousVideo() }")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
