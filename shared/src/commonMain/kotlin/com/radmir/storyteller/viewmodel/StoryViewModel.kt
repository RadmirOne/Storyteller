package com.radmir.storyteller.viewmodel

import androidx.lifecycle.ViewModel
import com.radmir.storyteller.engine.StorySession
import com.radmir.storyteller.models.*
import com.radmir.storyteller.repository.MemoryProgressStore
import com.radmir.storyteller.repository.ProgressStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class DebugNavigationState(
    val canGoBack: Boolean = false,
    val canUndoChoice: Boolean = false,
    val canGoToPreviousScene: Boolean = false
)

class StoryViewModel(
    private val progressStore: ProgressStore = MemoryProgressStore(),
    private val debugToolsEnabled: Boolean = false
) : ViewModel() {
    private var session: StorySession? = null
    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState = _gameState.asStateFlow()
    private val _currentNode = MutableStateFlow<DialogueNode?>(null)
    val currentNode = _currentNode.asStateFlow()
    private val _currentScript = MutableStateFlow<StoryScript?>(null)
    val currentScript = _currentScript.asStateFlow()
    private val _sceneUiState = MutableStateFlow<SceneUiState?>(null)
    val sceneUiState = _sceneUiState.asStateFlow()
    private val _journal = MutableStateFlow<List<JournalEntry>>(emptyList())
    val journal = _journal.asStateFlow()
    private val _history = MutableStateFlow<List<StoryScript>>(emptyList())
    val history: StateFlow<List<StoryScript>> = _history.asStateFlow()
    private val _savedStory = MutableStateFlow<StoryScript?>(null)
    val savedStory = _savedStory.asStateFlow()
    private val _saveError = MutableStateFlow<String?>(null)
    val saveError = _saveError.asStateFlow()
    private val _debugNavigation = MutableStateFlow(DebugNavigationState())
    val debugNavigation = _debugNavigation.asStateFlow()
    private val _playbackRevision = MutableStateFlow(0)
    val playbackRevision = _playbackRevision.asStateFlow()

    init {
        try { _savedStory.value = readProgress()?.story }
        catch (e: Exception) { _saveError.value = "Не удалось прочитать сохранение. Можно начать новую игру." }
    }

    fun loadStory(json: String) {
        val fresh = StorySession(json)
        session = fresh
        _playbackRevision.value++
        publish(fresh)
        saveProgress()
    }

    fun continueStory(json: String) {
        val progress = try { readProgress() }
        catch (e: Exception) { throw ProgressException("Не удалось прочитать сохранение. Начните новую игру.") }
            ?: throw ProgressException("Сохранение не найдено. Начните новую игру.")
        val restored = StorySession(json, progress)
        session = restored
        _playbackRevision.value++
        _saveError.value = null
        publish(restored)
    }

    fun selectChoice(choiceId: String) = move(choiceId)
    fun advance() = move(null)

    fun debugPreviousNode() {
        val count = session?.progress()?.steps?.size ?: return
        rewind(count - 1)
    }

    fun debugUndoChoice() {
        val steps = session?.progress()?.steps ?: return
        rewind(steps.indexOfLast { it.choiceId != null })
    }

    fun debugPreviousScene() {
        val count = session?.previousSceneStepCount() ?: return
        rewind(count)
    }

    private fun rewind(stepCount: Int) {
        if (!debugToolsEnabled) return
        val current = session ?: return
        val progress = current.progress()
        if (stepCount !in 0 until progress.steps.size) return
        val restored = StorySession(Json.encodeToString(current.story), progress.copy(steps = progress.steps.take(stepCount)))
        session = restored
        _playbackRevision.value++
        publish(restored)
        saveProgress()
    }

    private fun move(choiceId: String?) {
        val current = session ?: return
        if (!current.move(choiceId)) return
        publish(current)
        saveProgress()
    }

    /** Synchronous atomic replacement: a completed action cannot be overtaken by an older write. */
    fun saveProgress() {
        val current = session ?: return
        try {
            progressStore.write(Json.encodeToString(current.progress()))
            _savedStory.value = current.story
            _saveError.value = null
        } catch (e: Exception) {
            _saveError.value = "Не удалось сохранить прогресс. Последние реплики могут быть потеряны при закрытии."
        }
    }

    private fun readProgress(): StoryProgress? = progressStore.read()?.let {
        Json.decodeFromString<StoryProgress>(it).also { progress ->
            if (progress.version != 1) throw ProgressException("Неподдерживаемая версия сохранения.")
        }
    }

    private fun publish(current: StorySession) {
        val steps = current.progress().steps
        _debugNavigation.value = if (debugToolsEnabled) DebugNavigationState(
            canGoBack = steps.isNotEmpty(),
            canUndoChoice = steps.any { it.choiceId != null },
            canGoToPreviousScene = current.previousSceneStepCount() != null
        ) else DebugNavigationState()
        _gameState.value = current.state
        _currentNode.value = current.node
        _currentScript.value = current.story
        _sceneUiState.value = current.scene
        _journal.value = current.journal
        if (current.story !in _history.value) _history.value += current.story
    }
}
