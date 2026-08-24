package com.radmir.storyteller

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.radmir.storyteller.screens.StoriesScreen
import com.radmir.storyteller.screens.StoryScreen
import com.radmir.storyteller.viewmodel.StoryViewModel

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showStories by remember { mutableStateOf(false) }
        var showStory by remember { mutableStateOf(false) }

        val storyViewModel = remember { StoryViewModel() }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { showStories = !showStories }) {
                Text("Open Stories")
            }

            AnimatedVisibility(showStories) {
                StoriesScreen(onStoryStarted = { json ->
                    storyViewModel.loadStory(json)
                    showStory = true
                    showStories = false
                })
            }

            AnimatedVisibility(showStory) {
                StoryScreen(viewModel = storyViewModel)
            }
        }
    }
}