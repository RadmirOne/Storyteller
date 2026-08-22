package com.radmir.storyteller

import com.radmir.storyteller.models.*
import com.radmir.storyteller.repository.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking

class StoryValidationTest {
    private val node = DialogueNode("start", "narrator", "Начало")
    private fun script(node: DialogueNode = this.node) = StoryScript(
        id = "test", title = "Тест", characters = listOf(Character("narrator", "Рассказчик")),
        startSceneId = "forest", startNodeId = "start",
        scenes = mapOf("forest" to Scene("forest", "files/forest.png", mapOf("start" to node)))
    )

    @Test fun acceptsVersionOneAndLegacyVersionDefault() {
        val json = Json.encodeToString(script())
        assertEquals(1, StoryRepository().parseScript(json).schemaVersion)
        validateStory(script())
    }

    @Test fun appearanceConfigSupportsLegacyDefaultsAndCustomValues() {
        val repository = StoryRepository()
        assertEquals(CharacterAppearance(), repository.parseScript(Json.encodeToString(script())).characterAppearance)
        val configured = script().copy(characterAppearance = CharacterAppearance(false, 750, .25f, false))
        assertEquals(configured, repository.loadScript(Json.encodeToString(configured)))
        validateStory(script().copy(characterAppearance = CharacterAppearance(durationMs = 0, slideDistance = 0f)))
    }

    @Test fun rejectsInvalidAppearanceSettings() {
        listOf(
            CharacterAppearance(durationMs = -1),
            CharacterAppearance(slideDistance = -.1f),
            CharacterAppearance(slideDistance = 1.1f),
            CharacterAppearance(slideDistance = Float.NaN),
            CharacterAppearance(slideDistance = Float.POSITIVE_INFINITY)
        ).forEach { settings ->
            assertFailsWith<StoryValidationException> {
                validateStory(script().copy(characterAppearance = settings))
            }
        }
    }

    @Test fun rejectsUnsupportedVersionAndMissingStart() {
        assertFailsWith<StoryValidationException> { validateStory(script().copy(schemaVersion = 3)) }
        assertFailsWith<StoryValidationException> { validateStory(script().copy(startNodeId = "missing")) }
        assertFailsWith<StoryValidationException> { validateStory(script().copy(startSceneId = "missing")) }
    }

    @Test fun reportsBrokenReferencesTogether() {
        val bad = node.copy(characterId = "unknown", nextNodeId = "missing",
            stageCharacters = listOf(StageCharacter("ghost")))
        val errors = assertFailsWith<StoryValidationException> { validateStory(script(bad)) }.problems
        assertEquals(3, errors.size)
        assertTrue(errors.all { "forest/start" in it })
    }

    @Test fun rejectsAmbiguousTransitionsDuplicateIdsAndInvalidCamera() {
        val choice = Choice("go", "Вперёд", "start")
        val bad = node.copy(nextNodeId = "start", choices = listOf(choice, choice),
            camera = CameraView(2f), cameraDurationMs = -1)
        assertEquals(4, assertFailsWith<StoryValidationException> { validateStory(script(bad)) }.problems.size)
    }

    @Test fun rejectsMismatchedKeysAndUnsafeResourcePaths() {
        assertFailsWith<StoryValidationException> { validateStory(script(node.copy(id = "other"))) }
        val bad = script().copy(scenes = mapOf("forest" to Scene("other", "../forest.png")))
        assertFailsWith<StoryValidationException> { validateStory(bad) }
    }

    @Test fun failedLoadPreservesRepository() {
        val repository = StoryRepository()
        val valid = repository.loadScript(Json.encodeToString(script()))
        assertFailsWith<StoryValidationException> {
            repository.loadScript(Json.encodeToString(script().copy(schemaVersion = 9)))
        }
        assertEquals(valid, repository.getScript())
    }

    @Test fun resourceErrorsNameTheMissingImage() = runBlocking {
        val error = assertFailsWith<StoryValidationException> {
            validateStoryResources(script()) { throw IllegalStateException("Missing") }
        }
        assertTrue(error.message!!.contains("files/forest.png"))
    }

    @Test fun resourceValidationPreservesCancellation(): Unit = runBlocking {
        assertFailsWith<CancellationException> {
            validateStoryResources(script()) { throw CancellationException("Cancelled") }
        }
    }
}
