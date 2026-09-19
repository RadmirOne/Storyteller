package com.radmir.storyteller

import com.radmir.storyteller.engine.StoryEngine
import com.radmir.storyteller.repository.StoryRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith
import com.radmir.storyteller.repository.StoryValidationException

class StoryEngineTest {

    private val scriptJson = """
        {
          "id": "test_story",
          "title": "Тест",
          "characters": [ { "id": "narrator", "name": "Рассказчик" } ],
          "startSceneId": "a",
          "startNodeId": "a1",
          "scenes": {
            "a": {
              "id": "a",
              "backgroundResource": "files/scenes/a.png",
              "nodes": {
                "a1": {
                  "id": "a1",
                  "characterId": "narrator",
                  "text": "a1",
                  "choices": [
                    { "id": "to_b", "text": "В сцену b", "targetSceneId": "b", "targetNodeId": "b1" },
                    { "id": "stay", "text": "Остаться", "targetNodeId": "a2" }
                  ]
                },
                "a2": {
                  "id": "a2",
                  "characterId": "narrator",
                  "text": "a2",
                  "nextNodeId": "a3"
                },
                "a3": {
                  "id": "a3",
                  "characterId": "narrator",
                  "text": "a3"
                }
              }
            },
            "b": {
              "id": "b",
              "backgroundResource": "files/scenes/b.png",
              "nodes": {
                "b1": {
                  "id": "b1",
                  "characterId": "narrator",
                  "text": "b1",
                  "nextSceneId": "a",
                  "nextNodeId": "a3"
                }
              }
            }
          }
        }
    """.trimIndent()

    private fun newEngine(): StoryEngine = StoryEngine(StoryRepository()).apply {
        initialize(scriptJson)
    }

    @Test
    fun initializesAtStartSceneAndNode() {
        val engine = newEngine()
        val state = engine.getGameState()!!

        assertEquals("a", state.currentSceneId)
        assertEquals("a1", state.currentNodeId)
        assertEquals("a", engine.getCurrentScene()?.id)
        assertEquals("a1", engine.getCurrentNode()?.id)
        assertTrue(state.visitedNodes.isEmpty())
    }

    @Test
    fun selectChoiceWithTargetSceneChangesSceneAndNode() {
        val engine = newEngine()

        val state = engine.selectChoice("to_b")!!

        assertEquals("b", state.currentSceneId)
        assertEquals("b1", state.currentNodeId)
        assertEquals(setOf("a/a1"), state.visitedNodes)
        assertEquals("b1", engine.getCurrentNode()?.id)
    }

    @Test
    fun selectChoiceWithoutTargetSceneStaysInScene() {
        val engine = newEngine()

        val state = engine.selectChoice("stay")!!

        assertEquals("a", state.currentSceneId)
        assertEquals("a2", state.currentNodeId)
        assertEquals(setOf("a/a1"), state.visitedNodes)
    }

    @Test
    fun advanceWithoutSceneStaysInScene() {
        val engine = newEngine()
        engine.selectChoice("stay")!!

        val state = engine.advance()!!

        assertEquals("a", state.currentSceneId)
        assertEquals("a3", state.currentNodeId)
        assertEquals(setOf("a/a1", "a/a2"), state.visitedNodes)
    }

    @Test
    fun advanceWithNextSceneChangesScene() {
        val engine = newEngine()
        engine.selectChoice("to_b")!!

        val state = engine.advance()!!

        assertEquals("a", state.currentSceneId)
        assertEquals("a3", state.currentNodeId)
        assertEquals(setOf("a/a1", "b/b1"), state.visitedNodes)
    }

    @Test
    fun advanceAtEndNodeDoesNotChangeState() {
        val engine = newEngine()
        engine.selectChoice("stay")!!
        engine.advance()!!

        val state = engine.advance()!!

        assertEquals("a3", state.currentNodeId)
        assertFalse("a/a3" in state.visitedNodes)
    }

    @Test
    fun unknownChoiceReturnsNullAndKeepsState() {
        val engine = newEngine()

        val state = engine.selectChoice("nope")

        assertEquals(null, state)
        assertEquals("a1", engine.getGameState()!!.currentNodeId)
    }

    @Test
    fun invalidTargetNodeIsRejectedBeforeStarting() {
        val invalidJson = """
            {
              "id": "bad",
              "title": "Плохой граф",
              "characters": [ { "id": "narrator", "name": "Рассказчик" } ],
              "startSceneId": "a",
              "startNodeId": "a1",
              "scenes": {
                "a": {
                  "id": "a",
                  "backgroundResource": "files/scenes/a.png",
                  "nodes": {
                    "a1": {
                      "id": "a1",
                      "characterId": "narrator",
                      "text": "a1",
                      "choices": [
                        { "id": "broken", "text": "В никуда", "targetSceneId": "b", "targetNodeId": "missing" }
                      ]
                    }
                  }
                },
                "b": {
                  "id": "b",
                  "backgroundResource": "files/scenes/b.png",
                  "nodes": {}
                }
              }
            }
        """.trimIndent()
        val engine = newEngine()
        assertFailsWith<StoryValidationException> { engine.initialize(invalidJson) }
        assertEquals("a1", engine.getGameState()!!.currentNodeId)
        assertEquals("a1", engine.getCurrentNode()!!.id)
    }
}
