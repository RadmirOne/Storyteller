package com.radmir.storyteller.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.radmir.storyteller.screens.scene.CharacterSprite
import com.radmir.storyteller.screens.scene.DialogueOverlay
import com.radmir.storyteller.screens.scene.SceneBackground
import com.radmir.storyteller.viewmodel.StoryViewModel

@Composable
fun StoryScreen(viewModel: StoryViewModel = viewModel()) {
    val gameState by viewModel.gameState.collectAsState()
    val currentNode by viewModel.currentNode.collectAsState()
    val currentScript by viewModel.currentScript.collectAsState()
    val sceneState by viewModel.sceneUiState.collectAsState()

    // Use local variables to facilitate smart casting and avoid issues with delegated properties
    val current = currentNode
    val script = currentScript
    val scene = sceneState

    if (gameState == null || current == null || script == null || scene == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("Загрузка...", color = Color.White)
        }
        return
    }

    val character = script.characters.find { it.id == current.characterId }
    val speakerName = character?.name ?: "Неизвестный"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        SceneBackground(state = scene)
        scene.stage.forEach { stageCharacter ->
            key(stageCharacter.characterId) {
                val sprite = script.characters.find { it.id == stageCharacter.characterId }
                CharacterSprite(
                    stage = stageCharacter,
                    spriteResource = sprite?.spriteResource
                )
            }
        }
        key(scene.scene.id, current.id) {
            DialogueOverlay(
                speakerName = speakerName,
                text = current.text,
                choices = current.choices,
                hasNext = current.nextNodeId != null,
                onSelectChoice = { viewModel.selectChoice(it) },
                onAdvance = { viewModel.advance() }
            )
        }
    }
}
