package com.myplayer.ui.screen.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myplayer.data.repository.DoubleTapAction
import com.myplayer.data.repository.MultiFingerAction
import com.myplayer.viewmodel.SettingsViewModel
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureSettingsScreen(
    onNavigateBack: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val playbackSettings by settingsViewModel.playbackSettings.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val dirLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Ignore
            }
            settingsViewModel.updateScreenshotLocation(it.toString())
        }
    }

    var showDoubleTapDialog by remember { mutableStateOf(false) }
    var showSeekDurationDialog by remember { mutableStateOf(false) }
    var showTwoFingerActionDialog by remember { mutableStateOf(false) }
    var showThreeFingerActionDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Gestures & Taps",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Swipe Gestures Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GestureSectionHeader("Swipe Gestures")

                SettingToggleCard(
                    icon = Icons.Default.SwipeLeft,
                    title = "Horizontal Swipe seeking",
                    subtitle = "Swipe left/right to seek through video",
                    checked = playbackSettings.seekGestureEnabled,
                    onCheckedChange = { settingsViewModel.updateSeekGesture(it) }
                )

                SettingToggleCard(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    title = "Vertical Swipe Volume",
                    subtitle = "Swipe up/down on right side to adjust volume",
                    checked = playbackSettings.volumeGestureEnabled,
                    onCheckedChange = { settingsViewModel.updateVolumeGesture(it) }
                )

                SettingToggleCard(
                    icon = Icons.Default.LightMode,
                    title = "Vertical Swipe Brightness",
                    subtitle = "Swipe up/down on left side to adjust brightness",
                    checked = playbackSettings.brightnessGestureEnabled,
                    onCheckedChange = { settingsViewModel.updateBrightnessGesture(it) }
                )
            }

            // Press & Tap Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GestureSectionHeader("Press & Tap Controls")

                SettingClickableCard(
                    icon = Icons.Default.TouchApp,
                    title = "Double Tap Action",
                    subtitle = when (playbackSettings.doubleTapAction) {
                        DoubleTapAction.BOTH -> "Seek on double-tap"
                        DoubleTapAction.PLAY_PAUSE -> "Play / Pause"
                        DoubleTapAction.FAST_FORWARD -> "Fast Forward Only"
                        DoubleTapAction.REWIND -> "Rewind Only"
                        DoubleTapAction.NONE -> "No Action"
                    },
                    onClick = { showDoubleTapDialog = true }
                )

                SettingClickableCard(
                    icon = Icons.Default.Timer,
                    title = "Double Tap Seek Duration",
                    subtitle = "${playbackSettings.doubleTapSeekDuration / 1000} seconds",
                    onClick = { showSeekDurationDialog = true }
                )

                // Press & Hold Acceleration (Double slider card)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
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
                        modifier = Modifier.fillMaxWidth().clickable { settingsViewModel.updateLongPressEnabled(!playbackSettings.longPressEnabled) }.padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f).padding(end = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                            Column {
                                Text("Press & Hold Playback Acceleration", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Tap and hold screen to temporarily accelerate video", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(checked = playbackSettings.longPressEnabled, onCheckedChange = { settingsViewModel.updateLongPressEnabled(it) })
                    }
                }
            }

            // Multi-finger Actions
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GestureSectionHeader("Multi-finger Gestures")

                SettingClickableCard(
                    icon = Icons.Default.Gesture,
                    title = "Two-Finger Action",
                    subtitle = when (playbackSettings.twoFingerAction) {
                        MultiFingerAction.PLAY_PAUSE -> "Play / Pause"
                        MultiFingerAction.FAST_PLAY -> "Fast Play (2x)"
                        MultiFingerAction.MUTE -> "Mute"
                        MultiFingerAction.NONE -> "No Action"
                        MultiFingerAction.SCREENSHOT -> "Take Screenshot"
                        MultiFingerAction.PINCH_ZOOM -> "Pinch to Zoom"
                    },
                    onClick = { showTwoFingerActionDialog = true }
                )

                SettingClickableCard(
                    icon = Icons.Default.SettingsAccessibility,
                    title = "Three-Finger Action",
                    subtitle = when (playbackSettings.threeFingerAction) {
                        MultiFingerAction.PLAY_PAUSE -> "Play / Pause"
                        MultiFingerAction.FAST_PLAY -> "Fast Play (2x)"
                        MultiFingerAction.MUTE -> "Mute"
                        MultiFingerAction.NONE -> "No Action"
                        MultiFingerAction.SCREENSHOT -> "Take Screenshot"
                        MultiFingerAction.PINCH_ZOOM -> "Pinch to Zoom"
                    },
                    onClick = { showThreeFingerActionDialog = true }
                )

                if (playbackSettings.twoFingerAction == MultiFingerAction.SCREENSHOT ||
                    playbackSettings.threeFingerAction == MultiFingerAction.SCREENSHOT) {
                    SettingClickableCard(
                        icon = Icons.Default.Folder,
                        title = "Screenshot Save Location",
                        subtitle = getDisplayPath(playbackSettings.screenshotLocation),
                        onClick = { dirLauncher.launch(null) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    // Dialogs
    if (showDoubleTapDialog) {
        AlertDialog(
            onDismissRequest = { showDoubleTapDialog = false },
            title = { Text("Double Tap Action") },
            text = {
                Column(Modifier.selectableGroup()) {
                    listOf(
                        DoubleTapAction.BOTH to "Seek left/right",
                        DoubleTapAction.PLAY_PAUSE to "Play / Pause",
                        DoubleTapAction.FAST_FORWARD to "Fast Forward Only",
                        DoubleTapAction.REWIND to "Rewind Only",
                        DoubleTapAction.NONE to "Disable double-tap"
                    ).forEach { pair ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .selectable(
                                    selected = (playbackSettings.doubleTapAction == pair.first),
                                    onClick = {
                                        settingsViewModel.updateDoubleTapAction(pair.first)
                                        showDoubleTapDialog = false
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (playbackSettings.doubleTapAction == pair.first),
                                onClick = null
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = pair.second,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDoubleTapDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSeekDurationDialog) {
        AlertDialog(
            onDismissRequest = { showSeekDurationDialog = false },
            title = { Text("Double Tap Seek Duration") },
            text = {
                Column(Modifier.selectableGroup()) {
                    listOf(5000L, 10000L, 15000L, 20000L, 30000L).forEach { durationMs ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .selectable(
                                    selected = (playbackSettings.doubleTapSeekDuration == durationMs),
                                    onClick = {
                                        settingsViewModel.updateDoubleTapSeekDuration(durationMs)
                                        showSeekDurationDialog = false
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (playbackSettings.doubleTapSeekDuration == durationMs),
                                onClick = null
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = "${durationMs / 1000} seconds",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSeekDurationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showTwoFingerActionDialog) {
        AlertDialog(
            onDismissRequest = { showTwoFingerActionDialog = false },
            title = { Text("Two-Finger Tap Action") },
            text = {
                Column(Modifier.selectableGroup()) {
                    listOf(
                        MultiFingerAction.PLAY_PAUSE to "Play / Pause",
                        MultiFingerAction.FAST_PLAY to "Fast Play (2x)",
                        MultiFingerAction.MUTE to "Mute / Unmute",
                        MultiFingerAction.SCREENSHOT to "Take Screenshot",
                        MultiFingerAction.PINCH_ZOOM to "Pinch to Zoom",
                        MultiFingerAction.NONE to "Disable gesture"
                    ).forEach { pair ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .selectable(
                                    selected = (playbackSettings.twoFingerAction == pair.first),
                                    onClick = {
                                        settingsViewModel.updateTwoFingerAction(pair.first)
                                        showTwoFingerActionDialog = false
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (playbackSettings.twoFingerAction == pair.first),
                                onClick = null
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = pair.second,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTwoFingerActionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showThreeFingerActionDialog) {
        AlertDialog(
            onDismissRequest = { showThreeFingerActionDialog = false },
            title = { Text("Three-Finger Tap Action") },
            text = {
                Column(Modifier.selectableGroup()) {
                    listOf(
                        MultiFingerAction.PLAY_PAUSE to "Play / Pause",
                        MultiFingerAction.FAST_PLAY to "Fast Play (2x)",
                        MultiFingerAction.MUTE to "Mute / Unmute",
                        MultiFingerAction.SCREENSHOT to "Take Screenshot",
                        MultiFingerAction.PINCH_ZOOM to "Pinch to Zoom",
                        MultiFingerAction.NONE to "Disable gesture"
                    ).forEach { pair ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .selectable(
                                    selected = (playbackSettings.threeFingerAction == pair.first),
                                    onClick = {
                                        settingsViewModel.updateThreeFingerAction(pair.first)
                                        showThreeFingerActionDialog = false
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (playbackSettings.threeFingerAction == pair.first),
                                onClick = null
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = pair.second,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThreeFingerActionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun GestureSectionHeader(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
    )
}

@Composable
private fun GestureToggleCardWithSlider(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    sliderTitle: String,
    sliderValue: Float,
    sliderDefaultValue: Float = 0.5f,
    sliderValueRange: ClosedFloatingPointRange<Float> = 0.1f..1.0f,
    sliderSteps: Int = 0,
    sliderValueFormatter: (Float) -> String = { "${(it * 100).roundToInt()}%" },
    onSliderValueChange: (Float) -> Unit,
    onSliderReset: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
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
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f).padding(end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    Column {
                        Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Switch(checked = checked, onCheckedChange = onCheckedChange)
            }
            if (checked) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Text(sliderTitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(sliderValueFormatter(sliderValue), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            if (onSliderReset != null) {
                                val isAtDefault = abs(sliderValue - sliderDefaultValue) < 0.001f
                                IconButton(onClick = onSliderReset, enabled = !isAtDefault, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(16.dp), tint = if (isAtDefault) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                    Slider(
                        value = sliderValue,
                        onValueChange = onSliderValueChange,
                        valueRange = sliderValueRange,
                        steps = sliderSteps,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}





private fun getDisplayPath(location: String): String {
    if (location.startsWith("content://")) {
        return try {
            val decoded = Uri.decode(location)
            val lastColon = decoded.lastIndexOf(':')
            if (lastColon != -1 && lastColon < decoded.length - 1) {
                decoded.substring(lastColon + 1)
            } else {
                Uri.parse(location).lastPathSegment ?: location
            }
        } catch (e: Exception) {
            location
        }
    }
    return location
}
