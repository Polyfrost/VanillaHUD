package org.polyfrost.vanillahud.render

//? if > 1.8.9 {
import net.minecraft.client.gui.GuiGraphicsExtractor
//?} else {
/*import org.polyfrost.vanillahud.compat.GuiGraphicsExtractor
*///?}
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.vanillahud.hud.HudInternals
import org.polyfrost.vanillahud.hud.TabListHud
import org.polyfrost.vanillahud.hud.VanillaHud
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

//? if <=1.21.5 && >1.8.9 {
/*import com.mojang.math.Axis
*///?}

object HudTransform {
    private var depth = 0

    private val scissors = ArrayDeque<Int>()
    private const val PUSHED = 0
    private const val SCISSORED = 1
    private const val SKIPPED = 2

    /** quarter turns to undo per icon while an icon layer draws */
    @JvmStatic
    var iconTurns = 0
        private set
    private var iconDepth = 0

    /** open [beginUpright] blocks, which already move their content so [mirrorShift] leaves it alone */
    private var uprightDepth = 0

    /** per open [begin], twice the centre y of a [VanillaHud.mirrored] element or NaN, kept unboxed */
    private var mirrors = FloatArray(8)
    private var mirrorDepth = 0
    private var mirror2Y = Float.NaN

    private fun push(graphics: GuiGraphicsExtractor) {
        graphics.pose().pushMatrix()
        depth++
    }

    private fun pop(graphics: GuiGraphicsExtractor) {
        if (depth <= 0) return
        depth--
        try {
            graphics.pose().popMatrix()
        } catch (_: RuntimeException) {
        }
    }

    private fun resolve(provider: VanillaHud): VanillaHud? {
        for (h in HudManager.activeInstances) {
            if (provider.javaClass.isInstance(h)) {
                return h as VanillaHud
            }
        }
        return null
    }

    private val MISSING = Any()

    private val resolved = HashMap<Class<out VanillaHud>, Any>()

    private fun resolveCached(provider: VanillaHud): VanillaHud? =
        resolved.getOrPut(provider.javaClass) { resolve(provider) ?: MISSING } as? VanillaHud

    /**
     * The HUD the options actually live on.
     *
     * [resolve] exists because OneConfig registers the instance the mixins hold as a provider and
     * deserialises the configured copies into [HudManager.activeInstances] separately. The user's
     * settings land on that copy, so anything option shaped has to be read off it rather than off
     * the template, or it reads back as the field default.
     */
    @JvmStatic
    // resolve only hands back instances of the provider's own class
    @Suppress("UNCHECKED_CAST")
    fun <T : VanillaHud> live(provider: T): T = (resolveCached(provider) ?: provider) as T

    private class Placement {
        var valid = false
        var w = 0
        var h = 0
        var natW = 0f
        var natH = 0f
        var turns = 0
        var rev = 0
        var scoreboardRev = 0
        var huds = 0
        var schema = 0
        var hx = 0f
        var hy = 0f
        var hud: VanillaHud? = null
        var s = 1f
        var ox = 0f
        var oy = 0f
        var gx = 0f
        var gy = 0f
    }

    private val placements = HashMap<Class<out VanillaHud>, Placement>()

    @JvmStatic
    fun begin(graphics: GuiGraphicsExtractor, provider: VanillaHud) {
        val editing = HudManager.isEditing
        val resolvedHud = resolveCached(provider)
        val src = resolvedHud ?: provider
        if (!editing && !src.hasContent()) {
            scissors.addLast(SKIPPED)
            return
        }
        val w = graphics.guiWidth()
        val h = graphics.guiHeight()
        val turns = src.quarterTurns
        val natW = src.unrotatedWidth
        val natH = src.unrotatedHeight
        val p = placements.getOrPut(provider.javaClass) { Placement() }
        if (editing || !p.valid || p.w != w || p.h != h || p.natW != natW || p.natH != natH || p.turns != turns ||
            p.rev != VanillaHud.positionRevision || p.scoreboardRev != HudInternals.scoreboardRevision ||
            p.huds != HudManager.activeInstances.size || p.hud !== resolvedHud || p.schema != (resolvedHud?.posSchema ?: 0) ||
            resolvedHud.let { it != null && (it.frameX != p.hx || it.frameY != p.hy || it.frameScale != p.s) }
        ) {
            val hud = resolvedHud
            hud?.reseedDefaultForScreen()
            val anchored = hud != null && hud.anchorsToVanillaOrigin()
            val s = hud?.frameScale ?: 1f
            if (anchored && editing) hud.pinToVanillaOrigin(w, h, s)
            val defX = src.scaledOriginX(w, h, s)
            val defY = src.scaledOriginY(w, h, s)
            p.hud = hud
            p.s = s
            p.ox = src.vanillaOriginX(w, h)
            p.oy = src.vanillaOriginY(w, h)
            val fx = hud?.frameX
            val fy = hud?.frameY
            p.hx = fx ?: 0f
            p.hy = fy ?: 0f
            p.gx = if (anchored || fx == null) defX else fx
            p.gy = if (anchored || fy == null) defY else fy
            p.valid = !editing
            p.w = w
            p.h = h
            p.natW = natW
            p.natH = natH
            p.turns = turns
            p.rev = VanillaHud.positionRevision
            p.scoreboardRev = HudInternals.scoreboardRevision
            p.huds = HudManager.activeInstances.size
            p.schema = hud?.posSchema ?: 0
        }
        val hud = p.hud
        val s = p.s
        val ox = p.ox
        val oy = p.oy
        val gx = p.gx
        val gy = p.gy

        var scissored = false
        val tab = src as? TabListHud
        if (tab != null && tab.animation && !editing) {
            val frac = tab.clipFraction()
            if (frac < 1f) {
                val foreign = if (frac > 0f) tab.foreignBounds() else null
                val top = if (foreign != null) gy + (foreign.top - oy) * s else gy - tab.backgroundTop * s
                val clipH = (foreign?.height ?: src.height) * s
                graphics.enableScissor(0, floor(top).toInt(), w, ceil(top + clipH * frac).toInt())
                scissored = true
            }
        }
        scissors.addLast(if (scissored) SCISSORED else PUSHED)

        // rotate about the content centre then shift so the rotated bounding box lands on gx gy
        val rotW = if (turns % 2 != 0) natH else natW
        val rotH = if (turns % 2 != 0) natW else natH
        val drawTurns = drawTurns(src)
        val theta = drawTurns * (PI.toFloat() / 2f)
        val turned = drawTurns != 0

        mirror2Y = if (src.mirrored) 2f * oy + natH else Float.NaN
        if (mirrorDepth == mirrors.size) mirrors = mirrors.copyOf(mirrorDepth * 2)
        mirrors[mirrorDepth++] = mirror2Y

        push(graphics)
        val pose = graphics.pose()
        //? if <=1.21.5 && >1.8.9 {
        /*pose.translate(gx, gy, 0f)
        pose.scale(s, s, 1f)
        if (turned) {
            pose.translate(rotW / 2f, rotH / 2f, 0f)
            pose.mulPose(Axis.ZP.rotation(theta))
            pose.translate(-natW / 2f - ox, -natH / 2f - oy, 0f)
        } else {
            pose.translate(-ox, -oy, 0f)
        }
        *///?} else {
        pose.translate(gx, gy)
        pose.scale(s, s)
        if (turned) {
            pose.translate(rotW / 2f, rotH / 2f)
            pose.rotate(theta)
            pose.translate(-natW / 2f - ox, -natH / 2f - oy)
        } else {
            pose.translate(-ox, -oy)
        }
        //?}
    }

    /** marks a layer whose sprites are standalone icons so each one can be kept upright */
    @JvmStatic
    fun beginIcons(graphics: GuiGraphicsExtractor, provider: VanillaHud) {
        begin(graphics, provider)
        if (iconDepth++ == 0) iconTurns = drawTurns(live(provider))
    }

    @JvmStatic
    fun resetFrame() {
        resolved.clear()
        iconDepth = 0
        iconTurns = 0
        depth = 0
        scissors.clear()
        uprightDepth = 0
        mirrorDepth = 0
        mirror2Y = Float.NaN
    }

    @JvmStatic
    fun endIcons(graphics: GuiGraphicsExtractor) {
        if (--iconDepth <= 0) {
            iconDepth = 0
            iconTurns = 0
        }
        end(graphics)
    }

    @JvmStatic
    fun uprightIcon(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int, height: Int): Boolean {
        if (iconTurns == 0) return false
        val theta = -iconTurns * (PI.toFloat() / 2f)
        val cx = x + width / 2f
        val cy = y + height / 2f + mirrorShift(y, y + height)
        push(graphics)
        val pose = graphics.pose()
        //? if <=1.21.5 && >1.8.9 {
        /*pose.translate(cx, cy, 0f)
        pose.mulPose(Axis.ZP.rotation(theta))
        pose.translate(-cx, -cy, 0f)
        *///?} else {
        pose.translate(cx, cy)
        pose.rotate(theta)
        pose.translate(-cx, -cy)
        //?}
        return true
    }

    private fun drawTurns(hud: VanillaHud): Int = if (hud.mirrored) (hud.quarterTurns + 2) % 4 else hud.quarterTurns

    /** how far to move a piece from [y0] to [y1] to reach its flipped height */
    @JvmStatic
    fun mirrorShift(y0: Int, y1: Int): Int {
        if (mirror2Y.isNaN() || uprightDepth > 0) return 0
        return (mirror2Y - y0 - y1).roundToInt()
    }

    /** keeps content around [cx] [cy] upright when rotated, or moves it to its flipped height */
    @JvmStatic
    fun beginUpright(graphics: GuiGraphicsExtractor, provider: VanillaHud, cx: Float, cy: Float) {
        val turns = drawTurns(live(provider))
        push(graphics)
        uprightDepth++
        val pose = graphics.pose()
        if (!mirror2Y.isNaN()) {
            //? if <=1.21.5 && >1.8.9 {
            /*pose.translate(0f, mirror2Y - 2f * cy, 0f)
            *///?} else
            pose.translate(0f, mirror2Y - 2f * cy)
        }
        if (turns == 0) return
        val theta = -turns * (PI.toFloat() / 2f)
        //? if <=1.21.5 && >1.8.9 {
        /*pose.translate(cx, cy, 0f)
        pose.mulPose(Axis.ZP.rotation(theta))
        pose.translate(-cx, -cy, 0f)
        *///?} else {
        pose.translate(cx, cy)
        pose.rotate(theta)
        pose.translate(-cx, -cy)
        //?}
    }

    @JvmStatic
    fun endUpright(graphics: GuiGraphicsExtractor) {
        if (uprightDepth > 0) uprightDepth--
        pop(graphics)
    }

    @JvmStatic
    fun end(graphics: GuiGraphicsExtractor) {
        val state = scissors.removeLastOrNull()
        if (state == SKIPPED) return
        if (mirrorDepth > 0) mirrorDepth--
        mirror2Y = if (mirrorDepth > 0) mirrors[mirrorDepth - 1] else Float.NaN
        pop(graphics)
        if (state == SCISSORED) graphics.disableScissor()
    }
}
