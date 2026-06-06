package me.konyaco.collinsdictionary.android.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper

class PronunciationPlayer {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var mediaPlayer: MediaPlayer? = null

    fun play(
        url: String,
        onStarted: () -> Unit = {},
        onFinished: () -> Unit = {},
        onError: (Throwable) -> Unit = {},
    ) {
        release()

        val player = MediaPlayer()
        mediaPlayer = player

        try {
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            player.setDataSource(url)
            player.setOnPreparedListener {
                postToMain(onStarted)
                it.start()
            }
            player.setOnCompletionListener {
                releasePlayer(it)
                postToMain(onFinished)
            }
            player.setOnErrorListener { failedPlayer, what, extra ->
                releasePlayer(failedPlayer)
                postToMain {
                    onError(IllegalStateException("Audio playback failed: what=$what extra=$extra"))
                }
                true
            }
            player.prepareAsync()
        } catch (throwable: Throwable) {
            releasePlayer(player)
            postToMain { onError(throwable) }
        }
    }

    fun release() {
        mediaPlayer?.let(::releasePlayer)
    }

    private fun releasePlayer(player: MediaPlayer) {
        if (mediaPlayer === player) {
            mediaPlayer = null
        }

        runCatching {
            if (player.isPlaying) {
                player.stop()
            }
        }
        runCatching { player.reset() }
        runCatching { player.release() }
    }

    private fun postToMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }
}
