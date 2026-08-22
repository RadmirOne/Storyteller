package com.radmir.storyteller.repository

import androidx.compose.runtime.Composable

/** write must replace the complete record atomically, or throw without losing the previous record. */
interface ProgressStore {
    fun read(): String?
    fun write(value: String)
}

class MemoryProgressStore : ProgressStore {
    private var value: String? = null
    override fun read(): String? = value
    override fun write(value: String) { this.value = value }
}

@Composable
expect fun rememberProgressStore(): ProgressStore
