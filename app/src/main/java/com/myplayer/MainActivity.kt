package com.myplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.myplayer.data.media.MediaStoreHelper
import com.myplayer.data.repository.VideoRepository
import com.myplayer.player.engine.MPVPlayerEngine
import com.myplayer.ui.navigation.AppNavigation
import com.myplayer.ui.theme.MyPlayerTheme
import com.myplayer.viewmodel.HomeViewModel
import com.myplayer.viewmodel.PlayerViewModel
import com.myplayer.viewmodel.SettingsViewModel
import com.myplayer.viewmodel.VideoListViewModel
import com.myplayer.viewmodel.FileOperationsViewModel
import android.content.res.Configuration
import android.util.Log
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.net.Uri
import com.myplayer.data.repository.PlaybackSettingsRepository
import com.myplayer.data.repository.ViewSettingsRepository
import com.myplayer.player.service.MediaPlaybackService

class MainActivity : ComponentActivity() {

    private val _isInPipMode = mutableStateOf(false)
    private val deepLinkUri = mutableStateOf<Uri?>(null)

    private fun handleIntent(intent: Intent?): Uri? {
        if (intent == null) return null
        if (intent.getBooleanExtra("DEEP_LINK_CONSUMED", false)) return null

        var uri: Uri? = null
        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_VIEW == action) {
            uri = intent.data
        } else if (Intent.ACTION_SEND == action && type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrBlank()) {
                uri = Uri.parse(sharedText.trim())
            }
        }

        if (uri != null) {
            intent.putExtra("DEEP_LINK_CONSUMED", true)
        }
        return uri
    }

    private val pipReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null || !playerViewModelLazy.isInitialized()) return
            when (intent.action) {
                "com.myplayer.PIP_PREV" -> playerViewModel.playPrevious()
                "com.myplayer.PIP_PLAY_PAUSE" -> {
                    playerViewModel.togglePlayback()
                    updatePipActions()
                }
                "com.myplayer.PIP_NEXT" -> playerViewModel.playNext()
            }
        }
    }

    private fun buildPipActionsList(): List<android.app.RemoteAction> {
        val actions = mutableListOf<android.app.RemoteAction>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 1. Prev Action
            val prevIntent = Intent("com.myplayer.PIP_PREV")
            val prevPendingIntent = PendingIntent.getBroadcast(
                this,
                1,
                prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val prevIcon = android.graphics.drawable.Icon.createWithResource(this, android.R.drawable.ic_media_previous)
            val prevAction = android.app.RemoteAction(prevIcon, "Previous", "Previous", prevPendingIntent)
            prevAction.setEnabled(playerViewModel.hasPrevious.value)
            actions.add(prevAction)

            // 2. Play/Pause Action
            val isPlaying = playerViewModel.isPlaying.value
            val playPauseIconRes = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            val playPauseTitle = if (isPlaying) "Pause" else "Play"
            val playPauseIntent = Intent("com.myplayer.PIP_PLAY_PAUSE")
            val playPausePendingIntent = PendingIntent.getBroadcast(
                this,
                2,
                playPauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val playPauseIcon = android.graphics.drawable.Icon.createWithResource(this, playPauseIconRes)
            val playPauseAction = android.app.RemoteAction(playPauseIcon, playPauseTitle, playPauseTitle, playPausePendingIntent)
            actions.add(playPauseAction)

            // 3. Next Action
            val nextIntent = Intent("com.myplayer.PIP_NEXT")
            val nextPendingIntent = PendingIntent.getBroadcast(
                this,
                3,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val nextIcon = android.graphics.drawable.Icon.createWithResource(this, android.R.drawable.ic_media_next)
            val nextAction = android.app.RemoteAction(nextIcon, "Next", "Next", nextPendingIntent)
            nextAction.setEnabled(playerViewModel.hasNext.value)
            actions.add(nextAction)
        }
        return actions
    }

    private fun updatePipActions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = android.app.PictureInPictureParams.Builder()
                .setActions(buildPipActionsList())
                .build()
            setPictureInPictureParams(params)
        }
    }

    private val mediaStoreHelper by lazy { MediaStoreHelper(this) }
    private val repository by lazy { VideoRepository(mediaStoreHelper, this) }
    private val viewSettingsRepo by lazy { ViewSettingsRepository.getInstance(applicationContext) }
    private val playbackSettingsRepo by lazy { PlaybackSettingsRepository(applicationContext) }

    private val homeViewModel by lazy { HomeViewModel(applicationContext, repository) }
    private val settingsViewModel by lazy { ViewModelProvider(this)[SettingsViewModel::class.java] }
    private val videoListViewModel by lazy { VideoListViewModel(repository, viewSettingsRepo, playbackSettingsRepo) }
    private val fileOpsViewModel by lazy { ViewModelProvider(this)[FileOperationsViewModel::class.java] }

    private val playerEngine by lazy { MPVPlayerEngine(applicationContext) }
    private val playerViewModelLazy = lazy {
        val factory = PlayerViewModel.Factory(application, playerEngine)
        ViewModelProvider(this, factory)[PlayerViewModel::class.java]
    }
    private val playerViewModel by playerViewModelLazy

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Notification permission is required for Background Play controls", Toast.LENGTH_LONG).show()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            homeViewModel.loadFolders()
            videoListViewModel.loadVideos()
            checkNotificationPermission()
        } else {
            Toast.makeText(this, "Permission denied to read videos", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val uri = handleIntent(intent)
        deepLinkUri.value = uri
        if (uri != null) {
            logExternalVideoHistory(uri)
        }

        // Asynchronously copy yt-dlp assets
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                com.myplayer.player.ytdlp.YtdlpManager.copyAssets(applicationContext)
                Log.d("MainActivity", "yt-dlp assets copy completed")
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to copy yt-dlp assets at startup", e)
            }
        }

        // Register PiP action receiver
        val filter = IntentFilter().apply {
            addAction("com.myplayer.PIP_PREV")
            addAction("com.myplayer.PIP_PLAY_PAUSE")
            addAction("com.myplayer.PIP_NEXT")
        }
        ContextCompat.registerReceiver(
            this,
            pipReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        // Listen for playback and navigation state changes to update PiP actions dynamically
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    playerViewModel.isPlaying,
                    playerViewModel.hasNext,
                    playerViewModel.hasPrevious
                ) { _, _, _ -> }.collect {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isInPictureInPictureMode) {
                        updatePipActions()
                    }
                }
            }
        }



        checkAndRequestPermissions()

        setContent {
            val isDark by settingsViewModel.isDarkTheme.collectAsState()
            val dynamicColor by settingsViewModel.dynamicColor.collectAsState()
            val selectedPalette by settingsViewModel.selectedPalette.collectAsState()
            val isNavBarTransparent by settingsViewModel.isNavBarTransparent.collectAsState()
            val isAmoledTheme by settingsViewModel.isAmoledTheme.collectAsState()

            MyPlayerTheme(
                forceDark = isDark,
                dynamicColor = dynamicColor,
                palette = selectedPalette,
                isNavBarTransparent = isNavBarTransparent,
                isAmoledTheme = isAmoledTheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        homeViewModel = homeViewModel,
                        playerViewModel = { playerViewModel },
                        playerEngine = { playerEngine },
                        settingsViewModel = settingsViewModel,
                        videoListViewModel = videoListViewModel,
                        fileOpsViewModel = fileOpsViewModel,
                        isInPipMode = _isInPipMode.value,
                        onEnterPip = { enterPipMode() },
                        initialUri = deepLinkUri.value,
                        onDeepLinkHandled = { deepLinkUri.value = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val uri = handleIntent(intent)
        if (uri != null) {
            deepLinkUri.value = uri
            logExternalVideoHistory(uri)
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun checkMediaManagementPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!android.provider.MediaStore.canManageMedia(this)) {
                try {
                    val intent = Intent(android.provider.Settings.ACTION_REQUEST_MANAGE_MEDIA).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            homeViewModel.loadFolders()
            videoListViewModel.loadVideos()
            checkNotificationPermission()
            checkMediaManagementPermission()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInPictureInPictureMode) {
            return
        }
        val bgPlayEnabled = if (playerViewModelLazy.isInitialized()) playerViewModel.playbackSettings.value.backgroundPlayEnabled else false
        if (bgPlayEnabled && playerViewModelLazy.isInitialized() && playerViewModel.isPlaying.value) {
            return
        }
        if (playerViewModelLazy.isInitialized()) {
            playerViewModel.pause()
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        _isInPipMode.value = isInPictureInPictureMode
    }

    @Suppress("DEPRECATION")
    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val width = playerViewModel.videoWidth.value.coerceAtLeast(1)
                    val height = playerViewModel.videoHeight.value.coerceAtLeast(1)
                    val ratio = width.toFloat() / height.toFloat()
                    val builder = android.app.PictureInPictureParams.Builder()
                    if (ratio in 0.4184f..2.39f) {
                        builder.setAspectRatio(android.util.Rational(width.toInt(), height.toInt()))
                    }
                    builder.setActions(buildPipActionsList())
                    enterPictureInPictureMode(builder.build())
                } else {
                    enterPictureInPictureMode()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to enter PiP mode", e)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(this, MediaPlaybackService::class.java)
        stopService(serviceIntent)
    }

    override fun onStop() {
        super.onStop()
        val bgPlayEnabled = if (playerViewModelLazy.isInitialized()) playerViewModel.playbackSettings.value.backgroundPlayEnabled else false
        if (bgPlayEnabled && playerViewModelLazy.isInitialized() && playerViewModel.isPlaying.value) {
            val title = playerViewModel.currentUri.value?.lastPathSegment ?: "Video Playback"
            val serviceIntent = Intent(this, MediaPlaybackService::class.java).apply {
                data = playerViewModel.currentUri.value
                putExtra(MediaPlaybackService.EXTRA_VIDEO_TITLE, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(pipReceiver)
        } catch (_: Exception) {}
        val serviceIntent = Intent(this, MediaPlaybackService::class.java)
        stopService(serviceIntent)
    }

    private fun logExternalVideoHistory(uri: Uri) {
        val vm = playerViewModel
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val fileName = getFileNameFromUri(this@MainActivity, uri)
                val path = getPathFromUri(this@MainActivity, uri)
                val metadataResult = com.myplayer.data.mediainfo.MediaInfoOps.extractBasicMetadata(
                    this@MainActivity,
                    uri,
                    fileName
                )
                val metadata = metadataResult.getOrNull()
                val duration = metadata?.durationMs ?: 0L
                val size = metadata?.sizeBytes ?: 0L
                val width = metadata?.width ?: 0
                val height = metadata?.height ?: 0

                val video = com.myplayer.domain.model.Video(
                    uri = uri.toString(),
                    title = fileName,
                    duration = duration,
                    folderName = "",
                    path = path,
                    size = size,
                    width = width,
                    height = height,
                    dateAdded = System.currentTimeMillis(),
                    dateModified = System.currentTimeMillis()
                )

                vm.insertOrUpdateHistory(video)
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to log external video history", e)
            }
        }
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        if (uri.scheme == "file") {
            return java.io.File(uri.path ?: "").name
        } else if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        return cursor.getString(nameIndex)
                    }
                }
            } catch (e: Exception) {
                // fallback
            }
        }
        return uri.lastPathSegment ?: "Unknown Video"
    }

    private fun getPathFromUri(context: Context, uri: Uri): String {
        if (uri.scheme == "file") {
            return uri.path ?: uri.toString()
        } else if (uri.scheme == "content") {
            try {
                val projection = arrayOf(android.provider.MediaStore.Video.Media.DATA)
                context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    val columnIndex = cursor.getColumnIndex(android.provider.MediaStore.Video.Media.DATA)
                    if (columnIndex != -1 && cursor.moveToFirst()) {
                        return cursor.getString(columnIndex)
                    }
                }
            } catch (e: Exception) {
                // fallback
            }
        }
        return uri.toString()
    }
}