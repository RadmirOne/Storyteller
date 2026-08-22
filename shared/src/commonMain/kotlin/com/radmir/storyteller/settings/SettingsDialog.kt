package com.radmir.storyteller.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun SettingsDialog(
    settings: ReadingSettings,
    onSettingsChange: (ReadingSettings) -> Unit,
    onDismiss: () -> Unit,
    error: String? = null,
) {
    // Sliders preview locally and write once on release, avoiding disk writes during dragging.
    var textScale by remember(settings.textScale) { mutableStateOf(settings.textScale) }
    var volume by remember(settings.volume) { mutableStateOf(settings.volume) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Настройки чтения") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Размер текста: ${(textScale * 100).roundToInt()}%")
                Slider(
                    value = textScale,
                    onValueChange = { textScale = it },
                    valueRange = 0.85f..1.5f,
                    onValueChangeFinished = { onSettingsChange(settings.copy(textScale = textScale)) },
                )
                Text("Вдалеке мерцал огонь маяка.", fontSize = (18f * textScale).sp)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Уменьшить анимации", Modifier.weight(1f))
                    Switch(settings.reduceMotion, { onSettingsChange(settings.copy(reduceMotion = it)) })
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Звук", Modifier.weight(1f))
                    Switch(settings.soundEnabled, { onSettingsChange(settings.copy(soundEnabled = it)) })
                }
                Text("Громкость: ${(volume * 100).roundToInt()}%")
                Slider(
                    value = volume,
                    onValueChange = { volume = it },
                    enabled = settings.soundEnabled,
                    onValueChangeFinished = { onSettingsChange(settings.copy(volume = volume)) },
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Готово") } },
    )
}
