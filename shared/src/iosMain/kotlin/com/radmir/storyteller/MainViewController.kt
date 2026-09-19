package com.radmir.storyteller

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController(debugToolsEnabled: Boolean = false) = ComposeUIViewController {
    App(debugToolsEnabled = debugToolsEnabled)
}
