package com.radmir.storyteller.models

/** Reserved speaker id for the player-controlled protagonist. */
const val PLAYER_CHARACTER_ID = "protagonist"

fun StoryScript.playerName(player: PlayerCharacter?): String = player?.name
    ?: characters.find { it.id == PLAYER_CHARACTER_ID }?.name ?: "Вы"

fun StoryScript.speakerName(characterId: String, player: PlayerCharacter?): String =
    if (characterId == PLAYER_CHARACTER_ID) playerName(player)
    else characters.find { it.id == characterId }?.name ?: "Рассказчик"

/** Literal, single-pass replacement: names are never interpreted as templates. */
fun StoryScript.personalize(text: String, player: PlayerCharacter?): String =
    text.replace("{{playerName}}", playerName(player))

fun StoryScript.personalize(node: DialogueNode, player: PlayerCharacter?): DialogueNode = node.copy(
    text = personalize(node.text, player),
    choices = node.choices?.map { it.copy(text = personalize(it.text, player)) }
)
