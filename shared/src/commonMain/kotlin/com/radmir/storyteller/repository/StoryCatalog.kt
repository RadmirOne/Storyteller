package com.radmir.storyteller.repository

import com.radmir.storyteller.models.StoryProgress
import com.radmir.storyteller.models.StoryScript
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class StoryCatalog(val stories: List<StoryCatalogEntry>)

@Serializable
data class StoryCatalogEntry(val id: String, val resource: String)

fun progressFileName(storyId: String): String {
    require(Regex("[A-Za-z0-9_-]{1,64}").matches(storyId)) { "Некорректный id истории для сохранения." }
    return "story-progress-$storyId.json"
}

fun parseStoryCatalog(json: String): StoryCatalog = Json.decodeFromString<StoryCatalog>(json).also { catalog ->
    require(catalog.stories.isNotEmpty()) { "Каталог историй пуст." }
    require(catalog.stories.map { it.id }.distinct().size == catalog.stories.size) { "В каталоге повторяется id истории." }
    catalog.stories.forEach { entry ->
        progressFileName(entry.id)
        require(entry.resource.startsWith("files/") && entry.resource.endsWith(".json") &&
            entry.resource.split('/').none { it.isBlank() || it == "." || it == ".." } && '\\' !in entry.resource) {
            "Некорректный путь сценария ${entry.id}."
        }
    }
}

fun StoryCatalogEntry.parseStory(json: String): StoryScript = StoryRepository().parseScript(json).also {
    require(it.id == id) { "Id истории ${it.id} не совпадает с каталогом: $id." }
}

/** Reads the old single slot only for its own story. It is never deleted or overwritten. */
class StoryProgressStore(
    private val storyId: String,
    private val destination: ProgressStore,
    private val legacy: ProgressStore
) : ProgressStore {
    init { progressFileName(storyId) }
    override fun read(): String? {
        destination.read()?.let { return it }
        val old = legacy.read() ?: return null
        val progress = try { Json.decodeFromString<StoryProgress>(old) } catch (_: Exception) { return null }
        return old.takeIf { progress.story.id == storyId }
    }
    override fun write(value: String) {
        require(Json.decodeFromString<StoryProgress>(value).story.id == storyId) { "Сохранение относится к другой истории." }
        destination.write(value)
    }
}
