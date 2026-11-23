package org.polyfrost.vanillahud.render

//? if > 1.8.9 {
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence
import org.polyfrost.compose.minecraft.platform.HostEvents
import java.util.Optional
//?} else {
/*import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.polyfrost.vanillahud.compat.GuiGraphicsExtractor
*///?}
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font as McFont
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack
import org.polyfrost.compose.composables.NodeBox
import org.polyfrost.compose.composables.PolyCanvas
import org.polyfrost.compose.composables.PolyModifier
import org.polyfrost.compose.composables.size
import org.polyfrost.compose.render.FilterQuality
import org.polyfrost.compose.render.PolyBitmap
import org.polyfrost.compose.render.PolyColor
import org.polyfrost.compose.render.PolyFont
import org.polyfrost.compose.render.RenderContext
import org.polyfrost.oneconfig.api.hud.v1.Font
import org.polyfrost.oneconfig.api.ui.v1.ExperimentalOneConfigUiApi
import org.polyfrost.compose.minecraft.mc.GuiItemRenderer
import org.polyfrost.compose.minecraft.mc.McItemIcons
import org.polyfrost.compose.minecraft.mc.McItemIconHandle
import org.polyfrost.compose.minecraft.mc.McHeadStyle
import org.polyfrost.compose.minecraft.mc.McPlayerFaces
import org.polyfrost.compose.minecraft.mc.McSkinLayers
import org.polyfrost.compose.minecraft.platform.McWindow
import net.fabricmc.loader.api.FabricLoader
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.vanillahud.hud.ClosedCaptionsHud
import org.polyfrost.vanillahud.hud.ExperienceLevelHud
import org.polyfrost.vanillahud.hud.HudStyle
import org.polyfrost.vanillahud.hud.VanillaHud
import java.util.TreeMap
import java.util.UUID
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.round
import kotlin.math.sin

private val COLOR_CODE = Regex("§[0-9a-fA-F]")

private val LEGACY_COLORS = intArrayOf(
    0x000000, 0x0000AA, 0x00AA00, 0x00AAAA, 0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA,
    0x555555, 0x5555FF, 0x55FF55, 0x55FFFF, 0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF,
)

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

    private data class Tint(val kind: Int, val hud: VanillaHud, val param: Int)

    private data class Quad(val m: Pose, val bitmap: PolyBitmap?, val x0: Float, val y0: Float, val x1: Float, val y1: Float, val u0: Float, val v0: Float, val u1: Float, val v1: Float, val color: Int, val tint: Tint? = null)

    private data class Run(val m: Pose, val text: String, val x: Float, val y: Float, val color: Int, val shadow: Boolean, val hud: VanillaHud, val chroma: Boolean, val tint: Tint?) {
        var key: TextKey? = null

        fun same(m: Pose, text: String, x: Float, y: Float, color: Int, shadow: Boolean, hud: VanillaHud, chroma: Boolean, tint: Tint?) =
            this.x.toRawBits() == x.toRawBits() && this.y.toRawBits() == y.toRawBits() && this.color == color && this.shadow == shadow &&
                this.chroma == chroma && this.hud === hud && this.tint == tint && this.m == m && this.text == text
    }

    private data class Head(val m: Pose, val layers: McSkinLayers, val x: Float, val y: Float, val size: Float, val style: McHeadStyle, val flip: Boolean, val color: Int) {
        fun same(m: Pose, layers: McSkinLayers, x: Float, y: Float, size: Float, style: McHeadStyle, flip: Boolean, color: Int) =
            this.layers === layers && this.x.toRawBits() == x.toRawBits() && this.y.toRawBits() == y.toRawBits() &&
                this.size.toRawBits() == size.toRawBits() && this.style == style && this.flip == flip && this.color == color && this.m == m
    }

    private class TextKey(val src: String, val style: Int, val font: Int, val runs: List<Run0>, val texts: Array<String>)

    private data class ClipStart(val m: Pose, val x0: Float, val y0: Float, val x1: Float, val y1: Float)

    private object ClipEnd

    private data class Corners(val x0: Float, val y0: Float, val x1: Float, val y1: Float, val radius: Float, val stroke: Float)

    private data class Rounded(val m: Pose, val c: Corners, val x0: Float, val y0: Float, val x1: Float, val y1: Float, val color: Int, val tint: Tint? = null)

    /** [width] and [height] differ while the game stretches an item that was just picked up */
    class Item(val stack: ItemStack, val x: Float, val y: Float, val width: Float, val height: Float) {
        var decorated = false

        //? if = 1.8.9 {
        /*private val size = stack.size
        private val damage = stack.damage
        private val nbt = stack.nbt

        private fun sameStack(o: Item) = stack === o.stack && size == o.size && damage == o.damage && nbt === o.nbt ||
            ItemStack.matches(stack, o.stack)
        *///?} else {
        private fun sameStack(o: Item) = ItemStack.matches(stack, o.stack)
        //?}

        fun same(o: Item) = x == o.x && y == o.y && width == o.width && height == o.height && decorated == o.decorated && sameStack(o)
    }

    /** what one of the game's HUD elements drew into a layer, which several of them can feed */
    private class Part(val layer: Layer) {
        var ran = false
        var recording = ArrayList<Any>()
        var shown = ArrayList<Any>()
    }

    private class Layer {
        var contentX = 0f
        var contentY = 0f
        var boxX = 0f
        var boxY = 0f
        var scale = 1f
        var frameX = 0f
        var frameY = 0f
        var shownX = 0f
        var shownY = 0f
        var shownScale = 1f
        var hud: VanillaHud? = null
        // replayed in key order, so a background part comes before what's drawn over it
        val parts = TreeMap<Int, Part>()
        val partList = ArrayList<Part>()
        /** the open part's recording */
        var recording = ArrayList<Any>()
        var previous = ArrayList<Any>()
        var shown = ArrayList<Any>()
        var spare = ArrayList<Any>()
        val icons = ArrayList<McItemIconHandle>()
    }

    private val layers = HashMap<Class<out VanillaHud>, Layer>()
    private val open = ArrayList<Part>()

    private var active: Layer? = null

    private fun layer(hud: Class<out VanillaHud>) = layers.getOrPut(hud) { Layer() }

    private var guiFramePending = false

    @JvmStatic
    fun beginFrame() {
        //? if > 1.8.9 {
        guiFramePending = true
        //?} else
        //swap()
    }

    @JvmStatic
    fun swapBeforePrepare() {
        if (!guiFramePending) return
        guiFramePending = false
        swap()
    }

    private const val TINT_BACKGROUND = 1
    private const val TINT_BORDER = 2
    private const val TINT_CAPTION = 3

    private val tints = arrayOfNulls<Tint>(32)
    private val tintRgb = IntArray(tints.size)
    private var tintCount = 0

    private fun registerTint(kind: Int, hud: VanillaHud, param: Int, argb: Int, staticArgb: Int) {
        if (active == null) return
        val rgb = argb and 0xFFFFFF
        if (rgb == staticArgb and 0xFFFFFF) return
        for (i in 0 until tintCount) {
            if (tintRgb[i] != rgb) continue
            val t = tints[i]
            if (t != null && (t.kind != kind || t.hud !== hud || t.param != param)) tints[i] = null
            return
        }
        if (tintCount == tints.size) return
        tintRgb[tintCount] = rgb
        tints[tintCount++] = Tint(kind, hud, param)
    }

    private fun tintOf(color: Int): Tint? {
        if (tintCount == 0) return null
        val rgb = color and 0xFFFFFF
        for (i in 0 until tintCount) if (tintRgb[i] == rgb) return tints[i]
        return null
    }

    private fun stable(color: Int, tint: Tint?) = if (tint == null) color else color and 0xFF000000.toInt()

    private fun argbOf(argb: Int, chroma: Boolean, speed: Float): Int = if (chroma) PolyColor(argb, true, speed).argb else argb

    @OptIn(ExperimentalOneConfigUiApi::class)
    private fun tinted(color: Int, tint: Tint?): Int {
        if (tint == null) return color
        val hud = tint.hud
        val live = when (tint.kind) {
            TINT_BACKGROUND -> hud.backgroundStyle.let { argbOf(it.bgColor, it.bgChroma, it.bgChromaSpeed) }
            TINT_BORDER -> hud.backgroundStyle.let { argbOf(it.borderColor, it.borderChroma, it.borderChromaSpeed) }
            else -> (hud as? ClosedCaptionsHud)?.captionTextRgb(tint.param) ?: 0
        }
        return (color and 0xFF000000.toInt()) or (live and 0xFFFFFF)
    }

    @JvmStatic
    fun backgroundTint(hud: VanillaHud, argb: Int): Int {
        val s = hud.backgroundStyle
        if (s.bgChroma) registerTint(TINT_BACKGROUND, hud, 0, argb, s.bgColor)
        return argb
    }

    @JvmStatic
    @OptIn(ExperimentalOneConfigUiApi::class)
    fun borderTint(hud: VanillaHud, argb: Int): Int {
        val s = hud.backgroundStyle
        if (s.borderEnabled && s.borderChroma) registerTint(TINT_BORDER, hud, 0, argb, s.borderColor)
        return argb
    }

    fun captionTint(hud: ClosedCaptionsHud, fade: Int, argb: Int, staticArgb: Int): Int {
        if (hud.textStyle.textChroma) registerTint(TINT_CAPTION, hud, fade, argb, staticArgb)
        return argb
    }

    private fun swap() {
        tintCount = 0
        open.clear()
        active = null
        var iconsChanged = false
        val previewing = VanillaHud.previewing()
        val previewChanged = gnetum && previewing != wasPreviewing
        wasPreviewing = previewing
        for (l in layers.values) {
            val hud = l.hud
            val held = !previewChanged && hud != null && gnetum && wouldDraw(hud)
            var ran = false
            val parts = l.partList
            for (i in parts.indices) {
                val p = parts[i]
                if (p.ran && !previewChanged) {
                    ran = true
                    val done = p.recording
                    p.recording = p.shown.also { it.clear() }
                    p.shown = done
                } else {
                    p.recording.clear()
                    if (!held) p.shown.clear()
                }
                p.ran = false
            }
            if (held && !ran) continue
            val done = l.spare.also { it.clear() }
            for (i in parts.indices) {
                val src = parts[i].shown
                done.ensureCapacity(done.size + src.size)
                for (j in src.indices) done.add(src[j])
            }
            val moved = l.shownX != l.frameX || l.shownY != l.frameY || l.shownScale != l.scale
            val shown = l.shown
            var sameOps = !moved && done.size == shown.size
            if (sameOps) for (i in done.indices) {
                val op = done[i]
                val y = shown[i]
                if (if (op is Item && y is Item) op.same(y) else op == y) continue
                sameOps = false
                break
            }
            if (!sameOps) l.hud?.capturedDrawsChanged()
            l.spare = l.shown
            l.shown = done
            l.shownX = l.frameX
            l.shownY = l.frameY
            l.shownScale = l.scale
            if (syncIcons(l)) iconsChanged = true
        }
        // render new/changed icons now, since the atlas's own pass comes after OneConfig draws this frame
        //? if >=1.21.11
        if (iconsChanged) McItemIcons.renderPending()
    }

    private fun syncIcons(l: Layer): Boolean {
        val hud = l.hud
        var n = 0
        var changed = false
        if (hud != null) for (op in l.shown) {
            if (op !is Item) continue
            val icon = l.icons.getOrNull(n) ?: McItemIcons.open(op.stack).also {
                l.icons += it
                changed = true
            }
            if (icon.stack !== op.stack) {
                icon.update(op.stack)
                changed = true
            }
            // the atlas only bumps the revision for count and durability changes on icons that show them
            icon.decorations = op.decorated
            icon.setBaseSize(restSize(hud, op, l.scale) * McWindow.guiScale(), 1.5f)
            n++
        }
        while (l.icons.size > n) {
            l.icons.removeLast().close()
            changed = true
        }
        return changed
    }

    /** an icon's resting size in the game's units, which it keeps while the game stretches a picked up item */
    private fun restSize(hud: VanillaHud, op: Item, scale: Float) = if (op.width == op.height) op.width else 16f * hud.elementScale * scale

    @JvmStatic
    fun closeIcons() {
        open.clear()
        active = null
        for (l in layers.values) {
            for (icon in l.icons) icon.close()
            l.icons.clear()

            // drop the recordings too, or a HUD could replay them before its first capture in the next world
            val hadDraws = l.shown.isNotEmpty()
            l.parts.clear()
            l.partList.clear()
            l.recording.clear()
            l.previous.clear()
            l.shown.clear()
            l.spare.clear()
            if (hadDraws) l.hud?.capturedDrawsChanged()
        }
    }

    // Gnetum runs each HUD element on only one of its passes, and on none while it holds the HUD's
    // framerate down, so an element that skipped a frame keeps what it drew last time. That goes per
    // part, since the elements feeding one layer, like the food bar and mount health, can land on
    // different passes.
    private val gnetum = FabricLoader.getInstance().isModLoaded("gnetum")

    private var wasPreviewing = false

    private fun wouldDraw(hud: VanillaHud) = hud.shouldDraw() && (HudManager.isEditing || hud.hasContent())

    /** [part] tells apart the game's HUD elements that draw into the same [hud] */
    fun begin(hud: VanillaHud, part: Int = 0) {
        if (open.isEmpty()) tintCount = 0
        val l = layer(hud.javaClass)
        l.hud = hud
        val p = l.parts[part] ?: Part(l).also {
            l.parts[part] = it
            l.partList.clear()
            l.partList.addAll(l.parts.values)
        }
        p.ran = true
        l.recording = p.recording
        l.previous = p.shown
        open.add(p)
        active = l
        //? if = 1.8.9
        //modelViewStale = true
    }

    /** set by the game's locator bar whenever it draws, which tells a draw Gnetum skipped from one that had nothing to show */
    @JvmField
    var locatorDrawn = false

    /** keeps what [part] of [hud] drew last time, for when the game's element that feeds it didn't run */
    @JvmStatic
    fun skipped(hud: VanillaHud, part: Int) {
        layers[hud.javaClass]?.parts?.get(part)?.ran = false
    }

    /** whether [hud] is being drawn right now, which a nested draw of it must not transform again */
    @JvmStatic
    fun isCapturing(hud: VanillaHud) = open.any { it.layer === layers[hud.javaClass] }

    fun end() {
        //? if = 1.8.9
        //modelViewStale = true
        val p = open.removeLastOrNull() ?: return
        for (i in open.lastIndex downTo 0) {
            val q = open[i]
            if (q.layer === p.layer) {
                p.layer.recording = q.recording
                p.layer.previous = q.shown
                break
            }
        }
        active = open.lastOrNull()?.layer
    }

    /** where the open element landed on screen, which is what a stretched background has to line up with */
    fun place(frameX: Float, frameY: Float, boxX: Float, boxY: Float, scale: Float, contentX: Float, contentY: Float) {
        val l = active ?: return
        l.frameX = frameX
        l.frameY = frameY
        l.boxX = boxX
        l.boxY = boxY
        l.scale = scale
        l.contentX = contentX
        l.contentY = contentY
    }

    private var lastPose = Pose(0f, 0f, 0f, 0f, 0f, 0f)

    private val poses = arrayOfNulls<Pose>(64)
    private var poseNext = 0

    private fun samePose(p: Pose, m00: Float, m01: Float, m10: Float, m11: Float, m20: Float, m21: Float) =
        p.m20.toRawBits() == m20.toRawBits() && p.m21.toRawBits() == m21.toRawBits() && p.m00.toRawBits() == m00.toRawBits() &&
            p.m01.toRawBits() == m01.toRawBits() && p.m10.toRawBits() == m10.toRawBits() && p.m11.toRawBits() == m11.toRawBits()

    private fun poseOf(m00: Float, m01: Float, m10: Float, m11: Float, m20: Float, m21: Float): Pose {
        val p = lastPose
        if (samePose(p, m00, m01, m10, m11, m20, m21)) return p
        val ring = poses
        var i = poseNext
        var left = ring.size
        while (left-- > 0) {
            i = if (i == 0) ring.size - 1 else i - 1
            val q = ring[i] ?: continue
            if (samePose(q, m00, m01, m10, m11, m20, m21)) {
                lastPose = q
                return q
            }
        }
        val made = Pose(m00, m01, m10, m11, m20, m21)
        ring[poseNext] = made
        poseNext = if (poseNext == ring.size - 1) 0 else poseNext + 1
        lastPose = made
        return made
    }

    //? if >=1.21.8 {
    private fun pose(g: GuiGraphicsExtractor) = g.pose().let { poseOf(it.m00, it.m01, it.m10, it.m11, it.m20, it.m21) }
    //?} elif > 1.8.9 {
    /*private fun pose(g: GuiGraphicsExtractor) = g.pose().last().pose().let { poseOf(it.m00(), it.m01(), it.m10(), it.m11(), it.m30(), it.m31()) }
    *///?} else {
    /*private val modelView = BufferUtils.createFloatBuffer(16)

    private var modelViewStale = true

    private var blending = false

    @JvmStatic
    fun matrixChanged() {
        modelViewStale = true
    }

    @JvmStatic
    fun blend(enabled: Boolean) {
        blending = enabled
    }

    private fun pose(g: GuiGraphicsExtractor): Pose {
        if (!modelViewStale) return lastPose
        modelViewStale = false
        modelView.clear()
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelView)
        return poseOf(modelView.get(0), modelView.get(1), modelView.get(4), modelView.get(5), modelView.get(12), modelView.get(13))
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

    /** the nine-sliced sprite being drawn, whose middle the game draws by coordinates in its atlas, or tiles of it */
    @JvmField
    var nineSlice: TextureAtlasSprite? = null

    @JvmStatic
    fun texture(g: GuiGraphicsExtractor, id: Identifier, x0: Int, x1: Int, y0: Int, y1: Int, u0: Float, u1: Float, v0: Float, v1: Float, color: Int): Boolean {
        val l = active ?: return false
        val slice = nineSlice?.takeIf { it.atlasLocation() == id }
        val bitmap = (if (slice != null) SpriteBitmaps.of(slice) else SpriteBitmaps.of(id)) ?: return false
        val dy = HudTransform.mirrorShift(y0, y1)
        if (slice == null) {
            l.recording += Quad(pose(g), bitmap, x0.toFloat(), (y0 + dy).toFloat(), x1.toFloat(), (y1 + dy).toFloat(), u0, v0, u1, v1, color)
            return true
        }
        val frame = slice.contents().height().toFloat() / bitmap.height
        val width = slice.u1 - slice.u0
        val height = slice.v1 - slice.v0
        l.recording += Quad(
            pose(g), bitmap, x0.toFloat(), (y0 + dy).toFloat(), x1.toFloat(), (y1 + dy).toFloat(),
            (u0 - slice.u0) / width, (v0 - slice.v0) / height * frame, (u1 - slice.u0) / width, (v1 - slice.v0) / height * frame, color,
        )
        return true
    }

    /** the game repeats one tile of the nine-sliced sprite over the rectangle, which is recorded a tile at a time */
    @JvmStatic
    fun tiled(g: GuiGraphicsExtractor, tileW: Int, tileH: Int, x0: Int, y0: Int, x1: Int, y1: Int, u0: Float, u1: Float, v0: Float, v1: Float, color: Int): Boolean {
        val l = active ?: return false
        val sprite = nineSlice ?: return false
        val bitmap = SpriteBitmaps.of(sprite) ?: return false
        val frame = sprite.contents().height().toFloat() / bitmap.height
        val spriteW = sprite.u1 - sprite.u0
        val spriteH = sprite.v1 - sprite.v0
        val pose = pose(g)
        val dy = HudTransform.mirrorShift(y0, y1)
        val left = (u0 - sprite.u0) / spriteW
        val top = (v0 - sprite.v0) / spriteH * frame
        val tileU = (u1 - u0) / spriteW
        val tileV = (v1 - v0) / spriteH * frame
        for (y in y0 until y1 step tileH) {
            val h = minOf(tileH, y1 - y)
            for (x in x0 until x1 step tileW) {
                val w = minOf(tileW, x1 - x)
                l.recording += Quad(
                    pose, bitmap, x.toFloat(), (y + dy).toFloat(), (x + w).toFloat(), (y + h + dy).toFloat(),
                    left, top, left + tileU * w / tileW, top + tileV * h / tileH, color,
                )
            }
        }
        return true
    }

    @JvmStatic
    fun head(g: GuiGraphicsExtractor, id: UUID, x: Int, y: Int, size: Int, style: McHeadStyle, flip: Boolean, color: Int): Boolean {
        val l = active ?: return false
        val layers = McPlayerFaces.layers(id) ?: return false
        val dy = HudTransform.mirrorShift(y, y + size)
        addHead(l, pose(g), layers, x.toFloat(), (y + dy).toFloat(), size.toFloat(), style, flip, color)
        return true
    }
    //?} else {
    /*private var bound: Identifier? = null
    private var tintR = 1f
    private var tintG = 1f
    private var tintB = 1f
    private var tintA = 1f

    private val tint: Int
        get() = ((tintA * 255f).toInt() shl 24) or ((tintR * 255f).toInt() shl 16) or ((tintG * 255f).toInt() shl 8) or (tintB * 255f).toInt()

    @JvmStatic
    fun bind(id: Identifier?) {
        bound = id
    }

    @JvmStatic
    fun color(r: Float, g: Float, b: Float, a: Float) {
        tintR = r
        tintG = g
        tintB = b
        tintA = a
    }

    @JvmStatic
    fun isActive() = capturing

    private var fontGeneration = 0

    @JvmStatic
    fun resourcesReloaded() {
        fontGeneration++
        SpriteBitmaps.clear()
    }

    @JvmStatic
    fun texture(g: GuiGraphicsExtractor, x: Int, y: Int, w: Int, h: Int, u0: Float, v0: Float, u1: Float, v1: Float): Boolean {
        val l = active ?: return false
        val bitmap = SpriteBitmaps.of(bound ?: return false) ?: return false
        l.recording += Quad(iconPose(g, x, y, w, h), bitmap, x.toFloat(), y.toFloat(), (x + w).toFloat(), (y + h).toFloat(), u0, v0, u1, v1, tint)
        return true
    }

    @JvmStatic
    fun head(g: GuiGraphicsExtractor, id: UUID, x: Int, y: Int, size: Int, style: McHeadStyle, flip: Boolean): Boolean {
        val l = active ?: return false
        val layers = McPlayerFaces.layers(id) ?: return false
        addHead(l, iconPose(g, x, y, size, size), layers, x.toFloat(), y.toFloat(), size.toFloat(), style, flip, tint)
        return true
    }
    *///?}

    private fun addHead(l: Layer, m: Pose, layers: McSkinLayers, x: Float, y: Float, size: Float, style: McHeadStyle, flip: Boolean, color: Int) {
        val old = l.previous.getOrNull(l.recording.size) as? Head
        l.recording += if (old != null && old.same(m, layers, x, y, size, style, flip, color)) old else Head(m, layers, x, y, size, style, flip, color)
    }

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
    private fun stretched(l: Layer, hud: VanillaHud, m: Pose, x0: Int, y0: Int, x1: Int, y1: Int, color: Int, tint: Tint?) {
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
        val box = Corners(l.boxX, l.boxY, boxR, boxB, hud.layoutStyle.bgRadius * l.scale, 0f)
        l.recording += Rounded(SCREEN, box, left, top, right, bottom, color, tint)
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
        val tint = tintOf(color)
        val stored = stable(color, tint)
        l.hud?.let { hud ->
            if ((inBackground || hud.everyFillIsBackground) && hud.stretchesBackground && hud.customBackgroundBox) {
                stretched(l, hud, pose(g), x0, y0, x1, y1, stored, tint)
                return true
            }
        }
        corners?.let {
            l.recording += Rounded(pose(g), it, minOf(x0, x1).toFloat(), minOf(y0, y1).toFloat(), maxOf(x0, x1).toFloat(), maxOf(y0, y1).toFloat(), stored, tint)
            return true
        }
        l.recording += Quad(pose(g), null, minOf(x0, x1).toFloat(), minOf(y0, y1).toFloat(), maxOf(x0, x1).toFloat(), maxOf(y0, y1).toFloat(), 0f, 0f, 0f, 0f, stored, tint)
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
        val sx = if (m.m01 == 0f) abs(m.m00) else hypot(m.m00, m.m01)
        val sy = if (m.m10 == 0f) abs(m.m11) else hypot(m.m10, m.m11)
        l.recording += Item(stack, m.x(x + 8f, y + 8f) - 8f * sx, m.y(x + 8f, y + 8f) - 8f * sy, 16f * sx, 16f * sy)
        return true
    }

    //? if = 1.8.9 {
    /*/** servers pad lines with characters the game's font draws nothing for, which Compose's fallback font shows as boxes */
    private fun drawableText(text: String): String {
        var ascii = true
        var printable = true
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c == '§' && i + 1 < text.length) {
                ascii = false
                i += 2
                continue
            }
            if (c.code >= 128) {
                ascii = false
                printable = false
                break
            }
            if (c.code < 32 || c.code == 127) printable = false
            i++
        }
        if (ascii || printable) return text
        val font = Minecraft.getInstance().font
        return buildString(text.length) {
            var i = 0
            while (i < text.length) {
                val c = text[i++]
                if (c == '§' && i < text.length) append(c).append(text[i++])
                else if (font.getWidth(c) > 0) append(c)
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
    var textWidth = -1
        private set

    @JvmStatic
    fun text(g: GuiGraphicsExtractor, text: String, x: Int, y: Int, color: Int, shadow: Boolean): Boolean {
        textWidth = -1
        val l = active ?: return false
        val hud = l.hud ?: return false
        if (hud is ExperienceLevelHud && color and 0xFFFFFF == 0) return true
        //? if >=1.21.6 {
        if (color ushr 24 != 0) textWidth = addText(l, hud, pose(g), text, x, y, color, shadow, tintOf(color))
        //?} elif > 1.8.9 {
        /*textWidth = addText(l, hud, pose(g), text, x, y, fontColor(color, blending = true), shadow, tintOf(color))
        *///?} else {
        /*textWidth = addText(l, hud, pose(g), text, x, y, fontColor(color, blending), shadow, tintOf(color))
        *///?}
        return true
    }

    private fun styleKey(s: HudStyle) =
        (s.caseType shl 3) or (if (s.textBold) 4 else 0) or (if (s.textItalic) 2 else 0) or (if (s.textUnderline) 1 else 0)

    //? if = 1.8.9 {
    /*private fun fontKey() = if (Minecraft.getInstance().isUnicode) fontGeneration * 2 + 1 else fontGeneration * 2
    *///?} else
    private fun fontKey() = 0

    private fun addText(l: Layer, hud: VanillaHud, m: Pose, text: String, x: Int, y: Int, color: Int, shadow: Boolean, tint: Tint?): Int {
        val s = hud.textStyle
        val style = styleKey(s)
        val font = fontKey()
        val pos = l.recording.size
        val previous = l.previous
        val last = if (pos < previous.size) (previous[pos] as? Run)?.key else null
        val key = if (last != null && last.style == style && last.font == font && last.src == text) last else {
            //? if = 1.8.9 {
            /*val runs = legacyRuns(drawableText(text))
            *///?} else
            val runs = legacyRuns(text)
            TextKey(text, style, font, runs, Array(runs.size) { styled(s, runs[it]) })
        }
        return addRuns(l, hud, m, key.runs, key, x, y, color, shadow, tint)
    }

    //? if > 1.8.9 {
    /** one run per colour since the Compose text call takes a single RGB, false when it needs a font Compose can't draw */
    @JvmStatic
    fun text(g: GuiGraphicsExtractor, text: FormattedCharSequence, x: Int, y: Int, color: Int, shadow: Boolean): Boolean {
        textWidth = -1
        val l = active ?: return false
        val hud = l.hud ?: return false
        //? if >=1.21.6 {
        if (color ushr 24 == 0) return true
        //?}
        val defaultFont = Style.EMPTY.font
        val runs = StyleRuns()
        text.accept { _, style, codepoint ->
            if (style.font != defaultFont) runs.custom = true
            runs.add(style, codepoint)
            !runs.custom
        }
        if (runs.custom) return false
        textWidth = addRuns(l, hud, pose(g), runs.finish(), null, x, y,
            //? if >=1.21.6 {
            color,
            //?} else
            //fontColor(color, blending = true),
            shadow, tintOf(color))
        return true
    }

    private fun codes(style: Style): String {
        if (!style.isBold && !style.isItalic && !style.isUnderlined && !style.isStrikethrough && !style.isObfuscated) return ""
        return styleCodes(style)
    }

    private fun styleCodes(style: Style) = buildString {
        if (style.isBold) append("§l")
        if (style.isItalic) append("§o")
        if (style.isUnderlined) append("§n")
        if (style.isStrikethrough) append("§m")
        if (style.isObfuscated) append("§k")
    }

    private class StyleRuns {
        val runs = ArrayList<Run0>()
        var custom = false
        private var style: Style? = null
        private val sb = StringBuilder()

        fun add(style: Style, codepoint: Int) {
            val last = this.style
            if (last == null || last != style) {
                flush()
                this.style = style
            }
            sb.appendCodePoint(codepoint)
        }

        fun finish(): List<Run0> {
            flush()
            return runs
        }

        private fun flush() {
            val s = style ?: return
            val color = s.color
            runs += Run0(HudCapture.codes(s), color != null, if (color != null) color.value else 0, sb.toString())
            sb.setLength(0)
        }
    }
    //?}

    private class Run0(val codes: String, val hasRgb: Boolean, val rgb: Int, val text: String)

    private fun legacyRuns(text: String): List<Run0> {
        if ('§' !in text) return listOf(Run0("", false, 0, text))
        val runs = ArrayList<Run0>()
        val sb = StringBuilder()
        var hasRgb = false
        var rgb = 0
        var i = 0
        while (i < text.length) {
            val c = text[i]
            val color = if (c == '§' && i + 1 < text.length) "0123456789abcdefr".indexOf(text[i + 1].lowercaseChar()) else -1
            if (color < 0) {
                sb.append(c)
                i++
                continue
            }
            if (sb.isNotEmpty()) runs += Run0("", hasRgb, rgb, sb.toString())
            sb.clear()
            hasRgb = color < LEGACY_COLORS.size
            rgb = if (hasRgb) LEGACY_COLORS[color] else 0
            i += 2
        }
        if (sb.isNotEmpty()) runs += Run0("", hasRgb, rgb, sb.toString())
        return runs
    }

    private fun addRuns(l: Layer, hud: VanillaHud, m: Pose, runs: List<Run0>, key: TextKey?, x: Int, y: Int, color: Int, shadow: Boolean, tint: Tint?): Int {
        // vanilla outlines the experience level with four black draws, the hud's own shadow replaces them
        if (hud is ExperienceLevelHud && color and 0xFFFFFF == 0) return -1
        val s = hud.textStyle
        val measure = restyledHud() === hud
        val runShadow = shadow || s.showShadow
        val recording = l.recording
        val previous = l.previous
        val pos = recording.size
        val fy = y.toFloat()
        var cursor = x.toFloat()
        var total = 0.0
        val last = runs.lastIndex
        for (i in runs.indices) {
            val r = runs[i]
            val run = if (key != null) key.texts[i] else styled(s, r)
            val chroma = chromaText(hud, s, r)
            val argb = if (chroma) color else runColor(hud, s, r, color)
            val runTint = if (!chroma && !r.hasRgb && argb == color) tint else null
            val stored = stable(argb, runTint)
            val at = pos + i
            val old = if (at < previous.size) previous[at] as? Run else null
            if (old != null && old.key === (if (i == 0) key else null) && old.same(m, run, cursor, fy, stored, runShadow, hud, chroma, runTint)) {
                recording += old
            } else {
                val made = Run(m, run, cursor, fy, stored, runShadow, hud, chroma, runTint)
                if (i == 0) made.key = key
                recording += made
            }
            if (measure || i < last) {
                val w = widthOf(hud, s, run)
                cursor += w
                total += w.toDouble()
            }
        }
        return if (measure) ceil(total).toInt() else -1
    }

    private fun styled(s: HudStyle, r: Run0): String {
        val text = when (s.caseType) {
            1 -> r.text.uppercase()
            2 -> r.text.lowercase()
            else -> r.text
        }
        return if (s.prefix.isEmpty() && r.codes.isEmpty()) text else s.prefix + r.codes + text
    }

    private fun runColor(hud: VanillaHud, s: HudStyle, r: Run0, color: Int): Int {
        if (r.hasRgb) return (color and 0xFF000000.toInt()) or r.rgb
        val custom = s.textColor != -1 || s.textChroma
        if (!custom || hud.managesTextColor) return color
        return textColor(s, color)
    }

    private fun chromaText(hud: VanillaHud, s: HudStyle, r: Run0) = !r.hasRgb && s.textChroma && !hud.managesTextColor

    private fun textColor(s: HudStyle, color: Int): Int {
        val picked = argbOf(s.textColor, s.textChroma, s.textChromaSpeed)
        val alpha = (picked ushr 24) * (color ushr 24) / 255
        return (alpha shl 24) or (picked and 0xFFFFFF)
    }

    private val poppinsFonts = HashMap<String, PolyFont>()

    private fun poppins(hud: VanillaHud): PolyFont {
        val name = hud.getPoppinsFontName()
        return poppinsFonts.getOrPut(name) { PolyFont(name, 8f) }
    }

    private fun stripFormat(text: String): String {
        var i = text.indexOf('§')
        if (i < 0) return text
        val sb = StringBuilder(text.length)
        var from = 0
        while (i >= 0) {
            val n = i + 1
            if (n < text.length && !lineBreak(text[n])) {
                sb.append(text, from, i)
                from = if (n + 1 < text.length && text[n].isHighSurrogate() && text[n + 1].isLowSurrogate()) n + 2 else n + 1
                i = text.indexOf('§', from)
            } else {
                i = text.indexOf('§', n)
            }
        }
        return sb.append(text, from, text.length).toString()
    }

    private fun lineBreak(c: Char) = c.code == 0x0A || c.code == 0x0D || c.code == 0x85 || c.code == 0x2028 || c.code == 0x2029

    /** set while [widthOf] asks the game's font, whose width hook would otherwise style the text twice */
    private var rawWidth = false

    private fun widthOf(hud: VanillaHud, s: HudStyle, text: String): Float {
        if (s.font == Font.Poppins) return poppins(hud).measureTextWidth(stripFormat(text))
        //? if = 1.8.9 {
        /*return cachedRawWidth(Minecraft.getInstance().font, text).toFloat()
        *///?} else
        return measureRaw(Minecraft.getInstance().font, text).toFloat()
    }

    private fun measureRaw(font: McFont, text: String): Int {
        val outer = rawWidth
        rawWidth = true
        try {
            return font.width(text)
        } finally {
            rawWidth = outer
        }
    }

    //? if = 1.8.9 {
    /*private val rawWidths = HashMap<String, Int>()
    private var rawWidthsFont = -1

    private fun cachedRawWidth(font: McFont, text: String): Int {
        val key = fontKey()
        if (key != rawWidthsFont || rawWidths.size > 4096) {
            rawWidths.clear()
            rawWidthsFont = key
        }
        return rawWidths.getOrPut(text) { measureRaw(font, text) }
    }

    @JvmStatic
    fun width(font: McFont, text: String): Int = fontWidth(cachedRawWidth(font, text), text)
    *///?}

    /** the width [addRuns] lays [runs] out at, with the hud's case and bold applied to each */
    private fun widthOf(hud: VanillaHud, runs: List<Run0>): Int {
        val s = hud.textStyle
        var sum = 0.0
        for (i in runs.indices) sum += widthOf(hud, s, styled(s, runs[i])).toDouble()
        return ceil(sum).toInt()
    }

    private var measuringHud: VanillaHud? = null

    fun <T> measuring(hud: VanillaHud, measure: () -> T): T {
        val outer = measuringHud
        measuringHud = hud
        try {
            return measure()
        } finally {
            measuringHud = outer
        }
    }

    /**
     * The hud whose text the game is measuring, when [Replay] draws that text at another width than
     * the game's font would. Vanilla centres with the measured width and the box is sized from it.
     */
    private fun restyledHud(): VanillaHud? {
        if (rawWidth) return null
        val hud = measuringHud ?: active?.hud ?: return null
        val s = hud.textStyle
        return if (s.font == Font.Poppins || s.textBold || s.caseType != 0) hud else null
    }

    @JvmStatic
    fun fontWidth(width: Int, text: String): Int {
        val hud = restyledHud() ?: return width
        //? if = 1.8.9 {
        /*return widthOf(hud, legacyRuns(drawableText(text)))
        *///?} else
        return widthOf(hud, legacyRuns(text))
    }

    //? if > 1.8.9 {
    @JvmStatic
    fun fontWidth(width: Int, text: FormattedText): Int {
        val hud = restyledHud() ?: return width
        val runs = ArrayList<Run0>()
        text.visit({ style, part ->
            runs += Run0(codes(style), false, 0, part)
            Optional.empty<Unit>()
        }, Style.EMPTY)
        return widthOf(hud, runs)
    }

    @JvmStatic
    fun fontWidth(width: Int, text: FormattedCharSequence): Int {
        val hud = restyledHud() ?: return width
        return widthOf(hud, styleRuns(text))
    }

    /** the text split wherever its style changes, as [text] captures it */
    private fun styleRuns(text: FormattedCharSequence): List<Run0> {
        val runs = StyleRuns()
        text.accept { _, style, codepoint ->
            runs.add(style, codepoint)
            true
        }
        return runs.finish()
    }
    //?}

    private fun RenderContext.drawRun(op: Run) {
        deviceTransform()?.let { (sx, sy, tx, ty) -> translate(snap(op.x, sx, tx) - op.x, snap(op.y, sy, ty) - op.y) }
        val hud = op.hud
        val s = hud.textStyle
        val shadowColor = if (op.shadow) s.shadowColor else 0
        val shadowChroma = op.shadow && s.shadowChroma
        val shadow = if (op.shadow) argbOf(shadowColor, shadowChroma, s.shadowChromaSpeed) else 0
        val color = if (op.chroma) textColor(s, op.color) else tinted(op.color, op.tint)
        if (s.font == Font.Poppins) {
            val plain = stripFormat(op.text)
            val font = poppins(hud)
            val metrics = font.metrics
            translate(0f, (Minecraft.getInstance().font.lineHeight - metrics.descent - metrics.ascent) / 2f)
            if (op.shadow) fontText(plain, op.x + s.shadowOffsetX / 2f, op.y + s.shadowOffsetY / 2f, shadow, font)
            fontText(plain, op.x, op.y, color, font)
            return
        }
        val fullShadow = op.shadow && s.fullShadow
        if (fullShadow && hud is ExperienceLevelHud && shadow ushr 24 == 0xFF) {
            // vanilla's four draws a pixel off each side, which leave the corners open where mcTextOutline fills them
            val outline = stripColors(op.text)
            mcText(outline, op.x - 1f, op.y, shadow, false, 1f)
            mcText(outline, op.x + 1f, op.y, shadow, false, 1f)
            mcText(outline, op.x, op.y - 1f, shadow, false, 1f)
            mcText(outline, op.x, op.y + 1f, shadow, false, 1f)
            mcText(op.text, op.x, op.y, color, false, 1f)
        } else if (fullShadow) {
            mcTextOutline(op.text, op.x, op.y, color, shadow, 1f)
        } else if (op.shadow && (shadowColor != DEFAULT_SHADOW || shadowChroma)) {
            mcText(stripColors(op.text), op.x + 1f, op.y + 1f, shadow, false, 1f)
            mcText(op.text, op.x, op.y, color, false, 1f)
        } else {
            mcText(op.text, op.x, op.y, color, op.shadow, 1f)
        }
    }

    private fun stripColors(text: String) = if ('§' in text) text.replace(COLOR_CODE, "") else text

    private fun RenderContext.fontText(text: String, x: Float, y: Float, argb: Int, font: PolyFont) {
        if (argb ushr 24 == 0 || text.isEmpty()) return
        renderer.text(text, x, y, argb, font)
    }

    private fun RenderContext.replay(ops: List<Any>, from: Int, until: Int, item: (RenderContext.(Item, Int) -> Unit)? = null) {
        var clips = 0
        var items = 0
        for (i in from until until) {
            when (val op = ops[i]) {
                is Item -> if (item != null) item(op, items++)
                is Quad -> withPose(op.m) {
                    if (op.bitmap == null) {
                        val argb = tinted(op.color, op.tint)
                        if (argb ushr 24 != 0) renderer.rect(op.x0, op.y0, op.x1 - op.x0, op.y1 - op.y0, argb, 0f)
                    } else {
                        val t = deviceTransform()
                        val x0 = if (t == null) op.x0 else snap(op.x0, t[0], t[2])
                        val y0 = if (t == null) op.y0 else snap(op.y0, t[1], t[3])
                        val x1 = if (t == null) op.x1 else snap(op.x1, t[0], t[2])
                        val y1 = if (t == null) op.y1 else snap(op.y1, t[1], t[3])
                        renderer.image(op.bitmap, x0, y0, x1 - x0, y1 - y0, op.u0, op.v0, op.u1, op.v1, op.color, FilterQuality.None)
                    }
                }
                is Head -> withPose(op.m) {
                    val t = deviceTransform()
                    val x0 = if (t == null) op.x else snap(op.x, t[0], t[2])
                    val y0 = if (t == null) op.y else snap(op.y, t[1], t[3])
                    val x1 = if (t == null) op.x + op.size else snap(op.x + op.size, t[0], t[2])
                    McPlayerFaces.drawHead(this, op.layers, x0, y0, x1 - x0, op.style, op.flip, op.color)
                }
                is Run -> withPose(op.m) { drawRun(op) }
                is Rounded -> withPose(op.m) {
                    val c = op.c
                    val argb = tinted(op.color, op.tint)
                    save()
                    clipRect(op.x0, op.y0, op.x1 - op.x0, op.y1 - op.y0)
                    if (argb ushr 24 != 0) {
                        if (c.stroke > 0f) renderer.rectStroke(c.x0, c.y0, c.x1 - c.x0, c.y1 - c.y0, argb, c.stroke, c.radius)
                        else renderer.rect(c.x0, c.y0, c.x1 - c.x0, c.y1 - c.y0, argb, c.radius)
                    }
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

    private fun snap(v: Float, scale: Float, offset: Float) = (round(v * scale + offset) - offset) / scale

    private inline fun RenderContext.withPose(m: Pose, draw: RenderContext.() -> Unit) {
        save()
        withPoseOpen(m)
        draw()
        restore()
    }

    private fun RenderContext.withPoseOpen(m: Pose) {
        translate(m.m20, m.m21)
        if (m.m01 == 0f && m.m10 == 0f && m.m00 > 0f) {
            if (m.m00 != 1f) scale(m.m00, abs(m.m11))
            return
        }
        val turn = Math.toDegrees(atan2(m.m01, m.m00).toDouble()).toFloat()
        if (turn != 0f) renderer.rotate(turn)
        val sx = hypot(m.m00, m.m01)
        if (sx != 1f) scale(sx, hypot(m.m10, m.m11))
    }

    private fun RenderContext.undoPose(m: Pose) {
        if (m.m01 == 0f && m.m10 == 0f && m.m00 > 0f) {
            if (m.m00 != 1f) scale(1f / m.m00, 1f / abs(m.m11))
            translate(-m.m20, -m.m21)
            return
        }
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
        NodeBox(modifier = hud.hudBackground(PolyModifier.size(width, height))) {
            PolyCanvas(PolyModifier.size(width, height)) { x, y, _, _ ->
                val ops = layer.shown
                painting(layer, x, y) {
                    replay(ops, 0, ops.size) { op, n -> layer.icons.getOrNull(n)?.let { drawIcon(hud, it, op, layer.shownScale) } }
                }
            }
        }
    }

    private inline fun RenderContext.painting(layer: Layer, x: Float, y: Float, draw: RenderContext.() -> Unit) {
        val s = layer.shownScale
        save()
        translate(x, y)
        if (s != 1f) scale(1f / s, 1f / s)
        translate(-layer.shownX, -layer.shownY)
        draw()
        restore()
    }

    private fun RenderContext.drawIcon(hud: VanillaHud, icon: McItemIconHandle, op: Item, scale: Float) {
        val size = restSize(hud, op, scale)
        val x = op.x + (op.width - size) / 2f
        val y = op.y + (op.height - size) * 0.75f
        if ((renderer as? GuiItemRenderer)?.guiItem(op.stack, op.x, op.y, op.width, op.height) != true) {
            if (op.width == size && op.height == size) icon.draw(this, x, y, size, size)
            else {
                // the game stretches an item that was just picked up about the point 3/4 down it
                val px = op.x + op.width / 2f
                val py = op.y + op.height * 0.75f
                save()
                translate(px, py)
                scale(op.width / size, op.height / size)
                translate(-px, -py)
                icon.draw(this, x, y, size, size)
                restore()
            }
        }
        // vanilla draws the count and durability bar after popping the stretch
        if (op.decorated) icon.drawDecorations(this, x, y, size)
    }
}

/** the pixels behind the textures vanilla's HUD draws, read fresh when a resource reload swaps the sprite */
private object SpriteBitmaps {
    private class Entry(val owner: Any, val bitmap: PolyBitmap?)

    private val cache = HashMap<Identifier, Entry>()

    private val mc get() = Minecraft.getInstance()

    fun clear() = cache.clear()

    private inline fun cached(key: Identifier, owner: Any, read: () -> PolyBitmap?): PolyBitmap? {
        cache[key]?.let { if (it.owner === owner) return it.bitmap }
        return read().also { cache[key] = Entry(owner, it) }
    }

    //? if > 1.8.9 {
    init {
        HostEvents.onResourcesReloaded(::clear)
    }

    fun of(sprite: TextureAtlasSprite): PolyBitmap? {
        val contents = sprite.contents()
        val name = contents.name()
        cache[name]?.let { if (it.owner === contents) return it.bitmap }
        val folder = when (sprite.atlasLocation().path) {
            "textures/atlas/gui.png" -> if (name.path.startsWith("mob_effect/")) "textures/" else "textures/gui/sprites/"
            "textures/atlas/mob_effects.png" -> "textures/mob_effect/"
            else -> return null
        }
        return cached(name, contents) { load(name.withPath("$folder${name.path}.png")) }
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
        mc.resourceManager.getResource(id).orElse(null)?.open()?.use { PolyBitmap.decode(it.readAllBytes()) }
    }.getOrNull()
    //?} else {
    /*fun of(id: Identifier): PolyBitmap? {
        val texture = mc.textureManager.get(id) ?: return null
        return cached(id, texture) { load(id) ?: readBack(id) }
    }

    private fun load(id: Identifier): PolyBitmap? = runCatching {
        mc.resourceManager.getResource(id).asStream().use { PolyBitmap.decode(it.readAllBytes()) }
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
