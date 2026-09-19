package com.radmir.storyteller.viewmodel

import androidx.lifecycle.ViewModel
import com.radmir.storyteller.engine.StoryEngine
import com.radmir.storyteller.models.CameraView
import com.radmir.storyteller.models.DialogueNode
import com.radmir.storyteller.models.GameState
import com.radmir.storyteller.models.SceneStartEffect
import com.radmir.storyteller.models.StageCharacter
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

    private val _currentScript = MutableStateFlow<StoryScript?>(null)
    val currentScript: StateFlow<StoryScript?> = _currentScript.asStateFlow()

    private val _sceneUiState = MutableStateFlow<SceneUiState?>(null)
    val sceneUiState: StateFlow<SceneUiState?> = _sceneUiState.asStateFlow()

    private val _history = MutableStateFlow<List<StoryScript>>(emptyList())
    val history: StateFlow<List<StoryScript>> = _history.asStateFlow()

    private var currentStage: List<StageCharacter> = emptyList()
    private var currentCamera: CameraView = CameraView()

    fun loadStory(json: String) {
        val initialState = engine.initialize(json)
        val script = repository.getScript() ?: return
        if (initialState != null) {
            currentStage = emptyList()
            currentCamera = CameraView()
            _gameState.value = initialState
            _currentNode.value = engine.getCurrentNode()
            _currentScript.value = script
            rebuildSceneState(previousSceneId = null, enterEffect = SceneStartEffect.NONE)

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
        val previousState = engine.getGameState()
        val previousNode = engine.getCurrentNode()
        val choice = previousNode?.choices?.find { it.id == choiceId }
        val newState = engine.selectChoice(choiceId)
        if (newState == null || newState == previousState) return
        _gameState.value = newState
        updateCurrentNode()
        val sceneChanged = previousState != null &&
            previousState.currentSceneId != newState.currentSceneId
        val effect = if (sceneChanged) {
            choice?.targetSceneStartEffect ?: SceneStartEffect.NONE
        } else {
            SceneStartEffect.NONE
        }
        rebuildSceneState(previousState?.currentSceneId, effect)
    }

    fun advance() {
        val previousState = engine.getGameState()
        val previousNode = engine.getCurrentNode()
        val newState = engine.advance()
        if (newState == null || newState == previousState) return
        _gameState.value = newState
        updateCurrentNode()
        val sceneChanged = previousState != null &&
            previousState.currentSceneId != newState.currentSceneId
        val effect = if (sceneChanged) {
            previousNode?.nextSceneStartEffect ?: SceneStartEffect.NONE
        } else {
            SceneStartEffect.NONE
        }
        rebuildSceneState(previousState?.currentSceneId, effect)
    }

    private fun rebuildSceneState(previousSceneId: String?, enterEffect: SceneStartEffect) {
        val script = repository.getScript() ?: return
        val state = engine.getGameState() ?: return
        val scene = script.scenes[state.currentSceneId] ?: return
        val node: DialogueNode = engine.getCurrentNode() ?: return
        if (previousSceneId != state.currentSceneId) {
            currentStage = emptyList()
            currentCamera = CameraView()
        }
        if (node.camera != null) {
            currentCamera = node.camera
        }
        if (node.stageCharacters != null) {
            currentStage = node.stageCharacters
        }
        _sceneUiState.value = SceneUiState(
            scene = scene,
            nodeId = node.id,
            cameraTarget = currentCamera,
            cameraStart = node.cameraStart,
            cameraDurationMs = node.cameraDurationMs ?: DEFAULT_CAMERA_DURATION_MS,
            stage = currentStage,
            enterEffect = enterEffect,
            sceneChanged = previousSceneId == null || previousSceneId != state.currentSceneId
        )
    }
}
