import re

# 1. Update AppearanceSettingsScreen
with open("app/src/main/java/com/myplayer/ui/screen/settings/AppearanceSettingsScreen.kt", "r") as f:
    app_code = f.read()

# Make ThemeOption internal
app_code = app_code.replace("private fun ThemeOption(", "internal fun ThemeOption(")

lang_dialog_match = re.search(r"(    var showLanguageDialog.*?)(?=    // Main scaffold)", app_code, re.DOTALL)
lang_dialog = lang_dialog_match.group(1) if lang_dialog_match else ""
app_code = app_code.replace(lang_dialog, "")

lang_thumb_sections_match = re.search(r'(            // LANGUAGE section.*?)(?=            // INFO chip)', app_code, re.DOTALL)
lang_thumb_sections = lang_thumb_sections_match.group(1) if lang_thumb_sections_match else ""
app_code = app_code.replace(lang_thumb_sections, "")

with open("app/src/main/java/com/myplayer/ui/screen/settings/AppearanceSettingsScreen.kt", "w") as f:
    f.write(app_code)

# 2. Update SettingsScreen.kt
with open("app/src/main/java/com/myplayer/ui/screen/SettingsScreen.kt", "r") as f:
    set_code = f.read()

# Add imports
imports = """import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.foundation.selection.selectableGroup
import com.myplayer.ui.screen.settings.ThemeOption
"""
set_code = set_code.replace("import com.myplayer.viewmodel.SettingsViewModel", "import com.myplayer.viewmodel.SettingsViewModel\n" + imports)

# Add onNavigateToThumbnails to signature
set_code = set_code.replace("    onNavigateToAppearance: () -> Unit = {},", "    onNavigateToAppearance: () -> Unit = {},\n    onNavigateToThumbnails: () -> Unit = {},")

# Replace strings
settings_vars = """
    val settingsAppearance = stringResource(R.string.settings_appearance)
    val settingsPlayer = stringResource(R.string.settings_player)
    val settingsDeveloper = stringResource(R.string.settings_developer)
    
    val settingsDisplayLanguage = stringResource(R.string.appearance_display_language)
    val settingsMarathi = stringResource(R.string.appearance_marathi)
    val settingsEnglish = stringResource(R.string.appearance_english)
    val settingsLanguage = stringResource(R.string.appearance_language)
"""
set_code = set_code.replace("    val settingsAppearance = stringResource(R.string.settings_appearance)\n    val settingsPlayer = stringResource(R.string.settings_player)\n    val settingsDeveloper = stringResource(R.string.settings_developer)", settings_vars)

# Insert showLanguageDialog at top of scaffold
set_code = set_code.replace("    Scaffold(", lang_dialog + "    Scaffold(")

old_appearance_section = """            settingsSection(settingsAppearance) {
                item {
                    SettingsGroupCard {
                        SettingsItemRow(
                            icon = Icons.Default.Palette,
                            title = stringResource(R.string.settings_display),
                            subtitle = stringResource(R.string.settings_display_desc),
                            onClick = onNavigateToAppearance
                        )
                    }
                }
            }"""

new_appearance_section = """            settingsSection(settingsAppearance) {
                item {
                    SettingsGroupCard {
                        SettingsItemRow(
                            icon = Icons.Default.Palette,
                            title = stringResource(R.string.settings_display),
                            subtitle = stringResource(R.string.settings_display_desc),
                            onClick = onNavigateToAppearance
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 72.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        SettingsItemRow(
                            icon = Icons.Default.Photo,
                            title = "Thumbnails",
                            subtitle = "Show video thumbnails, configure thumbnail strategy, clear cache",
                            onClick = onNavigateToThumbnails
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 72.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        val currentLocales = AppCompatDelegate.getApplicationLocales()
                        val isMarathi = currentLocales.toLanguageTags().contains("mr")
                        SettingsItemRow(
                            icon     = Icons.Default.Language,
                            title    = settingsDisplayLanguage,
                            subtitle = if (isMarathi) settingsMarathi else settingsEnglish,
                            onClick  = { showLanguageDialog = true }
                        )
                    }
                }
            }"""
set_code = set_code.replace(old_appearance_section, new_appearance_section)

with open("app/src/main/java/com/myplayer/ui/screen/SettingsScreen.kt", "w") as f:
    f.write(set_code)

print("Patch complete")
