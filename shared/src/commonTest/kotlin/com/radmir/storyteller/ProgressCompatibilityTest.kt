package com.radmir.storyteller

import com.radmir.storyteller.engine.StorySession
import com.radmir.storyteller.models.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

class ProgressCompatibilityTest {
    private val script = StoryScript(
        id = "compatibility", title = "Original", schemaVersion = 3,
        characters = listOf(Character("voice", "Voice")),
        initialVariables = mapOf("trust" to 0), startSceneId = "room", startNodeId = "start",
        scenes = mapOf("room" to Scene("room", "files/room.png", mapOf(
            "start" to DialogueNode("start", "voice", "First", choices = listOf(
                Choice("help", "Help", "end", effects = ChoiceEffects(add = mapOf("trust" to 1)))
            )),
            "end" to DialogueNode("end", "voice", "Last")
        )))
    )

    private fun StoryScript.editStart(edit: (DialogueNode) -> DialogueNode): StoryScript = copy(
        scenes = scenes.mapValues { (_, scene) -> scene.copy(nodes = scene.nodes.mapValues { (id, node) ->
            if (id == "start") edit(node) else node
        }) }
    )

    @Test fun editorialUpdateRestoresRouteAndEffectsWithCurrentJournalText() {
        val original = StorySession(Json.encodeToString(script)).apply { assertTrue(move("help")) }
        val saved = Json.decodeFromString<StoryProgress>(Json.encodeToString(original.progress()))
        val updated = script.copy(title = "Revised", description = "New description",
            characters = script.characters.map { it.copy(name = "Narrator") })
            .editStart { node -> node.copy(text = "First revised", choices = node.choices!!.map {
                it.copy(text = "Help revised", unavailableReason = "Explanation revised")
            }) }
        val restored = StorySession(Json.encodeToString(updated), saved)
        assertEquals(original.state, restored.state)
        assertEquals(1, restored.state.variables["trust"])
        assertEquals(listOf("First revised", "Help revised", "Last"), restored.journal.map { it.text })
        assertEquals("Narrator", restored.journal.first().speaker)
        val restoredAgain = StorySession(Json.encodeToString(updated), restored.progress())
        assertEquals(restored.state, restoredAgain.state)
        assertEquals(restored.journal, restoredAgain.journal)
    }

    @Test fun behavioralAndStructuralUpdatesAreRejectedEvenWhenRouteStillReplays() {
        val saved = StorySession(Json.encodeToString(script)).apply { move("help") }.progress()
        val changes = listOf(
            script.copy(id = "other"),
            script.copy(initialVariables = mapOf("trust" to 2)),
            script.copy(startNodeId = "end"),
            script.editStart { it.copy(choices = it.choices!!.map { c -> c.copy(targetNodeId = "start") }) },
            script.editStart { it.copy(choices = it.choices!!.map { c -> c.copy(effects = ChoiceEffects()) }) },
            script.editStart { it.copy(choices = it.choices!!.map { c -> c.copy(conditions = mapOf("trust" to 0)) }) },
            script.editStart { it.copy(camera = CameraView(0.7f)) }
        )
        changes.forEach { updated ->
            assertFalse(script.isProgressCompatibleWith(updated))
            assertFailsWith<ProgressException> { StorySession(Json.encodeToString(updated), saved) }
        }
    }

    @Test fun legacySnapshotWithoutExplicitVersionAcceptsEditorialFixes() {
        val encoded = Json.encodeToString(StoryProgress(script))
        assertFalse(encoded.contains("\"version\""))
        val saved = Json.decodeFromString<StoryProgress>(encoded)
        val updated = script.copy(description = "Corrected")
        assertEquals("start", StorySession(Json.encodeToString(updated), saved).node.id)
        assertFailsWith<ProgressException> {
            StorySession(Json.encodeToString(updated), saved.copy(version = 99))
        }
    }
}
