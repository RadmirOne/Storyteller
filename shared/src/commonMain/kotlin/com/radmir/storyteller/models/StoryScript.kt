package com.radmir.storyteller.models

import kotlinx.serialization.Serializable

@Serializable
data class StoryScript(
    val id: String,
    val title: String,
    val description: String? = null,
    val characters: List<Character> = emptyList(),
    val startSceneId: String,
    val startNodeId: String,
    val scenes: Map<String, Scene> = emptyMap()
)