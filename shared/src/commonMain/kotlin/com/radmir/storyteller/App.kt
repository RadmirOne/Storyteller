package com.radmir.storyteller

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import storyteller.shared.generated.resources.Res
import storyteller.shared.generated.resources.compose_multiplatform
import com.radmir.storyteller.models.StoryScript
import com.radmir.storyteller.ui.StoryScreen
import com.radmir.storyteller.viewmodel.StoryViewModel
import kotlinx.serialization.json.Json

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        var showStory by remember { mutableStateOf(false) }

        val storyViewModel = remember { StoryViewModel() }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.Default) {
                val bytes = Res.readBytes("story.json")
                val jsonString = bytes.decodeToString()
                storyViewModel.loadStory(jsonString)
            }
        }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { showStory = !showStory }) {
                Text("Open Story")
            }

            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: $greeting")
                }
            }

            AnimatedVisibility(showStory) {
                StoryScreen(viewModel = storyViewModel)
            }
        }
    }
}