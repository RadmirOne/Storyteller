package com.radmir.storyteller

import com.radmir.storyteller.models.*
import com.radmir.storyteller.repository.*
import com.radmir.storyteller.viewmodel.StoryViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

class StoryCatalogTest {
    private fun story(id: String) = StoryScript(id = id, title = id,
        characters = listOf(Character("n", "Голос")), startSceneId = "s", startNodeId = "start",
        scenes = mapOf("s" to Scene("s", "files/cover.png", mapOf(
            "start" to DialogueNode("start", "n", "Начало", nextNodeId = "end"),
            "end" to DialogueNode("end", "n", "Конец")
        ))))

    @Test fun startingAnotherStoryDoesNotReplaceFirstStoryOrItsPlayer() {
        val legacy = MemoryProgressStore()
        val diskA = MemoryProgressStore()
        val diskB = MemoryProgressStore()
        fun storeA() = StoryProgressStore("a", diskA, legacy)
        fun storeB() = StoryProgressStore("b", diskB, legacy)
        val jsonA = Json.encodeToString(story("a"))
        val jsonB = Json.encodeToString(story("b"))
        val playerA = PlayerCharacter("Алиса", PlayerAppearance.DARK_HAIR, PlayerOutfit.COAT)
        StoryViewModel(storeA()).apply { loadStory(jsonA, playerA); advance() }
        val recordA = diskA.read()
        StoryViewModel(storeB()).apply {
            loadStory(jsonB, PlayerCharacter("Маша", PlayerAppearance.RED_HAIR, PlayerOutfit.SWEATER))
        }
        assertEquals(recordA, diskA.read())
        val restoredA = StoryViewModel(storeA()).apply { continueStory(jsonA) }
        assertEquals("end", restoredA.currentNode.value!!.id)
        assertEquals(playerA, restoredA.playerCharacter.value)
        val restoredB = StoryViewModel(storeB()).apply { continueStory(jsonB) }
        assertEquals("start", restoredB.currentNode.value!!.id)
        assertEquals("Маша", restoredB.playerCharacter.value!!.name)
    }

    @Test fun legacyFallbackBelongsOnlyToItsStoryAndIsNeverOverwritten() {
        val legacy = MemoryProgressStore()
        val old = Json.encodeToString(StoryProgress(story("a")))
        legacy.write(old)
        val destination = MemoryProgressStore()
        val a = StoryProgressStore("a", destination, legacy)
        val b = StoryProgressStore("b", MemoryProgressStore(), legacy)
        assertEquals(old, a.read())
        assertNull(b.read())
        val progressed = Json.encodeToString(StoryProgress(story("a"), steps = listOf(ProgressStep())))
        a.write(progressed)
        assertEquals(progressed, a.read())
        assertEquals(old, legacy.read())
        assertFailsWith<IllegalArgumentException> { b.write(progressed) }
    }

    @Test fun corruptDestinationIsNotSilentlyReplacedByLegacy() {
        val legacy = MemoryProgressStore().apply { write(Json.encodeToString(StoryProgress(story("a")))) }
        val destination = MemoryProgressStore().apply { write("broken") }
        val store = StoryProgressStore("a", destination, legacy)
        val vm = StoryViewModel(store)
        assertNotNull(vm.saveError.value)
        assertEquals("broken", store.read())
        assertFailsWith<ProgressException> { vm.continueStory(Json.encodeToString(story("a"))) }
        assertEquals("broken", destination.read())
    }

    @Test fun catalogRejectsDuplicateIdsUnsafePathsAndMismatchedStoryIds() {
        val entry = StoryCatalogEntry("a", "files/a.json")
        assertEquals("a", parseStoryCatalog(Json.encodeToString(StoryCatalog(listOf(entry)))).stories.single().id)
        listOf("../a", "a/b", "", "x".repeat(65)).forEach {
            assertFailsWith<IllegalArgumentException> { progressFileName(it) }
        }
        assertFailsWith<IllegalArgumentException> { parseStoryCatalog(Json.encodeToString(StoryCatalog(listOf(entry, entry)))) }
        listOf("../a.json", "files/../a.json", "files//a.json", "files/a.png").forEach { path ->
            assertFailsWith<IllegalArgumentException> {
                parseStoryCatalog(Json.encodeToString(StoryCatalog(listOf(entry.copy(resource = path)))))
            }
        }
        assertFailsWith<IllegalArgumentException> { entry.parseStory(Json.encodeToString(story("b"))) }
    }

    @Test fun playerOptionsMustBeNonemptyUniqueAndUseVersionThree() {
        val base = story("a").copy(schemaVersion = 3)
        listOf(
            PlayerOptions(appearances = emptyList()),
            PlayerOptions(outfits = emptyList()),
            PlayerOptions(appearances = listOf(PlayerAppearance.RED_HAIR, PlayerAppearance.RED_HAIR)),
            PlayerOptions(outfits = listOf(PlayerOutfit.COAT, PlayerOutfit.COAT))
        ).forEach { options ->
            assertFailsWith<StoryValidationException> { validateStory(base.copy(playerOptions = options)) }
        }
        val one = base.copy(playerOptions = PlayerOptions(listOf(PlayerAppearance.RED_HAIR), listOf(PlayerOutfit.COAT)))
        validateStory(one)
        assertFailsWith<StoryValidationException> { validateStory(one.copy(schemaVersion = 2)) }
    }
}
