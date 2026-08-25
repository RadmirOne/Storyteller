package com.radmir.storyteller.repository

import com.radmir.storyteller.models.StoryScript
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class StoryRepository {
    private var currentScript: StoryScript? = null
    private var lastPlayedStoryId: String? = null
    private var stories: List<StoryScript> = emptyList()

    fun setStories(jsonStrings: List<String>) {
        stories = jsonStrings.map { Json.decodeFromString<StoryScript>(it) }
    }

    fun getStories(): List<StoryScript> = stories

    fun loadScript(json: String): StoryScript {
        val script = Json.decodeFromString<StoryScript>(json)
        currentScript = script
        lastPlayedStoryId = script.id
        return script
    }

    fun getScript(): StoryScript? = currentScript

    fun getLastPlayedStoryId(): String? = lastPlayedStoryId

    fun setLastPlayedStoryId(id: String) {
        lastPlayedStoryId = id
    }
}