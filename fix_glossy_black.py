import re

with open("app/src/main/java/com/example/ui/theme/Glossy.kt", "r") as f:
    content = f.read()

# Fix glossyCard colors
content = content.replace("Color(0x333333)", "Color.Black")
content = content.replace("Color(0x1A1A1A)", "Color.Black")

# Fix glossyHeader colors
content = content.replace("Color(0xCC1A1A1C)", "Color(0xCC000000)")
content = content.replace("Color(0x99000000)", "Color(0x99000000)")

with open("app/src/main/java/com/example/ui/theme/Glossy.kt", "w") as f:
    f.write(content)

print("Updated Glossy.kt to use AMOLED black")
