package com.myplayer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myplayer.domain.model.Video
import com.myplayer.data.media.MediaStoreHelper
import com.myplayer.data.repository.VideoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecycleBinViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = VideoRepository(MediaStoreHelper(application), application)

    private val _trashedVideos = MutableStateFlow<List<Video>>(emptyList())
    val trashedVideos: StateFlow<List<Video>> = _trashedVideos.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadTrashedVideos()
    }

    fun loadTrashedVideos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val videos = withContext(Dispatchers.IO) {
                    repository.getTrashedVideos()
                }
                _trashedVideos.value = videos
            } finally {
                _isLoading.value = false
            }
        }
    }
}
