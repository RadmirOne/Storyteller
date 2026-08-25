package com.radmir.storyteller.screens.scene

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import org.jetbrains.compose.resources.decodeToImageBitmap
import storyteller.shared.generated.resources.Res

private val imageCache = HashMap<String, ImageBitmap>()

@Composable
fun rememberResourceImageBitmap(path: String): ImageBitmap? {
    var bitmap by remember(path) { mutableStateOf<ImageBitmap?>(imageCache[path]) }
    LaunchedEffect(path) {
        if (bitmap == null) {
            runCatching { Res.readBytes(path) }
                .onSuccess { bytes ->
                    runCatching { bytes.decodeToImageBitmap() }
                        .onSuccess { decoded ->
                            imageCache[path] = decoded
                            bitmap = decoded
                        }
                }
        }
    }
    return bitmap
}
