package org.polyfrost.vanillahud.hud

//? if >=26 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor as GuiGraphics*/
//?} else {
import net.minecraft.client.gui.GuiGraphics
//?}
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.oneconfig.api.hud.v1.LegacyHud
import org.polyfrost.oneconfig.utils.v1.dsl.mc

abstract class VanillaHud(
    id: String,
    title: String,
    category: Category,
) : LegacyHud(id, title, category) {

    abstract val naturalWidth: Float
    abstract val naturalHeight: Float
    abstract fun vanillaOriginX(screenWidth: Int, screenHeight: Int): Float
    abstract fun vanillaOriginY(screenWidth: Int, screenHeight: Int): Float

    // TODO: Implement render restraint options. (e.g. showInF3)
    fun shouldRender(): Boolean {
        return true
    }

    protected open val exampleText: String? get() = null

    override val width: Float get() = measuredWidth()
    override val height: Float get() = naturalHeight

    protected open fun measuredWidth(): Float {
        //? if <26 {
        val t = exampleText ?: return naturalWidth
        return try {
            mc.font.width(t).toFloat()
        } catch (_: Throwable) {
            naturalWidth
        }
        //?} else {
        /*return naturalWidth*/
        //?}
    }

    override fun update() = false
    override fun hasBackground() = false

    override fun defaultPosition(): Pair<Float, Float> {
        val w = HudManager.guiScreenWidth.toInt().coerceAtLeast(1)
        val h = HudManager.guiScreenHeight.toInt().coerceAtLeast(1)
        return Pair(vanillaOriginX(w, h), vanillaOriginY(w, h))
    }

    override fun render(mcCtx: GuiGraphics) {}
}
