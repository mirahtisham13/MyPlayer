import re

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "r") as f:
    content = f.read()

# Make sure they use containerColor = Color.Transparent and modifier = Modifier.glossyCard(isDark, cornerRadius=24.dp)
def replace_alert_dialog(match):
    prefix = match.group(1)
    return f"{prefix}androidx.compose.material3.AlertDialog(\n            modifier = Modifier.glossyCard(isDark = isDarkTheme, cornerRadius = 24.dp),\n            containerColor = Color.Transparent,"

content = re.sub(r"(\s*)androidx\.compose\.material3\.AlertDialog\(", replace_alert_dialog, content)

with open("app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt", "w") as f:
    f.write(content)

print("Updated dialogs")
