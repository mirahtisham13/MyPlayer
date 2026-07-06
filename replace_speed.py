import re

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "r") as f:
    content = f.read()

old_speed = """.background(Color(0xFF3B5998), CircleShape)
                                .clickable { showSpeedDialog = true }
                                .padding(horizontal = 12.dp, vertical = 12.dp)"""

new_speed = """.glossyCard(isDark = true, cornerRadius = 24.dp)
                                .clickable { showSpeedDialog = true }
                                .padding(horizontal = 12.dp, vertical = 12.dp)"""

content = content.replace(old_speed, new_speed)

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "w") as f:
    f.write(content)

print("Updated speed button")
