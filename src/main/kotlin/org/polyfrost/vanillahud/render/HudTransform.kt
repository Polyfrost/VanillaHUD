package org.polyfrost.vanillahud.render

import net.minecraft.client.gui.GuiGraphicsExtractor
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.vanillahud.hud.TabListHud
import org.polyfrost.vanillahud.hud.VanillaHud
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.floor

//? if <=1.21.5 {
/*import com.mojang.math.Axis
*///?}

object HudTransform {
    private var depth = 0

    private val scissors = ArrayDeque<Boolean>()

    /** quarter turns to undo per icon while an icon layer draws */
    private var iconTurns = 0
    private var iconDepth = 0

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

    @JvmStatic
    fun begin(graphics: GuiGraphicsExtractor, provider: VanillaHud) {
        val hud = resolve(provider)
        val w = graphics.guiWidth()
        val h = graphics.guiHeight()
        if (!HudManager.isEditing) {
            HudManager.guiScreenWidth = w.toFloat()
            HudManager.guiScreenHeight = h.toFloat()
        }
        hud?.applyForceDefault()
        hud?.reseedDefaultForScreen()
        val anchored = hud != null && hud.anchorsToVanillaOrigin()
        val s = hud?.effectiveScale ?: 1f
        if (anchored && HudManager.isEditing) hud.pinToVanillaOrigin(w, h, s)
        val ox = provider.vanillaOriginX(w, h)
        val oy = provider.vanillaOriginY(w, h)
        val defX = provider.scaledOriginX(w, h, s)
        val defY = provider.scaledOriginY(w, h, s)
        val gx = if (anchored) defX else (hud?.x ?: defX)
        val gy = if (anchored) defY else (hud?.y ?: defY)

        var scissored = false
        val tab = (hud ?: provider) as? TabListHud
        if (tab != null && tab.animation && !HudManager.isEditing) {
            val frac = tab.clipFraction()
            if (frac < 1f) {
                val foreign = if (frac > 0f) tab.foreignBounds() else null
                val top = if (foreign != null) gy + (foreign.top - oy) * s else gy - tab.backgroundTop * s
                val clipH = (foreign?.height ?: (hud?.height ?: provider.height)) * s
                graphics.enableScissor(0, floor(top).toInt(), w, ceil(top + clipH * frac).toInt())
                scissored = true
            }
        }
        scissors.addLast(scissored)

        // rotate about the content centre then shift so the rotated bounding box lands on gx gy
        val turns = provider.quarterTurns
        val natW = provider.unrotatedWidth
        val natH = provider.unrotatedHeight
        val rotW = provider.width
        val rotH = provider.height
        val theta = turns * (PI.toFloat() / 2f)

        push(graphics)
        val pose = graphics.pose()
        //? if <=1.21.5 {
        /*pose.translate(gx, gy, 0f)
        pose.scale(s, s, 1f)
        if (turns != 0) {
            pose.translate(rotW / 2f, rotH / 2f, 0f)
            pose.mulPose(Axis.ZP.rotation(theta))
            pose.translate(-natW / 2f - ox, -natH / 2f - oy, 0f)
        } else {
            pose.translate(-ox, -oy, 0f)
        }
        *///?} else {
        pose.translate(gx, gy)
        pose.scale(s, s)
        if (turns != 0) {
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
        if (iconDepth++ == 0) iconTurns = provider.quarterTurns
    }

    @JvmStatic
    fun resetFrame() {
        iconDepth = 0
        iconTurns = 0
        depth = 0
        scissors.clear()
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
        val cy = y + height / 2f
        push(graphics)
        val pose = graphics.pose()
        //? if <=1.21.5 {
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

    /** counter rotates around [cx] [cy] so content stays upright inside a rotated element */
    @JvmStatic
    fun beginUpright(graphics: GuiGraphicsExtractor, provider: VanillaHud, cx: Float, cy: Float) {
        val turns = provider.quarterTurns
        push(graphics)
        val pose = graphics.pose()
        if (turns == 0) return
        val theta = -turns * (PI.toFloat() / 2f)
        //? if <=1.21.5 {
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
        pop(graphics)
    }

    @JvmStatic
    fun end(graphics: GuiGraphicsExtractor) {
        pop(graphics)
        if (scissors.removeLastOrNull() == true) graphics.disableScissor()
    }
}
