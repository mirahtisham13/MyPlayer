package com.myplayer.util

import android.content.Context
import android.media.MediaExtractor
import android.provider.MediaStore
import java.io.File

object SubtitleHelper {
    private val SUBTITLE_EXTENSIONS = listOf("srt", "ass", "ssa", "vtt", "sub", "idx")

    fun getSubtitleExtension(context: Context, videoPath: String): String? {
        // 1. Check external subtitle files
        val externalExt = findExternalSubtitle(context, videoPath)
        if (externalExt != null) return externalExt

        // 2. Check embedded subtitle tracks in the container
        return findEmbeddedSubtitleFormat(videoPath)
    }

    private fun findExternalSubtitle(context: Context, videoPath: String): String? {
        try {
            val file = File(videoPath)
            val parent = file.parentFile ?: return null
            val baseName = file.nameWithoutExtension

            // Direct file existence check (faster, works for app-created or public dirs)
            for (ext in SUBTITLE_EXTENSIONS) {
                if (File(parent, "$baseName.$ext").exists()) {
                    return ext.uppercase()
                }
            }

            // Fallback to MediaStore query (Android 11+ Scoped Storage)
            val parentPath = parent.absolutePath
            val selection = "${MediaStore.Files.FileColumns.DATA} LIKE ?"
            val selectionArgs = arrayOf("$parentPath/$baseName.%")
            val projection = arrayOf(MediaStore.Files.FileColumns.DATA)

            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                while (cursor.moveToNext()) {
                    val path = cursor.getString(dataCol)
                    val ext = path.substringAfterLast('.', "").lowercase()
                    if (ext in SUBTITLE_EXTENSIONS) {
                        return ext.uppercase()
                    }
                }
            }
        } catch (_: Exception) {}
        return null
    }

    private fun findEmbeddedSubtitleFormat(videoPath: String): String? {
        try {
            val extractor = MediaExtractor()
            extractor.setDataSource(videoPath)
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString("mime") ?: continue
                if (mime.startsWith("text/") || mime.contains("subtitle") ||
                    mime == "application/x-subrip" ||
                    mime == "text/vtt" ||
                    mime == "text/x-ssa" ||
                    mime == "application/ttml+xml" ||
                    mime == "application/x-ass"
                ) {
                    extractor.release()
                    // Derive a short label from the mime type
                    return when {
                        mime.contains("subrip") || mime.contains("srt") -> "SRT"
                        mime.contains("vtt") -> "VTT"
                        mime.contains("ssa") || mime.contains("ass") -> "ASS"
                        mime.contains("ttml") -> "TTML"
                        else -> "SUB"
                    }
                }
            }
            extractor.release()
        } catch (_: Exception) {}
        return null
    }
}
