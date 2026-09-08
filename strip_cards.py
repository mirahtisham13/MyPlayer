import re

files = [
    "app/src/main/java/com/myplayer/ui/screen/settings/GestureSettingsScreen.kt",
    "app/src/main/java/com/myplayer/ui/screen/settings/PlayerInterfaceSettingsScreen.kt",
    "app/src/main/java/com/myplayer/ui/screen/settings/YtdlpSettingsScreen.kt"
]

def remove_function(code, func_name):
    pattern = r"@Composable\s*private fun " + func_name + r"\s*\("
    match = re.search(pattern, code)
    if not match:
        return code
    
    start_idx = match.start()
    
    # Find the opening brace of the function body
    brace_match = re.search(r"\{", code[start_idx:])
    if not brace_match:
        return code
        
    brace_start = start_idx + brace_match.start()
    
    # Count braces to find the end
    brace_count = 1
    idx = brace_start + 1
    while brace_count > 0 and idx < len(code):
        if code[idx] == '{':
            brace_count += 1
        elif code[idx] == '}':
            brace_count -= 1
        idx += 1
        
    return code[:start_idx] + code[idx:]

for file_path in files:
    with open(file_path, "r") as f:
        code = f.read()
    
    code = remove_function(code, "SettingToggleCard")
    code = remove_function(code, "SettingClickableCard")
    
    with open(file_path, "w") as f:
        f.write(code)

print("Removed cards using brace matching")
