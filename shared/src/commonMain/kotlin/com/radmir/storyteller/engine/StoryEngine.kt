package com.radmir.storyteller.engine

import com.radmir.storyteller.models.DialogueNode
import com.radmir.storyteller.models.GameState
import com.radmir.storyteller.models.StoryScript
import com.radmir.storyteller.repository.StoryRepository

class StoryEngine(private val repository: StoryRepository) {
    private var gameState: GameState? = null

    fun initialize(json: String): GameState? {
        val script = repository.loadScript(json)
        val startNodeId = script.startNodeId
        gameState = GameState(currentNodeId = startNodeId)
        return gameState
    }

    fun getCurrentNode(): DialogueNode? {
        val script = repository.getScript() ?: return null
        val currentNodeId = gameState?.currentNodeId ?: return null
        return script.nodes[currentNodeId]
    }

    fun getGameState(): GameState? = gameState

    fun selectChoice(choiceId: String): GameState? {
        val script = repository.getScript() ?: return null
        val currentGameState = gameState ?: return null
        val currentNode = script.nodes[currentGameState.currentNodeId] ?: return null
        
        val choice = currentNode.choices?.find { it.id == choiceId } ?: return null
        
        val currentVariables = currentGameState.variables
        val metConditions = choice.conditions?.all { (key, value) ->
            (currentVariables[key] ?: 0) >= value
        } ?: true

        if (metConditions) {
            val newVisitedNodes = currentGameState.visitedNodes.toMutableSet().apply {
                add(currentGameState.currentNodeId)
            }
            gameState = GameState(
                currentNodeId = choice.targetNodeId,
                variables = currentVariables,
                visitedNodes = newVisitedNodes
            )
        }
        return gameState
    }

    fun advance(): GameState? {
        val script = repository.getScript() ?: return null
        val currentGameState = gameState ?: return null
        val currentNode = script.nodes[currentGameState.currentNodeId] ?: return null

        if (currentNode.nextNodeId != null) {
            val newVisitedNodes = currentGameState.visitedNodes.toMutableSet().apply {
                add(currentGameState.currentNodeId)
            }
            gameState = GameState(
                currentNodeId = currentNode.nextNodeId,
                variables = currentGameState.variables,
                visitedNodes = newVisitedNodes
            )
        }
        return gameState
    }
}