package com.myplayer.domain.model

data class ViewSettings(
    val showQuickFab: Boolean = true,
    val selectByThumbnail: Boolean = true,
    val enableFabPreview: Boolean = true,
    val scanFoldersList: Set<String> = emptySet(),
    val showHistoryCard: Boolean = true,
    val isShortcutsVisible: Boolean = true,
    val isDetailsVisible: Boolean = true,

    val showStorageTracker: Boolean = true,
    val showLatestVideos: Boolean = true,

    // New fields for VideoList UI
    val layoutMode: LayoutMode = LayoutMode.LIST,
    val gridColumns: Int = 2,
    val showThumbnail: Boolean = true,
    val showLength: Boolean = true,
    val displayLengthOverThumbnail: Boolean = true,
    val showFileExtension: Boolean = false,
    val showSize: Boolean = true,
    val showDate: Boolean = true,
    val showPath: Boolean = false,
    val showPlayedTime: Boolean = true,
    val showResolution: Boolean = false,
    val showFrameRate: Boolean = false,
    val sortField: SortField = SortField.TITLE,
    val sortDirection: SortDirection = SortDirection.ASCENDING,
    val viewMode: ViewMode = ViewMode.ALL_FOLDERS,
    val thumbnailMode: ThumbnailMode = ThumbnailMode.SMART,
    val thumbnailFramePosition: Float = 33f,
    val showWatchBadge: Boolean = true,
    val showSubtitle: Boolean = true,

    // Folder specific settings
    val folderShowCount: Boolean = true,
    val folderShowSize: Boolean = true,
    val folderShowDate: Boolean = true,
    val folderShowWatchBadge: Boolean = true
)
