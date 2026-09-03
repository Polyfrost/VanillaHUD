package org.polyfrost.vanillahud.hud

import net.minecraft.client.gui.GuiGraphicsExtractor
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.oneconfig.api.hud.v1.LegacyHud
import org.polyfrost.oneconfig.api.hud.v1.Section
import org.polyfrost.oneconfig.utils.v1.dsl.mc
import org.polyfrost.vanillahud.mixin.access.IGui
import org.polyfrost.vanillahud.render.HudTransform

abstract class VanillaHud(
    val hudId: String,
    title: String,
    category: Category,
) : LegacyHud(hudId, title, category) {
    init {
        locked = true
    }

    abstract val naturalWidth: Float
    abstract val naturalHeight: Float

    /** top left of the unrotated content in the coordinates vanilla actually draws it at */
    abstract fun vanillaOriginX(screenWidth: Int, screenHeight: Int): Float
    abstract fun vanillaOriginY(screenWidth: Int, screenHeight: Int): Float

    /** top left of the rotated bounding box at rest which differs from the vanilla origin only when [quarterTurns] is set */
    open fun defaultOriginX(screenWidth: Int, screenHeight: Int): Float = vanillaOriginX(screenWidth, screenHeight)
    open fun defaultOriginY(screenWidth: Int, screenHeight: Int): Float = vanillaOriginY(screenWidth, screenHeight)

    /** clockwise quarter turns applied to the whole element from 0 to 3 */
    open val quarterTurns: Int get() = 0

    private val turned: Boolean get() = quarterTurns % 2 != 0

    protected open val anchorX: Float get() = 0f
    protected open val anchorY: Float get() = 0f

    /** fraction of the measured size the origin subtracts so the stored [Section] must match the origin formula */
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

    /** stores in [preferredSection] rather than the inferred one since [relativeX] and [relativeY] clamp near a section edge */
    private fun placeAt(absX: Float, absY: Float) {
        section = preferredSection
        x = absX
        y = absY
    }

    /** [capturePositionDefaults] but placing through [placeAt] */
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
        defaultOriginX(screenWidth, screenHeight) + (1f - scale) * width * anchorX

    fun scaledOriginY(screenWidth: Int, screenHeight: Int, scale: Float = effectiveScale): Float =
        defaultOriginY(screenWidth, screenHeight) + (1f - scale) * height * anchorY

    override fun multipleInstancesAllowed() = false
    override fun deletable() = false
    override fun showByDefault() = true

    private var seededWidth = -1
    private var seededHeight = -1
    private var seededHudWidth = -1f
    private var seededHudHeight = -1f
    private var seededTurns = -1
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

    /** re-derives the stored position each frame since relative coords drift when the measured size changes */
    fun pinToVanillaOrigin(screenWidth: Int, screenHeight: Int, scale: Float = effectiveScale) {
        if (tree == null) return
        if (HudManager.guiScreenWidth.toInt() != screenWidth || HudManager.guiScreenHeight.toInt() != screenHeight) return
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
        // a half turn leaves the size alone but still moves the default so turns belongs in the key
        if (w == seededWidth && h == seededHeight &&
            renderedW == seededHudWidth && renderedH == seededHudHeight &&
            quarterTurns == seededTurns
        ) return
        seededWidth = w
        seededHeight = h
        seededHudWidth = renderedW
        seededHudHeight = renderedH
        seededTurns = quarterTurns
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

    /** content size before [quarterTurns] is applied which is what the render transform works in */
    val unrotatedWidth: Float get() = measuredWidth()
    val unrotatedHeight: Float get() = measuredHeight()

    override val width: Float get() = if (turned) measuredHeight() else measuredWidth()
    override val height: Float get() = if (turned) measuredWidth() else measuredHeight()

    private fun syncRenderedSize() {
        val scale = effectiveScale
        renderedW = (width * scale).coerceAtLeast(1f)
        renderedH = (height * scale).coerceAtLeast(1f)
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
            HudTransform.resetFrame()
        }

        @JvmStatic
        fun previewing(hud: VanillaHud?): Boolean {
            if (HudManager.isEditorOpen) return true
            return HudManager.isConfigUiOpen && hud?.locked == false
        }
    }
}
