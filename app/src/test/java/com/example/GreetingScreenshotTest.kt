package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.model.FolderItem
import com.example.model.VideoItem
import com.example.ui.screens.FolderExplorerScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleFolders = listOf(
        FolderItem("AyuGram", "/storage/emulated/0/AyuGram", 1),
        FolderItem("Online Samples", "/Online Samples", 3)
    )
    val sampleVideos = listOf(
        VideoItem(
            id = 1,
            path = "/storage/emulated/0/AyuGram/movie.mp4",
            title = "AyuGram Cinematic Trailer.mp4",
            durationMs = 35917000L, // 59:17
            sizeBytes = 1258291200L,
            resolution = "1080p",
            hasSubtitle = true,
            isOnline = false
        ),
        VideoItem(
            id = 2,
            path = "/storage/emulated/0/AyuGram/vlog.mp4",
            title = "Summer Vlog.mp4",
            durationMs = 450000L, // 07:30
            sizeBytes = 250000000L,
            resolution = "1080p",
            hasSubtitle = false,
            isOnline = false
        )
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        FolderExplorerScreen(
            folders = sampleFolders,
            videos = sampleVideos,
            selectedFolder = FolderItem("AyuGram", "/storage/emulated/0/AyuGram", 2),
            isGridLayout = false,
            searchQuery = "",
            onFolderSelect = {},
            onVideoSelect = {},
            onDeleteVideo = {},
            onToggleLayout = {},
            onSearchQueryChange = {},
            hasStoragePermission = true,
            onRequestPermission = {},
            isDarkTheme = true,
            onToggleTheme = {},
            onPlayLastPlayed = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
