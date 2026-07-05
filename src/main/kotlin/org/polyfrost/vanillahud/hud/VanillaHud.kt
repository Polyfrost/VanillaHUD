package org.polyfrost.vanillahud.hud

import net.minecraft.client.gui.GuiGraphicsExtractor
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

    fun shouldRender(): Boolean {
        if (HudManager.isEditing) return true

        if (HudManager.isDebugScreenVisible && showInF3) return false
        if (HudManager.isTabListVisible && showInTab) return false
        if (HudManager.isGuiScreenOpen && showInScreens) return false

        return true
    }

    protected open val exampleText: String? get() = null

    override val width: Float get() = measuredWidth()
    override val height: Float get() = naturalHeight

    protected open fun measuredWidth(): Float {
        val t = exampleText ?: return naturalWidth
        return try {
            mc.font.width(t).toFloat()
        } catch (_: Throwable) {
            naturalWidth
        }
    }

    override fun update() = false
    override fun hasBackground() = false

    override fun defaultPosition(): Pair<Float, Float> {
        val w = HudManager.guiScreenWidth.toInt().coerceAtLeast(1)
        val h = HudManager.guiScreenHeight.toInt().coerceAtLeast(1)
        return Pair(vanillaOriginX(w, h), vanillaOriginY(w, h))
    }

    override fun render(mcCtx: GuiGraphicsExtractor) {}
}
