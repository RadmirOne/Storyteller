package com.radmir.storyteller.models

import kotlinx.serialization.Serializable

@Serializable
data class Choice(
    val id: String,
    val text: String,
    val targetNodeId: String,
    val targetSceneId: String? = null,
    val targetSceneStartEffect: SceneStartEffect? = null,
    val conditions: Map<String, Int>? = null,
    val effects: ChoiceEffects = ChoiceEffects(),
    val unavailableReason: String? = null,
    val hideWhenUnavailable: Boolean = false
)

@Serializable
data class ChoiceEffects(
    val set: Map<String, Int> = emptyMap(),
    val add: Map<String, Int> = emptyMap()
)

data class ChoiceAvailability(val available: Boolean, val visible: Boolean, val reason: String? = null)

/** Shared by the engine and reader so a disabled choice can never be executed. */
fun Choice.availability(variables: Map<String, Int>): ChoiceAvailability {
    val conditionsMet = conditions.orEmpty().all { (key, minimum) -> (variables[key] ?: 0) >= minimum }
    val effectsFit = effects.add.all { (key, amount) ->
        val value = (effects.set[key] ?: variables[key] ?: 0).toLong() + amount.toLong()
        value in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()
    }
    val available = conditionsMet && effectsFit
    return ChoiceAvailability(available, available || !hideWhenUnavailable, when {
        !conditionsMet -> unavailableReason ?: "Недоступно из-за предыдущих решений."
        !effectsFit -> "Не удалось применить последствия выбора."
        else -> null
    })
}

/** Called only after availability and destination have been checked. Set precedes add. */
internal fun Choice.applyEffects(variables: Map<String, Int>): Map<String, Int> = variables.toMutableMap().apply {
    putAll(effects.set)
    effects.add.forEach { (key, amount) -> this[key] = ((this[key] ?: 0).toLong() + amount).toInt() }
}
