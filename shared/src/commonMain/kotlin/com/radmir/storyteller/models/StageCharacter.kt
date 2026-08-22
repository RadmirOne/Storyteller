package com.radmir.storyteller.models

import kotlinx.serialization.Serializable

@Serializable
enum class StagePosition { LEFT, CENTER, RIGHT }

@Serializable
data class StageCharacter(
    val characterId: String,
    val position: StagePosition = StagePosition.CENTER,
    val scale: Float = 1f,
    val visible: Boolean = true,
    val worldX: Float? = null,
    val groundY: Float = 0.92f
)
