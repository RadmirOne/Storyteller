package com.radmir.storyteller

import com.radmir.storyteller.engine.StoryEngine
import com.radmir.storyteller.repository.StoryRepository
import com.radmir.storyteller.repository.validateStoryResources
import com.radmir.storyteller.repository.MemoryProgressStore
import com.radmir.storyteller.viewmodel.StoryViewModel
import com.radmir.storyteller.models.GameState
import com.radmir.storyteller.models.availability
import java.io.File
import javax.imageio.ImageIO
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class BundledStoryTest {
    private val resources = listOf(
        File("src/commonMain/composeResources"), File("shared/src/commonMain/composeResources")
    ).first { it.isDirectory }

    @Test fun allPlayerSceneOutfitsHaveTransparentSurroundings() {
        val image = ImageIO.read(File(resources, "files/characters/player/wardrobe-scene.png"))
        assertTrue(image.colorModel.hasAlpha())
        val width = image.width / 3
        val height = image.height / 3
        for (row in 0..2) for (column in 0..2) {
            fun alpha(x: Int, y: Int) = (image.getRGB(column * width + x, row * height + y) ushr 24) and 255
            assertEquals(0, alpha(4, height / 2), "Левый край $row/$column")
            assertEquals(0, alpha(width - 5, height / 2), "Правый край $row/$column")
            assertTrue(alpha(width / 2, height / 2) >= 240, "Наряд $row/$column должен быть виден")
        }
    }

    @Test fun shortRomanceAlwaysStagesTheSpeaker() {
        val script = StoryRepository().parseScript(File(resources, "files/story.json").readText())
        val nodes = script.scenes.values.flatMap { it.nodes.values }
        assertTrue(nodes.size <= 25, "Короткая история: не более 25 узлов")
        assertEquals(5, nodes.count { it.nextNodeId == null && it.choices.isNullOrEmpty() })
        assertTrue(script.characters.any { it.id == "ilya" && it.spriteResource != null })
        assertTrue(script.characters.any { it.id == "mark" && it.spriteResource != null })
        nodes.forEach { node ->
            assertTrue(node.text.length <= 200, "${node.id}: реплика слишком длинная")
            val stage = assertNotNull(node.stageCharacters, "${node.id}: явный состав сцены")
            if (node.characterId == "protagonist") {
                assertTrue(stage.isEmpty(), "Героиня показана своим выбранным портретом")
            } else {
                assertEquals(listOf(node.characterId), stage.filter { it.visible }.map { it.characterId })
                assertEquals(node.characterId, node.camera?.targetCharacterId)
            }
        }
    }

    @Test fun everyReachablePageCanResumeItsStageAndJournal() {
        val json = File(resources, "files/story.json").readText()
        val pending = ArrayDeque<List<String?>>()
        pending.add(emptyList())
        val visited = mutableSetOf<Pair<String, String>>()
        val states = mutableSetOf<GameState>()
        while (pending.isNotEmpty()) {
            val route = pending.removeFirst()
            val store = MemoryProgressStore()
            val original = StoryViewModel(store).apply { loadStory(json) }
            route.forEach { if (it == null) original.advance() else original.selectChoice(it) }
            val state = original.gameState.value!!
            visited.add(state.currentSceneId to state.currentNodeId)
            if (!states.add(state)) continue
            val restored = StoryViewModel(store).apply { continueStory(json) }
            assertEquals(state, restored.gameState.value)
            assertEquals(original.sceneUiState.value!!.stage, restored.sceneUiState.value!!.stage)
            assertEquals(original.sceneUiState.value!!.cameraTarget, restored.sceneUiState.value!!.cameraTarget)
            assertEquals(original.journal.value, restored.journal.value)
            val node = original.currentNode.value!!
            if (node.nextNodeId != null) pending.add(route + listOf(null))
            node.choices.orEmpty().filter { it.availability(state.variables).available }
                .forEach { pending.add(route + it.id) }
        }
        val script = StoryRepository().parseScript(json)
        assertEquals(script.scenes.values.sumOf { it.nodes.size }, visited.size)
    }

    @Test fun storyPagesAreShortAndCharactersHaveRealTransparency() {
        val script = StoryRepository().parseScript(File(resources, "files/story.json").readText())
        script.scenes.forEach { (sceneId, scene) ->
            scene.nodes.forEach { (nodeId, node) ->
                assertTrue(node.text.length <= 300, "$sceneId/$nodeId: ${node.text.length} символов, максимум 300")
            }
        }
        script.characters.mapNotNull { it.spriteResource }.forEach { path ->
            val image = ImageIO.read(File(resources, path))
            assertTrue(image.colorModel.hasAlpha(), "$path должен иметь alpha-канал")
            var transparent = 0
            var opaque = 0
            var total = 0
            for (y in 0 until image.height step 32) for (x in 0 until image.width step 32) {
                val alpha = (image.getRGB(x, y) ushr 24) and 255
                if (alpha == 0) transparent++
                // Generated edges and painted interiors can have near-opaque alpha (e.g. 252/255).
                if (alpha >= 240) opaque++
                total++
            }
            assertTrue(transparent > total / 10 && opaque > total / 10, "$path: нужны прозрачный фон и видимый персонаж")
        }
    }

    @Test fun bundledStoryHasDecodableImagesAndAllNodesAreReachable() = runBlocking {
        val script = StoryRepository().parseScript(File(resources, "files/story.json").readText())
        validateStoryResources(script) { path ->
            assertNotNull(ImageIO.read(File(resources, path)), "Изображение $path")
        }
        val pending = ArrayDeque<Pair<String, String>>()
        pending.add(script.startSceneId to script.startNodeId)
        val visited = mutableSetOf<Pair<String, String>>()
        while (pending.isNotEmpty()) {
            val location = pending.removeFirst()
            if (!visited.add(location)) continue
            val node = script.scenes.getValue(location.first).nodes.getValue(location.second)
            node.nextNodeId?.let { pending.add((node.nextSceneId ?: location.first) to it) }
            node.choices.orEmpty().forEach { pending.add((it.targetSceneId ?: location.first) to it.targetNodeId) }
        }
        val all = script.scenes.flatMap { (sceneId, scene) -> scene.nodes.keys.map { sceneId to it } }.toSet()
        assertEquals(all, visited, "Все узлы встроенной истории должны быть достижимы")
    }

    @Test fun everyBundledTransitionCanBePlayedFromTheStart() {
        val json = File(resources, "files/story.json").readText()
        val script = StoryRepository().parseScript(json)
        val pending = ArrayDeque<List<String?>>()
        pending.add(emptyList())
        val visited = mutableSetOf<Pair<String, String>>()
        val states = mutableSetOf<GameState>()
        var endings = 0
        while (pending.isNotEmpty()) {
            val route = pending.removeFirst()
            val engine = StoryEngine(StoryRepository()).apply { initialize(json) }
            route.forEach { if (it == null) engine.advance() else engine.selectChoice(it) }
            val state = engine.getGameState()!!
            visited.add(state.currentSceneId to state.currentNodeId)
            if (!states.add(state)) continue
            val node = engine.getCurrentNode()!!
            if (node.nextNodeId != null) {
                val next = engine.advance()!!
                assertEquals(node.nextSceneId ?: state.currentSceneId, next.currentSceneId)
                assertEquals(node.nextNodeId, next.currentNodeId)
                pending.add(route + listOf(null))
            }
            node.choices.orEmpty().forEach { choice ->
                val branch = StoryEngine(StoryRepository()).apply { initialize(json) }
                route.forEach { if (it == null) branch.advance() else branch.selectChoice(it) }
                val next = branch.selectChoice(choice.id)!!
                if (!choice.availability(state.variables).available) {
                    assertEquals(state, next, "Недоступный выбор не должен менять прохождение")
                    return@forEach
                }
                assertEquals(choice.targetSceneId ?: state.currentSceneId, next.currentSceneId)
                assertEquals(choice.targetNodeId, next.currentNodeId)
                pending.add(route + choice.id)
            }
            if (node.nextNodeId == null && node.choices.isNullOrEmpty()) endings++
            else if (!node.choices.isNullOrEmpty()) {
                assertTrue(node.choices.any { it.availability(state.variables).available },
                    "${state.currentSceneId}/${state.currentNodeId}: нет доступного продолжения")
            }
        }
        assertEquals(script.scenes.values.sumOf { it.nodes.size }, visited.size)
        assertTrue(endings > 0)
    }

    @Test fun restorationConversationDependsOnTheArrivalChoiceAndSurvivesResume() {
        val json = File(resources, "files/story.json").readText()
        listOf("ask_restoration" to true, "enjoy_weekend" to false).forEach { (earlyChoice, unlocked) ->
            val store = MemoryProgressStore()
            val vm = StoryViewModel(store).apply {
                loadStory(json)
                repeat(4) { advance() }
                assertEquals("smile", currentNode.value!!.id)
                selectChoice(earlyChoice)
                advance(); advance()
            }
            val restored = StoryViewModel(store).apply { continueStory(json) }
            assertEquals("choose", restored.currentNode.value!!.id)
            assertEquals(vm.gameState.value, restored.gameState.value)
            val choice = restored.currentNode.value!!.choices!!.first { it.id == "see_restoration" }
            assertEquals(unlocked, choice.availability(restored.gameState.value!!.variables).available)
            restored.selectChoice(choice.id)
            assertEquals(if (unlocked) "restoration" else "choose", restored.currentNode.value!!.id)
            if (!unlocked) {
                restored.selectChoice("with_mira")
                assertEquals("tea", restored.currentNode.value!!.id)
            }
        }
    }
}
