package com.radmir.storyteller.screens.scene

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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

@Composable
fun BoxScope.DialogueOverlay(
    speakerName: String,
    text: String,
    choices: List<Choice>?,
    hasNext: Boolean,
    onSelectChoice: (String) -> Unit,
    onAdvance: () -> Unit
) {
    Surface(
        color = Color.Black.copy(alpha = 0.6f),
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = speakerName,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (!choices.isNullOrEmpty()) {
                choices.forEach { choice ->
                    Button(
                        onClick = { onSelectChoice(choice.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(choice.text)
                    }
                }
            } else if (hasNext) {
                Button(
                    onClick = onAdvance,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Далее")
                }
            } else {
                Text(
                    text = "Конец истории",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}
