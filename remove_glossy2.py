import re
import glob

files = [
    "app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt",
    "app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt"
]

for file_path in files:
    with open(file_path, "r") as f:
        content = f.read()

    content = re.sub(r'\s*\.glossyHeader\([^\)]*\)', '', content)
    content = re.sub(r'import com\.example\.ui\.theme\.glossyCard\n?', '', content)
    content = re.sub(r'import com\.example\.ui\.theme\.glossyHeader\n?', '', content)

    with open(file_path, "w") as f:
        f.write(content)
