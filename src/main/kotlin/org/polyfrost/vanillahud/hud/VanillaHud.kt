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
    init {
        locked = true
    }

    abstract val naturalWidth: Float
    abstract val naturalHeight: Float
    abstract fun vanillaOriginX(screenWidth: Int, screenHeight: Int): Float
    abstract fun vanillaOriginY(screenWidth: Int, screenHeight: Int): Float

    protected open val anchorX: Float get() = 0f
    protected open val anchorY: Float get() = 0f

    fun scaledOriginX(screenWidth: Int, screenHeight: Int, scale: Float = effectiveScale): Float =
        vanillaOriginX(screenWidth, screenHeight) + (1f - scale) * width * anchorX

    fun scaledOriginY(screenWidth: Int, screenHeight: Int, scale: Float = effectiveScale): Float =
        vanillaOriginY(screenWidth, screenHeight) + (1f - scale) * height * anchorY

    override fun multipleInstancesAllowed() = false
    override fun deletable() = false

    open fun linkTarget(): VanillaHud? = null

    private var seededWidth = -1
    private var seededHeight = -1
    private var forcePending = false

    fun queueForceDefault() {
        forcePending = true
    }

    fun cancelForceDefault() {
        forcePending = false
    }

    fun applyForceDefault() {
        if (!forcePending || tree == null) return
        syncRenderedSize()
        val w = HudManager.guiScreenWidth.toInt().coerceAtLeast(1)
        val h = HudManager.guiScreenHeight.toInt().coerceAtLeast(1)
        if (w != mc.window.guiScaledWidth || h != mc.window.guiScaledHeight) return
        try {
            capturePositionDefaults()
            val (dx, dy) = defaultPosition()
            setAbsolutePosition(dx, dy)
        } catch (_: Throwable) {
        }
        forcePending = false
    }

    private fun isAtDefaultPosition(fallback: Boolean = false): Boolean {
        return try {
            val relXDef = getProperty("relativeX")?.getMetadata<Float?>("default") ?: return fallback
            val relYDef = getProperty("relativeY")?.getMetadata<Float?>("default") ?: return fallback
            val sectionDef = getProperty("section")?.getMetadata<Any?>("default") ?: return fallback
            sectionDef == section &&
                kotlin.math.abs(relXDef - relativeX) < 1e-4f &&
                kotlin.math.abs(relYDef - relativeY) < 1e-4f
        } catch (_: Throwable) {
            fallback
        }
    }

    fun anchorsToVanillaOrigin(): Boolean =
        locked && !previewing && isAtDefaultPosition(fallback = true)

    fun reseedDefaultForScreen() {
        if (tree == null) return
        syncRenderedSize()
        val w = HudManager.guiScreenWidth.toInt().coerceAtLeast(1)
        val h = HudManager.guiScreenHeight.toInt().coerceAtLeast(1)
        if (w != mc.window.guiScaledWidth || h != mc.window.guiScaledHeight) return
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
        target.syncRenderedSize()
        val w = HudManager.guiScreenWidth.toInt().coerceAtLeast(1)
        val h = HudManager.guiScreenHeight.toInt().coerceAtLeast(1)
        val offX = scaledOriginX(w, h) - target.scaledOriginX(w, h)
        val offY = scaledOriginY(w, h) - target.scaledOriginY(w, h)
        setAbsolutePosition(target.x + offX, target.y + offY)
    }

    val previewing: Boolean get() = previewing(this)

    fun shouldRender(): Boolean {
        if (HudManager.isEditing) return true

        if (hidden) return false
        if (HudManager.isDebugScreenVisible && !showInF3) return false
        if (HudManager.isTabListVisible && !showInTab) return false
        if (HudManager.isGuiScreenOpen && !showInScreens) return false

        return true
    }

    protected open val exampleText: String? get() = null

    override val width: Float get() = measuredWidth()
    override val height: Float get() = measuredHeight()

    private fun syncRenderedSize() {
        val scale = effectiveScale
        renderedW = width * scale
        renderedH = height * scale
    }

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

    private var measuredFrame = -1L
    private var measuredKey = 0
    private var measured: Any? = null

    @Suppress("UNCHECKED_CAST")
    protected fun <T : Any> measureOnce(measure: () -> T?): T? {
        val key = measureKey()
        if (measuredFrame == frame && measuredKey == key) return measured as T?
        val value = measure()
        measured = value
        measuredFrame = frame
        measuredKey = key
        return value
    }

    private fun measureKey(): Int {
        var key = if (previewing) 1 else 0
        key = key * 31 + HudManager.guiScreenWidth.toInt()
        key = key * 31 + HudManager.guiScreenHeight.toInt()
        return key
    }

    override fun update() = false
    override fun hasBackground() = false

    override fun defaultPosition(): Pair<Float, Float> {
        val w = HudManager.guiScreenWidth.toInt().coerceAtLeast(1)
        val h = HudManager.guiScreenHeight.toInt().coerceAtLeast(1)
        return Pair(scaledOriginX(w, h), scaledOriginY(w, h))
    }

    override fun render(mcCtx: GuiGraphicsExtractor) {}

    companion object {
        private var frame = 0L

        @JvmStatic
        fun beginFrame() {
            frame++
        }

        @JvmStatic
        fun previewing(hud: VanillaHud?): Boolean {
            if (HudManager.isEditorOpen) return true
            return HudManager.isConfigUiOpen && hud?.locked == false
        }
    }
}
