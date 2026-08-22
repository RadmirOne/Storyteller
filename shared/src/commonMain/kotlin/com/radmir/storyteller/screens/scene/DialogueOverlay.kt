package com.radmir.storyteller.screens.scene

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.radmir.storyteller.models.Choice
import com.radmir.storyteller.models.availability

@Composable
fun BoxScope.DialogueOverlay(
    speakerName: String,
    text: String,
    choices: List<Choice>?,
    variables: Map<String, Int>,
    hasNext: Boolean,
    onSelectChoice: (String) -> Unit,
    onAdvance: () -> Unit,
    onReturnToMenu: () -> Unit,
    textScale: Float = 1f
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = speakerName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize * textScale,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * textScale),
                    color = Color(0xFFF0EDE7)
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (!choices.isNullOrEmpty()) {
                    choices.forEach { choice ->
                        val availability = choice.availability(variables)
                        if (!availability.visible) return@forEach
                        Button(
                            onClick = { onSelectChoice(choice.id) },
                            enabled = availability.available,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(choice.text, style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize * textScale,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * textScale))
                        }
                        availability.reason?.let { reason ->
                            Text(reason, style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = MaterialTheme.typography.bodySmall.fontSize * textScale,
                                lineHeight = MaterialTheme.typography.bodySmall.lineHeight * textScale),
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            if (!choices.isNullOrEmpty() && choices.none { it.availability(variables).available }) {
                Text("Нет доступных вариантов продолжения.", style = MaterialTheme.typography.bodyMedium)
                Button(onClick = onReturnToMenu, modifier = Modifier.fillMaxWidth()) {
                    Text("Вернуться в меню")
                }
            } else if (choices.isNullOrEmpty() && hasNext) {
                Button(
                    onClick = onAdvance,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Далее")
                }
            } else if (choices.isNullOrEmpty()) {
                Text(
                    text = "Конец истории",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Button(onClick = onReturnToMenu, modifier = Modifier.fillMaxWidth()) {
                    Text("Вернуться в меню")
                }
            }
        }
    }
}
