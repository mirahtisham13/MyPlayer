import re

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "r") as f:
    content = f.read()

speed_dialog_code = """    // Playback Speed Dialog
    if (showSpeedDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) { showSpeedDialog = false },
            contentAlignment = Alignment.BottomCenter
        ) {
            SpeedControlPanel(
                speed = speed,
                onSpeedChange = { s -> 
                    speed = s
                    player.setPlaybackSpeed(s)
                },
                onClose = { showSpeedDialog = false },
                modifier = Modifier.clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) {} // consume clicks so it doesn't close
            )
        }
    }"""

content = re.sub(r'    // Playback Speed Dialog\n    if \(showSpeedDialog\) \{[\s\S]*?            \}\n        \)\n    \}', speed_dialog_code, content)

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "w") as f:
    f.write(content)
