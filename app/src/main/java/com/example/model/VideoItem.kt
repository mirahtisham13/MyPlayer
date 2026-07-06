package com.example.model

import android.net.Uri

data class VideoItem(
    val id: Long,
    val path: String,
    val title: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val resolution: String,
    val hasSubtitle: Boolean,
    val subtitlePath: String? = null,
    val isOnline: Boolean = false
) {
    val durationFormatted: String
        get() {
            if (durationMs <= 0) return "00:00"
            val totalSeconds = durationMs / 1000
            val seconds = totalSeconds % 60
            val minutes = (totalSeconds / 60) % 60
            val hours = totalSeconds / 3600
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%d:%02d", minutes, seconds) // e.g. "59:17"
            }
        }
        
    val sizeFormatted: String
        get() {
            if (sizeBytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB")
            var size = sizeBytes.toDouble()
            var index = 0
            while (size >= 1024 && index < units.size - 1) {
                size /= 1024
                index++
            }
            return String.format("%.1f %s", size, units[index])
        }

    val displayTitle: String
        get() {
            return title.substringBeforeLast(".")
        }
}

data class FolderItem(
    val name: String,
    val path: String,
    val videoCount: Int
)
