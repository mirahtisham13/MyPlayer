import re

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "r") as f:
    content = f.read()

# Replace solid background with a mesh-like gradient background
new_bg = """                .background(
                    if (isDarkTheme) {
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF2A2A3A), Color(0xFF101015), Color.Black),
                            radius = 2000f,
                            center = Offset(500f, 500f)
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(Color(0xFFFDFDFD), Color(0xFFEBEBF5), Color(0xFFD1D1E0)),
                            radius = 2000f,
                            center = Offset(500f, 500f)
                        )
                    }
                )"""

content = content.replace(".background(MaterialTheme.colorScheme.background)", new_bg)

# Permission Banner
banner_old = """.background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))"""
banner_new = """.glossyCard(isDark = isDarkTheme, cornerRadius = 16.dp)"""

content = content.replace(banner_old, banner_new)

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "w") as f:
    f.write(content)
print("Updated backgrounds")
