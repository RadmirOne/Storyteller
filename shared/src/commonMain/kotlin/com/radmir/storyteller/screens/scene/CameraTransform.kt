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
    val coverScale = maxOf(
        container.width / image.width,
        container.height / image.height
    )
    val scale = coverScale * camera.zoom
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
    if (imageHeight >= container.height) {
        translationY = (container.height / 2f - camera.focusY * imageHeight)
            .coerceIn(container.height - imageHeight, 0f)
    } else {
        translationY = (container.height - imageHeight) / 2f
    }
    return CameraTransform(scale, translationX, translationY)
}
