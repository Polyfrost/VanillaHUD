package org.polyfrost.vanillahud.hud

import net.minecraft.client.gui.GuiGraphicsExtractor
import org.polyfrost.oneconfig.api.hud.v1.HudAnchor
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.oneconfig.api.hud.v1.LegacyHud
import org.polyfrost.oneconfig.api.hud.v1.Section
import org.polyfrost.oneconfig.utils.v1.dsl.mc
import org.polyfrost.vanillahud.mixin.access.IGui
import org.polyfrost.vanillahud.render.HudTransform
import org.polyfrost.vanillahud.util.ForceDefaultPosition

abstract class VanillaHud(
    val hudId: String,
    title: String,
    category: Category,
) : LegacyHud(hudId, title, category) {
    init {
        locked = true
        currentSchema = posSchema
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

    protected open val positionAnchorX: Float get() = anchorX
    protected open val positionAnchorY: Float get() = anchorY

    protected open val sectionAnchorY: Float get() = positionAnchorY

    private val preferredSection: Section get() = sectionFor(positionAnchorX, sectionAnchorY)

    private val preferredGrowthAnchor: HudAnchor
        get() = if (sectionFor(positionAnchorX, positionAnchorY) == preferredSection) HudAnchor.Auto
        else growthAnchorFor(positionAnchorX, positionAnchorY)

    private inline fun <T> asSystemWrite(block: () -> T): T {
        val wasSystem = HudInternals.systemReposition()
        HudInternals.systemReposition(true)
        try {
            return block()
        } finally {
            HudInternals.systemReposition(wasSystem)
        }
    }

    private fun placeAt(absX: Float, absY: Float) = asSystemWrite {
        restoringPosition(keepOnSuccess = true) {
            section = preferredSection
            x = absX
            y = absY
        }
    }

    private var sectionChecked = false

    private var measureOwed = false

    private val positionSchemaReady: Boolean get() = posSchema == currentSchema

    private fun ensureSection() {
        if (tree == null || !positionSchemaReady) return
        val wantedAnchor = preferredGrowthAnchor.takeIf { growthAnchor == HudAnchor.Auto && it != HudAnchor.Auto }
        if (sectionChecked && wantedAnchor == null) return
        val customized = ForceDefaultPosition.customized(this)
        if (!locked || isAnchored || customized != false) {
            sectionChecked = customized != null
            return
        }
        try {
            syncRenderedSize()
            if (wantedAnchor != null) asSystemWrite {
                restoringPosition(keepOnSuccess = true) { setGrowthAnchorKeepingPosition(wantedAnchor) }
            }
            if (section != preferredSection) placeAt(x, y)
            sectionChecked = true
        } catch (_: Throwable) {
            measureOwed = true
        }
    }

    private inline fun restoringPosition(keepOnSuccess: Boolean, block: () -> Unit) {
        val curSection = section
        val curAnchor = growthAnchor
        val curX = relativeX
        val curY = relativeY
        val curOffX = anchorOffsetX
        val curOffY = anchorOffsetY
        var placed = false
        try {
            block()
            placed = true
        } finally {
            if (!keepOnSuccess || !placed) {
                section = curSection
                growthAnchor = curAnchor
                relativeX = curX
                relativeY = curY
                anchorOffsetX = curOffX
                anchorOffsetY = curOffY
            }
        }
    }

    private fun captureDefaults() {
        if (tree == null) return
        val (dx, dy) = defaultPosition()
        restoringPosition(keepOnSuccess = false) {
            placeAt(dx, dy)
            getProperty("section").addMetadata("default", section)
            getProperty("relativeX").addMetadata("default", relativeX)
            getProperty("relativeY").addMetadata("default", relativeY)
            runCatching { getProperty("growthAnchor") }.getOrNull()?.addMetadata("default", growthAnchor)
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
    private var seededAnchor: HudAnchor? = null
    private var forcePending = false

    private fun forgetProfileState() {
        sectionChecked = false
        forcePending = false
        measureOwed = false
        seededWidth = -1
        seededHeight = -1
        seededHudWidth = -1f
        seededHudHeight = -1f
        seededTurns = -1
        seededAnchor = null
    }

    fun queueForceDefault() {
        forcePending = true
    }

    fun cancelForceDefault() {
        forcePending = false
    }

    fun applyForceDefault() {
        if (!forcePending || tree == null || !positionSchemaReady) return
        if (!locked || isAnchored) {
            forcePending = false
            return
        }
        val w = HudManager.guiScreenWidth.toInt()
        val h = HudManager.guiScreenHeight.toInt()
        if (w <= 0 || h <= 0) return
        try {
            syncRenderedSize()
            captureDefaults()
            val (dx, dy) = defaultPosition()
            placeAt(dx, dy)
            forcePending = false
        } catch (_: Throwable) {
            measureOwed = true
        }
    }

    private fun isAtDefaultPosition(fallback: Boolean = false): Boolean {
        if (isAnchored) return false
        return try {
            val relXDef = getProperty("relativeX").getMetadata<Float?>("default") ?: return fallback
            val relYDef = getProperty("relativeY").getMetadata<Float?>("default") ?: return fallback
            val sectionDef = getProperty("section").getMetadata<Any?>("default") ?: return fallback
            sectionDef == section &&
                kotlin.math.abs(relXDef - relativeX) < 1e-4f &&
                kotlin.math.abs(relYDef - relativeY) < 1e-4f
        } catch (_: Throwable) {
            fallback
        }
    }

    fun anchorsToVanillaOrigin(): Boolean =
        tree != null && locked && positionSchemaReady && isAtDefaultPosition(fallback = true)

    /** re-derives the stored position each frame since relative coords drift when the measured size changes */
    fun pinToVanillaOrigin(screenWidth: Int, screenHeight: Int, scale: Float = effectiveScale) {
        if (tree == null || !positionSchemaReady) return
        if (HudManager.guiScreenWidth.toInt() != screenWidth || HudManager.guiScreenHeight.toInt() != screenHeight) return
        try {
            syncRenderedSize()
            placeAt(scaledOriginX(screenWidth, screenHeight, scale), scaledOriginY(screenWidth, screenHeight, scale))
        } catch (_: Throwable) {
            measureOwed = true
        }
    }

    fun reseedDefaultForScreen(measure: Boolean = true) {
        if (tree == null || !positionSchemaReady) return
        val w = HudManager.guiScreenWidth.toInt()
        val h = HudManager.guiScreenHeight.toInt()
        if (w <= 0 || h <= 0) return
        val screenSame = w == seededWidth && h == seededHeight && quarterTurns == seededTurns &&
            growthAnchor == seededAnchor
        if (screenSame && !measure) return
        try {
            syncRenderedSize()
            if (screenSame && renderedW == seededHudWidth && renderedH == seededHudHeight) return
            val wasDefault = isAtDefaultPosition()
            captureDefaults()
            if (wasDefault) {
                val (dx, dy) = defaultPosition()
                placeAt(dx, dy)
            }
            commitSeed(w, h, renderedW, renderedH)
        } catch (_: Throwable) {
            commitSeed(w, h, -1f, -1f)
            measureOwed = true
        }
    }

    private fun commitSeed(w: Int, h: Int, hudW: Float, hudH: Float) {
        seededWidth = w
        seededHeight = h
        seededHudWidth = hudW
        seededHudHeight = hudH
        seededTurns = quarterTurns
        seededAnchor = growthAnchor
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
        measureOwed = false
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

    private fun measureKey(): Int = HudManager.guiScreenWidth.toInt() * 31 + if (previewing) 1 else 0

    override fun update() = false
    override fun hasBackground() = false

    override fun defaultPosition(): Pair<Float, Float> {
        val w = HudManager.guiScreenWidth.toInt()
        val h = HudManager.guiScreenHeight.toInt()
        return Pair(scaledOriginX(w, h), scaledOriginY(w, h))
    }

    override fun render(mcCtx: GuiGraphicsExtractor) {}

    companion object {
        private var currentSchema: Int? = null

        private var frame = 0L

        private var seenRevision = HudManager.revision

        @JvmStatic
        fun beginFrame(graphics: GuiGraphicsExtractor) {
            HudTransform.resetFrame()
            if (!HudManager.isEditing) {
                HudManager.guiScreenWidth = graphics.guiWidth().toFloat()
                HudManager.guiScreenHeight = graphics.guiHeight().toFloat()
            }
            refreshAll()
        }

        @JvmStatic
        internal fun refreshAll() {
            frame++
            val revision = HudManager.revision
            if (revision != seenRevision) {
                try {
                    ForceDefaultPosition.invalidate()
                    forgetProfileStateAll()
                    seenRevision = revision
                } catch (_: Throwable) {
                }
            }
            for (hud in HudManager.activeInstances) {
                if (hud !is VanillaHud || !hud.shouldDraw()) continue
                try {
                    if (!hud.measureOwed) {
                        hud.ensureSection()
                        hud.applyForceDefault()
                    }
                    hud.reseedDefaultForScreen(measure = false)
                } catch (_: Throwable) {
                }
            }
        }

        fun forgetProfileStateAll() {
            for (hud in HudManager.activeInstances) {
                if (hud is VanillaHud) hud.forgetProfileState()
            }
        }

        @JvmStatic
        fun previewing(hud: VanillaHud?): Boolean {
            if (HudManager.isEditorOpen) return true
            return HudManager.isConfigUiOpen && hud?.locked == false
        }
    }
}

internal fun sectionFor(fx: Float, fy: Float): Section = when {
    fy < 0.25f -> when {
        fx < 0.25f -> Section.TopLeft
        fx < 0.75f -> Section.TopCenter
        else -> Section.TopRight
    }
    fy < 0.75f -> when {
        fx < 0.25f -> Section.CenterLeft
        fx < 0.75f -> Section.Center
        else -> Section.CenterRight
    }
    else -> when {
        fx < 0.25f -> Section.BottomLeft
        fx < 0.75f -> Section.BottomCenter
        else -> Section.BottomRight
    }
}

internal fun growthAnchorFor(fx: Float, fy: Float): HudAnchor = when {
    fy < 0.25f -> when {
        fx < 0.25f -> HudAnchor.TopLeft
        fx < 0.75f -> HudAnchor.Top
        else -> HudAnchor.TopRight
    }
    fy < 0.75f -> when {
        fx < 0.25f -> HudAnchor.Left
        fx < 0.75f -> HudAnchor.Center
        else -> HudAnchor.Right
    }
    else -> when {
        fx < 0.25f -> HudAnchor.BottomLeft
        fx < 0.75f -> HudAnchor.Bottom
        else -> HudAnchor.BottomRight
    }
}
