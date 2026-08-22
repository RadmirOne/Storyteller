package com.radmir.storyteller.audio

import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.File

@Composable
internal actual fun rememberAudioPlayer(): AudioPlayer {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        object : AudioPlayer {
            private var player: MediaPlayer? = null
            override fun load(bytes: ByteArray) {
                release()
                val file = File.createTempFile("story-audio-", ".audio", context.cacheDir)
                try {
                    file.writeBytes(bytes)
                    val next = MediaPlayer()
                    try {
                        next.setDataSource(file.absolutePath)
                        next.isLooping = true
                        next.prepare()
                        player = next
                    } catch (error: Exception) {
                        next.release()
                        throw error
                    }
                } finally {
                    file.delete()
                }
            }
            override fun setPlayback(playing: Boolean, volume: Float) {
                val current = player ?: return
                current.setVolume(volume, volume)
                if (playing && !current.isPlaying) current.start()
                else if (!playing && current.isPlaying) current.pause()
            }
            override fun release() {
                player?.release()
                player = null
            }
        }
    }
}
