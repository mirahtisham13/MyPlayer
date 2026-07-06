import re

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "r") as f:
    content = f.read()

imports = """import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
"""
content = content.replace("import androidx.compose.ui.Modifier", imports + "import androidx.compose.ui.Modifier")

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "w") as f:
    f.write(content)
