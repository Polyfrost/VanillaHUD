package org.polyfrost.vanillahud.hud

import net.minecraft.client.gui.GuiGraphicsExtractor
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.oneconfig.api.hud.v1.LegacyHud
import org.polyfrost.oneconfig.api.hud.v1.Section
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

    /**
     * How much of this HUD's own measured size [vanillaOriginX] / [vanillaOriginY] subtracts:
     * `0` for an origin fixed to the left / top, `0.5` for a centered one, `1` for an origin fixed to the
     * right / bottom.
     *
     * This picks the [Section] the position is stored relative to. It has to match the origin formula,
     * otherwise the stored relative position only reproduces the vanilla origin at the size it was captured
     * at: a HUD measured against live data (a full tab list) would jump when the editor measures the same
     * HUD against its smaller example data.
     */
    protected open val positionAnchorX: Float get() = anchorX
    protected open val positionAnchorY: Float get() = anchorY

    private val preferredSection: Section
        get() = when {
            positionAnchorY < 0.25f -> when {
                positionAnchorX < 0.25f -> Section.TopLeft
                positionAnchorX < 0.75f -> Section.TopCenter
                else -> Section.TopRight
            }
            positionAnchorY < 0.75f -> when {
                positionAnchorX < 0.25f -> Section.CenterLeft
                positionAnchorX < 0.75f -> Section.Center
                else -> Section.CenterRight
            }
            else -> when {
                positionAnchorX < 0.25f -> Section.BottomLeft
                positionAnchorX < 0.75f -> Section.BottomCenter
                else -> Section.BottomRight
            }
        }

    /**
     * Stores [absX] / [absY] in [preferredSection], instead of letting the section be inferred from
     * whichever third of the screen the HUD happens to sit in.
     *
     * Only for positions derived from the vanilla origin. A position the user dragged to has to keep the
     * inferred section: [relativeX] / [relativeY] are clamped to a couple of grid units from the section's
     * own edge, so forcing a spot the user picked into a far-away section clamps it and lands somewhere else
     * entirely.
     */
    private fun placeAt(absX: Float, absY: Float) {
        section = preferredSection
        x = absX
        y = absY
    }

    /** [capturePositionDefaults], but placing through [placeAt]. */
    private fun captureDefaults() {
        if (tree == null) return
        val (dx, dy) = defaultPosition()
        val curSection = section
        val curX = relativeX
        val curY = relativeY
        placeAt(dx, dy)
        try {
            getProperty("section")?.addMetadata("default", section)
            getProperty("relativeX")?.addMetadata("default", relativeX)
            getProperty("relativeY")?.addMetadata("default", relativeY)
        } finally {
            section = curSection
            relativeX = curX
            relativeY = curY
        }
    }

    fun scaledOriginX(screenWidth: Int, screenHeight: Int, scale: Float = effectiveScale): Float =
        vanillaOriginX(screenWidth, screenHeight) + (1f - scale) * width * anchorX

    fun scaledOriginY(screenWidth: Int, screenHeight: Int, scale: Float = effectiveScale): Float =
        vanillaOriginY(screenWidth, screenHeight) + (1f - scale) * height * anchorY

    override fun multipleInstancesAllowed() = false
    override fun deletable() = false
    override fun showByDefault() = true

    open fun linkTarget(): VanillaHud? = null

    private var seededWidth = -1
    private var seededHeight = -1
    private var seededHudWidth = -1f
    private var seededHudHeight = -1f
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
            captureDefaults()
            val (dx, dy) = defaultPosition()
            placeAt(dx, dy)
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
        locked && isAtDefaultPosition(fallback = true)

    /**
     * Snaps the stored position back onto the vanilla origin.
     *
     * A HUD that has never been moved has to draw at the vanilla origin, so it must not be routed through
     * `relativeX`/`relativeY`: those are read back against [renderedW]/[renderedH], and the moment the
     * measured size changes - the editor swapping live data for example data, most of all - the same stored
     * offset resolves to a different spot. Re-deriving it from the origin every frame keeps the editor's
     * outline and handles on top of what is actually being drawn.
     */
    fun pinToVanillaOrigin(screenWidth: Int, screenHeight: Int, scale: Float = effectiveScale) {
        if (tree == null) return
        syncRenderedSize()
        try {
            placeAt(scaledOriginX(screenWidth, screenHeight, scale), scaledOriginY(screenWidth, screenHeight, scale))
        } catch (_: Throwable) {
        }
    }

    fun reseedDefaultForScreen() {
        if (tree == null) return
        syncRenderedSize()
        val w = HudManager.guiScreenWidth.toInt().coerceAtLeast(1)
        val h = HudManager.guiScreenHeight.toInt().coerceAtLeast(1)
        if (w != mc.window.guiScaledWidth || h != mc.window.guiScaledHeight) return
        if (w == seededWidth && h == seededHeight &&
            renderedW == seededHudWidth && renderedH == seededHudHeight
        ) return
        seededWidth = w
        seededHeight = h
        seededHudWidth = renderedW
        seededHudHeight = renderedH
        try {
            val wasDefault = isAtDefaultPosition()
            captureDefaults()
            if (wasDefault) {
                val (dx, dy) = defaultPosition()
                placeAt(dx, dy)
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

    fun shouldDraw(): Boolean {
        if (hidden && !HudManager.isEditing) return false
        if (HudManager.isDebugScreenVisible && !showInF3) return false
        if (HudManager.isTabListVisible && !showInTab) return false
        if (!HudManager.overrideShowInScreens && !HudManager.isEditing) {
            if (HudManager.isChatScreenOpen) {
                if (!showInChat) return false
            } else if (HudManager.isGuiScreenOpen && !showInScreens) return false
        }
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
