package com.radmir.storyteller.repository

import com.radmir.storyteller.models.StoryScript
import kotlinx.coroutines.CancellationException

class StoryValidationException(val problems: List<String>) :
    IllegalArgumentException(problems.joinToString("\n"))

fun validateStory(script: StoryScript) {
    val errors = mutableListOf<String>()
    fun check(valid: Boolean, message: String) { if (!valid) errors.add(message) }
    check(script.schemaVersion == 1, "Поддерживается только schemaVersion = 1.")
    check(script.id.isNotBlank() && script.title.isNotBlank(), "У истории должны быть id и title.")
    val characters = script.characters.map { it.id }
    check(characters.distinct().size == characters.size, "Идентификаторы персонажей повторяются.")
    script.characters.forEach {
        check(it.id.isNotBlank() && it.name.isNotBlank(), "У персонажа должны быть id и name.")
    }
    fun target(scene: String, node: String, source: String) {
        check(script.scenes[scene]?.nodes?.containsKey(node) == true,
            "$source: не найден узел $scene/$node.")
    }
    target(script.startSceneId, script.startNodeId, "Начало истории")
    script.scenes.forEach { (sceneId, scene) ->
        check(sceneId.isNotBlank() && scene.id == sceneId, "Сцена $sceneId: id должен совпадать с ключом.")
        scene.nodes.forEach { (nodeId, node) ->
            val path = "$sceneId/$nodeId"
            check(nodeId.isNotBlank() && node.id == nodeId, "$path: id должен совпадать с ключом.")
            check(node.characterId in characters, "$path: неизвестный персонаж ${node.characterId}.")
            listOfNotNull(node.camera, node.cameraStart).forEach {
                check(it.focusX.isFinite() && it.focusX in 0f..1f, "$path: focusX должен быть от 0 до 1.")
            }
            check(node.cameraDurationMs == null || node.cameraDurationMs in 0..Int.MAX_VALUE.toLong(),
                "$path: cameraDurationMs должен быть от 0 до ${Int.MAX_VALUE}.")
            val stage = node.stageCharacters.orEmpty()
            check(stage.map { it.characterId }.distinct().size == stage.size, "$path: персонаж на сцене повторяется.")
            stage.forEach {
                check(it.characterId in characters, "$path: неизвестный персонаж на сцене ${it.characterId}.")
                check(it.scale.isFinite() && it.scale > 0, "$path: scale должен быть положительным.")
            }
            val choices = node.choices.orEmpty()
            check(choices.map { it.id }.distinct().size == choices.size, "$path: id вариантов повторяются.")
            check(choices.isEmpty() || node.nextNodeId == null, "$path: choices и nextNodeId нельзя задавать одновременно.")
            check(node.nextNodeId != null || (node.nextSceneId == null && node.nextSceneStartEffect == null),
                "$path: переход требует nextNodeId.")
            node.nextNodeId?.let { target(node.nextSceneId ?: sceneId, it, path) }
            choices.forEach {
                check(it.id.isNotBlank() && it.text.isNotBlank(), "$path: у выбора должны быть id и text.")
                target(it.targetSceneId ?: sceneId, it.targetNodeId, "$path, выбор ${it.id}")
            }
        }
    }
    script.resourcePaths().forEach { path ->
        check(path.startsWith("files/") && path.split('/').none { it.isBlank() || it == ".." || it == "." } && '\\' !in path,
            "Некорректный путь ресурса: $path.")
    }
    if (errors.isNotEmpty()) throw StoryValidationException(errors)
}

fun StoryScript.resourcePaths(): Set<String> =
    (scenes.values.map { it.backgroundResource } + characters.mapNotNull { it.spriteResource }).toSet()

suspend fun validateStoryResources(script: StoryScript, loadImage: suspend (String) -> Unit) {
    val errors = mutableListOf<String>()
    for (path in script.resourcePaths()) {
        try {
            loadImage(path)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errors.add("Не удалось загрузить изображение: $path.")
        }
    }
    if (errors.isNotEmpty()) throw StoryValidationException(errors)
}
