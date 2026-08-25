package com.radmir.storyteller

import androidx.compose.ui.geometry.Size
import com.radmir.storyteller.models.CameraView
import com.radmir.storyteller.screens.scene.cameraTransform
import kotlin.test.Test
import kotlin.test.assertEquals

class CameraTransformTest {

    // container 1000x500, image 1600x900: coverScale = max(0.625, 0.5556) = 0.625
    private val container = Size(1000f, 500f)
    private val image = Size(1600f, 900f)

    @Test
    fun zoomOneCoversContainerWithClampedOffset() {
        val t = cameraTransform(CameraView(0.5f, 0.5f, 1f), container, image)

        assertEquals(0.625f, t.scale)
        // imageWidth = 1000 = container width -> centered, offset 0
        assertEquals(0f, t.translationX)
        // imageHeight = 562.5 > 500 -> centered vertically: 250 - 0.5 * 562.5 = -31.25
        assertEquals(-31.25f, t.translationY)
    }

    @Test
    fun focusPointIsCentered() {
        val t = cameraTransform(CameraView(0.75f, 0.5f, 1f), container, image)

        // focusX 0.75: 500 - 0.75 * 1000 = -250, clamp to [0, 0] -> 0
        assertEquals(0f, t.translationX)
        assertEquals(-31.25f, t.translationY)
    }

    @Test
    fun zoomGreaterThanOneScalesAndClampsOffsets() {
        val zoom = 2f
        val left = cameraTransform(CameraView(0f, 0.5f, zoom), container, image)
        val right = cameraTransform(CameraView(1f, 0.5f, zoom), container, image)

        assertEquals(1.25f, left.scale)
        assertEquals(1.25f, right.scale)
        // focusX 0 -> 500 clamped to 0; focusX 1 -> -1500 clamped to container - image = -1000
        assertEquals(0f, left.translationX)
        assertEquals(-1000f, right.translationX)
    }

    @Test
    fun degenerateSizesReturnIdentity() {
        val t = cameraTransform(CameraView(1f, 1f, 3f), Size.Zero, image)

        assertEquals(1f, t.scale)
        assertEquals(0f, t.translationX)
        assertEquals(0f, t.translationY)
    }
}
