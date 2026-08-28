package com.radmir.storyteller.models

import kotlinx.serialization.Serializable

@Serializable
data class CameraView(
    val focusX: Float = 0.5f
)
