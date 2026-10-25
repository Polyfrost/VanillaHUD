package org.polyfrost.vanillahud.render

//? if > 1.8.9 {
import net.minecraft.client.gui.GuiGraphicsExtractor
import org.polyfrost.vanillahud.hud.Huds
import org.polyfrost.vanillahud.hud.VanillaHud

/**
 * Vanilla draws the title and subtitle in one call under one transform, so the call runs once per
 * element under that element's transform and each run only draws its own line.
 */
object TitlePass {
    const val BOTH = 0
    const val TITLE = 1
    const val SUBTITLE = 2

    @JvmStatic
    var current = BOTH
        private set

    @JvmStatic
    fun draw(graphics: GuiGraphicsExtractor, render: Runnable) {
        pass(graphics, TITLE, Huds.title, render)
        pass(graphics, SUBTITLE, Huds.subtitle, render)
    }

    private fun pass(graphics: GuiGraphicsExtractor, pass: Int, hud: VanillaHud, render: Runnable) {
        if (!hud.shouldDraw()) return
        current = pass
        HudTransform.begin(graphics, hud)
        try {
            render.run()
        } finally {
            HudTransform.end(graphics)
            current = BOTH
        }
    }
}
//?}
