package com.radmir.storyteller

import com.radmir.storyteller.audio.validationErrors
import com.radmir.storyteller.repository.*
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.*

class StoryCatalogAssetsTest {
    private val resources = listOf(File("src/commonMain/composeResources"),
        File("shared/src/commonMain/composeResources")).first { it.isDirectory }.canonicalFile

    private fun resource(path: String): File = File(resources, path).canonicalFile.also {
        assertTrue(it.toPath().startsWith(resources.toPath()), "Ресурс вне каталога: $path")
        assertTrue(it.isFile && it.length() > 0, "Ресурс отсутствует или пуст: $path")
    }

    @Test fun catalogStoriesHaveValidScriptsAndReadableResources() {
        val catalog = parseStoryCatalog(resource("files/catalog.json").readText())
        catalog.stories.forEach { entry ->
            val story = entry.parseStory(resource(entry.resource).readText())
            val report = analyzeStory(story)
            println("${story.id}: ${report.nodeCount} узлов, ${report.endingCount} финалов, " +
                "${report.resources.size} ресурсов")
            println("  Недостижимые узлы: ${report.unreachableNodes.joinToString().ifEmpty { "нет" }}")
            println("  Проверьте доступность всех условных выборов: ${report.conditionalChoiceNodes.joinToString().ifEmpty { "нет" }}")
            report.imageResources.forEach { path ->
                assertNotNull(ImageIO.read(resource(path)), "Не читается изображение $path")
            }
            story.scenes.values.forEach { scene ->
                assertTrue(scene.audio.validationErrors().isEmpty(), "${story.id}/${scene.id}: неверный путь аудио")
            }
            report.audioResources.forEach { path ->
                val file = resource(path)
                val header = file.inputStream().use { it.readNBytes(12) }
                fun ascii(start: Int, end: Int) = header.copyOfRange(start, end).toString(Charsets.US_ASCII)
                assertTrue(header.size >= 12, "Короткий заголовок аудио: $path")
                val valid = when (file.extension.lowercase()) {
                    "wav" -> ascii(0, 4) == "RIFF" && ascii(8, 12) == "WAVE"
                    "mp3" -> ascii(0, 3) == "ID3" ||
                        ((header[0].toInt() and 255) == 255 && (header[1].toInt() and 224) == 224)
                    "m4a" -> ascii(4, 8) == "ftyp"
                    else -> false
                }
                assertTrue(valid, "Неверный заголовок аудио: $path")
            }
        }
    }
}
