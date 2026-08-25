package com.radmir.storyteller.models

import kotlinx.serialization.Serializable

@Serializable
data class DialogueNode(
    val id: String,
    val characterId: String,
    val text: String,
    val choices: List<Choice>? = null,
    val nextNodeId: String? = null,
    val imageResource: String? = null
)