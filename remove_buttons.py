import re

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "r") as f:
    content = f.read()

# Remove Search Button and Layout Toggle Button
content = re.sub(r'\s*// Search Button\s*IconButton\([\s\S]*?Icon\([\s\S]*?imageVector = Icons\.Default\.Search,[\s\S]*?\)\s*\}', '', content)
content = re.sub(r'\s*// Layout Toggle Button\s*IconButton\([\s\S]*?Icon\([\s\S]*?imageVector = if \(isGridLayout\) Icons\.Default\.ViewList else Icons\.Default\.GridView,[\s\S]*?\)\s*\}', '', content)

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "w") as f:
    f.write(content)
