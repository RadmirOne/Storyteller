package com.radmir.storyteller.screens.scene

import androidx.compose.ui.geometry.Size
import com.radmir.storyteller.models.CameraView

data class CameraTransform(
    val scale: Float,
    val translationX: Float,
    val translationY: Float
)

fun cameraTransform(camera: CameraView, container: Size, image: Size): CameraTransform {
    if (container.width <= 0f || container.height <= 0f || image.width <= 0f || image.height <= 0f) {
        return CameraTransform(1f, 0f, 0f)
    }
    val scale = container.height / image.height
    val imageWidth = image.width * scale
    val imageHeight = image.height * scale
    val translationX: Float
    val translationY: Float
    if (imageWidth >= container.width) {
        translationX = (container.width / 2f - camera.focusX * imageWidth)
            .coerceIn(container.width - imageWidth, 0f)
    } else {
        translationX = (container.width - imageWidth) / 2f
    }
    translationY = 0f
    return CameraTransform(scale, translationX, translationY)
}
