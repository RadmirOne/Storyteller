package com.radmir.storyteller

import com.radmir.storyteller.models.CameraView
import com.radmir.storyteller.models.Scene
import com.radmir.storyteller.models.SceneStartEffect
import com.radmir.storyteller.models.StageCharacter
import com.radmir.storyteller.models.StagePosition
import com.radmir.storyteller.models.StoryScript
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SceneModelsTest {
    private val json = Json

    private val scriptJson = """
        {
          "id": "forest_mystery",
          "title": "Таинственный лес",
          "characters": [
            { "id": "narrator", "name": "Рассказчик" },
            { "id": "old_man", "name": "Старик", "spriteResource": "files/characters/old_man.png" }
          ],
          "startSceneId": "forest",
          "startNodeId": "start",
          "scenes": {
            "forest": {
              "id": "forest",
              "backgroundResource": "files/scenes/forest.png",
              "nodes": {
                "start": {
                  "id": "start",
                  "characterId": "narrator",
                  "text": "Вы просыпаетесь в темном лесу.",
                  "cameraStart": { "focusX": 0.25 },
                  "camera": { "focusX": 0.75 },
                  "cameraDurationMs": 4000,
                  "choices": [
                    {
                      "id": "choice_left",
                      "text": "Пойти налево на звук воды.",
                      "targetSceneId": "river",
                      "targetNodeId": "river_bank",
                      "targetSceneStartEffect": "FADE"
                    }
                  ]
                }
              }
            },
            "river": {
              "id": "river",
              "backgroundResource": "files/scenes/river.png",
              "nodes": {
                "river_bank": {
                  "id": "river_bank",
                  "characterId": "old_man",
                  "text": "Вы выходите к бурной реке.",
                  "stageCharacters": [ { "characterId": "old_man", "position": "CENTER" } ],
                  "nextSceneId": "ending",
                  "nextNodeId": "game_over",
                  "nextSceneStartEffect": "FADE"
                }
              }
            },
            "ending": {
              "id": "ending",
              "backgroundResource": "files/scenes/ending.png",
              "nodes": {
                "game_over": {
                  "id": "game_over",
                  "characterId": "narrator",
                  "text": "Конец.",
                  "choices": []
                }
              }
            }
          }
        }
    """.trimIndent()

    @Test
    fun decodesScriptWithScenesAndNodes() {
        val script = json.decodeFromString<StoryScript>(scriptJson)

        assertEquals("forest_mystery", script.id)
        assertEquals("forest", script.startSceneId)
        assertEquals("start", script.startNodeId)
        assertEquals(setOf("forest", "river", "ending"), script.scenes.keys)
        val startNode = script.scenes.getValue("forest").nodes.getValue("start")
        assertEquals("narrator", startNode.characterId)
        assertTrue(startNode.choices!!.any { it.id == "choice_left" })
    }

    @Test
    fun decodesCameraAndStageCharacters() {
        val script = json.decodeFromString<StoryScript>(scriptJson)
        val startNode = script.scenes.getValue("forest").nodes.getValue("start")

        assertEquals(CameraView(0.75f), startNode.camera)
        assertEquals(CameraView(0.25f), startNode.cameraStart)
        assertEquals(4000L, startNode.cameraDurationMs)

        val riverNode = script.scenes.getValue("river").nodes.getValue("river_bank")
        assertEquals(
            listOf(StageCharacter("old_man", StagePosition.CENTER)),
            riverNode.stageCharacters
        )
        assertEquals(SceneStartEffect.FADE, riverNode.nextSceneStartEffect)
        assertEquals("ending", riverNode.nextSceneId)
    }

    @Test
    fun defaultsAreApplied() {
        val script = json.decodeFromString<StoryScript>(scriptJson)
        val choice = script.scenes.getValue("forest").nodes.getValue("start").choices!!.first()
        assertEquals("river", choice.targetSceneId)
        assertEquals(SceneStartEffect.FADE, choice.targetSceneStartEffect)

        val gameOver = script.scenes.getValue("ending").nodes.getValue("game_over")
        assertEquals(null, gameOver.camera)
        assertEquals(null, gameOver.stageCharacters)
        assertEquals(null, gameOver.nextSceneStartEffect)

        val emptyScene = Scene(id = "empty", backgroundResource = "files/scenes/x.png")
        assertTrue(emptyScene.nodes.isEmpty())
        assertEquals(CameraView(), CameraView(0.5f))
    }
}
