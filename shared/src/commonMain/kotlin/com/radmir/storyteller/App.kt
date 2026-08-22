package com.radmir.storyteller

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.radmir.storyteller.screens.CharacterCreationScreen
import com.radmir.storyteller.screens.StoriesScreen
import com.radmir.storyteller.screens.StoryScreen
import com.radmir.storyteller.screens.JournalDialog
import com.radmir.storyteller.repository.rememberProgressStore
import com.radmir.storyteller.viewmodel.StoryViewModel
import com.radmir.storyteller.repository.StoryCatalogEntry
import com.radmir.storyteller.repository.StoryRepository
import com.radmir.storyteller.screens.StoryCatalogScreen
import com.radmir.storyteller.settings.rememberReadingSettingsController
import com.radmir.storyteller.settings.SettingsDialog
import com.radmir.storyteller.audio.StoryAudio
import com.radmir.storyteller.audio.AudioSettings

@Composable
@Preview
fun App(debugToolsEnabled: Boolean = false) {
    MaterialTheme(colorScheme = darkColorScheme(
        primary = Color(0xFFE3BB79),
        onPrimary = Color(0xFF19252C),
        background = Color(0xFF0C1821),
        surface = Color(0xFF10212C),
        primaryContainer = Color(0xFF0C1821)
    )) {
        var selectedEntry by remember { mutableStateOf<StoryCatalogEntry?>(null) }
        var showStory by remember(selectedEntry) { mutableStateOf(false) }
        var characterSetupStory by remember { mutableStateOf<String?>(null) }
        var showJournal by remember { mutableStateOf(false) }
        var showDebug by remember { mutableStateOf(false) }
        var showSettings by remember { mutableStateOf(false) }
        val settingsController = rememberReadingSettingsController()
        val settings = settingsController.settings
        var audioError by remember(selectedEntry, showStory) { mutableStateOf<String?>(null) }

        val progressStore = rememberProgressStore(selectedEntry?.id)
        val storyViewModel = remember(progressStore, debugToolsEnabled) { StoryViewModel(progressStore, debugToolsEnabled) }
        val debugNavigation by storyViewModel.debugNavigation.collectAsState()
        val gameState by storyViewModel.gameState.collectAsState()
        val playbackRevision by storyViewModel.playbackRevision.collectAsState()
        val savedStory by storyViewModel.savedStory.collectAsState()
        val saveError by storyViewModel.saveError.collectAsState()
        val journal by storyViewModel.journal.collectAsState()
        val sceneUiState by storyViewModel.sceneUiState.collectAsState()
        StoryAudio(if (showStory) sceneUiState else null,
            AudioSettings(settings.soundEnabled, settings.volume, settings.volume),
            onError = { audioError = it })
        if (showSettings) SettingsDialog(settings, { settingsController.update(it) },
            onDismiss = { showSettings = false }, error = settingsController.error)
        if (showJournal) JournalDialog(journal, onClose = { showJournal = false }, textScale = settings.textScale)
        if (debugToolsEnabled && showDebug && showStory) {
            AlertDialog(onDismissRequest = { showDebug = false },
                title = { Text("Отладка прохождения") },
                text = {
                    Column {
                        Text("${gameState?.currentSceneId} / ${gameState?.currentNodeId}")
                        TextButton(onClick = { storyViewModel.debugPreviousNode() }, enabled = debugNavigation.canGoBack) {
                            Text("← Предыдущий узел")
                        }
                        TextButton(onClick = { storyViewModel.debugUndoChoice() }, enabled = debugNavigation.canUndoChoice) {
                            Text("Отменить последний выбор")
                        }
                        TextButton(onClick = { storyViewModel.debugPreviousScene() }, enabled = debugNavigation.canGoToPreviousScene) {
                            Text("← Предыдущая сцена")
                        }
                        Text("Возврат обновляет журнал и автосохранение.", style = MaterialTheme.typography.bodySmall)
                    }
                }, confirmButton = { TextButton(onClick = { showDebug = false }) { Text("Закрыть") } })
        }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when {
                showStory -> {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { showStory = false }) { Text("← Меню") }
                        Text(storyViewModel.currentScript.value?.title.orEmpty(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        TextButton(onClick = { showJournal = true }) { Text("Журнал") }
                        TextButton(onClick = { showSettings = true }, modifier = Modifier.semantics {
                            contentDescription = "Настройки чтения"
                        }) { Text("⋮") }
                        if (debugToolsEnabled) TextButton(onClick = { showDebug = true }) { Text("Debug") }
                    }
                    saveError?.let {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(it, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                            TextButton(onClick = { storyViewModel.saveProgress() }) { Text("Повторить") }
                        }
                    }
                    audioError?.let {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                            Text(it, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error)
                            TextButton(onClick = { audioError = null }) { Text("Скрыть") }
                        }
                    }
                    key(playbackRevision) {
                        StoryScreen(viewModel = storyViewModel, onReturnToMenu = { showStory = false }, settings = settings)
                    }
                }
                characterSetupStory != null -> {
                    val setupScript = remember(characterSetupStory) { StoryRepository().parseScript(characterSetupStory!!) }
                    CharacterCreationScreen(
                        options = setupScript.playerOptions,
                        backgroundResource = setupScript.scenes.getValue(setupScript.startSceneId).backgroundResource,
                        reduceMotion = settings.reduceMotion,
                        onBack = { characterSetupStory = null },
                        onCharacterCreated = { character ->
                            val json = characterSetupStory ?: return@CharacterCreationScreen
                            storyViewModel.loadStory(json, character)
                            characterSetupStory = null
                            showStory = true
                        }
                    )
                }
                selectedEntry != null -> key(selectedEntry!!.id) {
                    StoriesScreen(savedStory = savedStory, saveError = saveError,
                        entry = selectedEntry!!, onBackToCatalog = { selectedEntry = null },
                        hasPlayed = storyViewModel.currentScript.value != null, onStoryContinued = { json ->
                            storyViewModel.continueStory(json)
                            showStory = true
                        }, onStoryStarted = { json ->
                            characterSetupStory = json
                    })
                }
                else -> StoryCatalogScreen(onSelect = { selectedEntry = it }, onSettings = { showSettings = true })
            }
        }
    }
}
