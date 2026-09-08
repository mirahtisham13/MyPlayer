package com.myplayer.ui.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.myplayer.domain.model.Video
import com.myplayer.domain.model.VideoFolder
import com.myplayer.domain.model.ViewSettings

@Composable
fun SelectionBottomAppBar(
    selectedFolders: Set<VideoFolder>,
    videosByFolder: Map<VideoFolder, List<Video>>,
    viewSettings: ViewSettings,
    onClearSelection: () -> Unit,
    onMove: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    onShare: () -> Unit,
    onSelectAll: (() -> Unit)? = null
) {
    var showShareWarningDialog by remember { mutableStateOf(false) }
    val totalFiles = remember(selectedFolders, videosByFolder) {
        selectedFolders.sumOf { folder -> (videosByFolder[folder] ?: emptyList()).size }
    }

    if (showShareWarningDialog) {
        AlertDialog(
            onDismissRequest = { showShareWarningDialog = false },
            title = {
                Text(
                    text = "Share Folder Files",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = "Warning: You are sending $totalFiles file(s) at once across the selected folder(s). Are you sure you want to proceed?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showShareWarningDialog = false
                        onShare()
                    }
                ) { Text("Share") }
            },
            dismissButton = {
                TextButton(onClick = { showShareWarningDialog = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            HorizontalDivider(color = Color.DarkGray)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Select All on the left
            if (onSelectAll != null) {
                IconButton(onClick = onSelectAll) {
                    Icon(
                        imageVector = Icons.Filled.SelectAll,
                        contentDescription = "Select All",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Move
            IconButton(onClick = onMove) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DriveFileMove,
                    contentDescription = "Move",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
            // Copy
            IconButton(onClick = onCopy) {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = "Copy",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
            // Delete
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
            // Rename — only when 1 folder selected
            AnimatedVisibility(
                visible = selectedFolders.size == 1,
                enter = expandHorizontally(
                    animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow),
                    expandFrom = Alignment.CenterHorizontally
                ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) + scaleIn(
                    animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow),
                    initialScale = 0.7f
                ),
                exit = shrinkHorizontally(
                    animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow),
                    shrinkTowards = Alignment.CenterHorizontally
                ) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)) + scaleOut(
                    animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow),
                    targetScale = 0.7f
                )
            ) {
                IconButton(onClick = onRename) {
                    Icon(
                        imageVector = Icons.Filled.DriveFileRenameOutline,
                        contentDescription = "Rename",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            // Share
            IconButton(
                onClick = {
                    if (totalFiles > 1) showShareWarningDialog = true else onShare()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
            }
        }
    }
}
