import re
import glob

files = [
    "app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt",
    "app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt"
]

for file_path in files:
    with open(file_path, "r") as f:
        content = f.read()

    # Regex to match .glossyCard(...) with any parameters, handling newlines if necessary.
    # Since they are mostly on a single line, we can just do:
    content = re.sub(r'\s*\.glossyCard\([^\)]*\)', '', content)

    with open(file_path, "w") as f:
        f.write(content)
