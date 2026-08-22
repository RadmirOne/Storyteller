package com.radmir.storyteller

import com.radmir.storyteller.models.PlayerAppearance
import com.radmir.storyteller.models.PlayerOptions
import com.radmir.storyteller.models.PlayerOutfit
import com.radmir.storyteller.models.StoryScript
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerOptionsTest {
    @Test fun olderStoriesKeepAllProtagonistVariants() {
        val story = Json.decodeFromString<StoryScript>(
            """{"id":"legacy","title":"Legacy","startSceneId":"scene","startNodeId":"node"}"""
        )
        assertEquals(PlayerAppearance.entries.toList(), story.playerOptions.appearances)
        assertEquals(PlayerOutfit.entries.toList(), story.playerOptions.outfits)
    }

    @Test fun customVariantOrderAndUnequalListSizesSurviveSerialization() {
        val story = StoryScript(
            id = "custom", title = "Custom", startSceneId = "scene", startNodeId = "node",
            schemaVersion = 3,
            playerOptions = PlayerOptions(
                appearances = listOf(PlayerAppearance.RED_HAIR, PlayerAppearance.DARK_HAIR),
                outfits = listOf(PlayerOutfit.COAT),
            ),
        )
        assertEquals(story, Json.decodeFromString<StoryScript>(Json.encodeToString(story)))
    }
}
