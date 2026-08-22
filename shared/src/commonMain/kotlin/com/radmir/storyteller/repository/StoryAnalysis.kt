package com.radmir.storyteller.repository

import com.radmir.storyteller.models.StoryScript

data class StoryLocation(val sceneId: String, val nodeId: String) {
    override fun toString(): String = "$sceneId/$nodeId"
}

data class StoryAnalysisReport(
    val nodeCount: Int,
    val unreachableNodes: Set<StoryLocation>,
    val endings: Set<StoryLocation>,
    val conditionalChoiceNodes: Set<StoryLocation>,
    val imageResources: Set<String>,
    val audioResources: Set<String>
) {
    val endingCount: Int get() = endings.size
    val resources: Set<String> get() = imageResources + audioResources
}

/** Static graph analysis, ignoring conditions. Warnings are advice, never runtime validation. */
fun analyzeStory(story: StoryScript): StoryAnalysisReport {
    val nodes = story.scenes.flatMap { (sceneId, scene) ->
        scene.nodes.map { (nodeId, node) -> StoryLocation(sceneId, nodeId) to node }
    }.toMap()
    val pending = ArrayDeque<StoryLocation>()
    val visited = mutableSetOf<StoryLocation>()
    pending.add(StoryLocation(story.startSceneId, story.startNodeId))
    while (pending.isNotEmpty()) {
        val location = pending.removeFirst()
        val node = nodes[location] ?: continue
        if (!visited.add(location)) continue
        node.nextNodeId?.let { pending.add(StoryLocation(node.nextSceneId ?: location.sceneId, it)) }
        node.choices.orEmpty().forEach {
            pending.add(StoryLocation(it.targetSceneId ?: location.sceneId, it.targetNodeId))
        }
    }
    return StoryAnalysisReport(
        nodeCount = nodes.size,
        unreachableNodes = nodes.keys - visited,
        endings = nodes.filterValues { it.nextNodeId == null && it.choices.isNullOrEmpty() }.keys,
        conditionalChoiceNodes = nodes.filterValues { node ->
            !node.choices.isNullOrEmpty() && node.choices.all { !it.conditions.isNullOrEmpty() }
        }.keys,
        imageResources = (story.characters.mapNotNull { it.spriteResource } +
            story.scenes.values.map { it.backgroundResource }).toSet(),
        audioResources = story.scenes.values.flatMap { listOfNotNull(it.audio.music, it.audio.ambience) }.toSet()
    )
}
