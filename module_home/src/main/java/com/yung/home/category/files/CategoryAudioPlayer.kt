package com.yung.home.category.files

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer

internal class CategoryAudioPlayer(context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying == true

    fun play(
        url: String,
        onPrepared: () -> Unit,
        onCompleted: () -> Unit,
        onError: (String) -> Unit,
    ) {
        release()
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build(),
                )
                setDataSource(url)
                setOnPreparedListener {
                    it.start()
                    onPrepared()
                }
                setOnCompletionListener {
                    release()
                    onCompleted()
                }
                setOnErrorListener { _, what, extra ->
                    release()
                    onError("播放失败 ($what, $extra)")
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            release()
            onError(e.message ?: "播放失败")
        }
    }

    fun pause() {
        mediaPlayer?.takeIf { it.isPlaying }?.pause()
    }

    fun resume() {
        mediaPlayer?.takeIf { !it.isPlaying }?.start()
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
