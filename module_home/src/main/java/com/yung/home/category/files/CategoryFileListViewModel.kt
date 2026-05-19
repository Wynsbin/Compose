package com.yung.home.category.files

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.yung.home.category.data.model.CategoryFileItem
import com.yung.home.category.data.repository.FileRepository
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container as orbitContainer

data class CategoryFileListState(
    val categoryName: String = "",
    val isLoading: Boolean = true,
    val items: List<CategoryFileItem> = emptyList(),
    val error: String? = null,
    val playingItemId: String? = null,
    val playbackMessage: String? = null,
)

class CategoryFileListViewModel(
    application: Application,
    private val categoryId: String,
    categoryName: String,
    private val repository: FileRepository = FileRepository(),
) : AndroidViewModel(application), ContainerHost<CategoryFileListState, Nothing> {

    private val audioPlayer = CategoryAudioPlayer(application)

    override val container: Container<CategoryFileListState, Nothing> =
        orbitContainer(CategoryFileListState(categoryName = categoryName)) {
            loadFiles()
        }

    fun loadFiles() = intent {
        reduce { state.copy(isLoading = true, error = null) }
        repository.loadFiles(categoryId)
            .onSuccess { items ->
                reduce {
                    state.copy(
                        isLoading = false,
                        items = items,
                        error = null,
                    )
                }
            }
            .onFailure { throwable ->
                reduce {
                    state.copy(
                        isLoading = false,
                        error = throwable.message ?: "加载失败",
                    )
                }
            }
    }

    fun onItemClick(item: CategoryFileItem) = intent {
        val url = item.audioUrl?.trim().orEmpty()
        if (url.isEmpty()) {
            reduce { state.copy(playbackMessage = "无音频链接") }
            return@intent
        }

        if (state.playingItemId == item.id) {
            if (audioPlayer.isPlaying) {
                audioPlayer.pause()
                reduce { state.copy(playbackMessage = "已暂停") }
            } else {
                audioPlayer.resume()
                reduce { state.copy(playbackMessage = "正在播放") }
            }
            return@intent
        }

        reduce { state.copy(playingItemId = item.id, playbackMessage = "加载中…") }
        audioPlayer.play(
            url = url,
            onPrepared = { onPlaybackPrepared(item.id) },
            onCompleted = { onPlaybackCompleted() },
            onError = { onPlaybackError(it) },
        )
    }

    fun onPlaybackPrepared(itemId: String) = intent {
        reduce { state.copy(playingItemId = itemId, playbackMessage = "正在播放") }
    }

    fun onPlaybackCompleted() = intent {
        reduce { state.copy(playingItemId = null, playbackMessage = "播放完成") }
    }

    fun onPlaybackError(message: String) = intent {
        reduce { state.copy(playingItemId = null, playbackMessage = message) }
    }

    fun clearPlaybackMessage() = intent {
        reduce { state.copy(playbackMessage = null) }
    }

    override fun onCleared() {
        audioPlayer.release()
        super.onCleared()
    }
}
