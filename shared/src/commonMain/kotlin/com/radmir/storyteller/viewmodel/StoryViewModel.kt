package com.radmir.storyteller.viewmodel

import androidx.lifecycle.ViewModel
import com.radmir.storyteller.engine.StoryEngine
import com.radmir.storyteller.models.DialogueNode
import com.radmir.storyteller.models.GameState
import com.radmir.storyteller.repository.StoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StoryViewModel : ViewModel() {
    private val repository = StoryRepository()
    private val engine = StoryEngine(repository)

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    private val _currentNode = MutableStateFlow<DialogueNode?>(null)
    val currentNode: StateFlow<DialogueNode?> = _currentNode.asStateFlow()

    fun loadStory(json: String) {
        engine.initialize(json)
        updateCurrentNode()
    }

    private fun updateCurrentNode() {
        _currentNode.value = engine.getCurrentNode()
    }

    fun selectChoice(choiceId: String) {
        val newState = engine.selectChoice(choiceId)
        _gameState.value = newState
        updateCurrentNode()
    }

    fun advance() {
        val newState = engine.advance()
        _gameState.value = newState
        updateCurrentNode()
    }
}