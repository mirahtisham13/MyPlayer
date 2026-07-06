package com.example

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.ui.screens.FolderExplorerScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.Screen
import com.example.viewmodel.VideoPlayerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: VideoPlayerViewModel by viewModels()

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.initTheme(this)

        setContent {
            val context = this
            
            // Resolve correct permission depending on system SDK level
            val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                android.Manifest.permission.READ_MEDIA_VIDEO
            } else {
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            }

            var hasStoragePermission by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(context, permissionToRequest) == PackageManager.PERMISSION_GRANTED
                )
            }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { isGranted ->
                    hasStoragePermission = isGranted
                    if (isGranted) {
                        viewModel.scanLocalVideos(context)
                    }
                }
            )

            // Trigger file scan immediately if permission is already granted
            LaunchedEffect(hasStoragePermission) {
                if (hasStoragePermission) {
                    viewModel.scanLocalVideos(context)
                }
            }

            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    val currentScreen by viewModel.currentScreen.collectAsState()
                    val selectedFolder by viewModel.selectedFolder.collectAsState()
                    val folders by viewModel.folders.collectAsState()
                    val videosInFolder by viewModel.videosInSelectedFolder.collectAsState()
                    val activeVideo by viewModel.activeVideo.collectAsState()

                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "ScreenTransition"
                    ) { targetScreen ->
                        when (targetScreen) {
                            Screen.Explorer -> {
                                FolderExplorerScreen(
                                    folders = folders,
                                    videos = videosInFolder,
                                    selectedFolder = selectedFolder,
                                    onFolderSelect = { viewModel.selectFolder(it) },
                                    onVideoSelect = { viewModel.selectVideo(it) },
                                    onDeleteVideo = { viewModel.deleteVideo(it) },
                                    hasStoragePermission = hasStoragePermission,
                                    onRequestPermission = { permissionLauncher.launch(permissionToRequest) },
                                    isDarkTheme = isDarkTheme,
                                    onToggleTheme = { viewModel.toggleTheme(context) },
                                    onPlayLastPlayed = { viewModel.playLastPlayedVideo(context) },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                            Screen.Player -> {
                                activeVideo?.let { video ->
                                    VideoPlayerScreen(
                                        video = video,
                                        onBack = { viewModel.setScreen(Screen.Explorer) },
                                        onNextVideo = { viewModel.playNextVideo() },
                                        onPreviousVideo = { viewModel.playPreviousVideo() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
