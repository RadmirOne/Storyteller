package com.radmir.storyteller.models

import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val currentSceneId: String,
    val currentNodeId: String,
    val variables: Map<String, Int> = emptyMap(),
    val visitedNodes: Set<String> = emptySet()
)