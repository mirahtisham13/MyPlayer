import re

with open("app/src/main/java/com/example/ui/screens/SpeedControlPanel.kt", "r") as f:
    content = f.read()

# Make horizontal padding 16.dp on the Row, horizontalArrangement = Arrangement.Center
old_row = """        // Slider Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {"""

new_row = """        // Slider Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {"""

content = content.replace(old_row, new_row)

old_column = "Column(modifier = Modifier.weight(1f).padding(horizontal = 64.dp))"
new_column = "Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp))"
content = content.replace(old_column, new_column)

# Remove Close Icon and Spacer
close_icon = """            Spacer(modifier = Modifier.width(16.dp))
            
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }"""

content = content.replace(close_icon, "")

with open("app/src/main/java/com/example/ui/screens/SpeedControlPanel.kt", "w") as f:
    f.write(content)
