package org.polyfrost.vanillahud.render

import net.minecraft.client.Minecraft
//? if > 1.8.9 {
import net.minecraft.client.gui.GuiGraphicsExtractor
//?} else {
/*import net.minecraft.client.options.GameOptions
import org.polyfrost.vanillahud.compat.GuiGraphicsExtractor
*///?}
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.vanillahud.hud.BackdropHud
import org.polyfrost.vanillahud.hud.HotbarHud

internal data class KeybindLayout(val x: Float, val y: Float, val scale: Float)

object HotbarKeybinds {
    private const val HOTBAR_SLOTS = 9
    private const val SLOT_SIZE = 16f
    private const val INSET = 1f
    private const val MAX_LABEL_WIDTH = SLOT_SIZE - INSET * 2f
    private val GLYPH_HEIGHT = BackdropHud.GLYPH_HEIGHT.toFloat()
    private const val MIN_SCALE = 0.4f
    private const val MAX_SCALE = 1f

    const val TOP_LEFT = 0
    const val TOP_RIGHT = 1
    const val BOTTOM_LEFT = 2
    const val BOTTOM_RIGHT = 3

    private val MODERN_LABELS = mapOf(
        "key.keyboard.caps.lock" to "Cap",
        "key.keyboard.left.shift" to "LSh",
        "key.keyboard.right.shift" to "RSh",
        "key.keyboard.left.control" to "LCt",
        "key.keyboard.right.control" to "RCt",
        "key.keyboard.left.alt" to "LAl",
        "key.keyboard.left.option" to "LAl",
        "key.keyboard.right.alt" to "RAl",
        "key.keyboard.right.option" to "RAl",
        "key.keyboard.left.win" to "LMt",
        "key.keyboard.left.windows" to "LMt",
        "key.keyboard.left.command" to "LMt",
        "key.keyboard.left.meta" to "LMt",
        "key.keyboard.right.win" to "RMt",
        "key.keyboard.right.windows" to "RMt",
        "key.keyboard.right.command" to "RMt",
        "key.keyboard.right.meta" to "RMt",
        "key.keyboard.space" to "Spc",
        "key.keyboard.tab" to "Tab",
        "key.keyboard.escape" to "Esc",
        "key.keyboard.enter" to "Ent",
        "key.keyboard.backspace" to "Bsp",
        "key.keyboard.delete" to "Del",
        "key.keyboard.insert" to "Ins",
        "key.keyboard.home" to "Hom",
        "key.keyboard.end" to "End",
        "key.keyboard.page.up" to "PgU",
        "key.keyboard.page.down" to "PgD",
        "key.keyboard.num.lock" to "Num",
        "key.keyboard.scroll.lock" to "Scr",
        "key.keyboard.print.screen" to "Prt",
        "key.keyboard.pause" to "Pau",
        "key.keyboard.up" to "Up",
        "key.keyboard.down" to "Dn",
        "key.keyboard.left" to "Lt",
        "key.keyboard.right" to "Rt",
    )

    private val LEGACY_LABELS = mapOf(
        "CAPITAL" to "Cap",
        "LSHIFT" to "LSh",
        "RSHIFT" to "RSh",
        "LCONTROL" to "LCt",
        "RCONTROL" to "RCt",
        "LMENU" to "LAl",
        "RMENU" to "RAl",
        "LMETA" to "LMt",
        "RMETA" to "RMt",
        "SPACE" to "Spc",
        "TAB" to "Tab",
        "ESCAPE" to "Esc",
        "RETURN" to "Ent",
        "BACK" to "Bsp",
        "DELETE" to "Del",
        "INSERT" to "Ins",
        "HOME" to "Hom",
        "END" to "End",
        "PRIOR" to "PgU",
        "NEXT" to "PgD",
        "NUMLOCK" to "Num",
        "SCROLL" to "Scr",
        "SYSRQ" to "Prt",
        "PAUSE" to "Pau",
        "UP" to "Up",
        "DOWN" to "Dn",
        "LEFT" to "Lt",
        "RIGHT" to "Rt",
        "ADD" to "N+",
        "SUBTRACT" to "N-",
        "MULTIPLY" to "N*",
        "DIVIDE" to "N/",
        "DECIMAL" to "N.",
        "NUMPADENTER" to "NEn",
        "NUMPADEQUALS" to "N=",
        "GRAVE" to "`",
        "MINUS" to "-",
        "EQUALS" to "=",
        "LBRACKET" to "[",
        "RBRACKET" to "]",
        "BACKSLASH" to "\\",
        "SEMICOLON" to ";",
        "APOSTROPHE" to "'",
        "COMMA" to ",",
        "PERIOD" to ".",
        "SLASH" to "/",
    )

    @JvmStatic
    fun draw(graphics: GuiGraphicsExtractor, hud: HotbarHud, slot: Int, x: Int, y: Int) {
        if (slot !in 0 until HOTBAR_SLOTS || HudManager.isSamplingVanilla) return
        val color = hud.keybindColor.argb
        if (color ushr 24 == 0) return
        val label = labelFor(slot) ?: return
        val font = Minecraft.getInstance().font
        val width = font.width(label)
        if (width <= 0) return
        val layout = layoutKeybind(x, y, width, hud.keybindScale, hud.keybindPosition)
        val shadow = hud.textStyle.showShadow
        val pose = graphics.pose()
        pose.pushMatrix()
        //? if <=1.21.5 && >1.8.9 {
        /*pose.translate(layout.x, layout.y, 0f)
        pose.scale(layout.scale, layout.scale, 1f)
        *///?} else {
        pose.translate(layout.x, layout.y)
        pose.scale(layout.scale, layout.scale)
        //?}
        try {
            //? if >=26 {
            graphics.text(font, label, 0, 0, color, shadow)
            //?} elif > 1.8.9 {
            /*graphics.drawString(font, label, 0, 0, color, shadow)
            *///?} else {
            /*font.draw(label, 0f, 0f, color, shadow)
            *///?}
        } finally {
            pose.popMatrix()
        }
    }

    private fun labelFor(slot: Int): String? {
        //? if > 1.8.9 {
        val keys = Minecraft.getInstance().options.keyHotbarSlots
        if (slot !in keys.indices) return null
        val key = keys[slot]
        if (key.isUnbound) return null
        return formatModernKey(key.saveString()) { key.translatedKeyMessage.string }
        //?} else {
        /*val keys = Minecraft.getInstance().options.hotbarKeyBindings
        if (slot !in keys.indices) return null
        val code = keys[slot].keyCode
        return formatLegacyKey(code, if (code > 0) GameOptions.getKeyName(code) else null)
        *///?}
    }

    internal fun layoutKeybind(
        slotX: Int,
        slotY: Int,
        textWidth: Int,
        requestedScale: Float,
        position: Int,
    ): KeybindLayout {
        val visualWidth = (textWidth - 1).coerceAtLeast(1).toFloat()
        val scale = minOf(requestedScale.coerceIn(MIN_SCALE, MAX_SCALE), MAX_LABEL_WIDTH / visualWidth)
        val drawnW = visualWidth * scale
        val drawnH = GLYPH_HEIGHT * scale
        val right = position == TOP_RIGHT || position == BOTTOM_RIGHT
        val bottom = position == BOTTOM_LEFT || position == BOTTOM_RIGHT
        val drawX = if (right) slotX + SLOT_SIZE - INSET - drawnW else slotX + INSET
        val drawY = if (bottom) slotY + SLOT_SIZE - INSET - drawnH else slotY + INSET
        return KeybindLayout(drawX, drawY, scale)
    }

    internal fun formatModernKey(saveString: String, translated: () -> String): String? {
        if (saveString == "key.keyboard.unknown") return null
        if (saveString.startsWith("key.mouse.")) {
            val suffix = saveString.removePrefix("key.mouse.")
            return when (suffix) {
                "left" -> "M1"
                "right" -> "M2"
                "middle" -> "M3"
                else -> if (suffix.isNotEmpty() && suffix.all(Char::isDigit)) "M$suffix" else shortenDisplayName(translated())
            }
        }
        if (saveString.startsWith("key.keyboard.keypad.")) {
            val suffix = saveString.removePrefix("key.keyboard.keypad.")
            return when (suffix) {
                "add" -> "N+"
                "subtract" -> "N-"
                "multiply" -> "N*"
                "divide" -> "N/"
                "decimal", "period" -> "N."
                "equal" -> "N="
                "enter" -> "NEn"
                else -> if (suffix.length == 1 && suffix[0].isDigit()) "N$suffix" else shortenDisplayName(translated())
            }
        }
        MODERN_LABELS[saveString]?.let { return it }
        return shortenDisplayName(translated())
    }

    internal fun formatLegacyKey(keyCode: Int, keyName: String?): String? {
        if (keyCode == 0) return null
        if (keyCode < 0) {
            val button = keyCode + 101
            return if (button > 0) "M$button" else null
        }
        val raw = keyName?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        LEGACY_LABELS[raw]?.let { return it }
        if (raw.startsWith("NUMPAD") && raw.length == 7 && raw[6].isDigit()) return "N${raw[6]}"
        return shortenDisplayName(raw)
    }

    private fun shortenDisplayName(text: String): String? {
        val clean = text.trim()
        if (clean.isEmpty()) return null
        if (clean.length == 1) return clean.uppercase()
        return if (clean.length <= 3) clean else clean.take(3)
    }
}
