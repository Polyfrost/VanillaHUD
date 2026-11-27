package org.polyfrost.vanillahud.test

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.polyfrost.vanillahud.render.HotbarKeybinds

class HotbarKeybindsTest {
    @Test
    fun `layout positions keybinds within each corner inset of the 16x16 slot`() {
        val topLeft = HotbarKeybinds.layoutKeybind(100, 200, textWidth = 6, requestedScale = 0.8f, HotbarKeybinds.TOP_LEFT)
        assertEquals(101f, topLeft.x, 1e-4f)
        assertEquals(201f, topLeft.y, 1e-4f)
        assertEquals(0.8f, topLeft.scale, 1e-4f)

        val topRight = HotbarKeybinds.layoutKeybind(100, 200, textWidth = 6, requestedScale = 0.8f, HotbarKeybinds.TOP_RIGHT)
        assertEquals(115f - 5f * 0.8f, topRight.x, 1e-4f)
        assertEquals(201f, topRight.y, 1e-4f)

        val bottomLeft = HotbarKeybinds.layoutKeybind(100, 200, textWidth = 6, requestedScale = 0.8f, HotbarKeybinds.BOTTOM_LEFT)
        assertEquals(101f, bottomLeft.x, 1e-4f)
        assertEquals(215f - 7f * 0.8f, bottomLeft.y, 1e-4f)

        val bottomRight = HotbarKeybinds.layoutKeybind(100, 200, textWidth = 6, requestedScale = 0.8f, HotbarKeybinds.BOTTOM_RIGHT)
        assertEquals(115f - 5f * 0.8f, bottomRight.x, 1e-4f)
        assertEquals(215f - 7f * 0.8f, bottomRight.y, 1e-4f)
    }

    @Test
    fun `layout shrinks wide labels so they stay within the 14px slot bounds`() {
        val wide = HotbarKeybinds.layoutKeybind(10, 20, textWidth = 29, requestedScale = 1f, HotbarKeybinds.TOP_RIGHT)
        assertEquals(0.5f, wide.scale, 1e-4f)
        assertEquals(11f, wide.x, 1e-4f)
        assertTrue(wide.x >= 11f && wide.x + 28f * wide.scale <= 25f + 1e-4f)
    }

    @Test
    fun `modern key formatting handles digits, letters, F-keys, mouse, keypad, modifiers, and unbound`() {
        assertNull(HotbarKeybinds.formatModernKey("key.keyboard.unknown") { "Not Bound" })
        assertEquals("1", HotbarKeybinds.formatModernKey("key.keyboard.1") { "1" })
        assertEquals("Q", HotbarKeybinds.formatModernKey("key.keyboard.q") { "q" })
        assertEquals("F12", HotbarKeybinds.formatModernKey("key.keyboard.f12") { "F12" })
        assertEquals("M1", HotbarKeybinds.formatModernKey("key.mouse.left") { "Left Button" })
        assertEquals("M2", HotbarKeybinds.formatModernKey("key.mouse.right") { "Right Button" })
        assertEquals("M3", HotbarKeybinds.formatModernKey("key.mouse.middle") { "Middle Button" })
        assertEquals("M4", HotbarKeybinds.formatModernKey("key.mouse.4") { "Button 4" })
        assertEquals("M5", HotbarKeybinds.formatModernKey("key.mouse.5") { "Button 5" })
        assertEquals("N0", HotbarKeybinds.formatModernKey("key.keyboard.keypad.0") { "Keypad 0" })
        assertEquals("N9", HotbarKeybinds.formatModernKey("key.keyboard.keypad.9") { "Keypad 9" })
        assertEquals("N+", HotbarKeybinds.formatModernKey("key.keyboard.keypad.add") { "Keypad +" })
        assertEquals("NEn", HotbarKeybinds.formatModernKey("key.keyboard.keypad.enter") { "Keypad Enter" })
        assertEquals("Cap", HotbarKeybinds.formatModernKey("key.keyboard.caps.lock") { "Caps Lock" })
        assertEquals("LSh", HotbarKeybinds.formatModernKey("key.keyboard.left.shift") { "Left Shift" })
        assertEquals("LCt", HotbarKeybinds.formatModernKey("key.keyboard.left.control") { "Left Control" })
        assertEquals("LAl", HotbarKeybinds.formatModernKey("key.keyboard.left.option") { "Left Option" })
        assertEquals("LMt", HotbarKeybinds.formatModernKey("key.keyboard.left.command") { "Left Command" })
        assertEquals("Spc", HotbarKeybinds.formatModernKey("key.keyboard.space") { "Space" })
        assertEquals("Up", HotbarKeybinds.formatModernKey("key.keyboard.up") { "Up Arrow" })
        assertEquals("Wor", HotbarKeybinds.formatModernKey("key.keyboard.world.1") { "World 1" })
    }

    @Test
    fun `legacy key formatting handles unbound, mouse buttons, special keys, numpad, and punctuation`() {
        assertNull(HotbarKeybinds.formatLegacyKey(0, "NONE"))
        assertNull(HotbarKeybinds.formatLegacyKey(42, null))
        assertEquals("M1", HotbarKeybinds.formatLegacyKey(-100, null))
        assertEquals("M4", HotbarKeybinds.formatLegacyKey(-97, null))
        assertEquals("Cap", HotbarKeybinds.formatLegacyKey(58, "CAPITAL"))
        assertEquals("LSh", HotbarKeybinds.formatLegacyKey(42, "LSHIFT"))
        assertEquals("LAl", HotbarKeybinds.formatLegacyKey(56, "LMENU"))
        assertEquals("N5", HotbarKeybinds.formatLegacyKey(76, "NUMPAD5"))
        assertEquals("`", HotbarKeybinds.formatLegacyKey(41, "GRAVE"))
        assertEquals("'", HotbarKeybinds.formatLegacyKey(40, "APOSTROPHE"))
        assertEquals("1", HotbarKeybinds.formatLegacyKey(2, "1"))
        assertEquals("E", HotbarKeybinds.formatLegacyKey(18, "e"))
    }
}
