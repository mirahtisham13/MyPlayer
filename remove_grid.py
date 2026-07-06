import re

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "r") as f:
    content = f.read()

# Replace Folder grid logic
folder_grid_pattern = r'if \(isGridLayout\) \{[\s\S]*?LazyVerticalGrid\([\s\S]*?\}\s*\}\s*\} else \{\s*(LazyColumn\([\s\S]*?\}\s*\})\s*\}'
content = re.sub(folder_grid_pattern, r'\1', content)

# Replace Video grid logic
video_grid_pattern = r'if \(isGridLayout\) \{[\s\S]*?LazyVerticalGrid\([\s\S]*?\}\s*\}\s*\} else \{\s*(LazyColumn\([\s\S]*?\}\s*\})\s*\}'
content = re.sub(video_grid_pattern, r'\1', content)

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "w") as f:
    f.write(content)
