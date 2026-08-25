package com.radmir.storyteller.models

import kotlinx.serialization.Serializable

@Serializable
data class StoryScript(
    val id: String,
    val title: String,
    val description: String? = null,
    val characters: List<Character> = emptyList(),
    val startNodeId: String,
    val nodes: Map<String, DialogueNode>
)