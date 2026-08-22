package com.radmir.storyteller.models

import kotlinx.serialization.Serializable

@Serializable
data class StoryScript(
    val title: String,
    val startNodeId: String,
    val nodes: Map<String, DialogueNode>
)