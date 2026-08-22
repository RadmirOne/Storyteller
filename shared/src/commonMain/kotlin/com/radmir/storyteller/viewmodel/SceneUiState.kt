package com.radmir.storyteller.viewmodel

import com.radmir.storyteller.models.CameraView
import com.radmir.storyteller.models.Scene
import com.radmir.storyteller.models.SceneStartEffect
import com.radmir.storyteller.models.StageCharacter

internal const val DEFAULT_CAMERA_DURATION_MS = 700L

data class SceneUiState(
    val scene: Scene,
    val nodeId: String,
    val cameraTarget: CameraView,
    val cameraStart: CameraView? = null,
    val cameraDurationMs: Long = DEFAULT_CAMERA_DURATION_MS,
    val stage: List<StageCharacter> = emptyList(),
    val enterEffect: SceneStartEffect = SceneStartEffect.NONE,
    val sceneChanged: Boolean = false,
    val cameraCueId: String = nodeId
)
