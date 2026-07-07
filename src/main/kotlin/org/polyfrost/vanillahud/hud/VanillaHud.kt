package org.polyfrost.vanillahud.hud

import net.minecraft.client.gui.GuiGraphicsExtractor
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.oneconfig.api.hud.v1.LegacyHud
import org.polyfrost.oneconfig.utils.v1.dsl.mc
import org.polyfrost.vanillahud.mixin.access.IGui

abstract class VanillaHud(
    id: String,
    title: String,
    category: Category,
) : LegacyHud(id, title, category) {
    abstract val naturalWidth: Float
    abstract val naturalHeight: Float
    abstract fun vanillaOriginX(screenWidth: Int, screenHeight: Int): Float
    abstract fun vanillaOriginY(screenWidth: Int, screenHeight: Int): Float

    override fun multipleInstancesAllowed() = false

    open fun linkTarget(): VanillaHud? = null

    fun applyLink() {
        val target = linkTarget() ?: return
        if (target === this || target.linkTarget() === this) return
        target.applyLink()
        val w = HudManager.guiScreenWidth.toInt().coerceAtLeast(1)
        val h = HudManager.guiScreenHeight.toInt().coerceAtLeast(1)
        val offX = vanillaOriginX(w, h) - target.vanillaOriginX(w, h)
        val offY = vanillaOriginY(w, h) - target.vanillaOriginY(w, h)
        setAbsolutePosition(target.x + offX, target.y + offY)
    }

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

    protected val hudAccessor: IGui?
        get() = try {
            //? if >=26.2 {
            mc.gui.hud as? IGui
            //?} else {
            /*mc.gui as? IGui
            *///?}
        } catch (_: Throwable) {
            null
        }

    protected fun textWidth(text: () -> String?): Float =
        try {
            text()?.let { mc.font.width(it).toFloat() } ?: naturalWidth
        } catch (_: Throwable) {
            naturalWidth
        }

    protected open fun measuredWidth(): Float = textWidth { exampleText }

    override fun update() = false
    override fun hasBackground() = false

    override fun defaultPosition(): Pair<Float, Float> {
        val w = HudManager.guiScreenWidth.toInt().coerceAtLeast(1)
        val h = HudManager.guiScreenHeight.toInt().coerceAtLeast(1)
        return Pair(vanillaOriginX(w, h), vanillaOriginY(w, h))
    }

    override fun render(mcCtx: GuiGraphicsExtractor) {}
}
