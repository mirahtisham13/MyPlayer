import re

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "r") as f:
    content = f.read()

# Fix proper dark mode background
old_bg = """                    if (isDarkTheme) {
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF2A2A3A), Color(0xFF101015), Color.Black),
                            radius = 2000f,
                            center = Offset(500f, 500f)
                        )
                    } else {"""
new_bg = """                    if (isDarkTheme) {
                        Brush.radialGradient(
                            colors = listOf(Color.Black, Color.Black),
                            radius = 2000f,
                            center = Offset(500f, 500f)
                        )
                    } else {"""
content = content.replace(old_bg, new_bg)

# Fix bottom buttons background
old_bottom_bg = "containerColor = if (isDarkTheme) Color(0x33FFFFFF) else Color(0x99FFFFFF),"
new_bottom_bg = "containerColor = if (isDarkTheme) Color.Black else Color(0x99FFFFFF),"
content = content.replace(old_bottom_bg, new_bottom_bg)

# Move bottom buttons below a little more by adding bottom padding to the Row
old_bottom_row = "modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),"
new_bottom_row = "modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, bottom = 12.dp, top = 4.dp),"
content = content.replace(old_bottom_row, new_bottom_row)

# Move CheckCircle to below right (BottomEnd)
# FolderListRow
content = content.replace(
    'Box(modifier = Modifier.fillMaxSize().background(Color(0x66000000), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {\n                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Selected", tint = Color(0xFF00C969), modifier = Modifier.size(24.dp))\n                }',
    'Box(modifier = Modifier.fillMaxSize().background(Color(0x66000000), RoundedCornerShape(10.dp)), contentAlignment = Alignment.BottomEnd) {\n                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Selected", tint = Color(0xFF00C969), modifier = Modifier.padding(2.dp).size(20.dp))\n                }'
)

# FolderGridCard
content = content.replace(
    'contentAlignment = Alignment.TopStart\n            ) {\n                Icon(\n                    imageVector = Icons.Default.CheckCircle,\n                    contentDescription = "Selected",\n                    tint = Color(0xFF00C969),\n                    modifier = Modifier.padding(8.dp).size(24.dp)\n                )',
    'contentAlignment = Alignment.BottomEnd\n            ) {\n                Icon(\n                    imageVector = Icons.Default.CheckCircle,\n                    contentDescription = "Selected",\n                    tint = Color(0xFF00C969),\n                    modifier = Modifier.padding(8.dp).size(24.dp)\n                )'
)

# VideoListRow & VideoGridCard
content = content.replace(
    'Box(modifier = Modifier.fillMaxSize().background(Color(0x66000000)), contentAlignment = Alignment.Center) {\n                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Selected", tint = Color(0xFF00C969), modifier = Modifier.size(24.dp))\n                }',
    'Box(modifier = Modifier.fillMaxSize().background(Color(0x66000000)), contentAlignment = Alignment.BottomEnd) {\n                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Selected", tint = Color(0xFF00C969), modifier = Modifier.padding(4.dp).size(20.dp))\n                }'
)
content = content.replace(
    'Box(modifier = Modifier.fillMaxSize().background(Color(0x66000000)), contentAlignment = Alignment.Center) {\n                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Selected", tint = Color(0xFF00C969), modifier = Modifier.size(24.dp))\n                    }',
    'Box(modifier = Modifier.fillMaxSize().background(Color(0x66000000)), contentAlignment = Alignment.BottomEnd) {\n                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Selected", tint = Color(0xFF00C969), modifier = Modifier.padding(4.dp).size(20.dp))\n                    }'
)

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "w") as f:
    f.write(content)
print("Updated dark mode and checkmarks")
