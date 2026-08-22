package com.radmir.storyteller

import com.radmir.storyteller.models.*
import com.radmir.storyteller.repository.MemoryProgressStore
import com.radmir.storyteller.viewmodel.StoryViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

class DialoguePersonalizationTest {
    private val player = PlayerCharacter("Алиса", PlayerAppearance.RED_HAIR, PlayerOutfit.COAT)
    private val story = StoryScript(
        id = "personal", title = "Персонализация",
        characters = listOf(Character("protagonist", "Вы"), Character("mira", "Мира")),
        startSceneId = "s", startNodeId = "hello",
        scenes = mapOf("s" to Scene(id = "s", backgroundResource = "files/bg.png", nodes = mapOf(
            "hello" to DialogueNode("hello", "mira", "Привет, {{playerName}}!", choices = listOf(
                Choice("answer", "Я — {{playerName}}", "reply")
            )),
            "reply" to DialogueNode("reply", "protagonist", "Меня зовут {{playerName}}.")
        )))
    )

    @Test fun dialogueChoicesJournalAndResumeShareTheSameName() {
        val json = Json.encodeToString(story)
        val store = MemoryProgressStore()
        val vm = StoryViewModel(store, debugToolsEnabled = true).apply { loadStory(json, player) }
        assertEquals("Привет, Алиса!", vm.currentNode.value!!.text)
        assertEquals("Я — Алиса", vm.currentNode.value!!.choices!!.single().text)
        vm.selectChoice("answer")
        assertEquals(listOf("Мира", "Ваш выбор", "Алиса"), vm.journal.value.map { it.speaker })
        assertEquals("Меня зовут Алиса.", vm.journal.value.last().text)
        assertEquals("Я — Алиса", vm.journal.value[1].text)
        val restored = StoryViewModel(store).apply { continueStory(json) }
        assertEquals(vm.journal.value, restored.journal.value)
        assertEquals(player, restored.playerCharacter.value)
        assertEquals(story, restored.currentScript.value) // Keep templates in the saved script.
        vm.debugPreviousNode()
        assertEquals("Привет, Алиса!", vm.currentNode.value!!.text)
    }

    @Test fun legacyStoriesAndNpcNamesRemainUsable() {
        assertEquals("Вы", story.speakerName("protagonist", null))
        assertEquals("Мира", story.speakerName("mira", player))
        assertEquals("Привет, Вы!", story.personalize("Привет, {{playerName}}!", null))
        assertEquals("Без шаблона", story.personalize("Без шаблона", player))
    }

    @Test fun namesAreInsertedLiterallyWithoutRecursiveExpansion() {
        val special = player.copy(name = "\$1 {{playerName}}")
        assertEquals("Привет, \$1 {{playerName}}!", story.personalize("Привет, {{playerName}}!", special))
    }
}
