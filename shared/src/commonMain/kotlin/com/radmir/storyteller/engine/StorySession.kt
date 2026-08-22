package com.radmir.storyteller.engine

import com.radmir.storyteller.models.*
import com.radmir.storyteller.repository.StoryRepository
import com.radmir.storyteller.viewmodel.DEFAULT_CAMERA_DURATION_MS
import com.radmir.storyteller.viewmodel.SceneUiState

/** Deterministic reading session: replay restores stage inheritance and the exact journal order. */
internal class StorySession(
    json: String,
    progress: StoryProgress? = null,
    newPlayerCharacter: PlayerCharacter? = null
) {
    private val repository = StoryRepository()
    private val engine = StoryEngine(repository)
    val story: StoryScript
    val playerCharacter: PlayerCharacter? = progress?.playerCharacter ?: newPlayerCharacter
    val state: GameState get() = engine.getGameState()!!
    val node: DialogueNode get() = story.personalize(engine.getCurrentNode()!!, playerCharacter)
    private val steps = mutableListOf<ProgressStep>()
    private val sceneTrail = mutableListOf<String>()
    private val entries = mutableListOf<JournalEntry>()
    val journal: List<JournalEntry> get() = entries.toList()
    private var stage = emptyList<StageCharacter>()
    private var camera = CameraView()
    private var cameraCue = ""
    lateinit var scene: SceneUiState
        private set

    init {
        engine.initialize(json)
        story = repository.getScript()!!
        if (progress != null && (progress.version != 1 || !progress.story.isProgressCompatibleWith(story))) {
            throw ProgressException("История обновилась. Это сохранение несовместимо; начните новую игру.")
        }
        rebuild(null, SceneStartEffect.NONE)
        appendPage()
        sceneTrail.add(state.currentSceneId)
        progress?.steps?.forEach { step ->
            if (!move(step.choiceId)) throw ProgressException("Сохранение повреждено: не удалось восстановить маршрут.")
        }
        if (progress != null) {
            scene = scene.copy(cameraStart = scene.cameraTarget, cameraDurationMs = 0,
                enterEffect = SceneStartEffect.NONE, sceneChanged = true)
        }
    }

    fun progress(): StoryProgress = StoryProgress(
        story = story,
        steps = steps.toList(),
        playerCharacter = playerCharacter
    )

    fun previousSceneStepCount(): Int? = sceneTrail.indexOfLast { it != state.currentSceneId }
        .takeIf { it >= 0 }

    fun move(choiceId: String?): Boolean {
        val previous = state
        val oldNode = node
        val choice = if (choiceId != null) oldNode.choices?.find { it.id == choiceId } else null
        if (choiceId == null && !oldNode.choices.isNullOrEmpty()) return false
        val next = if (choiceId == null) engine.advance() else engine.selectChoice(choiceId)
        // A successful self-loop can produce equal values and must still enter the route/journal.
        if (next == null || next === previous) return false
        if (choice != null) entries.add(JournalEntry(previous.currentSceneId, previous.currentNodeId,
            "Ваш выбор", choice.text, isChoice = true))
        steps.add(ProgressStep(choiceId))
        sceneTrail.add(next.currentSceneId)
        val effect = if (previous.currentSceneId != next.currentSceneId) {
            if (choiceId == null) oldNode.nextSceneStartEffect else choice?.targetSceneStartEffect
        } else SceneStartEffect.NONE
        rebuild(previous.currentSceneId, effect ?: SceneStartEffect.NONE)
        appendPage()
        return true
    }

    private fun appendPage() {
        entries.add(JournalEntry(state.currentSceneId, node.id,
            story.speakerName(node.characterId, playerCharacter), node.text))
    }

    private fun rebuild(previousSceneId: String?, effect: SceneStartEffect) {
        val changed = previousSceneId != state.currentSceneId
        if (changed) { stage = emptyList(); camera = CameraView() }
        node.stageCharacters?.let { stage = it }
        node.camera?.let { camera = it }
        if (changed || node.camera != null || node.cameraStart != null ||
            (node.stageCharacters != null && camera.targetCharacterId != null)) {
            cameraCue = "${state.currentSceneId}/${node.id}"
        }
        fun resolve(value: CameraView): CameraView = value.targetCharacterId?.let { id ->
            CameraView(stage.first { it.characterId == id && it.visible }.worldX!!)
        } ?: value
        scene = SceneUiState(
            scene = engine.getCurrentScene()!!, nodeId = node.id,
            cameraTarget = resolve(camera), cameraStart = node.cameraStart?.let(::resolve),
            cameraDurationMs = node.cameraDurationMs ?: DEFAULT_CAMERA_DURATION_MS,
            stage = stage, enterEffect = effect, sceneChanged = changed, cameraCueId = cameraCue
        )
    }
}
