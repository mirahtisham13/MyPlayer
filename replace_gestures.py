import re

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "r") as f:
    content = f.read()

content = content.replace("import android.app.Activity", "import android.app.Activity\nimport android.content.pm.ActivityInfo")

content = content.replace(
    "var controlsVisible by remember { mutableStateOf(true) }",
    "var controlsVisible by remember { mutableStateOf(true) }\n    var isControlsLocked by remember { mutableStateOf(false) }"
)

gesture_on_drag = """                        onDrag = { change, dragAmount ->
                            change.consume()
                            val width = size.width"""

gesture_on_drag_new = """                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (isControlsLocked) return@detectDragGestures
                            val width = size.width"""

content = content.replace(gesture_on_drag, gesture_on_drag_new)

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "w") as f:
    f.write(content)
