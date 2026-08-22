package com.radmir.storyteller

import com.radmir.storyteller.audio.SceneAudio
import com.radmir.storyteller.models.*
import com.radmir.storyteller.repository.*
import kotlin.test.*

class StoryAnalysisTest {
    @Test fun graphCyclesCrossSceneLinksAndConditionalWarningsAreAnalyzedWithoutExecutingEffects() {
        val story = StoryScript("analysis", "Analysis", startSceneId = "a", startNodeId = "start",
            characters = listOf(Character("n", "N", "files/n.png")), scenes = mapOf(
                "a" to Scene("a", "files/bg.png", mapOf(
                    "start" to DialogueNode("start", "n", "", choices = listOf(
                        Choice("loop", "", "start", conditions = mapOf("trust" to 1)),
                        Choice("exit", "", "end", "b", conditions = mapOf("trust" to 5))
                    )),
                    "unused" to DialogueNode("unused", "n", "")
                ), audio = SceneAudio("files/audio/music.wav", "files/audio/waves.wav")),
                "b" to Scene("b", "files/bg.png", mapOf("end" to DialogueNode("end", "n", "")))
            ))
        val report = analyzeStory(story)
        assertEquals(3, report.nodeCount)
        assertEquals(setOf(StoryLocation("a", "unused")), report.unreachableNodes)
        assertEquals(2, report.endingCount)
        assertEquals(setOf(StoryLocation("a", "start")), report.conditionalChoiceNodes)
        assertEquals(setOf("files/n.png", "files/bg.png"), report.imageResources)
        assertEquals(setOf("files/audio/music.wav", "files/audio/waves.wav"), report.audioResources)
        assertEquals(4, report.resources.size)
    }

    @Test fun unconditionalFallbackAvoidsWarningAndLinearTransitionFindsEnding() {
        val story = StoryScript("linear", "Linear", startSceneId = "s", startNodeId = "a", scenes = mapOf(
            "s" to Scene("s", "files/bg.png", mapOf(
                "a" to DialogueNode("a", "n", "", nextNodeId = "b"),
                "b" to DialogueNode("b", "n", "", choices = listOf(
                    Choice("locked", "", "c", conditions = mapOf("x" to 1)), Choice("fallback", "", "c"))),
                "c" to DialogueNode("c", "n", "")
            ))))
        val report = analyzeStory(story)
        assertTrue(report.unreachableNodes.isEmpty())
        assertTrue(report.conditionalChoiceNodes.isEmpty())
        assertEquals(setOf(StoryLocation("s", "c")), report.endings)
    }
}
