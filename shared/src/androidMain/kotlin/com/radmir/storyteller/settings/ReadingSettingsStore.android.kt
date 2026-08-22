package com.radmir.storyteller.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import com.radmir.storyteller.repository.AndroidProgressStore
import java.io.File

class AndroidReadingSettingsStore(file: File) : ReadingSettingsStore {
    private val delegate = AndroidProgressStore(file)
    override fun read() = delegate.read()
    override fun write(value: String) = delegate.write(value)
}

@Composable
actual fun rememberReadingSettingsStore(): ReadingSettingsStore {
    if (LocalInspectionMode.current) return remember { MemoryReadingSettingsStore() }
    val context = LocalContext.current.applicationContext
    return remember(context) { AndroidReadingSettingsStore(File(context.filesDir, "reading-settings.json")) }
}
