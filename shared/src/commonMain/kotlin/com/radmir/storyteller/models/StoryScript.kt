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
    val scenes: Map<String, Scene> = emptyMap(),
    val schemaVersion: Int = 1,
    val characterAppearance: CharacterAppearance = CharacterAppearance()
)

@Serializable
data class CharacterAppearance(
    val enabled: Boolean = true,
    val durationMs: Int = 400,
    val slideDistance: Float = 1f,
    val fade: Boolean = true
)
