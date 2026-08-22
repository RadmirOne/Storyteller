package com.radmir.storyteller.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.radmir.storyteller.screens.scene.DialogueCharacter
import com.radmir.storyteller.screens.scene.DialogueOverlay
import com.radmir.storyteller.screens.scene.SceneBackground
import com.radmir.storyteller.viewmodel.StoryViewModel
import com.radmir.storyteller.models.PLAYER_CHARACTER_ID
import com.radmir.storyteller.models.StagePosition
import com.radmir.storyteller.models.speakerName

@Composable
fun StoryScreen(viewModel: StoryViewModel = viewModel(), onReturnToMenu: () -> Unit) {
    val gameState by viewModel.gameState.collectAsState()
    val currentNode by viewModel.currentNode.collectAsState()
    val currentScript by viewModel.currentScript.collectAsState()
    val sceneState by viewModel.sceneUiState.collectAsState()
    val playerCharacter by viewModel.playerCharacter.collectAsState()

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

    val speakerName = script.speakerName(current.characterId, playerCharacter)
    val speakingPlayer = playerCharacter.takeIf { current.characterId == PLAYER_CHARACTER_ID }
    val displayedScene = scene.copy(stage = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Stable viewport: dialogue length must not resize the camera and actors.
        Box(modifier = Modifier.weight(0.62f).fillMaxWidth().clipToBounds()) {
            SceneBackground(state = displayedScene, characters = script.characters)
            val position = scene.stage.find { it.characterId == current.characterId && it.worldX == null }?.position
                ?: if (speakingPlayer != null) StagePosition.RIGHT else StagePosition.LEFT
            key(script.id, scene.scene.id, current.characterId, position) {
                DialogueCharacter(
                    speakerName = speakerName,
                    spriteResource = script.characters.find { it.id == current.characterId }?.spriteResource,
                    player = speakingPlayer,
                    position = position,
                    appearance = script.characterAppearance,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Box(modifier = Modifier.weight(0.38f).fillMaxWidth()) {
            key(scene.scene.id, current.id) {
                DialogueOverlay(
                    speakerName = speakerName,
                    text = current.text,
                    choices = current.choices,
                    hasNext = current.nextNodeId != null,
                    onSelectChoice = { viewModel.selectChoice(it) },
                    onAdvance = { viewModel.advance() },
                    onReturnToMenu = onReturnToMenu
                )
            }
        }
    }
}
