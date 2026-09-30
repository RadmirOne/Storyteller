package com.radmir.storyteller.repository

import com.radmir.storyteller.models.StoryScript
import com.radmir.storyteller.models.PlayerOptions
import kotlinx.coroutines.CancellationException
import com.radmir.storyteller.audio.validationErrors

class StoryValidationException(val problems: List<String>) :
    IllegalArgumentException(problems.joinToString("\n"))

fun validateStory(script: StoryScript) {
    val errors = mutableListOf<String>()
    fun check(valid: Boolean, message: String) { if (!valid) errors.add(message) }
    check(script.schemaVersion in 1..3, "Поддерживаются schemaVersion 1, 2 и 3.")
    check(script.schemaVersion >= 3 || script.initialVariables.isEmpty(), "initialVariables требует schemaVersion 3.")
    check(script.initialVariables.keys.none { it.isBlank() }, "Имена переменных initialVariables не должны быть пустыми.")
    check(script.characterAppearance.durationMs >= 0, "characterAppearance.durationMs должен быть неотрицательным.")
    check(script.characterAppearance.slideDistance.isFinite() && script.characterAppearance.slideDistance in 0f..1f,
        "characterAppearance.slideDistance должен быть от 0 до 1.")
    check(script.id.isNotBlank() && script.title.isNotBlank(), "У истории должны быть id и title.")
    check(script.schemaVersion >= 3 || script.playerOptions == PlayerOptions(), "playerOptions требует schemaVersion 3.")
    check(script.playerOptions.appearances.isNotEmpty() && script.playerOptions.outfits.isNotEmpty(),
        "playerOptions должен содержать внешности и одежду.")
    check(script.playerOptions.appearances.distinct().size == script.playerOptions.appearances.size &&
        script.playerOptions.outfits.distinct().size == script.playerOptions.outfits.size,
        "В playerOptions не должно быть повторяющихся вариантов.")
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
        errors.addAll(scene.audio.validationErrors().map { "Сцена $sceneId: $it" })
        check(script.schemaVersion >= 3 || (scene.audio.music == null && scene.audio.ambience == null),
            "Сцена $sceneId: audio требует schemaVersion 3.")
        check(sceneId.isNotBlank() && scene.id == sceneId, "Сцена $sceneId: id должен совпадать с ключом.")
        scene.nodes.forEach { (nodeId, node) ->
            val path = "$sceneId/$nodeId"
            check(nodeId.isNotBlank() && node.id == nodeId, "$path: id должен совпадать с ключом.")
            check(node.characterId in characters, "$path: неизвестный персонаж ${node.characterId}.")
            node.speakerSpriteResource?.let {
                check(script.schemaVersion >= 3, "$path: speakerSpriteResource требует schemaVersion 3.")
                check(node.characterId != "protagonist", "$path: образ героини задаётся playerOptions.")
            }
            listOfNotNull(node.camera, node.cameraStart).forEach {
                check(it.focusX.isFinite() && it.focusX in 0f..1f, "$path: focusX должен быть от 0 до 1.")
                it.targetCharacterId?.let { id ->
                    check(script.schemaVersion >= 2, "$path: targetCharacterId требует schemaVersion 2.")
                    check(script.characters.any { actor -> actor.id == id && actor.spriteResource != null },
                        "$path: цель камеры $id должна быть персонажем со спрайтом.")
                }
            }
            check(node.cameraDurationMs == null || node.cameraDurationMs in 0..Int.MAX_VALUE.toLong(),
                "$path: cameraDurationMs должен быть от 0 до ${Int.MAX_VALUE}.")
            val stage = node.stageCharacters.orEmpty()
            check(stage.map { it.characterId }.distinct().size == stage.size, "$path: персонаж на сцене повторяется.")
            stage.forEach {
                check(it.characterId in characters, "$path: неизвестный персонаж на сцене ${it.characterId}.")
                check(it.scale.isFinite() && it.scale > 0, "$path: scale должен быть положительным.")
                check(it.worldX == null || (it.worldX.isFinite() && it.worldX in 0f..1f), "$path: worldX должен быть от 0 до 1.")
                check(it.groundY.isFinite() && it.groundY in 0f..1f, "$path: groundY должен быть от 0 до 1.")
                check(script.schemaVersion >= 2 || (it.worldX == null && it.groundY == 0.92f),
                    "$path: координаты персонажа требуют schemaVersion 2.")
            }
            val choices = node.choices.orEmpty()
            check(choices.map { it.id }.distinct().size == choices.size, "$path: id вариантов повторяются.")
            check(choices.isEmpty() || node.nextNodeId == null, "$path: choices и nextNodeId нельзя задавать одновременно.")
            check(node.nextNodeId != null || (node.nextSceneId == null && node.nextSceneStartEffect == null),
                "$path: переход требует nextNodeId.")
            node.nextNodeId?.let { target(node.nextSceneId ?: sceneId, it, path) }
            choices.forEach {
                check(it.id.isNotBlank() && it.text.isNotBlank(), "$path: у выбора должны быть id и text.")
                val choicePath = "$path, выбор ${it.id}"
                check(script.schemaVersion >= 3 || (it.effects.set.isEmpty() && it.effects.add.isEmpty() &&
                    it.unavailableReason == null && !it.hideWhenUnavailable),
                    "$choicePath: effects и настройки недоступности требуют schemaVersion 3.")
                check(it.unavailableReason == null || it.unavailableReason.isNotBlank(),
                    "$choicePath: unavailableReason не должен быть пустым.")
                if (script.schemaVersion >= 3) {
                    (it.conditions.orEmpty().keys + it.effects.set.keys + it.effects.add.keys).forEach { variable ->
                        check(variable in script.initialVariables, "$choicePath: переменная $variable не объявлена в initialVariables.")
                    }
                }
                target(it.targetSceneId ?: sceneId, it.targetNodeId, "$path, выбор ${it.id}")
            }
        }
    }
    script.resourcePaths().forEach { path ->
        check(path.startsWith("files/") && path.split('/').none { it.isBlank() || it == ".." || it == "." } && '\\' !in path,
            "Некорректный путь ресурса: $path.")
    }
    errors.addAll(validateCameraRoutes(script))
    if (errors.isNotEmpty()) throw StoryValidationException(errors.distinct())
}

private data class CameraRoute(
    val sceneId: String, val nodeId: String,
    val visibleWorldActors: Set<String> = emptySet(), val inheritedTarget: String? = null
)

/** Check every reachable incoming stage, including branches that merge and scene resets. */
private fun validateCameraRoutes(script: StoryScript): List<String> {
    val errors = mutableSetOf<String>()
    val pending = ArrayDeque<CameraRoute>()
    pending.add(CameraRoute(script.startSceneId, script.startNodeId))
    val visited = mutableSetOf<CameraRoute>()
    while (pending.isNotEmpty()) {
        val route = pending.removeFirst()
        if (!visited.add(route)) continue
        val node = script.scenes[route.sceneId]?.nodes?.get(route.nodeId) ?: continue
        val actors = node.stageCharacters?.filter { it.visible && it.worldX != null }
            ?.map { it.characterId }?.toSet() ?: route.visibleWorldActors
        val target = if (node.camera != null) node.camera.targetCharacterId else route.inheritedTarget
        listOfNotNull(target, node.cameraStart?.targetCharacterId).forEach { id ->
            if (id !in actors) errors.add("${route.sceneId}/${route.nodeId}: цель камеры $id не видна или не имеет worldX на одном из маршрутов.")
        }
        fun enqueue(sceneId: String, nodeId: String) {
            pending.add(if (sceneId == route.sceneId) CameraRoute(sceneId, nodeId, actors, target)
                else CameraRoute(sceneId, nodeId))
        }
        node.nextNodeId?.let { enqueue(node.nextSceneId ?: route.sceneId, it) }
        node.choices.orEmpty().forEach { enqueue(it.targetSceneId ?: route.sceneId, it.targetNodeId) }
    }
    return errors.toList()
}

fun StoryScript.resourcePaths(): Set<String> =
    (scenes.values.map { it.backgroundResource } + characters.mapNotNull { it.spriteResource } +
        scenes.values.flatMap { scene -> scene.nodes.values.mapNotNull { it.speakerSpriteResource } }).toSet()

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
