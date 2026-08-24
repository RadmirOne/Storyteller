package com.radmir.storyteller.viewmodel

import androidx.lifecycle.ViewModel
import com.radmir.storyteller.engine.StoryEngine
import com.radmir.storyteller.models.DialogueNode
import com.radmir.storyteller.models.GameState
import com.radmir.storyteller.models.StoryScript
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

    private val _history = MutableStateFlow<List<StoryScript>>(emptyList())
    val history: StateFlow<List<StoryScript>> = _history.asStateFlow()

    fun loadStory(json: String) {
        val script = repository.loadScript(json)
        val initialState = engine.initialize(json)
        if (initialState != null) {
            _gameState.value = initialState
            _currentNode.value = engine.getCurrentNode()
            
            // Add to history if not already present
            val currentHistory = _history.value.toMutableList()
            if (!currentHistory.contains(script)) {
                currentHistory.add(script)
                _history.value = currentHistory
            }
        }
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