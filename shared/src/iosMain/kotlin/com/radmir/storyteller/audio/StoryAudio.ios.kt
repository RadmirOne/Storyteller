package com.radmir.storyteller.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSData
import platform.Foundation.create

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun rememberAudioPlayer(): AudioPlayer = remember {
    object : AudioPlayer {
        private var player: AVAudioPlayer? = null
        override fun load(bytes: ByteArray) {
            release()
            require(bytes.isNotEmpty())
            val data = bytes.usePinned { NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong()) }
            val next = AVAudioPlayer(data = data, error = null)
            next.numberOfLoops = -1
            check(next.prepareToPlay()) { "Unsupported audio data" }
            player = next
        }
        override fun setPlayback(playing: Boolean, volume: Float) {
            val current = player ?: return
            current.volume = volume
            if (playing && !current.playing) current.play()
            else if (!playing && current.playing) current.pause()
        }
        override fun release() {
            player?.stop()
            player = null
        }
    }
}
