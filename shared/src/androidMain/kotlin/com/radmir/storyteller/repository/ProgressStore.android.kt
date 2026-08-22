package com.radmir.storyteller.repository

import android.util.AtomicFile
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import java.io.File

class AndroidProgressStore(file: File) : ProgressStore {
    private val atomic = AtomicFile(file)
    override fun read(): String? = try {
        atomic.openRead().bufferedReader(Charsets.UTF_8).use { it.readText() }
    } catch (e: java.io.FileNotFoundException) {
        if (atomic.baseFile.exists()) throw e else null
    }

    override fun write(value: String) {
        val output = atomic.startWrite()
        try {
            output.write(value.toByteArray(Charsets.UTF_8))
            atomic.finishWrite(output)
        } catch (e: Exception) {
            atomic.failWrite(output)
            throw e
        }
    }
}

@Composable
actual fun rememberProgressStore(storyId: String?): ProgressStore {
    if (LocalInspectionMode.current) return remember(storyId) { MemoryProgressStore() }
    val context = LocalContext.current.applicationContext
    return remember(context, storyId) {
        val legacy = AndroidProgressStore(File(context.filesDir, "story-progress.json"))
        if (storyId == null) legacy else StoryProgressStore(storyId,
            AndroidProgressStore(File(context.filesDir, progressFileName(storyId))), legacy)
    }
}
