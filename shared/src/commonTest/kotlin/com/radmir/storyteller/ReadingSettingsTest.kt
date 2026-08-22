package com.radmir.storyteller

import com.radmir.storyteller.settings.*
import kotlin.test.*

class ReadingSettingsTest {
    @Test fun settingsSurviveControllerRecreation() {
        val store = MemoryReadingSettingsStore()
        val value = ReadingSettings(1.4f, true, false, 0.25f)
        assertTrue(ReadingSettingsController(store).update(value))
        assertEquals(value, ReadingSettingsController(store).settings)
    }

    @Test fun missingFieldsAndFutureFieldsRemainCompatible() {
        val store = MemoryReadingSettingsStore()
        store.write("""{"reduceMotion":true,"futureSetting":42}""")
        val controller = ReadingSettingsController(store)
        assertEquals(ReadingSettings(reduceMotion = true), controller.settings)
        assertNull(controller.error)
    }

    @Test fun corruptRecordUsesDefaultsAndCanBeReplaced() {
        val store = MemoryReadingSettingsStore()
        store.write("{broken")
        val controller = ReadingSettingsController(store)
        assertEquals(ReadingSettings(), controller.settings)
        assertNotNull(controller.error)
        assertTrue(controller.update(ReadingSettings(reduceMotion = true)))
        assertNull(controller.error)
        assertTrue(ReadingSettingsController(store).settings.reduceMotion)
    }

    @Test fun invalidValuesCannotEscapeIntoUiOrAudio() {
        val store = MemoryReadingSettingsStore()
        store.write("""{"textScale":100,"volume":-4}""")
        val controller = ReadingSettingsController(store)
        assertEquals(1.5f, controller.settings.textScale)
        assertEquals(0f, controller.settings.volume)
        assertTrue(controller.update(ReadingSettings(textScale = Float.NaN, volume = Float.POSITIVE_INFINITY)))
        assertEquals(ReadingSettings(), ReadingSettingsController(store).settings)
    }

    @Test fun failedWriteKeepsPreviouslySavedSettingsAndAllowsRetry() {
        var fail = false
        val memory = MemoryReadingSettingsStore()
        val store = object : ReadingSettingsStore {
            override fun read() = memory.read()
            override fun write(value: String) {
                if (fail) error("Disk unavailable")
                memory.write(value)
            }
        }
        val controller = ReadingSettingsController(store)
        val saved = ReadingSettings(volume = 0.2f)
        assertTrue(controller.update(saved))
        fail = true
        assertFalse(controller.update(saved.copy(reduceMotion = true)))
        assertEquals(saved, controller.settings)
        assertEquals(saved, ReadingSettingsController(store).settings)
        assertNotNull(controller.error)
        fail = false
        assertTrue(controller.update(saved.copy(reduceMotion = true)))
        assertNull(controller.error)
    }
}
