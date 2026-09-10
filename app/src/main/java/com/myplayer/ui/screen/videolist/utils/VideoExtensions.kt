package com.myplayer.ui.screens.videolist.utils

import android.net.Uri
import com.myplayer.domain.model.Video
import com.myplayer.ui.screen.videolist.components.common.VideoWatchState
import com.myplayer.ui.screen.videolist.components.common.getWatchState

/**
 * Common extension functions for Video objects and lists inside the VideoList screen.
 */

fun Video.getWatchStatus(lastPlayedAt: Long, lastPositionMs: Long, isLastPlayed: Boolean = false): VideoWatchState {
    return getWatchState(lastPlayedAt, lastPositionMs, this.duration, isLastPlayed)
}

fun List<Video>.getUris(): List<Uri> {
    return this.mapNotNull {
        runCatching { Uri.parse(it.uri) }.getOrNull()
    }
}

fun Long.formatAsDuration(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
