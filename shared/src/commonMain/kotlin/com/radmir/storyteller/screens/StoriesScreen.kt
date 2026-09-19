package com.radmir.storyteller.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radmir.storyteller.models.StoryScript
import com.radmir.storyteller.models.ProgressException
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
fun StoriesScreen(
    onStoryStarted: (String) -> Unit,
    onStoryContinued: (String) -> Unit,
    savedStory: StoryScript? = null,
    saveError: String? = null,
    hasPlayed: Boolean = false
) {
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var preview by remember { mutableStateOf<StoryScript?>(null) }
    var cover by remember { mutableStateOf<ImageBitmap?>(null) }
    val scope = rememberCoroutineScope()
    var confirmRestart by remember { mutableStateOf(false) }
    val canContinue = preview != null && preview == savedStory

    fun openStory(resume: Boolean) {
        isLoading = true
        error = null
        scope.launch {
            try {
                val json = withContext(Dispatchers.Default) {
                    Res.readBytes("files/story.json").decodeToString().also {
                        val script = StoryRepository().parseScript(it)
                        validateStoryResources(script) { path -> loadResourceImage(path) }
                    }
                }
                if (resume) onStoryContinued(json) else onStoryStarted(json)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error = storyLoadError(e)
            } finally {
                isLoading = false
            }
        }
    }

    if (confirmRestart) {
        AlertDialog(onDismissRequest = { confirmRestart = false },
            title = { Text("Начать заново?") },
            text = { Text("Текущее прохождение и журнал реплик будут заменены новой игрой.") },
            confirmButton = { TextButton(onClick = { confirmRestart = false; openStory(false) }) { Text("Начать заново") } },
            dismissButton = { TextButton(onClick = { confirmRestart = false }) { Text("Отмена") } })
    }

    LaunchedEffect(Unit) {
        try {
            val result = withContext(Dispatchers.Default) {
                val script = StoryRepository().parseScript(Res.readBytes("files/story.json").decodeToString())
                script to loadResourceImage(script.scenes.getValue(script.startSceneId).backgroundResource)
            }
            preview = result.first
            cover = result.second
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = storyLoadError(e)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        cover?.let {
            Image(it, contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(), alignment = Alignment.CenterEnd)
        }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
            0f to Color(0x33101B26), 0.35f to Color(0x22101B26),
            0.66f to Color(0xE6101B26), 1f to Color(0xFF0C1821)
        )))
        Text("STORYTELLER", color = Color(0xFFE3BB79), fontSize = 12.sp, letterSpacing = 4.sp,
            modifier = Modifier.align(Alignment.TopStart).padding(28.dp))
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(0.65f)
                .verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text("ВИЗУАЛЬНАЯ НОВЕЛЛА", color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(12.dp))
            Text(preview?.title ?: "Ваш следующий сюжет", fontFamily = FontFamily.Serif,
                fontSize = 38.sp, lineHeight = 42.sp, color = Color(0xFFF6F0E6))
            Spacer(Modifier.height(14.dp))
            preview?.description?.let {
                Text(it, color = Color(0xFFD0D7DB), fontSize = 15.sp, lineHeight = 22.sp)
                Spacer(Modifier.height(20.dp))
            }
            val endingCount = preview?.scenes?.values?.sumOf { scene ->
                scene.nodes.values.count { it.nextNodeId == null && it.choices.isNullOrEmpty() }
            }
            if (endingCount != null) {
                Surface(color = Color(0x332B4656), shape = RoundedCornerShape(50)) {
                    Text("Вариантов финала: $endingCount", color = Color(0xFFE0D8C8), fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
                }
                Spacer(Modifier.height(20.dp))
            }
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(12.dp))
            }
            saveError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(12.dp))
            }
            if (preview != null && savedStory != null && !canContinue) {
                Text("История обновилась. Прежнее сохранение несовместимо; начните новую игру.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(12.dp))
            }
            Button(
                enabled = !isLoading,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (canContinue) openStory(true)
                    else if (savedStory != null || hasPlayed) confirmRestart = true
                    else openStory(false)
                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(12.dp))
                }
                Text(when {
                    isLoading -> "Открываем историю…"
                    canContinue -> "Продолжить  →"
                    error != null -> "Повторить загрузку"
                    hasPlayed -> "Начать заново  →"
                    else -> "Начать историю  →"
                })
            }
            if (canContinue) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { confirmRestart = true }, enabled = !isLoading,
                    shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("Начать заново")
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("Читайте. Выбирайте. Проживите свою историю.", color = Color(0xFF93A5B1),
                fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

private fun storyLoadError(error: Exception): String = when (error) {
    is ProgressException -> error.message ?: "Не удалось восстановить прохождение."
    is StoryValidationException -> "Ошибка сценария:\n${error.message}"
    is SerializationException -> "Не удалось прочитать сценарий. Проверьте JSON и поля формата версии 1 или 2."
    else -> "Не удалось открыть историю. Попробуйте ещё раз."
}
