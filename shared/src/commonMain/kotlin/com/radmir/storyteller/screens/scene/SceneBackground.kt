package com.radmir.storyteller.screens.scene

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.radmir.storyteller.models.CameraView
import com.radmir.storyteller.models.SceneStartEffect
import com.radmir.storyteller.viewmodel.SceneUiState
import kotlin.math.roundToInt

private val CameraViewConverter = TwoWayConverter<CameraView, AnimationVector4D>(
    convertToVector = { AnimationVector4D(it.focusX, it.focusY, it.zoom, 0f) },
    convertFromVector = { CameraView(it.v1, it.v2, it.v3) }
)

@Composable
fun SceneBackground(state: SceneUiState) {
    val bitmap = rememberResourceImageBitmap(state.scene.backgroundResource)
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val camera = remember(state.nodeId) {
        Animatable(state.cameraTarget, CameraViewConverter)
    }
    LaunchedEffect(state.nodeId) {
        if (state.sceneChanged) {
            camera.snapTo(state.cameraStart ?: state.cameraTarget)
        } else {
            if (state.cameraStart != null) {
                camera.snapTo(state.cameraStart)
            }
            camera.animateTo(state.cameraTarget, tween(state.cameraDurationMs.toInt()))
        }
    }

    val fade = remember(state.nodeId) {
        Animatable(if (state.enterEffect == SceneStartEffect.FADE) 0f else 1f)
    }
    LaunchedEffect(state.nodeId) {
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
            val density = LocalDensity.current
            with(density) {
                Image(
                    bitmap = bmp,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .size(
                            (bmp.width * transform.scale).toDp(),
                            (bmp.height * transform.scale).toDp()
                        )
                        .absoluteOffset {
                            IntOffset(
                                transform.translationX.roundToInt(),
                                transform.translationY.roundToInt()
                            )
                        }
                )
            }
        }
    }
}
