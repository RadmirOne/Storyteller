package com.radmir.storyteller.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ReadingSettings(
    val textScale: Float = 1f,
    val reduceMotion: Boolean = false,
    val soundEnabled: Boolean = true,
    val volume: Float = 0.7f,
) {
    fun normalized() = copy(
        textScale = if (textScale.isFinite()) textScale.coerceIn(0.85f, 1.5f) else 1f,
        volume = if (volume.isFinite()) volume.coerceIn(0f, 1f) else 0.7f,
    )
}

interface ReadingSettingsStore {
    fun read(): String?
    fun write(value: String)
}

class MemoryReadingSettingsStore : ReadingSettingsStore {
    private var value: String? = null
    override fun read() = value
    override fun write(value: String) { this.value = value }
}

private val settingsJson = Json { ignoreUnknownKeys = true; encodeDefaults = true }

class ReadingSettingsController(private val store: ReadingSettingsStore) {
    var settings by mutableStateOf(ReadingSettings())
        private set
    var error by mutableStateOf<String?>(null)
        private set

    init {
        try {
            store.read()?.let { settings = settingsJson.decodeFromString<ReadingSettings>(it).normalized() }
        } catch (_: Exception) {
            error = "Не удалось загрузить настройки. Используются стандартные значения."
        }
    }

    /** Publish only after a successful write, so displayed settings match the saved record. */
    fun update(value: ReadingSettings): Boolean {
        val normalized = value.normalized()
        return try {
            store.write(settingsJson.encodeToString(normalized))
            settings = normalized
            error = null
            true
        } catch (_: Exception) {
            error = "Не удалось сохранить настройки. Попробуйте ещё раз."
            false
        }
    }
}

@Composable
expect fun rememberReadingSettingsStore(): ReadingSettingsStore

@Composable
fun rememberReadingSettingsController(): ReadingSettingsController {
    val store = rememberReadingSettingsStore()
    return remember(store) { ReadingSettingsController(store) }
}
