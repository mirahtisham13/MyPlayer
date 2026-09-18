package com.myplayer.ui.screen.settings

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.ui.res.stringResource
import com.myplayer.R
import com.myplayer.ui.theme.AppThemePalette
import androidx.compose.foundation.isSystemInDarkTheme
import com.myplayer.viewmodel.SettingsViewModel
import com.myplayer.domain.model.ThumbnailMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    onNavigateBack: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val isDark        by settingsViewModel.isDarkTheme.collectAsState()
    val dynamicColor  by settingsViewModel.dynamicColor.collectAsState()
    val selectedPalette by settingsViewModel.selectedPalette.collectAsState()
    val navBarTransparent by settingsViewModel.isNavBarTransparent.collectAsState()
    val isAmoledTheme by settingsViewModel.isAmoledTheme.collectAsState()
    val colorOverrides by settingsViewModel.colorOverrides.collectAsState()
    val isEffectivelyDark = isDark ?: isSystemInDarkTheme()

    // Color picker dialog state
    var pickerSlot by remember { mutableStateOf<String?>(null) }

    // Main scaffold
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings_display),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding() + 16.dp
                )
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Colour Preview strip — tap any swatch to customise
            ColorPreviewStrip(
                colorOverrides = colorOverrides,
                onSwatchClick = { slot -> pickerSlot = slot },
                onClearAll = { settingsViewModel.clearAllColorOverrides() }
            )

            // Color picker dialog
            pickerSlot?.let { slot ->
                ColorPickerDialog(
                    slot = slot,
                    currentColor = MaterialTheme.colorScheme.let { cs ->
                        when (slot) {
                            "primary"            -> cs.primary
                            "secondary"          -> cs.secondary
                            "tertiary"           -> cs.tertiary
                            "primaryContainer"   -> cs.primaryContainer
                            "secondaryContainer" -> cs.secondaryContainer
                            "tertiaryContainer"  -> cs.tertiaryContainer
                            "surfaceContainerHigh" -> cs.surfaceContainerHigh
                            "outline"            -> cs.outline
                            else                 -> cs.primary
                        }
                    },
                    hasOverride = colorOverrides.containsKey(slot),
                    onApply = { color ->
                        settingsViewModel.setColorOverride(slot, color.toArgb())
                        pickerSlot = null
                    },
                    onReset = {
                        settingsViewModel.setColorOverride(slot, null)
                        pickerSlot = null
                    },
                    onDismiss = { pickerSlot = null }
                )
            }

            // COLOUR PALETTE section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppearanceSectionLabel(stringResource(R.string.appearance_colour_palette))
                PalettePickerGrid(
                    selected    = selectedPalette,
                    isDark      = isDark ?: false,
                    onSelect    = { settingsViewModel.setSelectedPalette(it) }
                )
            }

            // THEME section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AppearanceSectionLabel(stringResource(R.string.appearance_theme))
                
                // Theme Selection Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.appearance_dark_theme),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val options = listOf(
                                stringResource(R.string.appearance_light),
                                stringResource(R.string.appearance_dark),
                                stringResource(R.string.appearance_system_default)
                            )
                            
                            options.forEachIndexed { index, label ->
                                val isSelected = when (index) {
                                    0 -> isDark == false
                                    1 -> isDark == true
                                    else -> isDark == null
                                }
                                SegmentedButton(
                                    selected = isSelected,
                                    onClick = {
                                        when (index) {
                                            0 -> settingsViewModel.setDarkTheme(false)
                                            1 -> settingsViewModel.setDarkTheme(true)
                                            2 -> settingsViewModel.resetDarkTheme()
                                        }
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                                ) {
                                    Text(label)
                                }
                            }
                        }
                    }
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    SettingToggleCard(
                        icon      = Icons.Default.Palette,
                        title     = stringResource(R.string.appearance_dynamic_colour),
                        subtitle  = stringResource(R.string.appearance_dynamic_colour_desc),
                        checked   = dynamicColor,
                        onCheckedChange = { settingsViewModel.setDynamicColor(it) }
                    )
                }

                if (isEffectivelyDark) {
                    SettingToggleCard(
                        icon      = Icons.Default.Brightness1,
                        title     = "AMOLED Theme",
                        subtitle  = "Pure black background for dark mode",
                        checked   = isAmoledTheme,
                        onCheckedChange = { settingsViewModel.setAmoledTheme(it) }
                    )
                }

                SettingToggleCard(
                    icon      = Icons.Default.WebAsset,
                    title     = "Transparent Navigation Buttons",
                    subtitle  = "Content scrolls behind the system navigation buttons",
                    checked   = navBarTransparent,
                    onCheckedChange = { settingsViewModel.setNavBarTransparent(it) }
                )
            }

            // INFO chip
            Surface(
                modifier       = Modifier.fillMaxWidth(),
                shape          = RoundedCornerShape(12.dp),
                color          = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier              = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint     = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text  = stringResource(R.string.appearance_restart_info),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Palette Picker Grid
@Composable
private fun PalettePickerGrid(
    selected: AppThemePalette,
    isDark: Boolean,
    onSelect: (AppThemePalette) -> Unit
) {
    val palettes = AppThemePalette.entries
    val rows = palettes.chunked(2)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { palette ->
                    PaletteCard(
                        palette  = palette,
                        isDark   = isDark,
                        isSelected = palette == selected,
                        modifier   = Modifier.weight(1f),
                        onClick    = { onSelect(palette) }
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PaletteCard(
    palette: AppThemePalette,
    isDark: Boolean,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val primary   = if (isDark) palette.darkPrimary   else palette.lightPrimary
    val secondary = if (isDark) palette.darkSecondary else palette.lightSecondary

    val borderWidth by animateDpAsState(
        targetValue   = if (isSelected) 2.5.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label         = "paletteBorder"
    )
    val borderColor by animateColorAsState(
        targetValue   = if (isSelected) primary else Color.Transparent,
        animationSpec = tween(250),
        label         = "paletteBorderColor"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape          = RoundedCornerShape(14.dp),
        color          = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = if (isSelected) 4.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(listOf(primary, secondary))
                    )
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = stringResource(R.string.appearance_selected),
                        tint     = Color.White,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp)
                            .size(18.dp)
                    )
                }
            }

            Row(
                verticalAlignment      = Alignment.CenterVertically,
                horizontalArrangement  = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(primary)
                )
                Text(
                    text  = palette.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) primary
                            else MaterialTheme.colorScheme.onSurface
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(primary)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(secondary)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(primary.copy(alpha = 0.4f))
                )
            }
        }
    }
}

@Composable
private fun ColorPreviewStrip(
    colorOverrides: Map<String, Int>,
    onSwatchClick: (String) -> Unit,
    onClearAll: () -> Unit
) {
    val slots = listOf(
        "primary" to MaterialTheme.colorScheme.primary,
        "secondary" to MaterialTheme.colorScheme.secondary,
        "tertiary" to MaterialTheme.colorScheme.tertiary,
        "primaryContainer" to MaterialTheme.colorScheme.primaryContainer,
        "secondaryContainer" to MaterialTheme.colorScheme.secondaryContainer,
        "tertiaryContainer" to MaterialTheme.colorScheme.tertiaryContainer,
        "surfaceContainerHigh" to MaterialTheme.colorScheme.surfaceContainerHigh,
        "outline" to MaterialTheme.colorScheme.outline,
    )
    val hasAnyOverride = colorOverrides.isNotEmpty()

    Surface(
        modifier       = Modifier.fillMaxWidth(),
        shape          = RoundedCornerShape(16.dp),
        color          = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text  = stringResource(R.string.appearance_current_palette),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                if (hasAnyOverride) {
                    TextButton(
                        onClick = onClearAll,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Reset all",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                slots.forEach { (slot, raw) ->
                    val animColor by animateColorAsState(
                        targetValue   = raw,
                        animationSpec = tween(400),
                        label         = "swatchAnim_$slot"
                    )
                    val isOverridden = colorOverrides.containsKey(slot)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(animColor)
                            .then(
                                if (isOverridden) Modifier.border(
                                    1.5.dp,
                                    Color.White.copy(alpha = 0.8f),
                                    RoundedCornerShape(6.dp)
                                ) else Modifier
                            )
                            .clickable { onSwatchClick(slot) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isOverridden) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Tap any colour to customise",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ColorPickerDialog(
    slot: String,
    currentColor: Color,
    hasOverride: Boolean,
    onApply: (Color) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val slotLabel = remember(slot) {
        when (slot) {
            "primary"            -> "Primary"
            "secondary"          -> "Secondary"
            "tertiary"           -> "Tertiary"
            "primaryContainer"   -> "Primary Container"
            "secondaryContainer" -> "Secondary Container"
            "tertiaryContainer"  -> "Tertiary Container"
            "surfaceContainerHigh" -> "Surface High"
            "outline"            -> "Outline"
            else                 -> slot
        }
    }

    // Initialize RGB from currentColor
    var r by remember(currentColor) { mutableFloatStateOf(currentColor.red * 255f) }
    var g by remember(currentColor) { mutableFloatStateOf(currentColor.green * 255f) }
    var b by remember(currentColor) { mutableFloatStateOf(currentColor.blue * 255f) }

    val previewColor by remember(r, g, b) {
        derivedStateOf { Color(r.toInt(), g.toInt(), b.toInt()) }
    }

    // Hex field
    var hexText by remember(currentColor) {
        val argb = currentColor.copy(alpha = 1f)
        val hex = String.format("%06X",
            (argb.red * 255).toInt() shl 16 or
            ((argb.green * 255).toInt() shl 8) or
            (argb.blue * 255).toInt()
        )
        mutableStateOf(hex)
    }
    var hexError by remember { mutableStateOf(false) }

    fun syncHexFromRgb() {
        hexText = String.format("%06X", r.toInt() shl 16 or (g.toInt() shl 8) or b.toInt())
        hexError = false
    }

    fun applyHex(text: String) {
        val clean = text.trimStart('#')
        if (clean.length == 6) {
            try {
                val v = clean.toLong(16)
                r = ((v shr 16) and 0xFF).toFloat()
                g = ((v shr 8) and 0xFF).toFloat()
                b = (v and 0xFF).toFloat()
                hexError = false
            } catch (e: NumberFormatException) { hexError = true }
        } else { hexError = if (clean.isEmpty()) false else true }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = slotLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Preview swatch
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(previewColor)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                )

                // Hex input
                OutlinedTextField(
                    value = hexText,
                    onValueChange = { input ->
                        hexText = input.trimStart('#').uppercase().take(6)
                        applyHex(hexText)
                    },
                    label = { Text("Hex") },
                    prefix = { Text("#") },
                    isError = hexError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Medium)
                )

                // RGB Sliders
                listOf(
                    Triple("R", r, { v: Float -> r = v; syncHexFromRgb() }),
                    Triple("G", g, { v: Float -> g = v; syncHexFromRgb() }),
                    Triple("B", b, { v: Float -> b = v; syncHexFromRgb() })
                ).forEach { (label, value, onChange) ->
                    val trackColor = when (label) {
                        "R" -> Color(value.toInt(), 0, 0)
                        "G" -> Color(0, value.toInt(), 0)
                        else -> Color(0, 0, value.toInt())
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (label) {
                                "R" -> Color(200, 50, 50)
                                "G" -> Color(50, 160, 50)
                                else -> Color(50, 100, 200)
                            },
                            modifier = Modifier.width(14.dp),
                            textAlign = TextAlign.Center
                        )
                        Slider(
                            value = value,
                            onValueChange = onChange,
                            valueRange = 0f..255f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = trackColor,
                                activeTrackColor = trackColor
                            )
                        )
                        Text(
                            text = value.toInt().toString(),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.width(28.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(previewColor) }) {
                Text("Apply", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (hasOverride) {
                    TextButton(onClick = onReset) {
                        Text(
                            "Reset",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun AppearanceSectionLabel(label: String) {
    Text(
        text       = label,
        style      = MaterialTheme.typography.labelLarge,
        color      = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier   = Modifier.padding(start = 4.dp, bottom = 6.dp)
    )
}

@Composable
internal fun ThemeOption(
    text: String,
    selected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            modifier           = Modifier.size(20.dp),
            tint               = if (selected) MaterialTheme.colorScheme.primary
                                 else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        RadioButton(selected = selected, onClick = null)
    }
}

@Composable
internal fun SettingToggleCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .then(if (enabled) Modifier.clickable { onCheckedChange(!checked) } else Modifier)
            .alpha(if (enabled) 1f else 0.38f)
            .border(
                BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f).padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (checked && enabled) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.secondaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (checked && enabled) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    }
}

@Composable
internal fun SettingClickableCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
            .alpha(if (enabled) 1f else 0.38f)
            .border(
                BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f).padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
