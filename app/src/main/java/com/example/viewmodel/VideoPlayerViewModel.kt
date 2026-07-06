package com.example.viewmodel

import android.content.Context
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.FolderItem
import com.example.model.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class Screen {
    Explorer, Player
}

class VideoPlayerViewModel : ViewModel() {

    private val _currentScreen = MutableStateFlow(Screen.Explorer)
    val currentScreen: StateFlow<Screen> = _currentScreen

    private val _selectedFolder = MutableStateFlow<FolderItem?>(null)
    val selectedFolder: StateFlow<FolderItem?> = _selectedFolder

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isGridLayout = MutableStateFlow(false)
    val isGridLayout: StateFlow<Boolean> = _isGridLayout

    private val _activeVideo = MutableStateFlow<VideoItem?>(null)
    val activeVideo: StateFlow<VideoItem?> = _activeVideo

    private val _allVideos = MutableStateFlow<List<VideoItem>>(emptyList())
    val allVideos: StateFlow<List<VideoItem>> = _allVideos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    fun initTheme(context: Context) {
        val prefs = context.getSharedPreferences("player_prefs", Context.MODE_PRIVATE)
        _isDarkTheme.value = prefs.getBoolean("is_dark_theme", true)
    }

    fun toggleTheme(context: Context) {
        val newVal = !_isDarkTheme.value
        _isDarkTheme.value = newVal
        val prefs = context.getSharedPreferences("player_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_dark_theme", newVal).apply()
    }

    fun playLastPlayedVideo(context: Context) {
        val prefs = context.getSharedPreferences("player_prefs", Context.MODE_PRIVATE)
        val lastPath = prefs.getString("last_played_path", null)
        if (lastPath != null) {
            val video = _allVideos.value.find { it.path == lastPath }
            if (video != null) {
                selectVideo(video)
                return
            }
        }
        // Fallback: play first video
        val firstVideo = _allVideos.value.firstOrNull()
        if (firstVideo != null) {
            selectVideo(firstVideo)
        }
    }

    init {
        // Load initial online samples and trigger local scans when permission is granted.
        loadOnlineSamples()
    }

    private fun loadOnlineSamples() {
        val onlineFolder = "/Online Samples"
                val samples = listOf(
            VideoItem(
                id = -1,
                path = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                title = "Demo Video.mp4",
                durationMs = 15000L, // 00:15
                sizeBytes = 15000000L, // 15 MB
                resolution = "1080p",
                hasSubtitle = false,
                subtitlePath = null,
                isOnline = true
            )
        )
        _allVideos.value = samples
    }

    fun scanLocalVideos(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            val localVideos = withContext(Dispatchers.IO) {
                val list = mutableListOf<VideoItem>()
                val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                val projection = arrayOf(
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.RESOLUTION,
                    MediaStore.Video.Media.WIDTH,
                    MediaStore.Video.Media.HEIGHT
                )

                try {
                    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                        val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                        val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                        val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                        val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                        val resCol = cursor.getColumnIndex(MediaStore.Video.Media.RESOLUTION)
                        val widthCol = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH)
                        val heightCol = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)

                        while (cursor.moveToNext()) {
                            val id = cursor.getLong(idCol)
                            val path = cursor.getString(dataCol) ?: continue
                            val name = cursor.getString(nameCol) ?: "Video $id"
                            val durationMs = cursor.getLong(durationCol)
                            val sizeBytes = cursor.getLong(sizeCol)

                            var resolution = ""
                            if (resCol != -1) {
                                resolution = cursor.getString(resCol) ?: ""
                            }
                            if (resolution.isEmpty() && widthCol != -1 && heightCol != -1) {
                                val w = cursor.getInt(widthCol)
                                val h = cursor.getInt(heightCol)
                                if (w > 0 && h > 0) {
                                    resolution = "${w}x${h}"
                                }
                            }
                            if (resolution.isEmpty()) {
                                resolution = "1080p"
                            }

                            // Subtitle detection next to local file
                            val srtFile = File(path.substringBeforeLast(".") + ".srt")
                            val vttFile = File(path.substringBeforeLast(".") + ".vtt")
                            val hasSubtitle = srtFile.exists() || vttFile.exists()

                            list.add(
                                VideoItem(
                                    id = id,
                                    path = path,
                                    title = name,
                                    durationMs = durationMs,
                                    sizeBytes = sizeBytes,
                                    resolution = resolution,
                                    hasSubtitle = hasSubtitle,
                                    subtitlePath = if (srtFile.exists()) srtFile.absolutePath else if (vttFile.exists()) vttFile.absolutePath else null,
                                    isOnline = false
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                list
            }

            // Combine online samples and local videos
            val currentOnline = _allVideos.value.filter { it.isOnline }
            _allVideos.value = currentOnline + localVideos
            _isLoading.value = false
        }
    }

    // List of folders containing videos, naturally sorted
    val folders: StateFlow<List<FolderItem>> = _allVideos
        .combine(_searchQuery) { videos, query ->
            val grouped = videos.groupBy { video ->
                if (video.isOnline) {
                    "Online Samples"
                } else {
                    val file = File(video.path)
                    file.parentFile?.name ?: "Internal Storage"
                }
            }

            val folderList = grouped.map { (name, items) ->
                val path = if (items.first().isOnline) "/Online Samples" else File(items.first().path).parent ?: ""
                FolderItem(name, path, items.size)
            }

            // Filter folders by query if necessary, then sort alphanumerically
            folderList
                .filter { it.name.contains(query, ignoreCase = true) }
                .sortedWith(compareBy { it.name })
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // List of videos in the active folder, filtered by search query
    val videosInSelectedFolder: StateFlow<List<VideoItem>> = combine(_allVideos, _selectedFolder, _searchQuery) { videos, folder, query ->
        if (folder == null) return@combine emptyList<VideoItem>()

        val folderVideos = videos.filter { video ->
            if (folder.path == "/Online Samples") {
                video.isOnline
            } else {
                !video.isOnline && File(video.path).parent == folder.path
            }
        }

        folderVideos
            .filter { it.displayTitle.contains(query, ignoreCase = true) }
            .sortedWith(compareBy { it.displayTitle })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setScreen(screen: Screen) {
        _currentScreen.value = screen
    }

    fun selectFolder(folder: FolderItem?) {
        _selectedFolder.value = folder
        _searchQuery.value = "" // Clear search on folder change
    }

    fun selectVideo(video: VideoItem?) {
        _activeVideo.value = video
        if (video != null) {
            _currentScreen.value = Screen.Player
        }
    }

    fun toggleLayout() {
        _isGridLayout.value = !_isGridLayout.value
    }

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun deleteVideo(video: VideoItem) {
        _allVideos.value = _allVideos.value.filter { it.id != video.id }
        if (_activeVideo.value?.id == video.id) {
            _activeVideo.value = null
            _currentScreen.value = Screen.Explorer
        }
    }

    fun playNextVideo() {
        val currentVideo = _activeVideo.value ?: return
        val currentList = videosInSelectedFolder.value
        if (currentList.isEmpty()) return
        val currentIndex = currentList.indexOfFirst { it.id == currentVideo.id }
        if (currentIndex != -1 && currentIndex < currentList.size - 1) {
            _activeVideo.value = currentList[currentIndex + 1]
        }
    }

    fun playPreviousVideo() {
        val currentVideo = _activeVideo.value ?: return
        val currentList = videosInSelectedFolder.value
        if (currentList.isEmpty()) return
        val currentIndex = currentList.indexOfFirst { it.id == currentVideo.id }
        if (currentIndex > 0) {
            _activeVideo.value = currentList[currentIndex - 1]
        }
    }
}