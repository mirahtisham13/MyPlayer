import re

with open("app/src/main/java/com/myplayer/ui/screen/SettingsScreen.kt", "r") as f:
    set_code = f.read()

# 1. Add onNavigateToThumbnails to signature
set_code = set_code.replace("    onNavigateToAppearance: () -> Unit = {},", "    onNavigateToAppearance: () -> Unit = {},\n    onNavigateToThumbnails: () -> Unit = {},")

# 2. Extract Language dialog
lang_dialog_match = re.search(r"(    var showLanguageDialog.*?)(?=    Scaffold)", set_code, re.DOTALL)
lang_dialog = lang_dialog_match.group(1) if lang_dialog_match else ""
set_code = set_code.replace(lang_dialog, "")

# 3. Modify APPEARANCE section to contain the rows directly
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

# 4. Remove the previously added Language and Thumbnail sections
sections_to_remove_match = re.search(r'(            settingsSection\(settingsLanguage\) \{.*?)            // Developer Section', set_code, re.DOTALL)
if sections_to_remove_match:
    set_code = set_code.replace(sections_to_remove_match.group(1), "")

# 5. Add back the language dialog at the end, just inside Scaffold padding
set_code = set_code.replace("        }\n    }\n}\n\nfun LazyListScope", "        }\n" + lang_dialog + "    }\n}\n\nfun LazyListScope")

# Also remove unneeded imports that might cause issues like ThumbnailMode
set_code = set_code.replace("import com.myplayer.domain.model.ThumbnailMode\n", "")
set_code = set_code.replace("import com.myplayer.ui.screen.settings.ThemeOption\n", "")
set_code = set_code.replace("import androidx.compose.foundation.selection.selectableGroup\n", "")
set_code = set_code.replace("    var showModeDialog by remember { mutableStateOf(false) }\n", "")
set_code = set_code.replace("    var showClearConfirmDialog by remember { mutableStateOf(false) }\n", "")

with open("app/src/main/java/com/myplayer/ui/screen/SettingsScreen.kt", "w") as f:
    f.write(set_code)

print("success")
