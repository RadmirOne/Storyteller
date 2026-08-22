package com.radmir.storyteller.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.radmir.storyteller.models.StoryScript
import com.radmir.storyteller.repository.*
import com.radmir.storyteller.screens.scene.loadResourceImage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import storyteller.shared.generated.resources.Res

private data class CatalogCard(val entry: StoryCatalogEntry, val story: StoryScript? = null,
    val cover: ImageBitmap? = null, val error: String? = null)

@Composable
fun StoryCatalogScreen(onSelect: (StoryCatalogEntry) -> Unit, onSettings: () -> Unit) {
    var cards by remember { mutableStateOf<List<CatalogCard>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var revision by remember { mutableStateOf(0) }
    LaunchedEffect(revision) {
        error = null
        cards = null
        try {
            cards = withContext(Dispatchers.Default) {
                parseStoryCatalog(Res.readBytes("files/catalog.json").decodeToString()).stories.map { entry ->
                    try {
                        val script = entry.parseStory(Res.readBytes(entry.resource).decodeToString())
                        CatalogCard(entry, script, loadResourceImage(script.scenes.getValue(script.startSceneId).backgroundResource))
                    }
                    catch (e: CancellationException) { throw e }
                    catch (e: Exception) { CatalogCard(entry, error = e.message ?: "Не удалось прочитать историю.") }
                }
            }
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { error = e.message ?: "Не удалось открыть каталог." }
    }
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Истории", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
            TextButton(onClick = onSettings) { Text("Настройки") }
        }
        Text("Выберите, куда отправиться сегодня", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp))
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Button(onClick = { revision++ }) { Text("Повторить загрузку") }
        }
        if (cards == null && error == null) CircularProgressIndicator()
        LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.weight(1f)) {
            items(cards.orEmpty(), key = { it.entry.id }) { card ->
                val story = card.story
                if (story == null) {
                    OutlinedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(card.entry.id, style = MaterialTheme.typography.titleMedium)
                            Text(card.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                            TextButton(onClick = { revision++ }) { Text("Повторить") }
                        }
                    }
                } else {
                    Card(onClick = { onSelect(card.entry) }, modifier = Modifier.fillMaxWidth()) {
                        card.cover?.let { Image(it, contentDescription = null, contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(150.dp)) }
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(story.title, style = MaterialTheme.typography.titleLarge)
                            story.description?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                            Text("Открыть историю →", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
