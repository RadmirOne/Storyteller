package com.radmir.storyteller

import com.radmir.storyteller.audio.*
import com.radmir.storyteller.models.Scene
import com.radmir.storyteller.models.StoryScript
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class StoryAudioTest {
    private fun scriptWithAudio() = StoryScript(
        id = "test", title = "Test", startSceneId = "room", startNodeId = "start",
        scenes = mapOf("room" to Scene("room", "files/room.png",
            audio = SceneAudio(music = "files/audio/song.mp3", ambience = "files/audio/song.mp3")))
    )

    @Test fun resourcePreflightReportsMissingAndEmptyFilesAndDeduplicates(): Unit = runBlocking {
        var calls = 0
        val missing = validateStoryAudioResources(scriptWithAudio()) {
            calls++
            error("Missing")
        }
        assertEquals(1, calls)
        assertTrue(missing.single().contains("files/audio/song.mp3"))
        assertTrue(validateStoryAudioResources(scriptWithAudio()) { byteArrayOf() }.single().contains("empty"))
        assertFailsWith<CancellationException> {
            validateStoryAudioResources(scriptWithAudio()) { throw CancellationException("Cancelled") }
        }
    }

    private class FakePlayer : AudioPlayer {
        var loads = 0
        var releases = 0
        val states = mutableListOf<Pair<Boolean, Float>>()
        override fun load(bytes: ByteArray) { loads++ }
        override fun setPlayback(playing: Boolean, volume: Float) { states += playing to volume }
        override fun release() { releases++ }
    }

    @Test fun backgroundDuringLoadingDoesNotStartPlayback() {
        val player = FakePlayer()
        val channel = AudioChannelController(player)
        channel.update(true, 0.7f)
        channel.update(false, 0.7f)
        channel.load(byteArrayOf(1))
        assertEquals(false to 0.7f, player.states.last())
        channel.update(true, 0.7f)
        assertEquals(true to 0.7f, player.states.last())
        assertEquals(1, player.loads)
    }

    @Test fun mutingAndResumingDoesNotReloadAudio() {
        val player = FakePlayer()
        val channel = AudioChannelController(player)
        channel.load(byteArrayOf(1))
        channel.update(true, 0.5f)
        channel.update(true, 0f)
        assertEquals(false to 0f, player.states.last())
        channel.update(true, 0.5f)
        assertEquals(true to 0.5f, player.states.last())
        assertEquals(1, player.loads)
    }

    @Test fun leavingReaderPreventsLateLoadAndPlayback() {
        val player = FakePlayer()
        val channel = AudioChannelController(player)
        channel.release()
        channel.load(byteArrayOf(1))
        channel.update(true, 1f)
        channel.release()
        assertEquals(0, player.loads)
        assertEquals(1, player.releases)
        assertTrue(player.states.isEmpty())
    }

    @Test fun resourcesMustBePackagedAudio() {
        assertTrue(SceneAudio(ambience = "files/audio/sea.wav").validationErrors().isEmpty())
        listOf("https://example.com/audio.mp3", "files/audio/../secret.wav", "files/audio/a.txt", "files/audio/dir\\a.wav",
            "files/audio//x.wav", "files/audio/./x.wav", "files/audio/.wav", "files/audio/ /x.wav").forEach {
            assertEquals(1, SceneAudio(music = it).validationErrors().size)
        }
        assertEquals(0f, effectiveAudioVolume(true, Float.NaN))
        assertEquals(0f, effectiveAudioVolume(false, 1f))
    }

    @Test fun playbackFailureIsReportedOnceAndStopsFurtherPlatformCalls() {
        var playbackCalls = 0
        var releases = 0
        var errors = 0
        val player = object : AudioPlayer {
            override fun load(bytes: ByteArray) {}
            override fun setPlayback(playing: Boolean, volume: Float) {
                playbackCalls++
                error("Platform player failed")
            }
            override fun release() { releases++; error("Already invalid") }
        }
        val channel = AudioChannelController(player) { errors++ }
        channel.load(byteArrayOf(1))
        repeat(5) { channel.update(true, 0.7f) }
        channel.release()
        assertEquals(1, playbackCalls)
        assertEquals(1, releases)
        assertEquals(1, errors)
    }

    @Test fun wavValidationRejectsTruncationAndMissingAudioData() {
        val valid = byteArrayOf(
            82, 73, 70, 70, 38, 0, 0, 0, 87, 65, 86, 69,
            102, 109, 116, 32, 16, 0, 0, 0, 1, 0, 1, 0,
            -128, 62, 0, 0, 0, 125, 0, 0, 2, 0, 16, 0,
            100, 97, 116, 97, 2, 0, 0, 0, 0, 0
        )
        assertNull(wavHeaderError(valid))
        assertNotNull(wavHeaderError(valid.copyOf(44)))
        assertNotNull(wavHeaderError(valid.copyOf().also { it[40] = 100 }))
        assertNotNull(wavHeaderError(valid.copyOf().also { it[36] = 0 }))
        assertNotNull(wavHeaderError("not a wav".encodeToByteArray()))
    }
}
