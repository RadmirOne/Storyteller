package com.radmir.storyteller.models

import kotlinx.serialization.Serializable

@Serializable
data class Scene(
    val id: String,
    val backgroundResource: String,
    val nodes: Map<String, DialogueNode> = emptyMap(),
    val audio: com.radmir.storyteller.audio.SceneAudio = com.radmir.storyteller.audio.SceneAudio()
)
