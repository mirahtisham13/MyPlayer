package com.myplayer.ui.screen

import android.app.Activity
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.net.Uri
import kotlin.math.roundToInt
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import com.myplayer.player.engine.MPVSurfaceView
import com.myplayer.player.engine.PlayerState
import com.myplayer.ui.common.PlayerControls
import com.myplayer.ui.common.GestureOverlay
import com.myplayer.ui.common.icons.AspectIcons
import com.myplayer.domain.model.PlayerButton
import com.myplayer.domain.model.Video
import kotlinx.coroutines.delay
import android.media.AudioManager
import android.content.Context
import com.myplayer.util.findActivity
import com.myplayer.player.model.TrackInfo
import com.myplayer.data.repository.SubtitleFont
import com.myplayer.data.repository.PlaybackSettings

import com.myplayer.data.repository.MultiFingerAction
import android.provider.MediaStore
import com.myplayer.ui.common.SubtitleSettingsSideSheet
import com.myplayer.ui.common.sheets.AudioSettingsSideSheet
import com.myplayer.ui.common.sheets.QualitySettingsSideSheet
import com.myplayer.ui.common.ComposeSubtitleOverlay
import com.myplayer.ui.common.sheets.PlayerSettingsSideSheet

import com.myplayer.player.model.ChapterInfo
import com.myplayer.ui.common.sheets.ChaptersSideSheet
import com.myplayer.player.model.DecoderMode
import com.myplayer.ui.common.sheets.DecoderSideSheet
import androidx.activity.compose.BackHandler
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.os.BatteryManager
import android.os.Build
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.myplayer.player.service.MediaPlaybackService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.myplayer.ui.common.formatTime
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myplayer.data.repository.DoubleTapAction
import com.myplayer.data.repository.FullScreenMode
import com.myplayer.data.repository.OrientationMode
import com.myplayer.data.repository.SoftButtonMode
import com.myplayer.player.model.AspectMode
import com.myplayer.ui.screen.videolist.components.common.PlayingIndicator
import com.myplayer.ui.screen.videolist.components.video.VideoThumbnail
import com.myplayer.ui.screen.videolist.components.video.DurationBadge
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyRow
import com.myplayer.domain.model.LayoutMode
import androidx.compose.ui.platform.LocalConfiguration
import com.myplayer.viewmodel.PreFetchedVideoMetadata

@Composable
fun PlayerScreen(
    playbackState: PlayerState,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    currentUri: Uri?,
    preFetchedMetadata: PreFetchedVideoMetadata? = null,
    videoWidth: Long,
    videoHeight: Long,
    videoRotation: Long,
    playbackSpeed: Float,
    savedBrightness: Float,
    savedVolume: Int,
    onPlayPauseToggle: () -> Unit,
    onSeek: (Long, Boolean) -> Unit,
    onSetPlaybackSpeed: (Float) -> Unit,
    onCycleSubtitle: () -> Unit,
    onCycleAudio: () -> Unit,
    onBackClick: () -> Unit,
    onSurfaceReady: () -> Unit,
    onSaveBrightness: (Float) -> Unit,
    onSaveVolume: (Int) -> Unit,
    modifier: Modifier = Modifier,
    seekBarStyle: String = "standard",
    hasNext: Boolean = false,
    hasPrevious: Boolean = false,
    onNextClick: () -> Unit = {},
    onPrevClick: () -> Unit = {},
    currentSubtitleText: String = "",
    subtitleTracks: List<TrackInfo> = emptyList(),
    audioTracks: List<TrackInfo> = emptyList(),
    audioBoosterEnabled: Boolean = false,
    audioBoostVolume: Int = 100,
    onToggleAudioBooster: (Boolean) -> Unit = {},
    onSetAudioBoostVolume: (Int) -> Unit = {},
    playbackSettings: PlaybackSettings = PlaybackSettings(),
    onSelectSubtitleTrack: (Int) -> Unit = {},
    onSelectAudioTrack: (Int) -> Unit = {},
    onSetSubtitleDelay: (Long) -> Unit = {},
    onSeekNextSubtitle: () -> Unit = {},
    onSeekPrevSubtitle: () -> Unit = {},
    onUpdateUseSystemCaptionStyle: (Boolean) -> Unit = {},
    onUpdateSubtitleFont: (SubtitleFont) -> Unit = {},
    onUpdateIsSubtitleBold: (Boolean) -> Unit = {},
    onUpdateForceAssSubtitleOverride: (Boolean) -> Unit = {},
    onUpdateSubtitleTextSizeScale: (Float) -> Unit = {},
    onUpdateSubtitleBgStyle: (Int) -> Unit = {},
    onUpdateSubtitleDelay: (Long) -> Unit = {},
    onUpdateSubtitleVerticalOffset: (Float) -> Unit = {},
    onUpdateSubtitleGesturesEnabled: (Boolean) -> Unit = {},
    onUpdateCustomPlaybackSpeed: (Float) -> Unit = {},
    onUpdateTapAndHoldSpeed: (Float) -> Unit = {},
    onUpdateDoubleTapSeekDuration: (Long) -> Unit = {},
    onUpdateLongPressEnabled: (Boolean) -> Unit = {},
    onUpdateLongPressSpeed: (Float) -> Unit = {},
    onUpdateDoubleTapAction: (DoubleTapAction) -> Unit = {},
    onUpdateTwoFingerAction: (MultiFingerAction) -> Unit = {},
    onUpdateThreeFingerAction: (MultiFingerAction) -> Unit = {},
    onUpdateOrientationMode: (OrientationMode) -> Unit = {},
    onUpdateFullScreenMode: (FullScreenMode) -> Unit = {},
    onUpdateAspectMode: (AspectMode) -> Unit = {},
    onUpdateSoftButtonMode: (SoftButtonMode) -> Unit = {},
    onUpdateControlIconSize: (String) -> Unit = {},
    onUpdateSeekBarStyle: (String) -> Unit = {},
    onUpdateAutoPlayEnabled: (Boolean) -> Unit = {},
    onUpdateShowSeekButtons: (Boolean) -> Unit = {},
    onUpdateShowNextPrevButtons: (Boolean) -> Unit = {},
    onUpdateShowRemainingTime: (Boolean) -> Unit = {},
    onUpdateShowBatteryClockOverlay: (Boolean) -> Unit = {},
    onUpdatePauseWhenObstructed: (Boolean) -> Unit = {},
    onUpdateKeepAwakeAlways: (Boolean) -> Unit = {},

    chapters: List<ChapterInfo> = emptyList(),
    onSelectChapter: (Int) -> Unit = {},
    currentDecoder: String = "AUTO",
    onUpdateDecoderMode: (DecoderMode) -> Unit = {},
    isHwSupported: Boolean = true,
    onTakeVideoScreenshot: () -> Unit = {},
    onCycleAspectMode: () -> Unit = {},
    isInPipMode: Boolean = false,
    onEnterPip: () -> Unit = {},
    onUpdateBackgroundPlayEnabled: (Boolean) -> Unit = {},
    onUpdateShowControlGradients: (Boolean) -> Unit = {},
    onUpdateShowUpNextQueue: (Boolean) -> Unit = {},
    onUpdateIsAmbientModeEnabled: (Boolean) -> Unit = {},
    onUpdateAmbientBlurStyle: (com.myplayer.data.repository.AmbientBlurStyle) -> Unit = {},
    networkSpeedBytesPerSec: Long = 0L,
    bufferDurationSeconds: Double = 0.0,
    isNetworkStream: Boolean = false,
    bufferedPosition: Long = 0L,
    isDynamicSpeedActive: Boolean = false,
    onSetDynamicSpeedActive: (Boolean) -> Unit = {},
    viewModel: com.myplayer.viewmodel.PlayerViewModel? = null,
    queueList: List<Video> = emptyList(),
    currentVideoId: String? = null,
    isQueueVisible: Boolean = false,
    onQueueVisibleChange: (Boolean) -> Unit = {},
    onQueueVideoClick: (Video) -> Unit = {},
    onUpdateQueueLayoutMode: (LayoutMode) -> Unit = {}
) {
    val localContext = LocalContext.current
    val owner = localContext.findActivity() as? androidx.lifecycle.ViewModelStoreOwner
    val resolvedViewModel = remember(owner) {
        owner?.let { androidx.lifecycle.ViewModelProvider(it)[com.myplayer.viewmodel.PlayerViewModel::class.java] }
    }
    val activeViewModel = viewModel ?: resolvedViewModel
    val bufferedPosition by (activeViewModel?.bufferedPosition ?: kotlinx.coroutines.flow.MutableStateFlow(bufferedPosition)).collectAsStateWithLifecycle()
    val engineMediaTitle by (activeViewModel?.mediaTitle ?: kotlinx.coroutines.flow.MutableStateFlow("")).collectAsStateWithLifecycle()
    val currentUri by (activeViewModel?.currentUri ?: kotlinx.coroutines.flow.MutableStateFlow(null)).collectAsStateWithLifecycle()
    val activeVideoWidth by (activeViewModel?.videoWidth ?: kotlinx.coroutines.flow.MutableStateFlow(0L)).collectAsStateWithLifecycle()
    val activeVideoHeight by (activeViewModel?.videoHeight ?: kotlinx.coroutines.flow.MutableStateFlow(0L)).collectAsStateWithLifecycle()
    val activeDuration by (activeViewModel?.duration ?: kotlinx.coroutines.flow.MutableStateFlow(0L)).collectAsStateWithLifecycle()

    val currentVideo = remember(currentVideoId, currentUri, engineMediaTitle, activeDuration, activeVideoWidth, activeVideoHeight, queueList) {
        queueList.firstOrNull { it.uri == currentVideoId || (currentUri != null && it.uri == currentUri.toString()) }
            ?: currentUri?.let { uri ->
                Video(
                    uri = uri.toString(),
                    title = engineMediaTitle.ifBlank { uri.lastPathSegment ?: "Video" },
                    duration = activeDuration,
                    folderName = uri.path?.substringBeforeLast('/', "")?.substringAfterLast('/') ?: "",
                    path = uri.path ?: "",
                    size = 0L,
                    width = activeVideoWidth.toInt(),
                    height = activeVideoHeight.toInt()
                )
            }
    }

    val isFrameCaptureMode by (activeViewModel?.isFrameCaptureMode ?: kotlinx.coroutines.flow.MutableStateFlow(false)).collectAsStateWithLifecycle()
    val currentFrame by (activeViewModel?.currentFrame ?: kotlinx.coroutines.flow.MutableStateFlow(0L)).collectAsStateWithLifecycle()
    val totalFrames by (activeViewModel?.totalFrames ?: kotlinx.coroutines.flow.MutableStateFlow(0L)).collectAsStateWithLifecycle()

    androidx.activity.compose.BackHandler(enabled = isFrameCaptureMode) {
        activeViewModel?.exitFrameCaptureMode()
    }

    LaunchedEffect(activeViewModel) {
        activeViewModel?.playerEvents?.collect { event ->
            when (event) {
                is com.myplayer.viewmodel.PlayerEvent.ExitPlayer -> {
                    onBackClick()
                }
                else -> {}
            }
        }
    }

    var controlsVisible by remember { mutableStateOf(true) }
    var isLocked by remember { mutableStateOf(false) }
    var showUnlockButton by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var showSubtitleSettingsSideSheet by remember { mutableStateOf(false) }
    var showAudioSettingsSideSheet by remember { mutableStateOf(false) }
    var showPlayerSettingsSideSheet by remember { mutableStateOf(false) }
    var showChaptersSideSheet by remember { mutableStateOf(false) }
    var showDecoderSideSheet by remember { mutableStateOf(false) }

    var showQualitySideSheet by remember { mutableStateOf(false) }
    var showImportSubtitleDialog by remember { mutableStateOf(false) }

    val topLeftButtons = remember(playbackSettings.topLeftControls) {
        // Landscape TopLeft: always starts with BACK_ARROW + VIDEO_TITLE (non-editable anchor)
        val parsed = playbackSettings.topLeftControls.split(',').mapNotNull { runCatching { PlayerButton.valueOf(it) }.getOrNull() }
        listOf(PlayerButton.BACK_ARROW, PlayerButton.VIDEO_TITLE) +
                parsed.filter { it != PlayerButton.BACK_ARROW && it != PlayerButton.VIDEO_TITLE }
    }
    val topRightButtons = remember(playbackSettings.topRightControls) {
        playbackSettings.topRightControls.split(',').mapNotNull { runCatching { PlayerButton.valueOf(it) }.getOrNull() }
    }
    val bottomLeftButtons = remember(playbackSettings.bottomLeftControls) {
        playbackSettings.bottomLeftControls.split(',').mapNotNull { runCatching { PlayerButton.valueOf(it) }.getOrNull() }
    }
    val bottomRightButtons = remember(playbackSettings.bottomRightControls) {
        playbackSettings.bottomRightControls.split(',').mapNotNull { runCatching { PlayerButton.valueOf(it) }.getOrNull() }
    }
    // Portrait TopLeft: always starts with BACK_ARROW + VIDEO_TITLE (non-editable anchor)
    val portraitTopLeftButtons = remember(playbackSettings.portraitTopLeftControls) {
        val parsed = playbackSettings.portraitTopLeftControls.split(',').mapNotNull { runCatching { PlayerButton.valueOf(it) }.getOrNull() }
        listOf(PlayerButton.BACK_ARROW, PlayerButton.VIDEO_TITLE) +
                parsed.filter { it != PlayerButton.BACK_ARROW && it != PlayerButton.VIDEO_TITLE }
    }
    val portraitTopRightButtons = remember(playbackSettings.portraitTopRightControls) {
        playbackSettings.portraitTopRightControls.split(',').mapNotNull { runCatching { PlayerButton.valueOf(it) }.getOrNull() }
    }
    val portraitBottomLeftButtons = remember(playbackSettings.portraitBottomLeftControls) {
        playbackSettings.portraitBottomLeftControls.split(',').mapNotNull { runCatching { PlayerButton.valueOf(it) }.getOrNull() }
    }
    val portraitBottomRightButtons = remember(playbackSettings.portraitBottomRightControls) {
        playbackSettings.portraitBottomRightControls.split(',').mapNotNull { runCatching { PlayerButton.valueOf(it) }.getOrNull() }
    }
    val context = LocalContext.current
    val windowInfo = LocalWindowInfo.current

    // HUD overlay state for Aspect Ratio changes
    var aspectOverlayText by remember { mutableStateOf<String?>(null) }
    var isFirstAspectEmission by remember { mutableStateOf(true) }

    LaunchedEffect(playbackSettings.aspectMode) {
        if (isFirstAspectEmission) {
            isFirstAspectEmission = false
        } else {
            aspectOverlayText = when (playbackSettings.aspectMode) {
                AspectMode.FIT -> "Fit Screen"
                AspectMode.STRETCH -> "Stretch"
                AspectMode.CROP -> "Crop"
                AspectMode.ORIGINAL -> "100% Original"
            }
        }
    }

    LaunchedEffect(aspectOverlayText) {
        if (aspectOverlayText != null) {
            delay(500L)
            aspectOverlayText = null
        }
    }

    //  Pinch-to-Zoom state 
    // videoScale and videoOffset are owned here so they can be applied to the
    // AndroidView via graphicsLayer.  GestureOverlay reports incremental changes
    // (scaleMultiplier, panDelta) via onZoomChange; we accumulate them here.
    var videoScale by remember { mutableFloatStateOf(1f) }
    var customAspectScaleX by remember { mutableFloatStateOf(1f) }
    var customAspectScaleY by remember { mutableFloatStateOf(1f) }
    var showCustomAspectDialog by remember { mutableStateOf(false) }
    var videoOffset by remember { mutableStateOf(Offset.Zero) }

    val onZoomChange: (Float, Offset) -> Unit = { scaleMultiplier, pan ->
        val newScale = (videoScale * scaleMultiplier).coerceIn(1f, 6f)
        videoScale = newScale
        if (newScale <= 1.01f) {
            // Snap back to zero offset when fully zoomed out
            videoOffset = Offset.Zero
            videoScale = 1f
        } else {
            // Clamp pan so the video never wanders completely off-screen
            val maxX = (newScale - 1f) * 900f
            val maxY = (newScale - 1f) * 500f
            videoOffset = Offset(
                (videoOffset.x + pan.x).coerceIn(-maxX, maxX),
                (videoOffset.y + pan.y).coerceIn(-maxY, maxY)
            )
        }
    }

    //  Resolve the real video title 
    // For content:// (MediaStore) URIs, lastPathSegment is just the row ID (e.g.
    // "1000551661").  Query ContentResolver for the actual DISPLAY_NAME instead.
    val videoTitle: String = remember(currentUri, engineMediaTitle) {
        val activeUri = currentUri
        if (activeUri == null) {
            return@remember if (!engineMediaTitle.isNullOrBlank()) engineMediaTitle else "Local Video"
        }
        
        val scheme = activeUri.scheme
        val isLocal = scheme == null || scheme == "content" || scheme == "file"
        
        if (isLocal) {
            // For local videos, try ContentResolver first to get DISPLAY_NAME
            try {
                context.contentResolver.query(
                    activeUri,
                    arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
                    null, null, null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameCol = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                        if (nameCol >= 0) {
                            val fullName = cursor.getString(nameCol) ?: ""
                            // Strip extension
                            val dot = fullName.lastIndexOf('.')
                            val nameWithoutExt = if (dot > 0) fullName.substring(0, dot) else fullName
                            if (nameWithoutExt.isNotBlank()) {
                                return@remember nameWithoutExt
                            }
                        }
                    }
                }
            } catch (_: Exception) { }
            
            // If ContentResolver fails, try to extract from the path segment
            val seg = activeUri.lastPathSegment ?: activeUri.toString()
            val name = seg.substringAfterLast('/')
            // Only use the segment name if it is not just a numeric ID and we have a valid engineMediaTitle
            val isNumericId = name.all { it.isDigit() }
            if (isNumericId && !engineMediaTitle.isNullOrBlank() && !engineMediaTitle.all { it.isDigit() }) {
                val dot = engineMediaTitle.lastIndexOf('.')
                return@remember if (dot > 0) engineMediaTitle.substring(0, dot) else engineMediaTitle
            }
            
            val dot = name.lastIndexOf('.')
            val resolvedLocalName = if (dot > 0) name.substring(0, dot) else name
            if (resolvedLocalName.isNotBlank() && !resolvedLocalName.all { it.isDigit() }) {
                return@remember resolvedLocalName
            }
        }
        
        // For network streams or as a fallback, use engineMediaTitle if available
        if (!engineMediaTitle.isNullOrBlank()) {
            val cleanTitle = if (engineMediaTitle.startsWith("http://") || engineMediaTitle.startsWith("https://")) {
                val parsed = runCatching { Uri.parse(engineMediaTitle) }.getOrNull()
                parsed?.lastPathSegment ?: engineMediaTitle
            } else {
                engineMediaTitle
            }
            return@remember cleanTitle
        }
        
        // Final fallback: segment extraction
        val seg = activeUri.lastPathSegment ?: activeUri.toString()
        val name = seg.substringAfterLast('/')
        val dot = name.lastIndexOf('.')
        if (dot > 0) name.substring(0, dot) else name
    }

    val audioManager = remember(context) { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val activity = remember(context) { context.findActivity() }

    // Define back handler with portrait forcing first
    val handleBack = {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) {
                currentContext.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                break
            }
            currentContext = currentContext.baseContext
        }
        onBackClick()
    }

    BackHandler(enabled = true) {
        if (isLocked) {
            showUnlockButton = true
        } else {
            handleBack()
        }
    }

    // Restore saved brightness and volume on startup
    LaunchedEffect(Unit) {
        activity?.let { act ->
            val lp = act.window.attributes
            lp.screenBrightness = savedBrightness
            act.window.attributes = lp
        }
        if (savedVolume >= 0) {
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, savedVolume.coerceIn(0, maxVol), 0)
        }
        try {
            val serviceIntent = Intent(context, MediaPlaybackService::class.java)
            context.stopService(serviceIntent)
        } catch (_: Exception) {}
    }

    // Dynamically adjust screen orientation based on user setting or video dimensions
    LaunchedEffect(videoWidth, videoHeight, videoRotation, preFetchedMetadata, playbackSettings.orientationMode) {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) {
                val orientationMode = playbackSettings.orientationMode
                when (orientationMode) {
                    OrientationMode.LANDSCAPE,
                    OrientationMode.AUTO -> {
                        val target = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        if (currentContext.requestedOrientation != target) {
                            currentContext.requestedOrientation = target
                        }
                    }
                    OrientationMode.PORTRAIT -> {
                        val target = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        if (currentContext.requestedOrientation != target) {
                            currentContext.requestedOrientation = target
                        }
                    }
                    OrientationMode.SYSTEM_DEFAULT -> {
                        val meta = preFetchedMetadata
                        if (meta != null && meta.width > 0 && meta.height > 0) {
                            val isRotated = meta.rotation == 90 || meta.rotation == 270
                            val displayWidth = if (isRotated) meta.height else meta.width
                            val displayHeight = if (isRotated) meta.width else meta.height
                            val target = if (displayWidth > displayHeight) {
                                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            } else {
                                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            }
                            if (currentContext.requestedOrientation != target) {
                                Log.d("PlayerScreen", "Setting orientation from pre-fetched metadata: target=$target")
                                currentContext.requestedOrientation = target
                            }
                        } else if (videoWidth > 0 && videoHeight > 0) {
                            val isRotated = videoRotation == 90L || videoRotation == 270L
                            val displayWidth = if (isRotated) videoHeight else videoWidth
                            val displayHeight = if (isRotated) videoWidth else videoHeight
                            val target = if (displayWidth > displayHeight) {
                                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            } else {
                                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            }
                            if (currentContext.requestedOrientation != target) {
                                Log.d("PlayerScreen", "Setting orientation from video dimensions: target=$target")
                                currentContext.requestedOrientation = target
                            }
                        }
                    }
                }
                break
            }
            currentContext = currentContext.baseContext
        }
    }

    val currentOnPlayPauseToggle by rememberUpdatedState(onPlayPauseToggle)
    val currentIsPlaying by rememberUpdatedState(isPlaying)

    // Observe and apply system navigation (soft button mode) settings dynamically
    LaunchedEffect(playbackSettings.softButtonMode, controlsVisible, isLocked) {
        activity?.let { act ->
            val window = act.window
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            when (playbackSettings.softButtonMode) {
                SoftButtonMode.AUTO_HIDE -> {
                    insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    if (controlsVisible && !isLocked) {
                        insetsController.show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
                    } else {
                        insetsController.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
                    }
                }
                SoftButtonMode.SHOW -> {
                    insetsController.show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
                }
                SoftButtonMode.HIDE -> {
                    insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    if (controlsVisible && !isLocked) {
                        insetsController.show(WindowInsetsCompat.Type.statusBars())
                    } else {
                        insetsController.hide(WindowInsetsCompat.Type.statusBars())
                    }
                    insetsController.hide(WindowInsetsCompat.Type.navigationBars())
                }
            }
        }
    }

    // Pause playback when the window focus is lost and the pauseWhenObstructed setting is enabled
    LaunchedEffect(windowInfo.isWindowFocused, playbackSettings.pauseWhenObstructed) {
        if (!windowInfo.isWindowFocused && playbackSettings.pauseWhenObstructed && currentIsPlaying) {
            currentOnPlayPauseToggle()
        }
    }

    // Keep device screen awake during playing, and also when paused if keepAwakeAlways is enabled
    val keepScreenOn = isPlaying || playbackSettings.keepAwakeAlways
    LaunchedEffect(keepScreenOn) {
        activity?.let { act ->
            if (keepScreenOn) {
                act.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                act.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    // Enforce standard vertical layout when leaving PlayerScreen to return to lists and pause audio,
    // and restore default screen brightness. Restore system bars on exit.
    DisposableEffect(Unit) {
        onDispose {
            activity?.let { act ->
                val window = act.window
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
                // Explicitly clear keep screen awake flag when leaving the player
                window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            var currentContext = context
            while (currentContext is ContextWrapper) {
                if (currentContext is Activity) {
                    currentContext.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    val lp = currentContext.window.attributes
                    lp.screenBrightness = -1.0f // Restore default screen brightness
                    currentContext.window.attributes = lp
                    break
                }
                currentContext = currentContext.baseContext
            }
            if (currentIsPlaying) {
                val bgPlayEnabled = playbackSettings.backgroundPlayEnabled
                if (bgPlayEnabled) {
                    val intent = Intent(context, MediaPlaybackService::class.java).apply {
                        data = currentUri
                        putExtra(MediaPlaybackService.EXTRA_VIDEO_TITLE, videoTitle)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                } else {
                    currentOnPlayPauseToggle()
                }
            }
        }
    }

    // Auto-hide controls after 3 seconds of inactivity during active playback, unless actively seeking
    LaunchedEffect(controlsVisible, isPlaying, isDragging) {
        if (controlsVisible && isPlaying && !isDragging) {
            delay(3000L)
            controlsVisible = false
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        val currentOnSurfaceReady by rememberUpdatedState(onSurfaceReady)

        // ALWAYS keep the AndroidView in the hierarchy so that surface attaches immediately.
        // graphicsLayer applies the zoom/pan state driven by GestureOverlay's onZoomChange.
        // No `transformable` modifier here - it would never receive events because
        // GestureOverlay sits on top and captures all touches first.
        AndroidView(
            factory = { ctx ->
                MPVSurfaceView(ctx).apply {
                    onSurfaceCreatedListener = {
                        currentOnSurfaceReady()
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = videoScale * customAspectScaleX,
                    scaleY = videoScale * customAspectScaleY,
                    translationX = videoOffset.x,
                    translationY = videoOffset.y
                )
        )

        when (playbackState) {
            is PlayerState.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                                    MaterialTheme.colorScheme.background
                                ),
                                radius = 2200f
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is PlayerState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = "Error",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Playback Error",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = playbackState.message,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = handleBack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Go Back")
                        }
                    }
                }
            }

            else -> {
                if (!isInPipMode) {
                    if (!isLocked && !isFrameCaptureMode) {
                        GestureOverlay(
                            isPlaying = isPlaying,
                            currentPosition = currentPosition,
                            duration = duration,
                            playbackSpeed = playbackSpeed,
                            savedBrightness = savedBrightness,
                            savedVolume = savedVolume,
                            onPlayPauseToggle = onPlayPauseToggle,
                            onSeek = onSeek,
                            onSetPlaybackSpeed = onSetPlaybackSpeed,
                            onSaveBrightness = onSaveBrightness,
                            onSaveVolume = onSaveVolume,
                            controlsVisible = controlsVisible,
                            onControlsVisibleChanged = { controlsVisible = it },
                            customPlaybackSpeed = playbackSettings.customPlaybackSpeed,
                            tapAndHoldSpeed = playbackSettings.tapAndHoldSpeed,
                            doubleTapSeekDurationMs = playbackSettings.doubleTapSeekDuration,
                            playbackSettings = playbackSettings,
                            onShowMuteIcon = {},
                            onTakeVideoScreenshot = onTakeVideoScreenshot,
                            onZoomChange = onZoomChange,
                            audioBoosterEnabled = audioBoosterEnabled,
                            audioBoostVolume = audioBoostVolume,
                            onSetAudioBoostVolume = onSetAudioBoostVolume,
                            isDynamicSpeedActive = isDynamicSpeedActive,
                            onSetDynamicSpeedActive = onSetDynamicSpeedActive,
                            onSaveTapAndHoldSpeed = onUpdateTapAndHoldSpeed
                        )
                    } else if (isLocked) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    awaitEachGesture {
                                        awaitFirstDown(requireUnconsumed = false)
                                        controlsVisible = !controlsVisible
                                    }
                                }
                        )
                    }
                }

                if (!isInPipMode) {
                    ComposeSubtitleOverlay(
                        subtitleText = currentSubtitleText,
                        textSizeScale = playbackSettings.subtitleTextSizeScale,
                        bgStyle = playbackSettings.subtitleBgStyle,
                        subtitleFont = playbackSettings.subtitleFont,
                        isSubtitleBold = playbackSettings.isSubtitleBold,
                        isSubtitleGestureEnabled = playbackSettings.subtitleGesturesEnabled,
                        verticalOffsetFraction = playbackSettings.subtitleVerticalOffset,
                        onVerticalOffsetFractionChanged = { offset ->
                            onUpdateSubtitleVerticalOffset(offset)
                        },
                        onSeekNext = onSeekNextSubtitle,
                        onSeekPrev = onSeekPrevSubtitle
                    )
                }

                if (!isInPipMode) {
                    // Top overlays: PersistentTopBar and/or SpeedSliderHUD
                    
                    // Persistent top bar overlay when controls are hidden
                    AnimatedVisibility(
                        visible = (!controlsVisible || isLocked) && playbackSettings.showBatteryClockOverlay,
                        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                    ) {
                        PersistentTopBar(
                            duration = duration,
                            currentPosition = currentPosition,
                            showRemainingTime = playbackSettings.showRemainingTime,
                            showBatteryClock = playbackSettings.showBatteryClockOverlay
                        )
                    }

                    // Dynamic Speed Overlay
                    AnimatedVisibility(
                        visible = isDynamicSpeedActive,
                        enter = fadeIn(animationSpec = tween(150)) + scaleIn(initialScale = 0.8f, animationSpec = tween(150)),
                        exit = fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.8f, animationSpec = tween(150)),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(top = 6.dp)
                    ) {
                        SpeedSliderHUD(
                            playbackSpeed = playbackSpeed
                        )
                    }
                    
                    // Unified Premium Controls Layer
                    AnimatedVisibility(
                        visible = controlsVisible && !isFrameCaptureMode,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        PlayerControls(
                            title = videoTitle,
                            isPlaying = isPlaying,
                            showControlGradients = playbackSettings.showControlGradients,
                            currentPosition = currentPosition,
                            bufferedPosition = bufferedPosition,
                            isNetworkStream = isNetworkStream,
                            duration = duration,
                            isDragging = isDragging,
                            onDraggingChanged = { isDragging = it },
                            onPlayPauseToggle = onPlayPauseToggle,
                            onSeek = onSeek,
                            onSpeedClick = {
                                showPlayerSettingsSideSheet = true
                            },
                            onShowChapters = {
                                showChaptersSideSheet = true
                            },
                            hasChapters = chapters.isNotEmpty(),
                            currentDecoder = currentDecoder,
                            onShowDecoder = {
                                showDecoderSideSheet = true
                            },
                            onCycleSubtitle = {
                                showSubtitleSettingsSideSheet = true
                            },
                            onCycleAudio = {
                                showAudioSettingsSideSheet = true
                            },
                            onBackClick = handleBack,
                            playbackSpeed = playbackSpeed,
                            seekBarStyle = seekBarStyle,
                            hasNext = hasNext,
                            hasPrevious = hasPrevious,
                            onNextClick = onNextClick,
                            onPrevClick = onPrevClick,
                            showSeekButtons = playbackSettings.showSeekButtons,
                            showNextPrevButtons = playbackSettings.showNextPrevButtons,
                            showRemainingTime = playbackSettings.showRemainingTime,
                            showBatteryClockOverlay = playbackSettings.showBatteryClockOverlay,
                            onToggleShowRemainingTime = { onUpdateShowRemainingTime(!playbackSettings.showRemainingTime) },
                            seekDurationSeconds = playbackSettings.seekDurationSeconds,
                            controlIconSize = playbackSettings.controlIconSize,
                            topLeftButtons = topLeftButtons,
                            topRightButtons = topRightButtons,
                            bottomLeftButtons = bottomLeftButtons,
                            bottomRightButtons = bottomRightButtons,
                            portraitTopLeftButtons = portraitTopLeftButtons,
                            portraitTopRightButtons = portraitTopRightButtons,
                            portraitBottomLeftButtons = portraitBottomLeftButtons,
                            portraitBottomRightButtons = portraitBottomRightButtons,
                            onLockClick = { isLocked = true },
                            onAspectClick = onCycleAspectMode,
                            onAspectLongClick = { showCustomAspectDialog = true },
                            onPipClick = onEnterPip,
                            isLocked = isLocked,
                            onUnlockClick = {
                                isLocked = false
                                controlsVisible = true
                            },
                            currentAspectMode = playbackSettings.aspectMode,
                            isBackgroundPlayEnabled = playbackSettings.backgroundPlayEnabled,
                            onBackgroundPlayClick = {
                                val newVal = !playbackSettings.backgroundPlayEnabled
                                onUpdateBackgroundPlayEnabled(newVal)
                                Toast.makeText(
                                    context,
                                    if (newVal) "Background play enabled" else "Background play disabled",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                             onTitleClick = {
                                if (playbackSettings.showUpNextQueue) {
                                    onQueueVisibleChange(true)
                                }
                             },
                             onScreenshotClick = {
                                activeViewModel?.enterFrameCaptureMode()
                             },
                             ytdlQuality = playbackSettings.ytdlQuality,
                            onShowQuality = { showQualitySideSheet = true },
                            modifier = Modifier
                        )
                    }

                    if (isFrameCaptureMode) {
                        com.myplayer.ui.common.FrameCaptureOverlay(
                            currentFrame = currentFrame,
                            totalFrames = totalFrames,
                            onStepBackward = { activeViewModel?.stepFrameBackward() },
                            onStepForward = { activeViewModel?.stepFrameForward() },
                            onSliderScrubbing = { activeViewModel?.onFrameSliderScrubbing(it) },
                            onSliderReleased = { activeViewModel?.onFrameSliderReleased(it) },
                            onCapture = { activeViewModel?.takeVideoScreenshot() },
                            onExit = { activeViewModel?.exitFrameCaptureMode() }
                        )
                    }
                }

                // Separate Buffering overlay if video stalls during playback
                if (playbackState is PlayerState.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp,
                        modifier = Modifier
                            .size(56.dp)
                            .align(Alignment.Center)
                    )
                }

                // Aspect Ratio Overlay HUD
                AnimatedVisibility(
                    visible = aspectOverlayText != null,
                    enter = fadeIn(animationSpec = tween(150)) + scaleIn(initialScale = 0.8f, animationSpec = tween(150)),
                    exit = fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.8f, animationSpec = tween(150)),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            val overlayIcon = when (playbackSettings.aspectMode) {
                                AspectMode.FIT -> AspectIcons.Fit
                                AspectMode.STRETCH -> AspectIcons.Stretch
                                AspectMode.CROP -> AspectIcons.Crop
                                AspectMode.ORIGINAL -> AspectIcons.Original
                            }
                            Icon(
                                imageVector = overlayIcon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = aspectOverlayText ?: "",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        SubtitleSettingsSideSheet(
            visible = showSubtitleSettingsSideSheet,
            playbackSettings = playbackSettings,
            subtitleTracks = subtitleTracks,
            onSelectSubtitleTrack = onSelectSubtitleTrack,
            onSetSubtitleDelay = onSetSubtitleDelay,
            onUpdateSubtitleFont = onUpdateSubtitleFont,
            onUpdateIsSubtitleBold = onUpdateIsSubtitleBold,
            onUpdateForceAssSubtitleOverride = onUpdateForceAssSubtitleOverride,
            onUpdateSubtitleTextSizeScale = onUpdateSubtitleTextSizeScale,
            onUpdateSubtitleBgStyle = onUpdateSubtitleBgStyle,
            onUpdateSubtitleDelay = onUpdateSubtitleDelay,
            onUpdateSubtitleVerticalOffset = onUpdateSubtitleVerticalOffset,
            onUpdateSubtitleGesturesEnabled = onUpdateSubtitleGesturesEnabled,
            onDismiss = { showSubtitleSettingsSideSheet = false },
            onImportSubtitleClick = {
                showImportSubtitleDialog = true
                showSubtitleSettingsSideSheet = false
            }
        )

        if (showImportSubtitleDialog) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showImportSubtitleDialog = false },
                properties = androidx.compose.ui.window.DialogProperties(
                    usePlatformDefaultWidth = false
                )
            ) {
                Surface(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth(0.9f)
                        .heightIn(max = 500.dp)
                        .fillMaxHeight(0.85f)
                        .clip(RoundedCornerShape(16.dp)),
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 4.dp
                ) {
                    StorageExplorerScreen(
                        operationType = "SELECT_FILE",
                        allowedExtensions = listOf(".srt", ".vtt", ".ssa", ".ass", ".ttml", ".sub", ".pgs", ".sbv"),
                        onFileSelected = { file ->
                            showImportSubtitleDialog = false
                            activeViewModel?.importSubtitle(Uri.fromFile(file))
                        },
                        onCancel = { showImportSubtitleDialog = false }
                    )
                }
            }
        }

        QualitySettingsSideSheet(
            visible = showQualitySideSheet,
            playbackSettings = playbackSettings,
            onSelectQuality = { quality ->
                activeViewModel?.changeYtdlQuality(quality)
            },
            onDataSaverToggled = { enabled ->
                activeViewModel?.toggleDataSaver(enabled)
            },
            onDismiss = { showQualitySideSheet = false }
        )

        AudioSettingsSideSheet(
            visible = showAudioSettingsSideSheet,
            audioTracks = audioTracks,
            audioBoosterEnabled = audioBoosterEnabled,
            onToggleAudioBooster = onToggleAudioBooster,
            onSelectAudioTrack = onSelectAudioTrack,
            onDismiss = { showAudioSettingsSideSheet = false }
        )

        PlayerSettingsSideSheet(
            visible = showPlayerSettingsSideSheet,
            currentSpeed = playbackSpeed,
            playbackSettings = playbackSettings,
            currentVideo = currentVideo,
            onSpeedSelected = { speed ->
                onUpdateCustomPlaybackSpeed(speed)
            },
            onUpdateDoubleTapAction = onUpdateDoubleTapAction,
            onUpdateDoubleTapSeekDuration = onUpdateDoubleTapSeekDuration,
            onUpdateTwoFingerAction = onUpdateTwoFingerAction,
            onUpdateThreeFingerAction = onUpdateThreeFingerAction,
            onUpdateLongPressEnabled = onUpdateLongPressEnabled,
            onUpdateTapAndHoldSpeed = onUpdateTapAndHoldSpeed,
            onUpdateLongPressSpeed = onUpdateLongPressSpeed,
            onUpdateOrientationMode = onUpdateOrientationMode,
            onUpdateFullScreenMode = onUpdateFullScreenMode,
            onUpdateAspectMode = onUpdateAspectMode,
            onUpdateSoftButtonMode = onUpdateSoftButtonMode,
            onUpdateControlIconSize = onUpdateControlIconSize,
            onUpdateSeekBarStyle = onUpdateSeekBarStyle,
            onUpdateAutoPlayEnabled = onUpdateAutoPlayEnabled,
            onUpdateShowSeekButtons = onUpdateShowSeekButtons,
            onUpdateShowNextPrevButtons = onUpdateShowNextPrevButtons,
            onUpdateShowRemainingTime = onUpdateShowRemainingTime,
            onUpdateShowBatteryClockOverlay = onUpdateShowBatteryClockOverlay,
            onUpdatePauseWhenObstructed = onUpdatePauseWhenObstructed,
            onUpdateKeepAwakeAlways = onUpdateKeepAwakeAlways,
            onUpdateShowControlGradients = onUpdateShowControlGradients,
            onUpdateShowUpNextQueue = onUpdateShowUpNextQueue,
            onUpdateIsAmbientModeEnabled = onUpdateIsAmbientModeEnabled,
            onUpdateAmbientBlurStyle = onUpdateAmbientBlurStyle,
            onDismiss = { showPlayerSettingsSideSheet = false }
        )

        ChaptersSideSheet(
            visible = showChaptersSideSheet,
            chapters = chapters,
            currentPositionMs = currentPosition,
            onSelectChapter = onSelectChapter,
            onDismiss = { showChaptersSideSheet = false }
        )

        DecoderSideSheet(
            visible = showDecoderSideSheet,
            currentMode = if (!isHwSupported) DecoderMode.SW else playbackSettings.decoderMode,
            onSelectMode = { mode ->
                onUpdateDecoderMode(mode)
            },
            onDismiss = { showDecoderSideSheet = false }
        )

        if (!isInPipMode && isNetworkStream) {
            AnimatedVisibility(
                visible = controlsVisible && !isLocked,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 70.dp, end = 16.dp)
            ) {
                StreamingDataPanel(
                    speedBps = networkSpeedBytesPerSec,
                    bufferSec = bufferDurationSeconds
                )
            }
        }

        if (isLocked) {
            // Auto-hide the unlock button after 3 seconds
            LaunchedEffect(showUnlockButton) {
                if (showUnlockButton) {
                    delay(3000L)
                    showUnlockButton = false
                }
            }
        }

        if (!isInPipMode && !isLocked && queueList.isNotEmpty() && playbackSettings.showUpNextQueue) {
            val density = LocalDensity.current
            val configuration = LocalConfiguration.current
            val queueLayoutMode = playbackSettings.queueLayoutMode
            val panelHeight = if (queueLayoutMode == LayoutMode.LIST) {
                configuration.screenHeightDp.dp
            } else {
                240.dp
            }
            val panelHeightPx = with(density) { panelHeight.toPx() }
            var dragOffsetY by remember { mutableStateOf(0f) }
            val offsetY by animateFloatAsState(
                targetValue = if (isQueueVisible) 0f else panelHeightPx,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                label = "QueuePanelOffset"
            )
            val totalOffsetY = (offsetY + dragOffsetY).coerceAtLeast(0f)

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(panelHeight)
                    .graphicsLayer {
                        translationY = totalOffsetY
                    }
                    .background(
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.85f),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectVerticalDragGestures(
                                    onDragEnd = {
                                        if (dragOffsetY > 80f) {
                                            onQueueVisibleChange(false)
                                        }
                                        dragOffsetY = 0f
                                    },
                                    onDragCancel = { dragOffsetY = 0f },
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffsetY = (dragOffsetY + dragAmount).coerceAtLeast(0f)
                                    }
                                )
                            }
                            .padding(top = 10.dp, bottom = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 40.dp, height = 4.dp)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), CircleShape)
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.VideoLibrary,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Up Next Queue",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${queueList.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onUpdateQueueLayoutMode(LayoutMode.LIST) },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = if (queueLayoutMode == LayoutMode.LIST) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ViewList,
                                        contentDescription = "List View",
                                        tint = if (queueLayoutMode == LayoutMode.LIST) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { onUpdateQueueLayoutMode(LayoutMode.GRID) },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = if (queueLayoutMode == LayoutMode.GRID) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ViewCarousel,
                                        contentDescription = "Grid/Carousel View",
                                        tint = if (queueLayoutMode == LayoutMode.GRID) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { onQueueVisibleChange(false) },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = Color.Transparent
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = "Hide Queue",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        thickness = 1.dp
                    )
                    val listState = rememberLazyListState()
                    val activeIndex = remember(queueList, currentVideoId) {
                        queueList.indexOfFirst { it.uri == currentVideoId }
                    }
                    LaunchedEffect(isQueueVisible, queueLayoutMode) {
                        if (isQueueVisible && activeIndex >= 0 && queueLayoutMode == LayoutMode.LIST) {
                            listState.animateScrollToItem(activeIndex)
                        }
                    }
                    if (queueLayoutMode == LayoutMode.LIST) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            itemsIndexed(
                                items = queueList,
                                key = { _, video -> video.uri }
                            ) { index, video ->
                                val isPlaying = video.uri == currentVideoId
                                QueueVideoItem(
                                    video = video,
                                    isPlaying = isPlaying,
                                    onClick = { onQueueVideoClick(video) }
                                )
                            }
                        }
                    } else {
                        val rowState = rememberLazyListState()
                        LaunchedEffect(isQueueVisible, queueLayoutMode) {
                            if (isQueueVisible && queueLayoutMode == LayoutMode.GRID && activeIndex >= 0) {
                                rowState.animateScrollToItem(activeIndex)
                            }
                        }
                        LazyRow(
                            state = rowState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            itemsIndexed(
                                items = queueList,
                                key = { _, video -> video.uri }
                            ) { index, video ->
                                val isPlaying = video.uri == currentVideoId
                                QueueVideoGridItem(
                                    video = video,
                                    isPlaying = isPlaying,
                                    onClick = { onQueueVideoClick(video) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCustomAspectDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showCustomAspectDialog = false },
            title = { Text("Custom Aspect Ratio", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Width", fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { customAspectScaleX = (customAspectScaleX - 0.02f).coerceAtLeast(0.1f) },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape).size(36.dp)
                            ) {
                                Icon(Icons.Rounded.Remove, contentDescription = "-")
                            }
                            Text(
                                "${(customAspectScaleX * 100).roundToInt()}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.defaultMinSize(minWidth = 48.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            IconButton(
                                onClick = { customAspectScaleX = (customAspectScaleX + 0.02f).coerceAtMost(3f) },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape).size(36.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "+")
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Height", fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { customAspectScaleY = (customAspectScaleY - 0.02f).coerceAtLeast(0.1f) },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape).size(36.dp)
                            ) {
                                Icon(Icons.Rounded.Remove, contentDescription = "-")
                            }
                            Text(
                                "${(customAspectScaleY * 100).roundToInt()}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.defaultMinSize(minWidth = 48.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            IconButton(
                                onClick = { customAspectScaleY = (customAspectScaleY + 0.02f).coerceAtMost(3f) },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape).size(36.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "+")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showCustomAspectDialog = false }) {
                    Text("Done")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = {
                    customAspectScaleX = 1f
                    customAspectScaleY = 1f
                }) {
                    Text("Reset")
                }
            }
        )
    }
}

@Composable
private fun PersistentTopBar(
    duration: Long,
    currentPosition: Long,
    showRemainingTime: Boolean,
    showBatteryClock: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var batteryPercentage by remember { mutableIntStateOf(100) }
    var currentTime by remember { mutableStateOf("") }
    var isCharging by remember { mutableStateOf(false) }

    DisposableEffect(showBatteryClock) {
        if (!showBatteryClock) return@DisposableEffect onDispose {}
        
        val timeFormat = SimpleDateFormat("h:mm a", Locale.ENGLISH)
        currentTime = timeFormat.format(Date())

        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level != -1 && scale != -1) {
                        batteryPercentage = (level * 100 / scale.toFloat()).toInt()
                    }
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
                } else if (intent.action == Intent.ACTION_TIME_TICK) {
                    currentTime = timeFormat.format(Date())
                }
            }
        }
        val filter = android.content.IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_TIME_TICK)
        }
        context.registerReceiver(receiver, filter)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        // ALWAYS show the time overlay on the top left
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black.copy(alpha = 0.45f))
                .padding(horizontal = 3.dp, vertical = 1.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val timeStr = if (showRemainingTime) {
                    val remainingMs = (duration - currentPosition).coerceAtLeast(0L)
                    "-${formatTime(remainingMs)}"
                } else {
                    formatTime(duration)
                }
                Text(
                    text = "${formatTime(currentPosition)} / $timeStr",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (showBatteryClock) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 3.dp, vertical = 1.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isCharging) {
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = Color.Green,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "$batteryPercentage%",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = currentTime,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun StreamingDataPanel(
    speedBps: Long,
    bufferSec: Double,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.6f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Download,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Speed: ${formatSpeed(speedBps)}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.HourglassBottom,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Buffer: ${String.format(Locale.US, "%.1fs", bufferSec)}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun formatSpeed(bytesPerSec: Long): String {
    val kb = bytesPerSec / 1024.0
    val mb = kb / 1024.0
    return when {
        mb >= 1.0 -> String.format(Locale.US, "%.2f MB/s", mb)
        kb >= 1.0 -> String.format(Locale.US, "%.1f KB/s", kb)
        else -> "$bytesPerSec B/s"
    }
}

@Composable
private fun SpeedSliderHUD(
    playbackSpeed: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .border(0.5.dp, Color.White.copy(alpha = 0.30f), RoundedCornerShape(8.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = "2X Speed",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun QueueVideoItem(
    video: Video,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isPlaying) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    } else {
        Color.Transparent
    }
    
    val titleColor = if (isPlaying) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 100.dp, height = 56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            VideoThumbnail(
                uri = video.uri,
                showPlayIcon = false,
                modifier = Modifier.fillMaxSize()
            )
            
            DurationBadge(duration = video.duration)
            
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    PlayingIndicator(
                        isPlaying = true,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = video.title,
                color = titleColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = video.folderName,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueueVideoGridItem(
    video: Video,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderStroke = if (isPlaying) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .width(160.dp)
            .height(150.dp),
        shape = RoundedCornerShape(12.dp),
        border = borderStroke,
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(topStart = 11.dp, topEnd = 11.dp))
            ) {
                VideoThumbnail(
                    uri = video.uri,
                    showPlayIcon = false,
                    modifier = Modifier.fillMaxSize()
                )
                DurationBadge(duration = video.duration)
                
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        PlayingIndicator(
                            isPlaying = true,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = video.title,
                    color = if (isPlaying) MaterialTheme.colorScheme.primary else Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

