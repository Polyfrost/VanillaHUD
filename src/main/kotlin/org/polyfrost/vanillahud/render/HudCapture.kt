package org.polyfrost.vanillahud.render

//? if > 1.8.9 {
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence
//?} else {
/*import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.polyfrost.vanillahud.compat.GuiGraphicsExtractor
*///?}
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack
import org.polyfrost.compose.composables.PolyBox
import org.polyfrost.compose.composables.PolyCanvas
import org.polyfrost.compose.composables.PolyModifier
import org.polyfrost.compose.composables.absoluteAt
import org.polyfrost.compose.composables.size
import org.polyfrost.compose.render.FilterQuality
import org.polyfrost.compose.render.PolyBitmap
import org.polyfrost.compose.render.PolyColor
import org.polyfrost.compose.render.PolyFont
import org.polyfrost.compose.render.RenderContext
import org.polyfrost.oneconfig.api.hud.v1.Font
import org.polyfrost.oneconfig.api.ui.v1.item.PolyItemIcon
import org.polyfrost.vanillahud.hud.VanillaHud
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private val FORMAT_CODE = Regex("§.")

private val COLOR_CODE = Regex("§[0-9a-fA-F]")

// what an untouched HUD stores, which leaves the text to the game's own shadow colour
private const val DEFAULT_SHADOW = 0x40000000

// 1.8.9 only draws this solid when blending happens to be off, which it isn't in the editor's preview
private const val SCOREBOARD_TEXT = 0x20FFFFFF

/**
 * The colour the game's font really draws with: it treats an alpha below 4 as opaque, and the alpha
 * means nothing while blending is off, which is how 1.8.9's scoreboard text at `0x20FFFFFF` looks solid.
 */
internal fun fontColor(color: Int, blending: Boolean): Int =
    if (!blending || color ushr 26 == 0 || color == SCOREBOARD_TEXT) color or 0xFF000000.toInt() else color

/**
 * Lets Compose draw the HUD while the game's HUD code keeps running for other mods.
 *
 * Injections into an element and draws added to it still work. While an element runs, its GUI draw
 * calls land here and [Replay] draws them. Calls that can't be replayed reach the screen as normal,
 * underneath the Compose layer.
 *
 * A frame's calls are read back on the next frame, since the older OneConfig hosts paint the HUD
 * before the game has run it.
 */
object HudCapture {
    private data class Pose(val m00: Float, val m01: Float, val m10: Float, val m11: Float, val m20: Float, val m21: Float) {
        fun x(px: Float, py: Float) = m00 * px + m10 * py + m20

        fun y(px: Float, py: Float) = m01 * px + m11 * py + m21

        /** this pose with quarter [turns] applied around a point, used to keep icons upright */
        fun turned(turns: Int, cx: Float, cy: Float): Pose {
            val theta = -turns * (PI.toFloat() / 2f)
            val c = cos(theta)
            val n = sin(theta)
            val a00 = m00 * c + m10 * n
            val a01 = m01 * c + m11 * n
            val a10 = m10 * c - m00 * n
            val a11 = m11 * c - m01 * n
            return Pose(a00, a01, a10, a11, x(cx, cy) - a00 * cx - a10 * cy, y(cx, cy) - a01 * cx - a11 * cy)
        }
    }

    private data class Quad(val m: Pose, val bitmap: PolyBitmap?, val x0: Float, val y0: Float, val x1: Float, val y1: Float, val u0: Float, val v0: Float, val u1: Float, val v1: Float, val color: Int)

    private data class Run(val m: Pose, val text: String, val x: Float, val y: Float, val color: Int, val shadow: Boolean, val hud: VanillaHud)

    private data class ClipStart(val m: Pose, val x0: Float, val y0: Float, val x1: Float, val y1: Float)

    private object ClipEnd

    private data class Corners(val x0: Float, val y0: Float, val x1: Float, val y1: Float, val radius: Float, val stroke: Float)

    private data class Rounded(val m: Pose, val c: Corners, val x0: Float, val y0: Float, val x1: Float, val y1: Float, val color: Int)

    class Item(val stack: ItemStack, val x: Float, val y: Float, val size: Float) {
        var decorated = false

        fun same(o: Item) = x == o.x && y == o.y && size == o.size && decorated == o.decorated && ItemStack.matches(stack, o.stack)
    }

    private class Layer {
        var contentX = 0f
        var contentY = 0f
        var boxX = 0f
        var boxY = 0f
        var scale = 1f
        var hud: VanillaHud? = null
        var recording = ArrayList<Any>()
        var shown = ArrayList<Any>()
        var items by mutableStateOf<List<Item>>(emptyList())
    }

    private val layers = HashMap<Class<out VanillaHud>, Layer>()
    private val open = ArrayList<Layer>()

    private val active: Layer? get() = open.lastOrNull()

    private fun layer(hud: Class<out VanillaHud>) = layers.getOrPut(hud) { Layer() }

    @JvmStatic
    fun beginFrame() {
        open.clear()
        for (l in layers.values) {
            val done = l.recording
            if (!sameOps(done, l.shown)) l.hud?.capturedDrawsChanged()
            l.recording = l.shown.also { it.clear() }
            l.shown = done
            val items = done.filterIsInstance<Item>()
            val old = l.items
            if (items.size != old.size || items.indices.any { !items[it].same(old[it]) }) {
                l.items = items.map { i -> Item(i.stack.copy(), i.x, i.y, i.size).also { it.decorated = i.decorated } }
            }
        }
    }

    private fun sameOps(a: List<Any>, b: List<Any>): Boolean = a.size == b.size && a.indices.all {
        val x = a[it]
        val y = b[it]
        if (x is Item && y is Item) x.same(y) else x == y
    }

    fun begin(hud: VanillaHud) {
        open.add(layer(hud.javaClass).also { it.hud = hud })
    }

    fun end() {
        open.removeLastOrNull()
    }

    /** where the open element landed on screen, which is what a stretched background has to line up with */
    fun place(boxX: Float, boxY: Float, scale: Float, contentX: Float, contentY: Float) {
        val l = active ?: return
        l.boxX = boxX
        l.boxY = boxY
        l.scale = scale
        l.contentX = contentX
        l.contentY = contentY
    }

    //? if >=1.21.8 {
    private fun pose(g: GuiGraphicsExtractor) = g.pose().let { Pose(it.m00, it.m01, it.m10, it.m11, it.m20, it.m21) }
    //?} elif > 1.8.9 {
    /*private fun pose(g: GuiGraphicsExtractor) = g.pose().last().pose().let { Pose(it.m00(), it.m01(), it.m10(), it.m11(), it.m30(), it.m31()) }
    *///?} else {
    /*private val modelView = BufferUtils.createFloatBuffer(16)

    private fun pose(g: GuiGraphicsExtractor): Pose {
        modelView.clear()
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelView)
        return Pose(modelView.get(0), modelView.get(1), modelView.get(4), modelView.get(5), modelView.get(12), modelView.get(13))
    }
    *///?}

    /** the pose for a sprite, turned back upright when an icon layer rotates with its element */
    private fun iconPose(g: GuiGraphicsExtractor, x: Int, y: Int, w: Int, h: Int): Pose {
        val p = pose(g)
        //? if > 1.8.9 {
        return p
        //?} else
        //return HudTransform.iconTurns.let { if (it == 0) p else p.turned(it, x + w / 2f, y + h / 2f) }
    }

    //? if > 1.8.9 {
    @JvmStatic
    fun sprite(g: GuiGraphicsExtractor, sprite: TextureAtlasSprite, x: Int, y: Int, w: Int, h: Int, color: Int): Boolean =
        sprite(g, sprite, 1, 1, 0, 0, 1, 1, x, y, w, h, color)

    @JvmStatic
    fun sprite(g: GuiGraphicsExtractor, sprite: TextureAtlasSprite, texW: Int, texH: Int, u: Int, v: Int, uW: Int, vH: Int, x: Int, y: Int, w: Int, h: Int, color: Int): Boolean {
        val l = active ?: return false
        val bitmap = SpriteBitmaps.of(sprite) ?: return false
        // an animated sprite's file holds every frame, this keeps to the first one
        val frame = sprite.contents().height().toFloat() / bitmap.height
        val dy = HudTransform.mirrorShift(y, y + h)
        l.recording += Quad(pose(g), bitmap, x.toFloat(), (y + dy).toFloat(), (x + w).toFloat(), (y + h + dy).toFloat(), u.toFloat() / texW, v.toFloat() / texH * frame, (u + uW).toFloat() / texW, (v + vH).toFloat() / texH * frame, color)
        return true
    }

    @JvmStatic
    fun texture(g: GuiGraphicsExtractor, id: Identifier, x0: Int, x1: Int, y0: Int, y1: Int, u0: Float, u1: Float, v0: Float, v1: Float, color: Int): Boolean {
        val l = active ?: return false
        val bitmap = SpriteBitmaps.of(id) ?: return false
        val dy = HudTransform.mirrorShift(y0, y1)
        l.recording += Quad(pose(g), bitmap, x0.toFloat(), (y0 + dy).toFloat(), x1.toFloat(), (y1 + dy).toFloat(), u0, v0, u1, v1, color)
        return true
    }
    //?} else {
    /*private var bound: Identifier? = null
    private var tint = -1

    @JvmStatic
    fun bind(id: Identifier?) {
        bound = id
    }

    @JvmStatic
    fun color(r: Float, g: Float, b: Float, a: Float) {
        tint = ((a * 255f).toInt() shl 24) or ((r * 255f).toInt() shl 16) or ((g * 255f).toInt() shl 8) or (b * 255f).toInt()
    }

    @JvmStatic
    fun isActive() = capturing

    @JvmStatic
    fun texture(g: GuiGraphicsExtractor, x: Int, y: Int, w: Int, h: Int, u0: Float, v0: Float, u1: Float, v1: Float): Boolean {
        val l = active ?: return false
        val bitmap = SpriteBitmaps.of(bound ?: return false) ?: return false
        l.recording += Quad(iconPose(g, x, y, w, h), bitmap, x.toFloat(), y.toFloat(), (x + w).toFloat(), (y + h).toFloat(), u0, v0, u1, v1, tint)
        return true
    }
    *///?}

    private var corners: Corners? = null

    val capturing: Boolean get() = active != null

    private val SCREEN = Pose(1f, 0f, 0f, 1f, 0f, 0f)

    private var inBackground = false

    /** whether the open element's background has to go through [background] */
    val stretching: Boolean
        get() = active?.hud?.let { it.stretchesBackground && it.customBackgroundBox } == true

    inline fun background(block: () -> Unit) {
        setBackground(true)
        try {
            block()
        } finally {
            setBackground(false)
        }
    }

    @PublishedApi
    internal fun setBackground(value: Boolean) {
        inBackground = value
    }

    /**
     * The game's fills for this background each cover part of the content, in whatever pose it drew
     * them. Edges on the content's outline move out to the box, and all of them clip one rounded box.
     */
    private fun stretched(l: Layer, hud: VanillaHud, m: Pose, x0: Int, y0: Int, x1: Int, y1: Int, color: Int) {
        val drawn = l.scale * hud.elementScale
        val tolerance = 1.5f * drawn
        val contentR = l.contentX + hud.width * drawn
        val contentB = l.contentY + hud.height * drawn
        val boxR = l.boxX + hud.boxWidth * l.scale
        val boxB = l.boxY + hud.boxHeight * l.scale
        fun snap(v: Float, edge: Float, to: Float) = if (abs(v - edge) <= tolerance) to else v
        val left = snap(m.x(minOf(x0, x1).toFloat(), 0f), l.contentX, l.boxX)
        val right = snap(m.x(maxOf(x0, x1).toFloat(), 0f), contentR, boxR)
        val top = snap(m.y(0f, minOf(y0, y1).toFloat()), l.contentY, l.boxY)
        val bottom = snap(m.y(0f, maxOf(y0, y1).toFloat()), contentB, boxB)
        val box = Corners(l.boxX, l.boxY, boxR, boxB, hud.bgRadius * l.scale, 0f)
        l.recording += Rounded(SCREEN, box, left, top, right, bottom, color)
    }

    /**
     * Fills made inside [block] become the part of one rounded box they cover, or its outline when
     * [stroke] is set. Compose would snap the sub-pixel strips the game needs for a curve to whole
     * pixels, and the overlaps would show as dark bars.
     */
    inline fun rounded(x0: Int, y0: Int, x1: Int, y1: Int, radius: Int, stroke: Int = 0, block: () -> Unit) {
        beginRounded(x0, y0, x1, y1, radius, stroke)
        try {
            block()
        } finally {
            endRounded()
        }
    }

    @PublishedApi
    internal fun beginRounded(x0: Int, y0: Int, x1: Int, y1: Int, radius: Int, stroke: Int) {
        corners = Corners(x0.toFloat(), y0.toFloat(), x1.toFloat(), y1.toFloat(), radius.toFloat(), stroke.toFloat())
    }

    @PublishedApi
    internal fun endRounded() {
        corners = null
    }

    @JvmStatic
    fun fill(g: GuiGraphicsExtractor, x0: Int, y0: Int, x1: Int, y1: Int, color: Int): Boolean {
        val l = active ?: return false
        l.hud?.let { hud ->
            if ((inBackground || hud.everyFillIsBackground) && stretching) {
                stretched(l, hud, pose(g), x0, y0, x1, y1, color)
                return true
            }
        }
        corners?.let {
            l.recording += Rounded(pose(g), it, minOf(x0, x1).toFloat(), minOf(y0, y1).toFloat(), maxOf(x0, x1).toFloat(), maxOf(y0, y1).toFloat(), color)
            return true
        }
        l.recording += Quad(pose(g), null, minOf(x0, x1).toFloat(), minOf(y0, y1).toFloat(), maxOf(x0, x1).toFloat(), maxOf(y0, y1).toFloat(), 0f, 0f, 0f, 0f, color)
        return true
    }

    @JvmStatic
    fun clipStart(g: GuiGraphicsExtractor, x0: Int, y0: Int, x1: Int, y1: Int) {
        active?.recording?.add(ClipStart(pose(g), x0.toFloat(), y0.toFloat(), x1.toFloat(), y1.toFloat()))
    }

    @JvmStatic
    fun clipEnd() {
        active?.recording?.add(ClipEnd)
    }

    @JvmStatic
    fun item(g: GuiGraphicsExtractor, stack: ItemStack, x: Int, y: Int): Boolean {
        val l = active ?: return false
        //? if > 1.8.9
        if (stack.isEmpty) return true
        val m = pose(g)
        val scale = hypot(m.m00, m.m01)
        l.recording += Item(stack, m.x(x + 8f, y + 8f) - 8f * scale, m.y(x + 8f, y + 8f) - 8f * scale, 16f * scale)
        return true
    }

    //? if = 1.8.9 {
    /*/** servers pad lines with characters the game's font draws nothing for, which Compose's fallback font shows as boxes */
    private fun drawableText(text: String): String {
        if (text.all { it.code < 128 }) return text
        val font = Minecraft.getInstance().font
        return buildString(text.length) {
            var i = 0
            while (i < text.length) {
                val c = text[i++]
                if (c == '§' && i < text.length) append(c).append(text[i++])
                else if (font.width(c.toString()) > 0) append(c)
            }
        }
    }

    /** the count and durability bar, which the 1.8.9 icon draws itself since the game paints them straight to GL */
    @JvmStatic
    fun itemDecorations(): Boolean {
        val l = active ?: return false
        (l.recording.lastOrNull() as? Item)?.decorated = true
        return true
    }
    *///?}

    @JvmStatic
    fun text(g: GuiGraphicsExtractor, text: String, x: Int, y: Int, color: Int, shadow: Boolean): Boolean {
        val l = active ?: return false
        val hud = l.hud ?: return false
        //? if >=1.21.6 {
        if (color ushr 24 != 0) addRuns(l, hud, pose(g), listOf(Run0("", null, text)), x, y, color, shadow)
        //?} elif > 1.8.9 {
        /*addRuns(l, hud, pose(g), listOf(Run0("", null, text)), x, y, fontColor(color, blending = true), shadow)
        *///?} else {
        /*addRuns(l, hud, pose(g), listOf(Run0("", null, drawableText(text))), x, y, fontColor(color, GL11.glIsEnabled(GL11.GL_BLEND)), shadow)
        *///?}
        return true
    }

    //? if > 1.8.9 {
    /** one run per colour since the Compose text call takes a single RGB, false when it needs a font Compose can't draw */
    @JvmStatic
    fun text(g: GuiGraphicsExtractor, text: FormattedCharSequence, x: Int, y: Int, color: Int, shadow: Boolean): Boolean {
        val l = active ?: return false
        val hud = l.hud ?: return false
        //? if >=1.21.6 {
        if (color ushr 24 == 0) return true
        //?}
        val defaultFont = Style.EMPTY.font
        var custom = false
        val runs = ArrayList<Pair<Style, StringBuilder>>()
        text.accept { _, style, codepoint ->
            if (style.font != defaultFont) custom = true
            if (runs.lastOrNull()?.first != style) runs += style to StringBuilder()
            runs.last().second.appendCodePoint(codepoint)
            !custom
        }
        if (custom) return false
        addRuns(l, hud, pose(g), runs.map { (style, sb) -> Run0(codes(style), style.color?.value, sb.toString()) }, x, y,
            //? if >=1.21.6 {
            color,
            //?} else
            //fontColor(color, blending = true),
            shadow)
        return true
    }

    private fun codes(style: Style) = buildString {
        if (style.isBold) append("§l")
        if (style.isItalic) append("§o")
        if (style.isUnderlined) append("§n")
        if (style.isStrikethrough) append("§m")
        if (style.isObfuscated) append("§k")
    }
    //?}

    private class Run0(val codes: String, val rgb: Int?, val text: String)

    private fun addRuns(l: Layer, hud: VanillaHud, m: Pose, runs: List<Run0>, x: Int, y: Int, color: Int, shadow: Boolean) {
        var cursor = x.toFloat()
        for (r in runs) {
            val run = styled(hud, r)
            l.recording += Run(m, run, cursor, y.toFloat(), runColor(hud, r.rgb, color), shadow || hud.showShadow, hud)
            cursor += widthOf(hud, run)
        }
    }

    private fun styled(hud: VanillaHud, r: Run0) = buildString {
        if (hud.textBold) append("§l")
        if (hud.textItalic) append("§o")
        if (hud.textUnderline) append("§n")
        append(r.codes)
        append(when (hud.caseType) {
            1 -> r.text.uppercase()
            2 -> r.text.lowercase()
            else -> r.text
        })
    }

    // black is left alone because vanilla outlines the experience level by drawing it in black
    private fun runColor(hud: VanillaHud, styled: Int?, color: Int): Int {
        if (styled != null) return (color and 0xFF000000.toInt()) or styled
        val custom = hud.textColor != -1 || hud.textChroma
        if (!custom || hud.managesTextColor || color and 0xFFFFFF == 0) return color
        val picked = PolyColor(hud.textColor, hud.textChroma, hud.textChromaSpeed).argb
        val alpha = (picked ushr 24) * (color ushr 24) / 255
        return (alpha shl 24) or (picked and 0xFFFFFF)
    }

    private fun poppins(hud: VanillaHud) = PolyFont(hud.getPoppinsFontName(), 8f)

    private fun widthOf(hud: VanillaHud, text: String) =
        if (hud.font == Font.Poppins) poppins(hud).measureTextWidth(text.replace(FORMAT_CODE, ""))
        else Minecraft.getInstance().font.width(text).toFloat()

    private fun RenderContext.drawRun(op: Run) {
        val hud = op.hud
        val shadow = PolyColor(hud.shadowColor, hud.shadowChroma, hud.shadowChromaSpeed).argb
        if (hud.font == Font.Poppins) {
            val plain = op.text.replace(FORMAT_CODE, "")
            val font = poppins(hud)
            if (op.shadow) text(plain, op.x + hud.shadowOffsetX / 2f, op.y + hud.shadowOffsetY / 2f, PolyColor(shadow), font)
            text(plain, op.x, op.y, PolyColor(op.color), font)
        } else if (op.shadow && hud.fullShadow) {
            mcTextOutline(op.text, op.x, op.y, op.color, shadow, 1f)
        } else if (op.shadow && (hud.shadowColor != DEFAULT_SHADOW || hud.shadowChroma)) {
            mcText(op.text.replace(COLOR_CODE, ""), op.x + 1f, op.y + 1f, shadow, false, 1f)
            mcText(op.text, op.x, op.y, op.color, false, 1f)
        } else {
            mcText(op.text, op.x, op.y, op.color, op.shadow, 1f)
        }
    }

    private fun RenderContext.replay(ops: List<Any>, from: Int, until: Int) {
        var clips = 0
        for (i in from until until) {
            when (val op = ops[i]) {
                is Quad -> withPose(op.m) {
                    if (op.bitmap == null) rect(op.x0, op.y0, op.x1 - op.x0, op.y1 - op.y0, PolyColor(op.color))
                    else renderer.image(op.bitmap, op.x0, op.y0, op.x1 - op.x0, op.y1 - op.y0, op.u0, op.v0, op.u1, op.v1, op.color, FilterQuality.None)
                }
                is Run -> withPose(op.m) { drawRun(op) }
                is Rounded -> withPose(op.m) {
                    val c = op.c
                    save()
                    clipRect(op.x0, op.y0, op.x1 - op.x0, op.y1 - op.y0)
                    if (c.stroke > 0f) rectStroke(c.x0, c.y0, c.x1 - c.x0, c.y1 - c.y0, PolyColor(op.color), c.stroke, c.radius)
                    else rect(c.x0, c.y0, c.x1 - c.x0, c.y1 - c.y0, PolyColor(op.color), c.radius)
                    restore()
                }
                is ClipStart -> {
                    save()
                    withPoseOpen(op.m)
                    clipRect(op.x0, op.y0, op.x1 - op.x0, op.y1 - op.y0)
                    undoPose(op.m)
                    clips++
                }
                is ClipEnd -> if (clips > 0) {
                    restore()
                    clips--
                }
            }
        }
        repeat(clips) { restore() }
    }

    private inline fun RenderContext.withPose(m: Pose, draw: RenderContext.() -> Unit) {
        save()
        withPoseOpen(m)
        draw()
        restore()
    }

    private fun RenderContext.withPoseOpen(m: Pose) {
        translate(m.m20, m.m21)
        val turn = Math.toDegrees(atan2(m.m01, m.m00).toDouble()).toFloat()
        if (turn != 0f) renderer.rotate(turn)
        val sx = hypot(m.m00, m.m01)
        if (sx != 1f) scale(sx, hypot(m.m10, m.m11))
    }

    private fun RenderContext.undoPose(m: Pose) {
        val sx = hypot(m.m00, m.m01)
        if (sx != 1f) scale(1f / sx, 1f / hypot(m.m10, m.m11))
        val turn = Math.toDegrees(atan2(m.m01, m.m00).toDouble()).toFloat()
        if (turn != 0f) renderer.rotate(-turn)
        translate(-m.m20, -m.m21)
    }

    /** draws what [hud] last recorded, read each frame so vanilla's animation needs no recomposition */
    @Composable
    fun Replay(hud: VanillaHud, width: Float, height: Float) {
        val layer = remember(hud.javaClass) { layer(hud.javaClass) }
        val live = HudTransform.live(hud)
        fun RenderContext.painting(x: Float, y: Float, draw: RenderContext.() -> Unit) {
            val s = live.frameScale
            save()
            translate(x, y)
            if (s != 1f) scale(1f / s, 1f / s)
            translate(-live.frameX, -live.frameY)
            draw()
            restore()
        }
        PolyBox(modifier = hud.hudBackground(PolyModifier.size(width, height))) {
            PolyCanvas(PolyModifier.size(width, height), live = true) { x, y, _, _ ->
                val ops = layer.shown
                val split = ops.indexOfFirst { it is Item }.let { if (it < 0) ops.size else it }
                painting(x, y) { replay(ops, 0, split) }
            }
            for ((i, item) in layer.items.withIndex()) key(i) {
                val s = live.frameScale
                PolyItemIcon(
                    item.stack, item.size / s,
                    PolyModifier.absoluteAt((item.x - live.frameX) / s, (item.y - live.frameY) / s),
                    decorations = item.decorated,
                )
            }
            PolyCanvas(PolyModifier.size(width, height), live = true) { x, y, _, _ ->
                val ops = layer.shown
                val split = ops.indexOfFirst { it is Item }
                if (split >= 0) painting(x, y) { replay(ops, split + 1, ops.size) }
            }
        }
    }
}

/** the pixels behind the textures vanilla's HUD draws, read fresh when a resource reload swaps the sprite */
private object SpriteBitmaps {
    private class Entry(val owner: Any, val bitmap: PolyBitmap?)

    private val cache = HashMap<Identifier, Entry>()

    private val mc get() = Minecraft.getInstance()

    private inline fun cached(key: Identifier, owner: Any, read: () -> PolyBitmap?): PolyBitmap? {
        cache[key]?.let { if (it.owner === owner) return it.bitmap }
        return read().also { cache[key] = Entry(owner, it) }
    }

    //? if > 1.8.9 {
    fun of(sprite: TextureAtlasSprite): PolyBitmap? {
        val name = sprite.contents().name()
        val folder = when (sprite.atlasLocation().path) {
            "textures/atlas/gui.png" -> "textures/gui/sprites/"
            "textures/atlas/mob_effects.png" -> "textures/mob_effect/"
            else -> return null
        }
        return cached(name, sprite.contents()) { load(name.withPath("$folder${name.path}.png")) }
    }

    fun of(id: Identifier): PolyBitmap? {
        val texture = mc.textureManager.getTexture(id)
        if (texture is TextureAtlas) return null
        return cached(id, texture) {
            if (texture is DynamicTexture) texture.pixels?.let { image ->
                val pixels = ByteArray(image.width * image.height * 4)
                for (y in 0 until image.height) for (x in 0 until image.width) {
                    //? if >=1.21.2 {
                    val c = image.getPixel(x, y)
                    //?} else {
                    /*val abgr = image.getPixelRGBA(x, y)
                    val c = (abgr and 0xFF00FF00.toInt()) or (abgr shl 16 and 0xFF0000) or (abgr ushr 16 and 0xFF)
                    *///?}
                    val i = (y * image.width + x) * 4
                    pixels[i] = (c shr 16).toByte()
                    pixels[i + 1] = (c shr 8).toByte()
                    pixels[i + 2] = c.toByte()
                    pixels[i + 3] = (c ushr 24).toByte()
                }
                PolyBitmap(image.width, image.height, pixels)
            } else load(id)
        }
    }

    private fun load(id: Identifier): PolyBitmap? = runCatching {
        mc.resourceManager.getResource(id).orElse(null)?.open()?.use { PolyBitmap.decode(it.readBytes()) }
    }.getOrNull()
    //?} else {
    /*fun of(id: Identifier): PolyBitmap? {
        val texture = mc.textureManager.get(id) ?: return null
        return cached(id, texture) { load(id) ?: readBack(id) }
    }

    private fun load(id: Identifier): PolyBitmap? = runCatching {
        mc.resourceManager.getResource(id).asStream().use { PolyBitmap.decode(it.readBytes()) }
    }.getOrNull()

    /** skins and other runtime textures only exist on the GPU here, read once per texture */
    private fun readBack(id: Identifier): PolyBitmap? = runCatching {
        mc.textureManager.bind(id)
        val w = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH)
        val h = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT)
        if (w <= 0 || h <= 0) return null
        val buffer = BufferUtils.createByteBuffer(w * h * 4)
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer)
        PolyBitmap(w, h, ByteArray(w * h * 4).also { buffer.get(it) })
    }.getOrNull()
    *///?}
}
