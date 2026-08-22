package com.radmir.storyteller

import com.radmir.storyteller.models.*
import com.radmir.storyteller.viewmodel.StoryViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

class SceneStateTest {
    private fun viewModel(clearStage: Boolean = false): StoryViewModel {
        val start = DialogueNode("start", "n", "Start", nextNodeId = "next",
            camera = CameraView(0.8f), stageCharacters = listOf(StageCharacter("n")))
        val next = DialogueNode("next", "n", "Next", nextSceneId = "b", nextNodeId = "start",
            stageCharacters = if (clearStage) emptyList() else null)
        val script = StoryScript("s", "Story", characters = listOf(Character("n", "Narrator")),
            startSceneId = "a", startNodeId = "start", scenes = mapOf(
                "a" to Scene("a", "files/a.png", mapOf("start" to start, "next" to next)),
                "b" to Scene("b", "files/b.png", mapOf("start" to DialogueNode("start", "n", "End")))
            ))
        return StoryViewModel().apply { loadStory(Json.encodeToString(script)) }
    }

    @Test fun inheritsWithinSceneAndResetsAcrossScenesWithSameNodeId() {
        val vm = viewModel()
        vm.advance()
        assertEquals(listOf(StageCharacter("n")), vm.sceneUiState.value!!.stage)
        assertEquals(CameraView(0.8f), vm.sceneUiState.value!!.cameraTarget)
        vm.advance()
        assertEquals("b", vm.sceneUiState.value!!.scene.id)
        assertTrue(vm.sceneUiState.value!!.stage.isEmpty())
        assertEquals(CameraView(), vm.sceneUiState.value!!.cameraTarget)
    }

    @Test fun emptyStageExplicitlyRemovesCharacters() {
        val vm = viewModel(clearStage = true)
        vm.advance()
        assertTrue(vm.sceneUiState.value!!.stage.isEmpty())
    }

    @Test fun unknownChoiceDoesNotClearUiState() {
        val vm = viewModel()
        val before = vm.gameState.value
        vm.selectChoice("missing")
        assertEquals(before, vm.gameState.value)
        assertNotNull(vm.currentNode.value)
    }
}
