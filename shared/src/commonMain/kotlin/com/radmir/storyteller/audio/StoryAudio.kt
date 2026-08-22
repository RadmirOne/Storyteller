package com.radmir.storyteller.audio

import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.radmir.storyteller.viewmodel.SceneUiState
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import storyteller.shared.generated.resources.Res

@Serializable
data class SceneAudio(val music: String? = null, val ambience: String? = null)

data class AudioSettings(
    val enabled: Boolean = true,
    val musicVolume: Float = 0.7f,
    val ambienceVolume: Float = 0.5f
)

interface AudioPlayer {
    fun load(bytes: ByteArray)
    fun setPlayback(playing: Boolean, volume: Float)
    fun release()
}

/** Keeps playback intent when bytes arrive after a lifecycle or volume change. */
internal class AudioChannelController(
    private val player: AudioPlayer,
    private val onPlaybackError: (Exception) -> Unit = {}
) {
    private var loaded = false
    private var released = false
    private var active = false
    private var volume = 0f

    fun load(bytes: ByteArray) {
        if (released) return
        player.load(bytes)
        loaded = true
        applyPlayback()
    }

    fun update(active: Boolean, volume: Float) {
        this.active = active
        this.volume = effectiveAudioVolume(true, volume)
        applyPlayback()
    }

    private fun applyPlayback() {
        if (loaded && !released) {
            try {
                player.setPlayback(active && volume > 0f, volume)
            } catch (error: Exception) {
                release()
                onPlaybackError(error)
            }
        }
    }

    fun release() {
        if (released) return
        released = true
        // A failed platform player must not crash disposal or retry on every recomposition.
        try { player.release() } catch (_: Exception) { }
    }
}

@Composable
internal expect fun rememberAudioPlayer(): AudioPlayer

internal fun effectiveAudioVolume(enabled: Boolean, volume: Float): Float =
    if (!enabled || !volume.isFinite()) 0f else volume.coerceIn(0f, 1f)

/** Returns errors for unsafe or unsupported packaged audio paths. */
fun SceneAudio.validationErrors(): List<String> = listOfNotNull(music, ambience).mapNotNull { path ->
    val segments = path.split('/')
    if (path.startsWith("files/audio/") && !path.contains("..") &&
        segments.none { it.isBlank() || it == "." } &&
        segments.last().substringBeforeLast('.').isNotBlank() &&
        !path.contains('\\') && path.substringAfterLast('.').lowercase() in setOf("wav", "mp3", "m4a")) null
    else "Invalid audio resource: $path (expected files/audio/*.wav, *.mp3 or *.m4a)"
}

/** Owns playback while mounted. Identical paths continue across dialogue and scene changes. */
@Composable
fun StoryAudio(
    state: SceneUiState?,
    settings: AudioSettings = AudioSettings(),
    onError: (String) -> Unit = {}
) {
    val owner = LocalLifecycleOwner.current
    var active by remember(owner) { mutableStateOf(owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, _ ->
            active = owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    AudioChannel(state?.scene?.audio?.music, active, effectiveAudioVolume(settings.enabled, settings.musicVolume), onError)
    AudioChannel(state?.scene?.audio?.ambience, active, effectiveAudioVolume(settings.enabled, settings.ambienceVolume), onError)
}

@Composable
private fun AudioChannel(path: String?, active: Boolean, volume: Float, onError: (String) -> Unit) {
    // A new path gets a new player; disposing a channel stops the old track immediately.
    key(path) {
        val player = rememberAudioPlayer()
        val reportError by rememberUpdatedState(onError)
        val controller = remember(player) {
            AudioChannelController(player) { reportError("Не удалось воспроизвести аудио: $path") }
        }
        DisposableEffect(controller) { onDispose { controller.release() } }
        LaunchedEffect(path, player) {
            if (path != null) {
                try {
                    require(SceneAudio(music = path).validationErrors().isEmpty()) { "Invalid audio path" }
                    controller.load(Res.readBytes(path))
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    reportError("Не удалось воспроизвести аудио: $path")
                }
            }
        }
        SideEffect {
            controller.update(active, volume)
        }
    }
}
