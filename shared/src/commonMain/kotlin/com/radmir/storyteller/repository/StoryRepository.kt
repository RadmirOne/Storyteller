package com.radmir.storyteller.repository

import com.radmir.storyteller.models.StoryScript
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class StoryRepository {
    private var currentScript: StoryScript? = null

    fun loadScript(json: String): StoryScript {
        val script = Json.decodeFromString<StoryScript>(json)
        currentScript = script
        return script
    }

    fun getScript(): StoryScript? = currentScript
}