import re

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "r") as f:
    content = f.read()

# Remove FolderGridCard
content = re.sub(r'// Subcomponent: Folder Grid Card[\s\S]*?fun FolderGridCard\([\s\S]*?\}\s*\}\s*\}\s*\}', '', content)

# Remove VideoGridCard
content = re.sub(r'// Subcomponent: Video Grid Card[\s\S]*?fun VideoGridCard\([\s\S]*?\}\s*\}\s*\}\s*\}', '', content)

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "w") as f:
    f.write(content)
