package com.radmir.storyteller.screens.scene

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.radmir.storyteller.models.StageCharacter

/** World sprites share the background's scale and translation, including during a camera tween. */
fun worldSpriteBounds(stage: StageCharacter, sprite: Size, background: Size, camera: CameraTransform): Rect {
    val height = background.height * 0.72f * stage.scale * camera.scale
    val width = height * sprite.width / sprite.height
    val centerX = (stage.worldX ?: 0.5f) * background.width * camera.scale + camera.translationX
    val bottom = stage.groundY * background.height * camera.scale + camera.translationY
    return Rect(centerX - width / 2f, bottom - height, centerX + width / 2f, bottom)
}
