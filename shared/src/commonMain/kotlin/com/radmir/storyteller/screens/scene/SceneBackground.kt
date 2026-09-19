package com.radmir.storyteller.screens.scene

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.radmir.storyteller.models.CameraView
import com.radmir.storyteller.models.Character
import com.radmir.storyteller.models.StageCharacter
import com.radmir.storyteller.models.SceneStartEffect
import com.radmir.storyteller.viewmodel.SceneUiState

private val CameraViewConverter = TwoWayConverter<CameraView, AnimationVector4D>(
    convertToVector = { AnimationVector4D(it.focusX, 0f, 0f, 0f) },
    convertFromVector = { CameraView(it.v1) }
)


@Composable
fun SceneBackground(state: SceneUiState, characters: List<Character> = emptyList()) {
    val bitmap = rememberResourceImageBitmap(state.scene.backgroundResource)
    val sprites = state.stage.filter { it.worldX != null }.mapNotNull { actor ->
        key(state.scene.id, actor.characterId) {
            val resource = characters.find { it.id == actor.characterId }?.spriteResource
            if (resource == null) null else rememberSpriteLayer(actor, resource)
        }
    }.sortedBy { it.stage.groundY }

    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val camera = remember(state.scene.id) {
        Animatable(state.cameraStart ?: state.cameraTarget, CameraViewConverter)
    }

    LaunchedEffect(state.scene.id, state.cameraCueId) {
        if (state.sceneChanged) {
            camera.snapTo(state.cameraStart ?: state.cameraTarget)
        } else if (state.cameraStart != null) {
            camera.snapTo(state.cameraStart)
        }
        camera.animateTo(state.cameraTarget, tween(state.cameraDurationMs.toInt()))
    }

    val fade = remember(state.scene.id) {
        Animatable(if (state.enterEffect == SceneStartEffect.FADE) 0f else 1f)
    }

    LaunchedEffect(state.scene.id) {
        if (state.enterEffect == SceneStartEffect.FADE) {
            fade.animateTo(1f, tween(500))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .alpha(fade.value)
            .onSizeChanged { containerSize = it }
    ) {
        val bmp = bitmap

        if (bmp != null && containerSize != IntSize.Zero) {
            val transform = cameraTransform(
                camera = camera.value,
                container = Size(containerSize.width.toFloat(), containerSize.height.toFloat()),
                image = Size(bmp.width.toFloat(), bmp.height.toFloat())
            )

            Canvas(
                modifier = Modifier.fillMaxSize(),
                onDraw = {
                    val scaledWidth = (bmp.width * transform.scale).toInt()
                    val scaledHeight = (bmp.height * transform.scale).toInt()

                    val drawOffsetX = transform.translationX.toInt()
                    val drawOffsetY = transform.translationY.toInt()

                    drawImage(
                        image = bmp,
                        dstOffset = IntOffset(drawOffsetX, drawOffsetY),
                        dstSize = IntSize(scaledWidth, scaledHeight),
                    )
                    sprites.forEach { sprite ->
                        val bounds = worldSpriteBounds(
                            sprite.stage,
                            Size(sprite.bitmap.width.toFloat(), sprite.bitmap.height.toFloat()),
                            Size(bmp.width.toFloat(), bmp.height.toFloat()),
                            transform
                        )
                        drawImage(
                            image = sprite.bitmap,
                            dstOffset = IntOffset(bounds.left.toInt(), bounds.top.toInt()),
                            dstSize = IntSize(bounds.width.toInt().coerceAtLeast(1), bounds.height.toInt().coerceAtLeast(1)),
                            alpha = sprite.alpha
                        )
                    }
                }
            )
        }
    }
}

private data class SpriteLayer(val stage: StageCharacter, val bitmap: ImageBitmap, val alpha: Float)

@Composable
private fun rememberSpriteLayer(stage: StageCharacter, resource: String): SpriteLayer? {
    val bitmap = rememberResourceImageBitmap(resource)
    val opacity = remember { Animatable(0f) }
    LaunchedEffect(stage.visible, bitmap) {
        if (bitmap != null) opacity.animateTo(if (stage.visible) 1f else 0f, tween(400))
    }
    return bitmap?.let { SpriteLayer(stage, it, opacity.value) }
}

