package com.radmir.storyteller.models

import kotlinx.serialization.Serializable

@Serializable
data class CameraView(
    val focusX: Float = 0.5f,
    val focusY: Float = 0.5f,
    val zoom: Float = 1f
)
