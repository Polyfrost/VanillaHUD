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
    override fun deletable() = false

    open fun linkTarget(): VanillaHud? = null

    private var seededWidth = -1
    private var seededHeight = -1

    private fun isAtDefaultPosition(): Boolean {
        return try {
            val relXDef = getProperty("relativeX")?.getMetadata<Float?>("default") ?: return false
            val relYDef = getProperty("relativeY")?.getMetadata<Float?>("default") ?: return false
            val sectionDef = getProperty("section")?.getMetadata<Any?>("default") ?: return false
            sectionDef == section &&
                kotlin.math.abs(relXDef - relativeX) < 1e-4f &&
                kotlin.math.abs(relYDef - relativeY) < 1e-4f
        } catch (_: Throwable) {
            false
        }
    }

    fun reseedDefaultForScreen() {
        if (tree == null) return
        val w = HudManager.guiScreenWidth.toInt().coerceAtLeast(1)
        val h = HudManager.guiScreenHeight.toInt().coerceAtLeast(1)
        if (w == seededWidth && h == seededHeight) return
        seededWidth = w
        seededHeight = h
        try {
            val wasDefault = isAtDefaultPosition()
            capturePositionDefaults()
            if (wasDefault) {
                val (dx, dy) = defaultPosition()
                setAbsolutePosition(dx, dy)
            }
        } catch (_: Throwable) {
        }
    }

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

        if (HudManager.isDebugScreenVisible && !showInF3) return false
        if (HudManager.isTabListVisible && !showInTab) return false
        if (HudManager.isGuiScreenOpen && !showInScreens) return false

        return true
    }

    protected open val exampleText: String? get() = null

    override val width: Float get() = measuredWidth()
    override val height: Float get() = measuredHeight()

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

    protected open fun measuredHeight(): Float = naturalHeight

    override fun update() = false
    override fun hasBackground() = false

    override fun defaultPosition(): Pair<Float, Float> {
        val w = HudManager.guiScreenWidth.toInt().coerceAtLeast(1)
        val h = HudManager.guiScreenHeight.toInt().coerceAtLeast(1)
        return Pair(vanillaOriginX(w, h), vanillaOriginY(w, h))
    }

    override fun render(mcCtx: GuiGraphicsExtractor) {}
}
