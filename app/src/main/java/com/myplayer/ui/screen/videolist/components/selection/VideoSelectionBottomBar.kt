package com.myplayer.ui.screens.videolist.components.selection

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.myplayer.domain.model.Video

@Composable
fun VideoSelectionBottomBar(
    selectedVideos: Set<Video>,
    onMove: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    onShare: () -> Unit,
    onMarkStatus: (String) -> Unit
) {
    VideoSelectionBottomBar(
        selectedVideos = selectedVideos,
        onMove = onMove,
        onCopy = onCopy,
        onDelete = onDelete,
        onRename = onRename,
        onShare = onShare,
        onMarkStatus = onMarkStatus,
        showTagAndShare = true,
        onSelectAll = null,
        totalCount = 0
    )
}

@Composable
fun VideoSelectionBottomBar(
    selectedVideos: Set<Video>,
    onMove: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    onShare: () -> Unit,
    onMarkStatus: (String) -> Unit,
    showTagAndShare: Boolean,
    onSelectAll: (() -> Unit)? = null,
    totalCount: Int = 0
) {
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
            // Rename — only when 1 item selected
            IconButton(
                onClick = onRename,
                enabled = selectedVideos.size == 1
            ) {
                Icon(
                    imageVector = Icons.Filled.DriveFileRenameOutline,
                    contentDescription = "Rename",
                    tint = if (selectedVideos.size == 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    modifier = Modifier.size(24.dp)
                )
            }
            // Share
            IconButton(onClick = onShare) {
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
