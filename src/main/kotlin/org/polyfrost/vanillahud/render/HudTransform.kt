package org.polyfrost.vanillahud.render

import net.minecraft.client.gui.GuiGraphicsExtractor
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.vanillahud.hud.VanillaHud

object HudTransform {
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
        hud?.applyLink()
        val w = graphics.guiWidth()
        val h = graphics.guiHeight()
        val ox = provider.vanillaOriginX(w, h)
        val oy = provider.vanillaOriginY(w, h)
        val gx = hud?.x ?: ox
        val gy = hud?.y ?: oy
        val s = hud?.effectiveScale ?: 1f
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
    }
}
