package com.radmir.storyteller

import androidx.compose.ui.geometry.Size
import com.radmir.storyteller.models.CameraView
import com.radmir.storyteller.screens.scene.cameraTransform
import kotlin.test.Test
import kotlin.test.assertEquals

class CameraTransformTest {

    // container 1000x500, image 1600x900: scale = 500 / 900 = 0.5556
    private val container = Size(1000f, 500f)
    private val image = Size(1600f, 900f)

    @Test
    fun zoomOneCoversContainerWithClampedOffset() {
        val t = cameraTransform(CameraView(0.5f), container, image)

        assertEquals(0.5556f, t.scale)
        // imageWidth = 1600 * 0.5556 = 888.88... < 1000 -> centered: (1000 - 888.88) / 2 = 55.56
        assertEquals(55.56f, t.translationX)
        assertEquals(0f, t.translationY)
    }

    @Test
    fun focusPointIsCentered() {
        val t = cameraTransform(CameraView(0.75f), container, image)

        // imageWidth = 888.88... < 1000 -> centered
        assertEquals(55.56f, t.translationX)
        assertEquals(0f, t.translationY)
    }

    @Test
    fun degenerateSizesReturnIdentity() {
        val t = cameraTransform(CameraView(1f), Size.Zero, image)

        assertEquals(1f, t.scale)
        assertEquals(0f, t.translationX)
        assertEquals(0f, t.translationY)
    }
}
