package com.radmir.storyteller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.rgb(12, 24, 33))
        )
        super.onCreate(savedInstanceState)

        setContent {
            App(debugToolsEnabled = BuildConfig.DEBUG)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
