package com.example.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.model.VideoItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.graphics.Brush
import kotlin.math.abs

enum class GestureType {
    NONE, BRIGHTNESS, VOLUME, SEEK
}

enum class AspectMode(val label: String, val mode: Int) {
    FIT("Fit (16:9)", AspectRatioFrameLayout.RESIZE_MODE_FIT),
    ZOOM("Zoom (Crop)", AspectRatioFrameLayout.RESIZE_MODE_ZOOM),
    FILL("Stretch (Fill)", AspectRatioFrameLayout.RESIZE_MODE_FILL)
}

@Composable
fun VideoPlayerScreen(
    video: VideoItem,
    onBack: () -> Unit,
    onNextVideo: () -> Unit,
    onPreviousVideo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // AudioManager for Volume gesture
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }

    // Init player
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    // Load media item and restore saved playback position
    LaunchedEffect(video) {
        val mediaItemBuilder = MediaItem.Builder().setUri(Uri.parse(video.path))

        // If online sample has explicit subtitle SRT/VTT file, load it!
        if (video.isOnline && video.subtitlePath != null) {
            val subtitleConfig = MediaItem.SubtitleConfiguration.Builder(Uri.parse(video.subtitlePath))
                .setMimeType(MimeTypes.TEXT_VTT)
                .setLanguage("en")
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .build()
            mediaItemBuilder.setSubtitleConfigurations(listOf(subtitleConfig))
        } else if (!video.isOnline && video.subtitlePath != null) {
            // For local video files
            val subtitleConfig = MediaItem.SubtitleConfiguration.Builder(Uri.parse("file://${video.subtitlePath}"))
                .setMimeType(MimeTypes.APPLICATION_SUBRIP)
                .setLanguage("en")
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .build()
            mediaItemBuilder.setSubtitleConfigurations(listOf(subtitleConfig))
        }

        player.setMediaItem(mediaItemBuilder.build())
        
        // Restore playback position if any
        val prefs = context.getSharedPreferences("player_prefs", Context.MODE_PRIVATE)
        val savedPos = prefs.getLong("pos_${video.path}", 0L)
        if (savedPos > 0) {
            player.seekTo(savedPos)
        }

        player.prepare()
    }

    // States for custom overlay controls
    var isPlaying by remember { mutableStateOf(true) }
    var dynamicBlurRadius by remember { mutableFloatStateOf(16f) }
    
    // Dynamic Blur Radius Calculation based on video content complexity
    LaunchedEffect(isPlaying) {
        withContext(Dispatchers.IO) {
            val retriever = android.media.MediaMetadataRetriever()
            try {
                if (video.path.startsWith("http")) {
                    retriever.setDataSource(video.path, HashMap<String, String>())
                } else {
                    retriever.setDataSource(context, Uri.parse(video.path))
                }
                while (isPlaying) {
                    val timeUs = withContext(kotlinx.coroutines.Dispatchers.Main) { player.currentPosition * 1000L }
                    val frame = retriever.getFrameAtTime(timeUs, android.media.MediaMetadataRetriever.OPTION_CLOSEST)
                    if (frame != null) {
                        // Calculate complexity (e.g., edge detection / variance approximation)
                        val step = Math.max(1, frame.width / 10)
                        var sum = 0
                        var count = 0
                        for (x in 0 until frame.width step step) {
                            for (y in 0 until frame.height step step) {
                                val pixel = frame.getPixel(x, y)
                                val brightness = (android.graphics.Color.red(pixel) + android.graphics.Color.green(pixel) + android.graphics.Color.blue(pixel)) / 3
                                sum += brightness
                                count++
                            }
                        }
                        val avg = if(count > 0) sum / count else 0
                        var diffSum = 0f
                        for (x in 0 until frame.width step step) {
                            for (y in 0 until frame.height step step) {
                                val pixel = frame.getPixel(x, y)
                                val brightness = (android.graphics.Color.red(pixel) + android.graphics.Color.green(pixel) + android.graphics.Color.blue(pixel)) / 3
                                diffSum += Math.abs(brightness - avg).toFloat()
                            }
                        }
                        val complexityScore = if(count > 0) diffSum / count else 0f
                        // Map complexity (0-100) to blur radius (8f to 32f)
                        val calculatedBlur = 8f + (complexityScore / 100f) * 24f
                        dynamicBlurRadius = calculatedBlur.coerceIn(8f, 32f)
                    }
                    delay(2000) // update every 2 seconds
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    retriever.release()
                } catch(e: Exception) {}
            }
        }
    }

    var videoRotation by remember { mutableFloatStateOf(0f) }

    // Parse video's default orientation from metadata or fallback
    val isLandscape = remember(video) {
        try {
            val parts = video.resolution.lowercase().split("x")
            if (parts.size >= 2) {
                parts[0].trim().toInt() > parts[1].trim().toInt()
            } else {
                true // Default to landscape
            }
        } catch (e: Exception) {
            true
        }
    }

    // Set activity requested orientation to match video's default orientation
    // use SENSOR modes so screen rotation is allowed to opposite sides
    DisposableEffect(isLandscape) {
        activity?.let { act ->
            act.requestedOrientation = if (isLandscape) {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            }
        }
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var speed by remember { mutableFloatStateOf(1.0f) }
    var aspectMode by remember { mutableStateOf(AspectMode.FIT) }
    var controlsVisible by remember { mutableStateOf(true) }
    var isControlsLocked by remember { mutableStateOf(false) }

    // Dialog flags
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showAudioDialog by remember { mutableStateOf(false) }

    // Gesture HUD indicators
    var activeGesture by remember { mutableStateOf(GestureType.NONE) }
    var gestureProgress by remember { mutableFloatStateOf(0f) }
    var seekProgressValue by remember { mutableLongStateOf(0L) }

    // Track state changes from player
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                duration = player.duration.coerceAtLeast(0L)
                if (playbackState == Player.STATE_ENDED) {
                    isPlaying = false
                }
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                isPlaying = playWhenReady
            }
        }
        player.addListener(listener)
        onDispose {
            // Save final playback position and last played video path
            val lastPos = player.currentPosition.coerceAtLeast(0L)
            context.getSharedPreferences("player_prefs", Context.MODE_PRIVATE).edit()
                .putLong("pos_${video.path}", lastPos)
                .putString("last_played_path", video.path)
                .apply()

            player.removeListener(listener)
            player.release()
            // Reset brightness override on back
            activity?.let {
                val layoutParams = it.window.attributes
                layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                it.window.attributes = layoutParams
            }
        }
    }

    // Keep updating current player progress and periodically saving it
    LaunchedEffect(isPlaying) {
        val prefs = context.getSharedPreferences("player_prefs", Context.MODE_PRIVATE)
        while (isPlaying) {
            val pos = player.currentPosition.coerceAtLeast(0L)
            currentPosition = pos
            prefs.edit()
                .putLong("pos_${video.path}", pos)
                .putString("last_played_path", video.path)
                .apply()
            delay(1000)
        }
    }

    // Auto-hide controls timer
    LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            delay(3500)
            controlsVisible = false
        }
    }

    // Standard Android Back pressed handler inside full player screen
    BackHandler {
        onBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Video View with constraints based layout that dynamically swaps width/height when rotated 90/270 degrees
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val containerWidth = maxWidth
            val containerHeight = maxHeight
            
            val isRotated = (videoRotation == 90f || videoRotation == 270f)
            val videoWidth = if (isRotated) containerHeight else containerWidth
            val videoHeight = if (isRotated) containerWidth else containerHeight
            
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = false // Hide default Media3 controls
                        this.player = player
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { playerView ->
                    playerView.resizeMode = aspectMode.mode
                },
                modifier = Modifier
                    .size(videoWidth, videoHeight)
                    .graphicsLayer {
                        rotationZ = videoRotation
                    }
            )
        }

        // Transparent Gesture and Click Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    controlsVisible = !controlsVisible
                }
                .pointerInput(Unit) {
                    var dragStartGesture = GestureType.NONE
                    var startVolume = 0
                    var startBrightness = 0.5f

                    detectDragGestures(
                        onDragStart = { offset ->
                            val width = size.width
                            val isRightHalf = offset.x > width / 2f
                            dragStartGesture = GestureType.NONE

                            // Read initial volumes/brightnesses
                            startVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                            activity?.let { act ->
                                val lp = act.window.attributes
                                startBrightness = if (lp.screenBrightness < 0) 0.5f else lp.screenBrightness
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (isControlsLocked) return@detectDragGestures
                            val width = size.width
                            val height = size.height

                            // Determine gesture type based on first substantial movement
                            if (dragStartGesture == GestureType.NONE) {
                                if (abs(dragAmount.x) > abs(dragAmount.y) && abs(dragAmount.x) > 10f) {
                                    dragStartGesture = GestureType.SEEK
                                } else if (abs(dragAmount.y) > 10f) {
                                    val isRight = change.position.x > width / 2f
                                    dragStartGesture = if (isRight) GestureType.VOLUME else GestureType.BRIGHTNESS
                                }
                            }

                            // Perform gesture adjustments
                            when (dragStartGesture) {
                                GestureType.VOLUME -> {
                                    activeGesture = GestureType.VOLUME
                                    val fraction = -dragAmount.y / height.toFloat()
                                    val volDiff = (fraction * maxVolume * 2).toInt()
                                    val newVolume = (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) + volDiff)
                                        .coerceIn(0, maxVolume)
                                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
                                    gestureProgress = newVolume.toFloat() / maxVolume.toFloat()
                                }
                                GestureType.BRIGHTNESS -> {
                                    activeGesture = GestureType.BRIGHTNESS
                                    val fraction = -dragAmount.y / height.toFloat()
                                    activity?.let { act ->
                                        val lp = act.window.attributes
                                        val curBright = if (lp.screenBrightness < 0) 0.5f else lp.screenBrightness
                                        val newBright = (curBright + fraction * 1.5f).coerceIn(0.01f, 1.0f)
                                        lp.screenBrightness = newBright
                                        act.window.attributes = lp
                                        gestureProgress = newBright
                                    }
                                }
                                GestureType.SEEK -> {
                                    activeGesture = GestureType.SEEK
                                    controlsVisible = true
                                    val seekFraction = dragAmount.x / width.toFloat()
                                    val seekAmount = (seekFraction * duration).toLong()
                                    seekProgressValue = (player.currentPosition + seekAmount).coerceIn(0L, duration)
                                    player.seekTo(seekProgressValue)
                                }
                                else -> {}
                            }
                        },
                        onDragEnd = {
                            activeGesture = GestureType.NONE
                        }
                    )
                }
        )

        // Custom Overlay Controls
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            ) {
                if (!isControlsLocked) {
                    // Header (Back + Video Title)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = video.displayTitle,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { showAudioDialog = true }) {
                            Icon(Icons.Default.MusicNote, contentDescription = "Audio Tracks", tint = Color.White)
                        }
                        IconButton(onClick = { showSubtitleDialog = true }) {
                            Icon(Icons.Default.Subtitles, contentDescription = "Subtitles", tint = Color.White)
                        }
                        Box(
                            modifier = Modifier
                                .clickable { showSpeedDialog = true }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${speed}X", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Left Controls (Rotation, Speed)
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 80.dp, start = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(onClick = {
                            videoRotation = (videoRotation + 90f) % 360f
                        }) {
                            Icon(Icons.Default.ScreenRotation, contentDescription = "Rotation", tint = Color.White)
                        }
                    }
                }

                // Bottom Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
                        .padding(16.dp)
                ) {
                    if (!isControlsLocked) {
                        // Seek Bar Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatTime(currentPosition),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Slider(
                                value = currentPosition.toFloat(),
                                onValueChange = {
                                    player.seekTo(it.toLong())
                                    currentPosition = it.toLong()
                                },
                                valueRange = 0f..(duration.toFloat().coerceAtLeast(1f)),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = Color.White,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp)
                                    .testTag("player_seek_slider")
                            )
                            Text(
                                text = "-" + formatTime(duration - currentPosition),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
    
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Media Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left
                        IconButton(onClick = { isControlsLocked = !isControlsLocked }) {
                            Icon(if (isControlsLocked) Icons.Default.Lock else Icons.Default.LockOpen, contentDescription = "Lock", tint = Color.White)
                        }

                        if (!isControlsLocked) {
                            // Center
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                                IconButton(onClick = onPreviousVideo) {
                                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(32.dp))
                                }
                                IconButton(
                                    onClick = { 
                                        if (player.playbackState == Player.STATE_ENDED) {
                                            player.seekTo(0)
                                            player.play()
                                        } else {
                                            if (isPlaying) player.pause() else player.play() 
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        tint = Color.White,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                                IconButton(onClick = onNextVideo) {
                                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(32.dp))
                                }
                            }
    
                            // Right
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    activity?.let { act ->
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                            act.enterPictureInPictureMode(android.app.PictureInPictureParams.Builder().build())
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.PictureInPictureAlt, contentDescription = "PiP", tint = Color.White)
                                }
                                IconButton(
                                    onClick = {
                                        aspectMode = when (aspectMode) {
                                            AspectMode.FIT -> AspectMode.ZOOM
                                            AspectMode.ZOOM -> AspectMode.FILL
                                            AspectMode.FILL -> AspectMode.FIT
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Fullscreen, contentDescription = "Aspect Ratio", tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Gesture Action floating HUD
        AnimatedVisibility(
            visible = activeGesture != GestureType.NONE,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val icon = when (activeGesture) {
                        GestureType.BRIGHTNESS -> Icons.Default.BrightnessLow
                        GestureType.VOLUME -> Icons.Default.VolumeUp
                        GestureType.SEEK -> Icons.Default.FastForward
                        else -> Icons.Default.PlayArrow
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val textValue = when (activeGesture) {
                        GestureType.BRIGHTNESS -> "${(gestureProgress * 100).toInt()}%"
                        GestureType.VOLUME -> "${(gestureProgress * 100).toInt()}%"
                        GestureType.SEEK -> "Seek: ${formatTime(seekProgressValue)}"
                        else -> ""
                    }

                    Text(
                        text = textValue,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (activeGesture != GestureType.SEEK) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { gestureProgress },
                            modifier = Modifier
                                .width(80.dp)
                                .height(4.dp),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.2f),
                        )
                    }
                }
            }
        }
    }

    // Playback Speed Dialog
    if (showSpeedDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) { showSpeedDialog = false },
            contentAlignment = Alignment.BottomCenter
        ) {
            SpeedControlPanel(
                speed = speed,
                onSpeedChange = { s -> 
                    speed = s
                    player.setPlaybackSpeed(s)
                },
                onClose = { showSpeedDialog = false },
                modifier = Modifier.clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) {} // consume clicks so it doesn't close
            )
        }
    }

    // Audio Dialog
    if (showAudioDialog) {
        AlertDialog(
            onDismissRequest = { showAudioDialog = false },
            title = { Text("Audio Tracks", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = Color.Transparent,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.9f), RoundedCornerShape(16.dp)),
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    val audioGroups = player.currentTracks.groups.filter { it.type == androidx.media3.common.C.TRACK_TYPE_AUDIO }
                    if (audioGroups.isEmpty()) {
                        Text("No audio tracks available", color = Color.Gray)
                    } else {
                        audioGroups.forEach { group ->
                            val isSelected = group.isSelected
                            val trackName = group.mediaTrackGroup.getFormat(0).let { format ->
                                val lang = format.language ?: "Unknown"
                                val label = format.label ?: ""
                                if (label.isNotEmpty()) "$label ($lang)" else lang
                            }
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        player.trackSelectionParameters = player.trackSelectionParameters
                                            .buildUpon()
                                            .setOverrideForType(androidx.media3.common.TrackSelectionOverride(group.mediaTrackGroup, 0))
                                            .build()
                                        showAudioDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(selectedColor = Color.White, unselectedColor = Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(trackName.uppercase(), color = if (isSelected) Color.White else Color.Gray)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAudioDialog = false }) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }

    // Subtitles Dialog
    if (showSubtitleDialog) {
        var subtitleTab by remember { mutableIntStateOf(0) }
        val context = androidx.compose.ui.platform.LocalContext.current
        
        val subtitlePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                val currentItem = player.currentMediaItem
                if (currentItem != null) {
                    val subtitleConfig = androidx.media3.common.MediaItem.SubtitleConfiguration.Builder(uri)
                        .setMimeType(androidx.media3.common.MimeTypes.APPLICATION_SUBRIP)
                        .setLanguage("en")
                        .setSelectionFlags(androidx.media3.common.C.SELECTION_FLAG_DEFAULT)
                        .build()
                        
                    val newItem = currentItem.buildUpon()
                        .setSubtitleConfigurations(currentItem.localConfiguration?.subtitleConfigurations.orEmpty() + subtitleConfig)
                        .build()
                        
                    val pos = player.currentPosition
                    player.setMediaItem(newItem)
                    player.seekTo(pos)
                    player.prepare()
                    player.play()
                }
                showSubtitleDialog = false
            }
        }
        
        AlertDialog(
            onDismissRequest = { showSubtitleDialog = false },
            title = { Text("Subtitles", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = Color.Transparent,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.9f), RoundedCornerShape(16.dp)),
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            text = "Tracks", 
                            color = if (subtitleTab == 0) Color.White else Color.Gray,
                            fontWeight = if (subtitleTab == 0) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.clickable { subtitleTab = 0 }.padding(8.dp)
                        )
                        Text(
                            text = "Sync", 
                            color = if (subtitleTab == 1) Color.White else Color.Gray,
                            fontWeight = if (subtitleTab == 1) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.clickable { subtitleTab = 1 }.padding(8.dp)
                        )
                        Text(
                            text = "Online", 
                            color = if (subtitleTab == 2) Color.White else Color.Gray,
                            fontWeight = if (subtitleTab == 2) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.clickable { subtitleTab = 2 }.padding(8.dp)
                        )
                    }
                    
                    if (subtitleTab == 0) {
                        val textGroups = player.currentTracks.groups.filter { it.type == androidx.media3.common.C.TRACK_TYPE_TEXT }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    player.trackSelectionParameters = player.trackSelectionParameters
                                        .buildUpon()
                                        .setIgnoredTextSelectionFlags(androidx.media3.common.C.SELECTION_FLAG_DEFAULT.inv())
                                        .setTrackTypeDisabled(androidx.media3.common.C.TRACK_TYPE_TEXT, true)
                                        .build()
                                    showSubtitleDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = player.trackSelectionParameters.disabledTrackTypes.contains(androidx.media3.common.C.TRACK_TYPE_TEXT),
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = Color.White, unselectedColor = Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("OFF", color = Color.White)
                        }
                        
                        textGroups.forEach { group ->
                            val isSelected = group.isSelected
                            val trackName = group.mediaTrackGroup.getFormat(0).let { format ->
                                val lang = format.language ?: "Unknown"
                                val label = format.label ?: ""
                                if (label.isNotEmpty()) "$label ($lang)" else lang
                            }
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        player.trackSelectionParameters = player.trackSelectionParameters
                                            .buildUpon()
                                            .setTrackTypeDisabled(androidx.media3.common.C.TRACK_TYPE_TEXT, false)
                                            .setOverrideForType(androidx.media3.common.TrackSelectionOverride(group.mediaTrackGroup, 0))
                                            .build()
                                        showSubtitleDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(selectedColor = Color.White, unselectedColor = Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(trackName.uppercase(), color = if (isSelected) Color.White else Color.Gray)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        androidx.compose.material3.Button(
                            onClick = { subtitlePickerLauncher.launch("*/*") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = "Import", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import from device")
                        }
                    } else if (subtitleTab == 1) {
                        var audioDelay by remember { mutableFloatStateOf(0f) }
                        var subDelay by remember { mutableFloatStateOf(0f) }
                        
                        Text("Audio Delay", color = Color.Gray, fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Slider(
                                value = audioDelay,
                                onValueChange = { audioDelay = it },
                                valueRange = -5f..5f,
                                modifier = Modifier.weight(1f)
                            )
                            Text(String.format("%.1f s", audioDelay), color = Color.White, modifier = Modifier.width(50.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Subtitle Delay", color = Color.Gray, fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Slider(
                                value = subDelay,
                                onValueChange = { subDelay = it },
                                valueRange = -5f..5f,
                                modifier = Modifier.weight(1f)
                            )
                            Text(String.format("%.1f s", subDelay), color = Color.White, modifier = Modifier.width(50.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                        }
                        
                        Text("Note: Real-time offset adjustments require custom MediaSource implementations which are beyond this basic ExoPlayer integration.", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(top = 16.dp))
                    } else if (subtitleTab == 2) {
                        var searchQuery by remember { mutableStateOf(video.title) }
                        
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search OpenSubtitles", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF4285F4),
                                unfocusedBorderColor = Color.Gray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        androidx.compose.material3.Button(
                            onClick = { /* Mock Search */ },
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Search")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("OpenSubtitles API integration requires a registered API key.", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSubtitleDialog = false }) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun IconButtonWithLabel(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
