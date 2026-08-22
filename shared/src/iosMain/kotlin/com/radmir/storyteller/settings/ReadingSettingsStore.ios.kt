package com.radmir.storyteller.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*

@OptIn(ExperimentalForeignApi::class)
class AppleReadingSettingsStore : ReadingSettingsStore {
    private val directory = (NSSearchPathForDirectoriesInDomains(
        NSApplicationSupportDirectory, NSUserDomainMask, true).first() as String) + "/Storyteller"
    private val path = "$directory/reading-settings.json"

    override fun read(): String? {
        if (!NSFileManager.defaultManager.fileExistsAtPath(path)) return null
        return NSString.create(contentsOfFile = path, encoding = NSUTF8StringEncoding, error = null)?.toString()
            ?: error("Cannot read settings")
    }

    override fun write(value: String) {
        check(NSFileManager.defaultManager.createDirectoryAtPath(
            directory, withIntermediateDirectories = true, attributes = null, error = null))
        check(NSString.create(string = value).writeToFile(
            path, atomically = true, encoding = NSUTF8StringEncoding, error = null))
    }
}

@Composable
actual fun rememberReadingSettingsStore(): ReadingSettingsStore = remember { AppleReadingSettingsStore() }
