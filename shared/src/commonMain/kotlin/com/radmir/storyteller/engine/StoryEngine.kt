package com.radmir.storyteller.engine

import com.radmir.storyteller.models.DialogueNode
import com.radmir.storyteller.models.GameState
import com.radmir.storyteller.models.Scene
import com.radmir.storyteller.models.StoryScript
import com.radmir.storyteller.repository.StoryRepository

class StoryEngine(private val repository: StoryRepository) {
    private var gameState: GameState? = null

    fun initialize(json: String): GameState? {
        val script = repository.loadScript(json)
        gameState = GameState(
            currentSceneId = script.startSceneId,
            currentNodeId = script.startNodeId
        )
        return gameState
    }

    fun getCurrentScene(): Scene? {
        val script = repository.getScript() ?: return null
        val sceneId = gameState?.currentSceneId ?: return null
        return script.scenes[sceneId]
    }

    fun getCurrentNode(): DialogueNode? {
        val state = gameState ?: return null
        return nodeIn(state.currentSceneId, state.currentNodeId)
    }

    fun getGameState(): GameState? = gameState

    fun selectChoice(choiceId: String): GameState? {
        val script = repository.getScript() ?: return null
        val currentGameState = gameState ?: return null
        val currentNode = nodeIn(currentGameState.currentSceneId, currentGameState.currentNodeId) ?: return null

        val choice = currentNode.choices?.find { it.id == choiceId } ?: return null

        val currentVariables = currentGameState.variables
        val metConditions = choice.conditions?.all { (key, value) ->
            (currentVariables[key] ?: 0) >= value
        } ?: true

        if (metConditions) {
            val targetSceneId = choice.targetSceneId ?: currentGameState.currentSceneId
            val targetNode = nodeIn(targetSceneId, choice.targetNodeId)
                ?: return currentGameState
            val newVisitedNodes = currentGameState.visitedNodes.toMutableSet().apply {
                add(nodeKey(currentGameState.currentSceneId, currentGameState.currentNodeId))
            }
            gameState = GameState(
                currentSceneId = targetSceneId,
                currentNodeId = targetNode.id,
                variables = currentVariables,
                visitedNodes = newVisitedNodes
            )
        }
        return gameState
    }

    fun advance(): GameState? {
        val currentGameState = gameState ?: return null
        val currentNode = nodeIn(currentGameState.currentSceneId, currentGameState.currentNodeId) ?: return null

        if (currentNode.nextNodeId != null) {
            val targetSceneId = currentNode.nextSceneId ?: currentGameState.currentSceneId
            val targetNode = nodeIn(targetSceneId, currentNode.nextNodeId)
                ?: return currentGameState
            val newVisitedNodes = currentGameState.visitedNodes.toMutableSet().apply {
                add(nodeKey(currentGameState.currentSceneId, currentGameState.currentNodeId))
            }
            gameState = GameState(
                currentSceneId = targetSceneId,
                currentNodeId = targetNode.id,
                variables = currentGameState.variables,
                visitedNodes = newVisitedNodes
            )
        }
        return gameState
    }

    private fun nodeIn(sceneId: String, nodeId: String): DialogueNode? {
        val script: StoryScript = repository.getScript() ?: return null
        return script.scenes[sceneId]?.nodes?.get(nodeId)
    }

    private fun nodeKey(sceneId: String, nodeId: String): String = "$sceneId/$nodeId"
}
