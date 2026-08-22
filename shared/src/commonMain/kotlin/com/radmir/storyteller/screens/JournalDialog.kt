package com.radmir.storyteller.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.radmir.storyteller.models.JournalEntry

@Composable
fun JournalDialog(entries: List<JournalEntry>, onClose: () -> Unit) {
    // The journal is a read-only snapshot; opening it never advances or rewinds the story.
    val pages = remember { entries.toList() }
    val scroll = rememberLazyListState(initialFirstVisibleItemIndex = (pages.size - 1).coerceAtLeast(0))
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().safeContentPadding()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Прочитанное", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    TextButton(onClick = onClose) { Text("Закрыть") }
                }
                HorizontalDivider()
                if (pages.isEmpty()) {
                    Text("Здесь появятся прочитанные реплики.", modifier = Modifier.padding(24.dp))
                }
                LazyColumn(state = scroll, contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.weight(1f)) {
                    itemsIndexed(pages) { _, entry ->
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(entry.speaker, color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge)
                            Text(if (entry.isChoice) "→ ${entry.text}" else entry.text,
                                style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}
