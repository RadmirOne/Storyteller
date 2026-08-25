package com.radmir.storyteller.models

import kotlinx.serialization.Serializable

@Serializable
data class Choice(
    val id: String,
    val text: String,
    val targetNodeId: String,
    val targetSceneId: String? = null,
    val targetSceneStartEffect: SceneStartEffect? = null,
    val conditions: Map<String, Int>? = null
)