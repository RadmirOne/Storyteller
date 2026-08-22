package com.radmir.storyteller.models

import kotlinx.serialization.Serializable

/** null choiceId is a linear 'Next' action. Only successful actions are recorded. */
@Serializable
data class ProgressStep(val choiceId: String? = null)

/**
 * Keep the exact script so changed content cannot silently reinterpret a saved route.
 * playerCharacter belongs only to this story playthrough; it is not an app-wide profile.
 */
@Serializable
data class StoryProgress(
    val story: StoryScript,
    val steps: List<ProgressStep> = emptyList(),
    val version: Int = 1,
    val playerCharacter: PlayerCharacter? = null
)

data class JournalEntry(
    val sceneId: String,
    val nodeId: String,
    val speaker: String,
    val text: String,
    val isChoice: Boolean = false
)

class ProgressException(message: String) : IllegalArgumentException(message)
