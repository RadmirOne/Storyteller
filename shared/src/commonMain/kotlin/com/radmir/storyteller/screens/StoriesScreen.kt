package com.radmir.storyteller.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.radmir.storyteller.repository.StoryRepository
import com.radmir.storyteller.repository.StoryValidationException
import com.radmir.storyteller.repository.validateStoryResources
import com.radmir.storyteller.screens.scene.loadResourceImage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import storyteller.shared.generated.resources.Res

@Composable
fun StoriesScreen(onStoryStarted: (String) -> Unit) {
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Истории", style = MaterialTheme.typography.headlineMedium)
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                isLoading = true
                error = null
                scope.launch {
                    try {
                        val jsonString = withContext(Dispatchers.Default) {
                            Res.readBytes("files/story.json").decodeToString().also {
                                val script = StoryRepository().parseScript(it)
                                validateStoryResources(script) { path -> loadResourceImage(path) }
                            }
                        }
                        onStoryStarted(jsonString)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: StoryValidationException) {
                        error = "Ошибка сценария:\n${e.message}"
                    } catch (e: SerializationException) {
                        error = "Не удалось прочитать сценарий. Проверьте JSON и поля формата версии 1."
                    } catch (e: Exception) {
                        error = "Не удалось открыть историю. Попробуйте ещё раз."
                    } finally {
                        isLoading = false
                    }
                }
            }) {
                Text(if (error == null) "Начать историю" else "Повторить")
            }
        }
    }
}
