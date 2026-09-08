package com.myplayer.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myplayer.R
import com.myplayer.domain.model.ThumbnailMode
import com.myplayer.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThumbnailSettingsScreen(
    onNavigateBack: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val viewSettingsVal by settingsViewModel.viewSettings.collectAsState()

    var showModeDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Thumbnail Settings",
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
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingToggleCard(
                icon = Icons.Default.Photo,
                title = "Show Video Thumbnails",
                subtitle = "Display generated thumbnails in video lists and folders",
                checked = viewSettingsVal.showThumbnail,
                onCheckedChange = { settingsViewModel.updateShowThumbnail(it) }
            )

            if (viewSettingsVal.showThumbnail) {
                SettingClickableCard(
                    icon = Icons.Default.Tune,
                    title = "Thumbnail Strategy",
                    subtitle = viewSettingsVal.thumbnailMode.displayName,
                    onClick = { showModeDialog = true }
                )

                if (viewSettingsVal.thumbnailMode == ThumbnailMode.FRAME_AT_POSITION) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Frame Position",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${viewSettingsVal.thumbnailFramePosition.toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = viewSettingsVal.thumbnailFramePosition,
                            onValueChange = { settingsViewModel.updateThumbnailFramePosition(it) },
                            valueRange = 1f..90f,
                            steps = 88,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            SettingClickableCard(
                icon = Icons.Default.DeleteSweep,
                title = "Clear Thumbnail Cache",
                subtitle = "Delete all cached video thumbnails from storage",
                onClick = { showClearConfirmDialog = true }
            )
        }

        if (showModeDialog) {
            AlertDialog(
                onDismissRequest = { showModeDialog = false },
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.background,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text("Thumbnail Strategy", style = MaterialTheme.typography.titleMedium)
                    }
                },
                text = {
                    Column(modifier = Modifier.selectableGroup()) {
                        ThumbnailMode.entries.forEach { mode ->
                            ThemeOption(
                                text = mode.displayName,
                                selected = viewSettingsVal.thumbnailMode == mode,
                                icon = Icons.Default.Check,
                                onClick = {
                                    settingsViewModel.updateThumbnailMode(mode)
                                    showModeDialog = false
                                }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showModeDialog = false }) { Text("Close") }
                }
            )
        }

        if (showClearConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showClearConfirmDialog = false },
                title = { Text("Clear thumbnail cache?") },
                text = { Text("This will delete cached thumbnails from storage. They will be regenerated as you browse.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            settingsViewModel.clearThumbnailCache()
                            showClearConfirmDialog = false
                        }
                    ) {
                        Text("Clear", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}


