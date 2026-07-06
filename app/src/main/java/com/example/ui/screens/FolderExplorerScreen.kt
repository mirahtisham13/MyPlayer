package com.example.ui.screens

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility

import androidx.compose.ui.platform.LocalContext
import android.widget.Toast


import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FolderItem
import com.example.model.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderExplorerScreen(
    folders: List<FolderItem>,
    videos: List<VideoItem>,
    selectedFolder: FolderItem?,
    onFolderSelect: (FolderItem?) -> Unit,
    onVideoSelect: (VideoItem) -> Unit,
    onDeleteVideo: (VideoItem) -> Unit,
    hasStoragePermission: Boolean,
    onRequestPermission: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onPlayLastPlayed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isSearchActive by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Dialog state for video details
    var selectedVideoForDetails by remember { mutableStateOf<VideoItem?>(null) }
    
    var selectedFolderPaths by remember { mutableStateOf(emptySet<String>()) }
    var selectedVideoIds by remember { mutableStateOf(emptySet<Long>()) }
    var showMoveDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val isSelectionMode = selectedFolderPaths.isNotEmpty() || selectedVideoIds.isNotEmpty()
    val allItemsCount = (if (selectedFolder == null) folders.size else 0) + videos.size
    val allSelected = allItemsCount > 0 && (selectedFolderPaths.size + selectedVideoIds.size) == allItemsCount

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                if (isSelectionMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { 
                            selectedFolderPaths = emptySet()
                            selectedVideoIds = emptySet()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel Selection", tint = MaterialTheme.colorScheme.onBackground)
                        }
                        
                        Text(
                            text = "${selectedFolderPaths.size + selectedVideoIds.size} selected",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        

                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedFolder != null) {
                            IconButton(
                                onClick = { onFolderSelect(null) },
                                modifier = Modifier.testTag("explorer_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to Folders",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
    
                        // Title: Display name of currently opened folder or "MyPlayer"
                        Text(
                            text = selectedFolder?.name ?: "MyPlayer",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
    
                        // Theme Toggle Button
                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier.testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (isSelectionMode) {
                androidx.compose.material3.BottomAppBar(
                    containerColor = if (isDarkTheme) Color.Black else Color(0x99FFFFFF),
                    contentColor = MaterialTheme.colorScheme.onBackground
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = allSelected,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    if (selectedFolder == null) {
                                        selectedFolderPaths = folders.map { it.path }.toSet()
                                    }
                                    selectedVideoIds = videos.map { it.id }.toSet()
                                } else {
                                    selectedFolderPaths = emptySet()
                                    selectedVideoIds = emptySet()
                                }
                            }
                        )
                        IconButton(onClick = { showMoveDialog = true }) {
                            Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = "Move")
                        }
                        
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                        
                        if (selectedFolderPaths.size + selectedVideoIds.size == 1) {
                            IconButton(onClick = { showRenameDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Rename")
                            }
                            IconButton(onClick = { showInfoDialog = true }) {
                                Icon(Icons.Default.Info, contentDescription = "Info")
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                androidx.compose.material3.FloatingActionButton(
                onClick = onPlayLastPlayed,
                containerColor = if (isDarkTheme) Color.White else Color.Black,
                contentColor = if (isDarkTheme) Color.Black else Color.White,
                shape = androidx.compose.foundation.shape.CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .testTag("play_last_played_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Last Played Video",
                    modifier = Modifier.size(36.dp)
                )
            }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                                .background(
                    if (isDarkTheme) {
                        Brush.radialGradient(
                            colors = listOf(Color.Black, Color.Black),
                            radius = 2000f,
                            center = Offset(500f, 500f)
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(Color(0xFFFDFDFD), Color(0xFFEBEBF5), Color(0xFFD1D1E0)),
                            radius = 2000f,
                            center = Offset(500f, 500f)
                        )
                    }
                )
        ) {
            // Permission Banner if not granted
            if (!hasStoragePermission) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = "Storage Needed",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Grant Storage Permission to Scan Local Videos",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(
                            onClick = onRequestPermission,
                            modifier = Modifier
                                .border(1.dp, Color.White, RoundedCornerShape(16.dp))
                                .testTag("request_permission_button")
                        ) {
                            Text("Grant Permission", color = Color.White)
                        }
                    }
                }
            }

            if (selectedFolder == null) {
                // Browsing Folders
                if (folders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No folders containing videos found.",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(folders) { folder ->
                                val isSelected = selectedFolderPaths.contains(folder.path)
                                FolderListRow(
                                    folder = folder, 
                                    isSelected = isSelected,
                                    onSelectToggle = {
                                        selectedFolderPaths = if (isSelected) selectedFolderPaths - folder.path else selectedFolderPaths + folder.path
                                    },
                                    onClick = { 
                                        if (isSelectionMode) {
                                            selectedFolderPaths = if (isSelected) selectedFolderPaths - folder.path else selectedFolderPaths + folder.path
                                        } else {
                                            onFolderSelect(folder) 
                                        }
                                    }
                                )
                            }
                        }
                }
            } else {
                // Browsing Videos in Folder
                if (videos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No videos match your query.",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(videos) { video ->
                                val isSelected = selectedVideoIds.contains(video.id)
                                VideoListRow(
                                    video = video,
                                    isSelected = isSelected,
                                    onSelectToggle = {
                                        selectedVideoIds = if (isSelected) selectedVideoIds - video.id else selectedVideoIds + video.id
                                    },
                                    onClick = { 
                                        if (isSelectionMode) {
                                            selectedVideoIds = if (isSelected) selectedVideoIds - video.id else selectedVideoIds + video.id
                                        } else {
                                            onVideoSelect(video)
                                        }
                                    },
                                    onDetails = { selectedVideoForDetails = video },
                                    onDelete = { onDeleteVideo(video) }
                                )
                            }
                        }
                }
            }
        }
    }

    // Video Details Dialog
    selectedVideoForDetails?.let { video ->
        VideoDetailsDialog(
            video = video,
            onDismiss = { selectedVideoForDetails = null }
        )
    }
    
    if (showMoveDialog) {
        MoveDialog(
            folders = folders,
            selectedCount = selectedFolderPaths.size + selectedVideoIds.size,
            onDismiss = { showMoveDialog = false },
                        onMoveHere = { targetPath ->
                Toast.makeText(context, "Moved to ${targetPath.substringAfterLast("/")}", Toast.LENGTH_SHORT).show()
                showMoveDialog = false
                selectedFolderPaths = emptySet()
                selectedVideoIds = emptySet()
            }
        )
    }

    if (showRenameDialog) {
        val oldName = if (selectedVideoIds.isNotEmpty()) {
            videos.find { it.id == selectedVideoIds.first() }?.displayTitle ?: ""
        } else {
            folders.find { it.path == selectedFolderPaths.first() }?.name ?: ""
        }
        var newName by remember { mutableStateOf(oldName) }
        
        androidx.compose.material3.AlertDialog(
            modifier = Modifier,
            containerColor = Color.Transparent,
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename") },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showRenameDialog = false
                    selectedFolderPaths = emptySet()
                    selectedVideoIds = emptySet()
                }) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showInfoDialog) {
        androidx.compose.material3.AlertDialog(
            modifier = Modifier,
            containerColor = Color.Transparent,
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Information") },
            text = {
                Column {
                    if (selectedVideoIds.isNotEmpty()) {
                        val video = videos.find { it.id == selectedVideoIds.first() }
                        if (video != null) {
                            Text("Name: ${video.displayTitle}")
                            Text("Location: ${video.path}")
                            Text("Size: ${video.sizeFormatted}")
                            Text("Resolution: ${video.resolution}")
                            Text("Format: ${video.path.substringAfterLast('.', "")}")
                        }
                    } else if (selectedFolderPaths.isNotEmpty()) {
                        val folder = folders.find { it.path == selectedFolderPaths.first() }
                        if (folder != null) {
                            Text("Name: ${folder.name}")
                            Text("Location: ${folder.path}")
                            Text("Videos: ${folder.videoCount}")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    if (showDeleteDialog) {
        androidx.compose.material3.AlertDialog(
            modifier = Modifier,
            containerColor = Color.Transparent,
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete") },
            text = { Text("Are you sure you want to delete ${selectedFolderPaths.size + selectedVideoIds.size} item(s)?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    selectedFolderPaths = emptySet()
                    selectedVideoIds = emptySet()
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Subcomponent: Move Dialog / Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveDialog(
    folders: List<com.example.model.FolderItem>,
    selectedCount: Int,
    onDismiss: () -> Unit,
    onMoveHere: (String) -> Unit
) {
    var selectedTargetFolder by remember { mutableStateOf<com.example.model.FolderItem?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    colors = listOf(Color.Black, Color.Black),
                    radius = 2000f,
                    center = Offset(500f, 500f)
                )
            ),
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text(
                            text = "Move",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f).padding(start = 16.dp)
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth().background(Color(0x33FFFFFF)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$selectedCount item${if (selectedCount > 1) "s" else ""} selected", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
            },
            bottomBar = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { /* Create folder */ }) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = "Create", tint = Color.White)
                        Text("CREATE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = { selectedTargetFolder?.let { onMoveHere(it.path) } },
                        enabled = selectedTargetFolder != null,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("MOVE HERE", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                Text(
                    text = "Phone Storage",
                    color = Color(0xFF3B82F6),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(16.dp)
                )
                
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(folders) { folder ->
                        val isSelected = selectedTargetFolder == folder
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable { selectedTargetFolder = folder }
                                .background(if (isSelected) Color(0x333B82F6) else Color.Transparent)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp, 48.dp)
                                    .background(Color(0x4D000000), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(folder.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Text("${folder.videoCount} videos", color = Color.Gray, fontSize = 14.sp)
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = Color(0xFF3B82F6))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Subcomponent: Video Thumbnail (Asynchronous)
@Composable
fun VideoThumbnail(
    videoPath: String,
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    var thumbnailBitmap by remember(videoPath) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(videoPath) {
        if (isOnline) return@LaunchedEffect // Display placeholder for online streams to save bandwidth

        withContext(Dispatchers.IO) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(videoPath)
                val timeUs = 1000000L // Frame at 1s
                val bmp = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                retriever.release()
                thumbnailBitmap = bmp
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Box(
        modifier = modifier
            .background(Color.Transparent)
            .border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (thumbnailBitmap != null) {
            Image(
                bitmap = thumbnailBitmap!!.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Elegant placeholder icon for online or loading
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x1AFFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// Subcomponent: Folder List Item
@Composable
fun FolderListRow(
    folder: FolderItem,
    isSelected: Boolean,
    onSelectToggle: () -> Unit,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.onBackground == Color.White
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Large rounded-rect folder container matching screenshot
        Box(
            modifier = Modifier
                .size(width = 88.dp, height = 72.dp)
                .background(
                    color = if (isDark) Color(0x33FFFFFF) else Color(0x1F000000),
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable { onSelectToggle() },
            contentAlignment = Alignment.Center
        ) {
            val icon = if (folder.name.contains("Camera", ignoreCase = true)) {
                Icons.Default.PhotoCamera
            } else {
                Icons.Default.Folder
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.size(56.dp)
            )
            
            if (isSelected) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0x66000000), RoundedCornerShape(10.dp)), contentAlignment = Alignment.BottomEnd) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Selected", tint = Color(0xFF00C969), modifier = Modifier.padding(2.dp).size(20.dp))
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = folder.name,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${folder.videoCount} videos",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                fontSize = 13.sp
            )
        }
    }
}



// Subcomponent: STRICT VIDEO ROW PATTERN (List Mode)
@Composable
fun VideoListRow(
    video: VideoItem,
    isSelected: Boolean,
    onSelectToggle: () -> Unit,
    onClick: () -> Unit,
    onDetails: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val isDark = MaterialTheme.colorScheme.onBackground == Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rounded 16:9 Thumbnail on left
        Box(
            modifier = Modifier
                .width(140.dp)
                .aspectRatio(1.77f) // approx 16:9
                .clip(RoundedCornerShape(16.dp))
                .clickable { onSelectToggle() }
        ) {
            VideoThumbnail(
                videoPath = video.path,
                isOnline = video.isOnline,
                modifier = Modifier.fillMaxSize()
            )

            // Overlaid precise video duration on bottom right
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(Color(0x80000000), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = video.durationFormatted,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (isSelected) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0x66000000)), contentAlignment = Alignment.BottomEnd) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Selected", tint = Color(0xFF00C969), modifier = Modifier.padding(4.dp).size(20.dp))
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Center Metadata
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = video.displayTitle,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )

            if (video.hasSubtitle) {
                Spacer(modifier = Modifier.height(4.dp))
                // Subtitle Badge - Prominent solid green badge beneath title
                Box(
                    modifier = Modifier
                        .background(Color(0xFF00C969), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "SRT",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Action three-dot menu on far right
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu Options",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier
            ) {
                DropdownMenuItem(
                    text = { Text("Details & Info", color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        showMenu = false
                        onDetails()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = Color.Red) },
                    onClick = {
                        showMenu = false
                        onDelete()
                    }
                )
            }
        }
    }
}

// Subcomponent: Video Card (Grid Mode)
@Composable
fun VideoGridCard(
    video: VideoItem,
    isSelected: Boolean,
    onSelectToggle: () -> Unit,
    onClick: () -> Unit,
    onDetails: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val isDark = MaterialTheme.colorScheme.onBackground == Color.White

    Box(
        modifier = Modifier
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.77f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelectToggle() }
            ) {
                VideoThumbnail(
                    videoPath = video.path,
                    isOnline = video.isOnline,
                    modifier = Modifier.fillMaxSize()
                )
    
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .background(Color(0x80000000), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = video.durationFormatted,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (isSelected) {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0x66000000)), contentAlignment = Alignment.BottomEnd) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Selected", tint = Color(0xFF00C969), modifier = Modifier.padding(4.dp).size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.displayTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (video.hasSubtitle) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF00C969), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SRT",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier
                ) {
                    DropdownMenuItem(
                        text = { Text("Details & Info", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            showMenu = false
                            onDetails()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
        }
    }
}

// Subcomponent: Video Details Popup Dialog
@Composable
fun VideoDetailsDialog(
    video: VideoItem,
    onDismiss: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.onBackground == Color.White
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Details",
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Video Details", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailItem(label = "Title", value = video.title)
                DetailItem(label = "Duration", value = video.durationFormatted)
                DetailItem(label = "Resolution", value = video.resolution)
                DetailItem(label = "File Size", value = video.sizeFormatted)
                DetailItem(label = "Subtitles", value = if (video.hasSubtitle) "Available (SRT / VTT)" else "None Detected")
                DetailItem(label = "Type", value = if (video.isOnline) "Streaming Stream" else "Local Storage File")
                DetailItem(label = "Full Path", value = video.path)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}

@Composable
fun DetailItem(label: String, value: String) {
    Column {
        Text(text = label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Text(text = value, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
    }
}
