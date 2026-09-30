package com.radmir.storyteller

import com.radmir.storyteller.engine.StoryEngine
import com.radmir.storyteller.engine.StorySession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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

    @Test fun demoChapterAlwaysStagesTheSpeaker() {
        val script = StoryRepository().parseScript(File(resources, "files/story.json").readText())
        val nodes = script.scenes.values.flatMap { it.nodes.values }
        assertTrue(nodes.size >= 100, "Демоглава должна содержать полноценные сцены")
        assertEquals(5, nodes.count { it.nextNodeId == null && it.choices.isNullOrEmpty() })
        assertTrue(script.characters.any { it.id == "ilya" && it.spriteResource != null })
        assertTrue(script.characters.any { it.id == "mark" && it.spriteResource != null })
        nodes.forEach { node ->
            assertTrue(node.text.length <= 300, "${node.id}: реплика слишком длинная")
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
            // Replay without serializing the full script after every intermediate step.
            // Persist the reached page once, then exercise the real Continue path.
            val original = StorySession(json)
            route.forEach { assertTrue(original.move(it)) }
            val state = original.state
            visited.add(state.currentSceneId to state.currentNodeId)
            if (!states.add(state)) continue
            store.write(Json.encodeToString(original.progress()))
            val restored = StoryViewModel(store).apply { continueStory(json) }
            assertEquals(state, restored.gameState.value)
            assertEquals(original.scene.stage, restored.sceneUiState.value!!.stage)
            assertEquals(original.scene.cameraTarget, restored.sceneUiState.value!!.cameraTarget)
            assertEquals(original.journal, restored.journal.value)
            assertEquals(original.node, restored.currentNode.value)
            val node = original.node
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
        (script.characters.mapNotNull { it.spriteResource } +
            script.scenes.values.flatMap { scene -> scene.nodes.values.mapNotNull { it.speakerSpriteResource } })
            .distinct().forEach { path ->
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
                advanceTo("tower", "choose")
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

    private fun StoryViewModel.advanceTo(scene: String, node: String,
        decisions: Map<String, String> = mapOf(
            "preparation" to "help_harbour", "courtyard" to "check_together",
            "opening" to "accept_thanks"
        )
    ) {
        repeat(200) {
            val state = gameState.value!!
            if (state.currentSceneId == scene && state.currentNodeId == node) return
            val page = currentNode.value!!
            if (page.choices.isNullOrEmpty()) {
                assertNotNull(page.nextNodeId, "Unexpected ending before $scene/$node")
                advance()
            } else {
                val id = decisions[state.currentSceneId] ?: error("Choose at ${state.currentSceneId}/${page.id}")
                val choice = page.choices.single { it.id == id }
                assertTrue(choice.availability(state.variables).available)
                selectChoice(id)
            }
        }
        fail("Did not reach $scene/$node")
    }

    @Test fun afternoonAndCreditChoicesUnlockCallbacksWithoutLockingAnyEnding() {
        val json = File(resources, "files/story.json").readText()
        for (interest in listOf(false, true)) for (archive in listOf(false, true))
            for (credit in listOf(false, true)) for (evening in listOf("ilya", "mark", "mira")) {
                val store = MemoryProgressStore()
                val vm = StoryViewModel(store).apply { loadStory(json) }
                val decisions = mapOf(
                    "arrival" to if (interest) "ask_restoration" else "enjoy_weekend",
                    "preparation" to if (archive) "read_archive" else "help_harbour",
                    "courtyard" to if (archive) "use_archive" else "check_together",
                    "opening" to if (credit) "share_credit" else "accept_thanks"
                )
                vm.advanceTo("courtyard", "p12", decisions)
                val repair = vm.currentNode.value!!.choices!!.single { it.id == "use_archive" }
                assertEquals(archive, repair.availability(vm.gameState.value!!.variables).available)
                if (!archive) {
                    val before = vm.gameState.value
                    vm.selectChoice("use_archive")
                    assertEquals(before, vm.gameState.value)
                }
                vm.advanceTo("tower", "choose", decisions)
                assertEquals(interest, vm.currentNode.value!!.choices!!.single { it.id == "see_restoration" }
                    .availability(vm.gameState.value!!.variables).available)
                assertTrue(vm.currentNode.value!!.choices!!.filter { it.id.startsWith("with_") }
                    .all { it.availability(vm.gameState.value!!.variables).available })
                vm.selectChoice("with_$evening")
                vm.advanceTo("${evening}_talk", "p10", decisions)
                val restored = StoryViewModel(store).apply { continueStory(json) }
                assertEquals(vm.gameState.value, restored.gameState.value)
                assertEquals(vm.journal.value, restored.journal.value)
                val expected = when (evening) {
                    "ilya" -> mapOf("remember_archive" to archive, "remember_credit" to credit)
                    "mark" -> mapOf("remember_harbour" to !archive, "remember_credit" to credit)
                    else -> mapOf("remember_team" to credit)
                }
                expected.forEach { (id, available) ->
                    val choice = restored.currentNode.value!!.choices!!.single { it.id == id }
                    assertEquals(available, choice.availability(restored.gameState.value!!.variables).available)
                    if (!available) assertFalse(choice.unavailableReason.isNullOrBlank())
                }
                val fallback = when (evening) {
                    "ilya" -> "stay_quiet"
                    "mark" -> "finish_tea"
                    else -> "enjoy_tea"
                }
                restored.selectChoice(fallback)
                if (evening == "mira") assertEquals("home", restored.currentNode.value!!.id)
                else {
                    assertEquals("ask", restored.currentNode.value!!.id)
                    assertEquals(2, restored.currentNode.value!!.choices!!.size)
                    assertTrue(restored.currentNode.value!!.choices!!.all {
                        it.availability(restored.gameState.value!!.variables).available
                    })
                }
            }
    }

    @Test fun undoingAfternoonChoiceRemovesItsConsequencesAfterResume() {
        val json = File(resources, "files/story.json").readText()
        val store = MemoryProgressStore()
        val vm = StoryViewModel(store, debugToolsEnabled = true).apply { loadStory(json) }
        vm.advanceTo("preparation", "p16", mapOf("arrival" to "enjoy_weekend"))
        vm.selectChoice("read_archive")
        vm.advanceTo("courtyard", "p12")
        assertEquals(1, vm.gameState.value!!.variables["archiveClue"])
        vm.debugUndoChoice()
        assertEquals("preparation", vm.gameState.value!!.currentSceneId)
        assertEquals(0, vm.gameState.value!!.variables["archiveClue"])
        vm.selectChoice("help_harbour")
        vm.advanceTo("courtyard", "p12")
        val restored = StoryViewModel(store).apply { continueStory(json) }
        assertEquals(0, restored.gameState.value!!.variables["archiveClue"])
        assertEquals(1, restored.gameState.value!!.variables["harbourTrust"])
        val locked = restored.currentNode.value!!.choices!!.single { it.id == "use_archive" }
        assertFalse(locked.availability(restored.gameState.value!!.variables).available)
        assertTrue(restored.journal.value.none { it.sceneId == "archive" })
    }

}
