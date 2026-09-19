package com.radmir.storyteller.screens.scene

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import org.jetbrains.compose.resources.decodeToImageBitmap
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import storyteller.shared.generated.resources.Res

private val imageCache = HashMap<String, ImageBitmap>()
private val imageCacheMutex = Mutex()

suspend fun loadResourceImage(path: String): ImageBitmap = imageCacheMutex.withLock {
    imageCache[path] ?: Res.readBytes(path).decodeToImageBitmap().also { imageCache[path] = it }
}

@Composable
fun rememberResourceImageBitmap(path: String): ImageBitmap? {
    var bitmap by remember(path) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(path) {
        if (bitmap == null) {
            bitmap = loadResourceImage(path)
        }
    }
    return bitmap
}
