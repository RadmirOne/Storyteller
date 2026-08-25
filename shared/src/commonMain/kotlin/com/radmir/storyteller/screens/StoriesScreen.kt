package com.radmir.storyteller.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.radmir.storyteller.viewmodel.StoryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import storyteller.shared.generated.resources.Res

@Composable
fun StoriesScreen(onStoryStarted: (String) -> Unit) {
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Available Stories", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                isLoading = true
                scope.launch(Dispatchers.Default) {
                    val bytes = Res.readBytes("files/story.json")
                    val jsonString = bytes.decodeToString()
                    onStoryStarted(jsonString)
                }
            }) {
                Text("Start Story")
            }
        }
    }
}