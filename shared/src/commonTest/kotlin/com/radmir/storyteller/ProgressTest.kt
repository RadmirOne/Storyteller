package com.radmir.storyteller

import com.radmir.storyteller.models.*
import com.radmir.storyteller.repository.*
import com.radmir.storyteller.viewmodel.StoryViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

class ProgressTest {
    private val script = StoryScript(
        id = "saved", title = "Сохранение", schemaVersion = 2,
        characters = listOf(Character("mira", "Мира", "files/mira.png")),
        startSceneId = "room", startNodeId = "start",
        scenes = mapOf(
            "room" to Scene("room", "files/room.png", mapOf(
                "start" to DialogueNode("start", "mira", "Первая реплика", nextNodeId = "inherited",
                    cameraStart = CameraView(0.1f), camera = CameraView(targetCharacterId = "mira"),
                    stageCharacters = listOf(StageCharacter("mira", worldX = 0.7f))),
                "inherited" to DialogueNode("inherited", "mira", "Продолжение", choices = listOf(
                    Choice("hide", "Побыть одному", "hidden"), Choice("leave", "Выйти", "end", "outside"))),
                "hidden" to DialogueNode("hidden", "mira", "Она выходит", nextNodeId = "end", nextSceneId = "outside",
                    camera = CameraView(0.4f), stageCharacters = emptyList())
            )),
            "outside" to Scene("outside", "files/outside.png", mapOf(
                "end" to DialogueNode("end", "mira", "Конец")
            ))
        )
    )
    private val json = Json.encodeToString(script)

    @Test fun debugUndoChoiceTruncatesLaterPagesAndAllowsAnotherBranch() {
        val store = MemoryProgressStore()
        val vm = StoryViewModel(store, debugToolsEnabled = true).apply {
            loadStory(json); advance(); selectChoice("hide"); advance()
        }
        vm.debugUndoChoice()
        assertEquals("inherited", vm.currentNode.value!!.id)
        assertEquals(2, vm.journal.value.size)
        assertTrue(vm.journal.value.none { it.isChoice })
        assertEquals(0.7f, vm.sceneUiState.value!!.cameraTarget.focusX)
        assertEquals(1, vm.sceneUiState.value!!.stage.size)
        assertEquals("inherited", StoryViewModel(store).apply { continueStory(json) }.currentNode.value!!.id)
        vm.selectChoice("leave")
        assertEquals("end", vm.currentNode.value!!.id)
        assertEquals("Выйти", vm.journal.value.single { it.isChoice }.text)
        assertTrue(vm.journal.value.none { it.text == "Она выходит" })
    }

    @Test fun debugPreviousSceneRestoresLastVisitedNodeAndThenPreviousNode() {
        val store = MemoryProgressStore()
        StoryViewModel(store).apply { loadStory(json); advance(); selectChoice("hide"); advance() }
        val vm = StoryViewModel(store, debugToolsEnabled = true).apply { continueStory(json) }
        assertTrue(vm.debugNavigation.value.canGoToPreviousScene)
        vm.debugPreviousScene()
        assertEquals("hidden", vm.currentNode.value!!.id)
        assertEquals("room", vm.gameState.value!!.currentSceneId)
        assertTrue(vm.sceneUiState.value!!.stage.isEmpty())
        assertFalse(vm.debugNavigation.value.canGoToPreviousScene)
        val revision = vm.playbackRevision.value
        vm.debugPreviousNode()
        assertEquals("inherited", vm.currentNode.value!!.id)
        assertTrue(vm.playbackRevision.value > revision)
        assertEquals(1, vm.sceneUiState.value!!.stage.size)
        assertEquals(setOf("room/start"), vm.gameState.value!!.visitedNodes)
    }

    @Test fun debugNavigationAtStartIsANoOp() {
        val store = MemoryProgressStore()
        val vm = StoryViewModel(store, debugToolsEnabled = true).apply { loadStory(json) }
        val record = store.read()
        vm.debugPreviousNode(); vm.debugPreviousScene(); vm.debugUndoChoice()
        assertEquals(record, store.read())
        assertEquals("start", vm.currentNode.value!!.id)
        assertFalse(vm.debugNavigation.value.canGoBack)
        assertFalse(vm.debugNavigation.value.canUndoChoice)
    }

    @Test fun releaseViewModelRejectsDebugActions() {
        val store = MemoryProgressStore()
        val vm = StoryViewModel(store).apply { loadStory(json); advance(); selectChoice("leave") }
        val record = store.read()
        vm.debugPreviousNode(); vm.debugPreviousScene(); vm.debugUndoChoice()
        assertEquals(record, store.read())
        assertEquals("end", vm.currentNode.value!!.id)
        assertFalse(vm.debugNavigation.value.canGoBack)
        assertFalse(vm.debugNavigation.value.canUndoChoice)
        assertFalse(vm.debugNavigation.value.canGoToPreviousScene)
    }

    @Test fun restoresInheritedStageCameraAndJournalInANewViewModel() {
        val store = MemoryProgressStore()
        val first = StoryViewModel(store).apply { loadStory(json); advance() }
        val restored = StoryViewModel(store).apply { continueStory(json) }
        assertEquals(first.gameState.value, restored.gameState.value)
        assertEquals(first.sceneUiState.value!!.stage, restored.sceneUiState.value!!.stage)
        assertEquals(CameraView(0.7f), restored.sceneUiState.value!!.cameraTarget)
        assertEquals(restored.sceneUiState.value!!.cameraTarget, restored.sceneUiState.value!!.cameraStart)
        assertEquals(0L, restored.sceneUiState.value!!.cameraDurationMs)
        assertEquals(first.journal.value, restored.journal.value)
        assertEquals(listOf("Первая реплика", "Продолжение"), restored.journal.value.map { it.text })
    }

    @Test fun selectedChoicesAreLoggedOnceAndClearedStageStaysEmpty() {
        val store = MemoryProgressStore()
        val first = StoryViewModel(store).apply { loadStory(json); advance(); selectChoice("hide") }
        val expected = first.journal.value
        val restored = StoryViewModel(store).apply { continueStory(json); continueStory(json) }
        assertEquals(expected, restored.journal.value)
        assertEquals(1, restored.journal.value.count { it.isChoice })
        assertEquals("Побыть одному", restored.journal.value.single { it.isChoice }.text)
        assertTrue(restored.sceneUiState.value!!.stage.isEmpty())
        assertEquals(CameraView(0.4f), restored.sceneUiState.value!!.cameraTarget)
    }

    @Test fun endingRestoresSceneResetAndDoesNotAppendOnNext() {
        val store = MemoryProgressStore()
        StoryViewModel(store).apply { loadStory(json); advance(); selectChoice("leave") }
        val restored = StoryViewModel(store).apply { continueStory(json) }
        assertEquals("end", restored.currentNode.value!!.id)
        assertTrue(restored.sceneUiState.value!!.stage.isEmpty())
        assertEquals(CameraView(), restored.sceneUiState.value!!.cameraTarget)
        val record = store.read()
        val journal = restored.journal.value
        restored.advance()
        assertEquals(record, store.read())
        assertEquals(journal, restored.journal.value)
    }

    @Test fun newGameReplacesProgressAndJournal() {
        val store = MemoryProgressStore()
        val vm = StoryViewModel(store).apply { loadStory(json); advance(); selectChoice("hide") }
        vm.loadStory(json)
        val restored = StoryViewModel(store).apply { continueStory(json) }
        assertEquals("start", restored.currentNode.value!!.id)
        assertEquals(1, restored.journal.value.size)
        assertTrue(restored.gameState.value!!.visitedNodes.isEmpty())
    }

    @Test fun invalidActionsDoNotWriteOrLogAnything() {
        val store = MemoryProgressStore()
        val vm = StoryViewModel(store).apply { loadStory(json); advance() }
        val record = store.read()
        val journal = vm.journal.value
        vm.selectChoice("unknown")
        vm.advance()
        assertEquals(record, store.read())
        assertEquals(journal, vm.journal.value)
    }

    @Test fun failedWriteKeepsPreviousRecordAndRetrySavesLatestPage() {
        val store = FailingStore()
        val vm = StoryViewModel(store).apply { loadStory(json) }
        val old = store.read()
        store.fail = true
        vm.advance()
        assertNotNull(vm.saveError.value)
        assertEquals(old, store.read())
        assertEquals("inherited", vm.currentNode.value!!.id)
        store.fail = false
        vm.saveProgress()
        assertNull(vm.saveError.value)
        assertEquals("inherited", StoryViewModel(store).apply { continueStory(json) }.currentNode.value!!.id)
    }

    @Test fun corruptRecordIsReportedWithoutDeletingIt() {
        val store = MemoryProgressStore().apply { write("broken JSON") }
        val vm = StoryViewModel(store)
        assertNotNull(vm.saveError.value)
        assertNull(vm.savedStory.value)
        assertFailsWith<ProgressException> { vm.continueStory(json) }
        assertEquals("broken JSON", store.read())
        assertNull(vm.currentNode.value)
    }

    @Test fun changedStoryAndInvalidRoutesAreRejectedWithoutReplacingSession() {
        val store = MemoryProgressStore()
        val vm = StoryViewModel(store).apply { loadStory(json) }
        val record = store.read()
        assertFailsWith<ProgressException> { vm.continueStory(Json.encodeToString(script.copy(title = "Новая версия"))) }
        assertEquals(record, store.read())
        assertEquals("start", vm.currentNode.value!!.id)
        store.write(Json.encodeToString(StoryProgress(script, listOf(ProgressStep("unknown")))))
        assertFailsWith<ProgressException> { vm.continueStory(json) }
        assertEquals("start", vm.currentNode.value!!.id)
        assertEquals(1, vm.journal.value.size)
    }

    @Test fun unsupportedSaveVersionIsReported() {
        val store = MemoryProgressStore().apply { write(Json.encodeToString(StoryProgress(script, version = 99))) }
        assertNotNull(StoryViewModel(store).saveError.value)
    }

    private class FailingStore : ProgressStore {
        private val memory = MemoryProgressStore()
        var fail = false
        override fun read() = memory.read()
        override fun write(value: String) {
            if (fail) error("Disk full")
            memory.write(value)
        }
    }
}
