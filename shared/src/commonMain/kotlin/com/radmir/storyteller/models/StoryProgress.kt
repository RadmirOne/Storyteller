package com.radmir.storyteller.models

import kotlinx.serialization.Serializable

/** null choiceId is a linear 'Next' action. Only successful actions are recorded. */
@Serializable
data class ProgressStep(val choiceId: String? = null)

/**
 * Keep the script snapshot so compatibility can be checked before replaying a saved route.
 * playerCharacter belongs only to this story playthrough; it is not an app-wide profile.
 */
@Serializable
data class StoryProgress(
    val story: StoryScript,
    val steps: List<ProgressStep> = emptyList(),
    val version: Int = 1,
    val playerCharacter: PlayerCharacter? = null
)

/**
 * Only editorial changes are safe without a route migration. Keep every other field in the
 * comparison, including future model fields, so newly added behavior fails closed by default.
 */
fun StoryScript.isProgressCompatibleWith(current: StoryScript): Boolean =
    withoutEditorialContent() == current.withoutEditorialContent()

private fun StoryScript.withoutEditorialContent(): StoryScript = copy(
    title = "",
    description = null,
    characters = characters.map { it.copy(name = "") },
    scenes = scenes.mapValues { (_, scene) ->
        scene.copy(nodes = scene.nodes.mapValues { (_, node) ->
            node.copy(text = "", choices = node.choices?.map { choice ->
                choice.copy(text = "", unavailableReason = null)
            })
        })
    }
)

data class JournalEntry(
    val sceneId: String,
    val nodeId: String,
    val speaker: String,
    val text: String,
    val isChoice: Boolean = false
)

class ProgressException(message: String) : IllegalArgumentException(message)
