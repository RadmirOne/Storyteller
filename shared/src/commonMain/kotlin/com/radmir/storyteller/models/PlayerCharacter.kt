package com.radmir.storyteller.models

import kotlinx.serialization.Serializable

@Serializable
enum class PlayerAppearance {
    LIGHT_HAIR,
    DARK_HAIR,
    RED_HAIR
}

@Serializable
enum class PlayerOutfit {
    JACKET,
    SWEATER,
    COAT
}

/** Player-defined protagonist data, stored independently from story-script characters. */
@Serializable
data class PlayerCharacter(
    val name: String,
    val appearance: PlayerAppearance,
    val outfit: PlayerOutfit
) {
    init {
        require(name.isNotBlank()) { "Имя персонажа не может быть пустым." }
    }
}
