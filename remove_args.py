import re

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "r") as f:
    content = f.read()

content = re.sub(r'\s*isGridLayout: Boolean,', '', content)
content = re.sub(r'\s*searchQuery: String,', '', content)
content = re.sub(r'\s*onToggleLayout: \(\) -> Unit,', '', content)
content = re.sub(r'\s*onSearchQueryChange: \(String\) -> Unit,', '', content)

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = re.sub(r'\s*isGridLayout = isGridLayout,', '', content)
content = re.sub(r'\s*searchQuery = searchQuery,', '', content)
content = re.sub(r'\s*onToggleLayout = \{ viewModel\.toggleLayout\(\) \},', '', content)
content = re.sub(r'\s*onSearchQueryChange = \{ viewModel\.updateSearch\(it\) \},', '', content)
content = re.sub(r'\s*val searchQuery by viewModel\.searchQuery\.collectAsState\(\)', '', content)
content = re.sub(r'\s*val isGridLayout by viewModel\.isGridLayout\.collectAsState\(\)', '', content)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
