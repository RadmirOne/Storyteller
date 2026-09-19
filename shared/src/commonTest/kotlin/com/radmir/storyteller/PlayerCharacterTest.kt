package com.radmir.storyteller

import com.radmir.storyteller.models.*
import com.radmir.storyteller.repository.MemoryProgressStore
import com.radmir.storyteller.viewmodel.StoryViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlayerCharacterTest {
    private fun script(id: String) = StoryScript(
        id = id,
        title = "История $id",
        schemaVersion = 2,
        characters = listOf(Character("narrator", "Рассказчик")),
        startSceneId = "start",
        startNodeId = "first",
        scenes = mapOf(
            "start" to Scene(
                id = "start",
                backgroundResource = "files/background.png",
                nodes = mapOf("first" to DialogueNode("first", "narrator", "Начало"))
            )
        )
    )

    @Test
    fun characterIsSavedAndRestoredForItsStory() {
        val store = MemoryProgressStore()
        val json = Json.encodeToString(script("lighthouse"))
        val character = PlayerCharacter(
            name = "Алекс",
            appearance = PlayerAppearance.RED_HAIR,
            outfit = PlayerOutfit.COAT
        )

        StoryViewModel(store).loadStory(json, character)
        val restored = StoryViewModel(store).apply { continueStory(json) }

        assertEquals(character, restored.playerCharacter.value)
    }

    @Test
    fun differentStoriesCanHaveDifferentCharacters() {
        val firstStore = MemoryProgressStore()
        val secondStore = MemoryProgressStore()
        val firstCharacter = PlayerCharacter("Мира", PlayerAppearance.LIGHT_HAIR, PlayerOutfit.SWEATER)
        val secondCharacter = PlayerCharacter("Ян", PlayerAppearance.DARK_HAIR, PlayerOutfit.JACKET)

        StoryViewModel(firstStore).loadStory(Json.encodeToString(script("first")), firstCharacter)
        StoryViewModel(secondStore).loadStory(Json.encodeToString(script("second")), secondCharacter)

        val restoredFirst = StoryViewModel(firstStore).apply {
            continueStory(Json.encodeToString(script("first")))
        }
        val restoredSecond = StoryViewModel(secondStore).apply {
            continueStory(Json.encodeToString(script("second")))
        }

        assertEquals(firstCharacter, restoredFirst.playerCharacter.value)
        assertEquals(secondCharacter, restoredSecond.playerCharacter.value)
    }

    @Test
    fun legacyProgressWithoutCharacterStillLoads() {
        val store = MemoryProgressStore()
        val story = script("legacy")
        store.write(Json.encodeToString(StoryProgress(story)))

        val restored = StoryViewModel(store).apply {
            continueStory(Json.encodeToString(story))
        }

        assertNull(restored.playerCharacter.value)
    }
}
