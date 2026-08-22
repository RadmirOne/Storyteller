package com.radmir.storyteller

import androidx.compose.ui.geometry.Size
import com.radmir.storyteller.models.*
import com.radmir.storyteller.repository.*
import com.radmir.storyteller.screens.scene.*
import com.radmir.storyteller.viewmodel.StoryViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

class WorldCameraTest {
    private val mira = StageCharacter("mira", worldX = 0.32f, groundY = 0.9f)
    private val lev = StageCharacter("lev", worldX = 0.68f, groundY = 0.9f)

    private fun script(second: DialogueNode = DialogueNode("second", "lev", "Лев",
        camera = CameraView(targetCharacterId = "lev"))) = StoryScript(
        id = "camera", title = "Камера", schemaVersion = 2,
        characters = listOf(Character("mira", "Мира", "files/mira.png"), Character("lev", "Лев", "files/lev.png")),
        startSceneId = "room", startNodeId = "first", scenes = mapOf("room" to Scene(
            "room", "files/room.png", mapOf(
                "first" to DialogueNode("first", "mira", "Мира", nextNodeId = "second",
                    stageCharacters = listOf(mira, lev), camera = CameraView(targetCharacterId = "mira")),
                "second" to second
            )
        ))
    )

    @Test fun focusFollowsSpeakerWithoutMovingTheActors() {
        val vm = StoryViewModel().apply { loadStory(Json.encodeToString(script())) }
        val before = vm.sceneUiState.value!!
        assertEquals(0.32f, before.cameraTarget.focusX)
        vm.advance()
        assertEquals(0.68f, vm.sceneUiState.value!!.cameraTarget.focusX)
        assertEquals(before.stage, vm.sceneUiState.value!!.stage)
    }

    @Test fun continuedTextKeepsTheActiveCameraCue() {
        val continuation = DialogueNode("second", "mira", "Продолжение той же реплики")
        val vm = StoryViewModel().apply { loadStory(Json.encodeToString(script(continuation))) }
        val cue = vm.sceneUiState.value!!.cameraCueId
        vm.advance()
        assertEquals(cue, vm.sceneUiState.value!!.cameraCueId)
        assertEquals(0.32f, vm.sceneUiState.value!!.cameraTarget.focusX)
    }

    @Test fun focusedActorIsCenteredAndMovesWithTheBackground() {
        val background = Size(1672f, 941f)
        val viewport = Size(400f, 500f)
        val sprite = Size(1024f, 1536f)
        val start = cameraTransform(CameraView(0.32f), viewport, background)
        val end = cameraTransform(CameraView(0.68f), viewport, background)
        val miraStart = worldSpriteBounds(mira, sprite, background, start)
        val miraEnd = worldSpriteBounds(mira, sprite, background, end)
        val levEnd = worldSpriteBounds(lev, sprite, background, end)
        assertEquals(200f, miraStart.center.x, 0.001f)
        assertEquals(200f, levEnd.center.x, 0.001f)
        assertEquals(end.translationX - start.translationX, miraEnd.left - miraStart.left, 0.001f)
        assertEquals(450f, levEnd.bottom, 0.001f)
    }

    @Test fun hiddenTargetAndVersionOneWorldCoordinatesAreRejected() {
        val hidden = DialogueNode("second", "lev", "Лев", stageCharacters = listOf(lev.copy(visible = false)),
            camera = CameraView(targetCharacterId = "lev"))
        assertFailsWith<StoryValidationException> { validateStory(script(hidden)) }
        assertFailsWith<StoryValidationException> { validateStory(script().copy(schemaVersion = 1)) }
    }

    @Test fun targetMustBePresentOnEveryIncomingBranch() {
        val original = script()
        val nodes = original.scenes.getValue("room").nodes.toMutableMap()
        nodes["first"] = nodes.getValue("first").copy(nextNodeId = null, choices = listOf(
            Choice("direct", "Прямо", "second"), Choice("hide", "Скрыть", "empty")))
        nodes["empty"] = DialogueNode("empty", "mira", "Пусто", nextNodeId = "second",
            camera = CameraView(), stageCharacters = emptyList())
        val branched = original.copy(scenes = mapOf("room" to original.scenes.getValue("room").copy(nodes = nodes)))
        val error = assertFailsWith<StoryValidationException> { validateStory(branched) }
        assertTrue(error.message!!.contains("room/second"))
    }
}
