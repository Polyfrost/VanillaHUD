package org.polyfrost.vanillahud.render

import net.minecraft.client.gui.GuiGraphicsExtractor
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.vanillahud.hud.TabListHud
import org.polyfrost.vanillahud.hud.VanillaHud
import kotlin.math.ceil
import kotlin.math.floor

object HudTransform {
    private var scissored = false

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
        val locked = hud?.let { it.locked && !it.previewing } ?: false
        if (!locked) {
            hud?.reseedDefaultForScreen()
            hud?.applyLink()
        }
        val ox = provider.vanillaOriginX(w, h)
        val oy = provider.vanillaOriginY(w, h)
        val gx = if (locked) ox else (hud?.x ?: ox)
        val gy = if (locked) oy else (hud?.y ?: oy)
        val s = hud?.effectiveScale ?: 1f

        scissored = false
        val tab = (hud ?: provider) as? TabListHud
        if (tab != null && tab.animation && !HudManager.isEditing) {
            val frac = tab.clipFraction()
            if (frac < 1f) {
                val clipH = (hud?.height ?: provider.height) * frac * s
                graphics.enableScissor(0, floor(gy).toInt(), w, ceil(gy + clipH).toInt())
                scissored = true
            }
        }

        val pose = graphics.pose()
        pose.pushMatrix()
        //? if <=1.21.5 {
        /*pose.translate(gx, gy, 0f)
        pose.scale(s, s, 1f)
        pose.translate(-ox, -oy, 0f)
        *///?} else {
        pose.translate(gx, gy)
        pose.scale(s, s)
        pose.translate(-ox, -oy)
        //?}
    }

    @JvmStatic
    fun end(graphics: GuiGraphicsExtractor) {
        graphics.pose().popMatrix()
        if (scissored) {
            graphics.disableScissor()
            scissored = false
        }
    }
}
