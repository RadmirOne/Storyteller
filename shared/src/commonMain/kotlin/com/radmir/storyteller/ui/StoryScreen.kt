package com.radmir.storyteller.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.radmir.storyteller.viewmodel.StoryViewModel

@Composable
fun StoryScreen(viewModel: StoryViewModel = viewModel()) {
    val gameState by viewModel.gameState.collectAsState()
    val currentNode by viewModel.currentNode.collectAsState()

    // Use a local variable to facilitate smart casting
    val current = currentNode

    if (gameState == null || current == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text("Загрузка...")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = current.speaker,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = current.text,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (current.choices != null && current.choices.isNotEmpty()) {
            current.choices.forEach { choice ->
                Button(
                    onClick = { viewModel.selectChoice(choice.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(choice.text)
                }
            }
        } else if (current.nextNodeId != null) {
            Button(
                onClick = { viewModel.advance() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Далее")
            }
        } else {
            Text(
                text = "Конец истории",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}