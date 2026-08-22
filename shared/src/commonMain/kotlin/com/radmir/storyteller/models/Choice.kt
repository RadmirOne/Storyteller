package com.radmir.storyteller.models

import kotlinx.serialization.Serializable

@Serializable
data class Choice(
    val id: String,
    val text: String,
    val targetNodeId: String,
    val conditions: Map<String, Int>? = null
)