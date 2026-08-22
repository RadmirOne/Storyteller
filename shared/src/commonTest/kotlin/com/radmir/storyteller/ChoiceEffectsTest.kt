package com.radmir.storyteller

import com.radmir.storyteller.engine.StoryEngine
import com.radmir.storyteller.models.*
import com.radmir.storyteller.repository.*
import com.radmir.storyteller.viewmodel.StoryViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

class ChoiceEffectsTest {
    private fun script(
        initial: Map<String, Int> = mapOf("trust" to 0),
        choices: List<Choice> = listOf(
            Choice("earn", "Помочь", "middle", effects = ChoiceEffects(add = mapOf("trust" to 1))),
            Choice("skip", "Пройти мимо", "middle")
        )
    ) = StoryScript(
        id = "effects", title = "Последствия", schemaVersion = 3,
        initialVariables = initial,
        characters = listOf(Character("n", "Голос")), startSceneId = "room", startNodeId = "start",
        scenes = mapOf("room" to Scene("room", "files/room.png", mapOf(
            "start" to DialogueNode("start", "n", "Начало", choices = choices),
            "middle" to DialogueNode("middle", "n", "Позже", nextNodeId = "gate"),
            "gate" to DialogueNode("gate", "n", "Решение", choices = listOf(
                Choice("secret", "Узнать секрет", "end", conditions = mapOf("trust" to 1),
                    unavailableReason = "Нужно помочь раньше."),
                Choice("leave", "Уйти", "end")
            )),
            "end" to DialogueNode("end", "n", "Конец")
        )))
    )

    @Test fun earlyChoiceUnlocksLaterOptionAndLinearStepsKeepVariables() {
        listOf("earn" to true, "skip" to false).forEach { (firstChoice, unlocked) ->
            val engine = StoryEngine(StoryRepository()).apply { initialize(Json.encodeToString(script())) }
            assertEquals(mapOf("trust" to 0), engine.getGameState()!!.variables)
            engine.selectChoice(firstChoice)
            engine.advance()
            val previous = engine.getGameState()!!
            val choice = engine.getCurrentNode()!!.choices!!.first()
            assertEquals(unlocked, choice.availability(previous.variables).available)
            engine.selectChoice("secret")
            if (unlocked) assertEquals("end", engine.getCurrentNode()!!.id)
            else {
                assertSame(previous, engine.getGameState())
                engine.selectChoice("leave")
                assertEquals("end", engine.getCurrentNode()!!.id)
            }
        }
    }

    @Test fun setPrecedesAddAndNegativeChangesAreSupported() {
        val choice = Choice("change", "Изменить", "middle", effects = ChoiceEffects(
            set = mapOf("trust" to 10), add = mapOf("trust" to -3)))
        val engine = StoryEngine(StoryRepository()).apply {
            initialize(Json.encodeToString(script(mapOf("trust" to 99), listOf(choice))))
        }
        assertEquals(7, engine.selectChoice("change")!!.variables["trust"])
    }

    @Test fun blockedUnknownAndOverflowChoicesHaveNoSideEffects() {
        val locked = Choice("locked", "Закрыто", "middle", conditions = mapOf("trust" to 1),
            effects = ChoiceEffects(add = mapOf("trust" to 1)))
        val engine = StoryEngine(StoryRepository()).apply {
            initialize(Json.encodeToString(script(choices = listOf(locked))))
        }
        val before = engine.getGameState()
        assertSame(before, engine.selectChoice("locked"))
        assertNull(engine.selectChoice("unknown"))
        assertSame(before, engine.getGameState())
        listOf(Int.MAX_VALUE to 1, Int.MIN_VALUE to -1).forEach { (initial, delta) ->
            val overflowing = locked.copy(conditions = null, effects = ChoiceEffects(add = mapOf("trust" to delta)))
            engine.initialize(Json.encodeToString(script(mapOf("trust" to initial), listOf(overflowing))))
            val previous = engine.getGameState()!!
            assertFalse(overflowing.availability(previous.variables).available)
            assertSame(previous, engine.selectChoice("locked"))
        }
    }

    @Test fun hiddenAndDisabledOptionsShareTheSameConditions() {
        val choice = Choice("c", "Выбор", "end", conditions = mapOf("trust" to 2, "clue" to 1),
            unavailableReason = "Найдите подсказку.")
        val missing = choice.availability(mapOf("trust" to 2))
        assertFalse(missing.available)
        assertTrue(missing.visible)
        assertEquals("Найдите подсказку.", missing.reason)
        assertFalse(choice.copy(hideWhenUnavailable = true).availability(mapOf("trust" to 2)).visible)
        val ready = choice.copy(hideWhenUnavailable = true).availability(mapOf("trust" to 2, "clue" to 1))
        assertTrue(ready.available && ready.visible)
        assertNull(ready.reason)
    }

    @Test fun resumeAndUndoRestoreEffectsExactlyOnceAndNewGameResetsThem() {
        val json = Json.encodeToString(script())
        val store = MemoryProgressStore()
        val original = StoryViewModel(store).apply { loadStory(json); selectChoice("earn"); advance() }
        val restored = StoryViewModel(store, debugToolsEnabled = true).apply { continueStory(json); continueStory(json) }
        assertEquals(original.gameState.value, restored.gameState.value)
        assertEquals(original.journal.value, restored.journal.value)
        assertEquals(1, restored.gameState.value!!.variables["trust"])
        restored.debugUndoChoice()
        assertEquals(0, restored.gameState.value!!.variables["trust"])
        restored.selectChoice("skip"); restored.advance()
        val savedBefore = store.read()
        val journalBefore = restored.journal.value
        restored.selectChoice("secret")
        assertEquals(savedBefore, store.read())
        assertEquals(journalBefore, restored.journal.value)
        restored.loadStory(json)
        assertEquals(0, restored.gameState.value!!.variables["trust"])
    }

    @Test fun repeatedSelfLoopsAreRecordedEvenWhenValuesDoNotChange() {
        val loop = Choice("loop", "Ещё раз", "start", effects = ChoiceEffects(set = mapOf("trust" to 1)))
        val json = Json.encodeToString(script(choices = listOf(loop)))
        val store = MemoryProgressStore()
        val vm = StoryViewModel(store).apply { loadStory(json); repeat(3) { selectChoice("loop") } }
        assertEquals(3, vm.journal.value.count { it.isChoice })
        assertEquals(3, Json.decodeFromString<StoryProgress>(store.read()!!).steps.size)
        val restored = StoryViewModel(store).apply { continueStory(json) }
        assertEquals(vm.gameState.value, restored.gameState.value)
        assertEquals(vm.journal.value, restored.journal.value)
    }

    @Test fun rejectsUndeclaredVariablesAndNewFieldsInOlderFormats() {
        val references = listOf(
            Choice("c", "Выбор", "middle", conditions = mapOf("typo" to 1)),
            Choice("c", "Выбор", "middle", effects = ChoiceEffects(set = mapOf("typo" to 1))),
            Choice("c", "Выбор", "middle", effects = ChoiceEffects(add = mapOf("typo" to 1)))
        )
        references.forEach { choice ->
            val error = assertFailsWith<StoryValidationException> { validateStory(script(choices = listOf(choice))) }
            assertTrue(error.message!!.contains("room/start, выбор c"))
            assertTrue(error.message!!.contains("typo"))
        }
        assertFailsWith<StoryValidationException> { validateStory(script(initial = mapOf(" " to 0))) }
        listOf(1, 2).forEach { version ->
            assertFailsWith<StoryValidationException> { validateStory(script().copy(schemaVersion = version)) }
            assertFailsWith<StoryValidationException> {
                validateStory(script().copy(schemaVersion = version, initialVariables = emptyMap()))
            }
            val base = script(choices = listOf(Choice("c", "Выбор", "middle", conditions = mapOf("missing" to 1))))
            val legacy = base.copy(schemaVersion = version, initialVariables = emptyMap(),
                scenes = base.scenes.mapValues { (_, scene) -> scene.copy(nodes = scene.nodes.mapValues { (_, node) ->
                    node.copy(choices = node.choices?.map { it.copy(unavailableReason = null) })
                }) })
            validateStory(legacy)
            val engine = StoryEngine(StoryRepository()).apply { initialize(Json.encodeToString(legacy)) }
            assertEquals("start", engine.selectChoice("c")!!.currentNodeId)
        }
    }
}
