package com.radmir.storyteller.models

import kotlinx.serialization.Serializable

@Serializable
data class Character(
    val id: String,
    val name: String,
    val spriteResource: String? = null
)