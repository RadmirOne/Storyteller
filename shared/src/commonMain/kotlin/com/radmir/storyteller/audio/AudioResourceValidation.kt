package com.radmir.storyteller.audio

import com.radmir.storyteller.models.StoryScript
import kotlinx.coroutines.CancellationException

/** Preflight unique audio resources before opening a story. Does not fully decode codecs. */
suspend fun validateStoryAudioResources(
    script: StoryScript,
    loadBytes: suspend (String) -> ByteArray
): List<String> {
    val errors = mutableListOf<String>()
    val paths = script.scenes.values.flatMap { listOfNotNull(it.audio.music, it.audio.ambience) }.distinct()
    for (path in paths) {
        val pathErrors = SceneAudio(music = path).validationErrors()
        if (pathErrors.isNotEmpty()) {
            errors.addAll(pathErrors)
            continue
        }
        try {
            val bytes = loadBytes(path)
            when {
                bytes.isEmpty() -> errors += "Audio resource is empty: $path"
                path.substringAfterLast('.').equals("wav", ignoreCase = true) ->
                    wavHeaderError(bytes)?.let { errors += "Invalid WAV resource $path: $it" }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            errors += "Cannot read audio resource: $path"
        }
    }
    return errors
}

internal fun wavHeaderError(bytes: ByteArray): String? {
    fun tag(offset: Int, text: String) = offset + text.length <= bytes.size &&
        text.indices.all { bytes[offset + it].toInt() == text[it].code }
    fun u32(offset: Int): Long = (0..3).fold(0L) { value, index ->
        value or ((bytes[offset + index].toLong() and 255L) shl (index * 8))
    }
    if (bytes.size < 12 || !tag(0, "RIFF") || !tag(8, "WAVE")) return "expected RIFF/WAVE header"
    val end = u32(4) + 8L
    if (end > bytes.size || end < 12) return "truncated RIFF container"
    var offset = 12L
    var formatFound = false
    var dataFound = false
    while (offset + 8 <= end) {
        val start = offset.toInt()
        val length = u32(start + 4)
        val next = offset + 8 + length
        if (next > end) return "truncated chunk"
        if (tag(start, "fmt ")) {
            if (length < 16) return "short format chunk"
            val channels = (bytes[start + 10].toInt() and 255) or ((bytes[start + 11].toInt() and 255) shl 8)
            if (channels == 0 || u32(start + 12) == 0L) return "invalid channels or sample rate"
            formatFound = true
        }
        if (tag(start, "data") && length > 0) dataFound = true
        offset = next + (length and 1L)
    }
    if (offset != end) return "incomplete chunk header or padding"
    if (!formatFound || !dataFound) return "missing format or nonempty data chunk"
    return null
}
