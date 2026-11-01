package org.polyfrost.vanillahud.hud

//? if > 1.8.9 {
import net.minecraft.client.gui.GuiGraphicsExtractor
//?} else {
/*import org.polyfrost.vanillahud.compat.GuiGraphicsExtractor
*///?}
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import org.polyfrost.compose.render.PolyColor
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.config.v1.Tree
import org.polyfrost.oneconfig.api.hud.v1.HudAnchor
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.oneconfig.api.hud.v1.Hud
import org.polyfrost.oneconfig.api.hud.v1.Section
import org.polyfrost.oneconfig.utils.v1.dsl.mc
import org.polyfrost.vanillahud.mixin.access.IGui
import org.polyfrost.vanillahud.render.HudCapture
import org.polyfrost.vanillahud.render.HudTransform
import org.polyfrost.vanillahud.util.ForceDefaultPosition
import kotlin.math.abs

abstract class VanillaHud(
    val hudId: String,
    title: String,
    category: Category,
) : Hud(hudId, title, category) {
    private var shownSize = 0L

    /** read by [Content] so a bump recomposes it, which contentChanged() alone does not do */
    private var recompose by mutableIntStateOf(0)

    init {
        currentSchema = posSchema
        showBackground = false
        bgRadius = 0f
    }

    override fun addCallbacks(tree: Tree) {
        super.addCallbacks(tree)
        tree.addDependencies()
    }

    protected open fun Tree.addDependencies() {}

    @Suppress("UNCHECKED_CAST")
    protected fun Tree.hideUnless(parent: String, vararg children: String) {
        val p = getProp(parent) as Property<Boolean>
        for (child in children) getProp(child).addDisplayCondition(p, true)
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T> Tree.hideUnless(parent: String, vararg children: String, test: (T?) -> Boolean) {
        val p = getProp(parent) as Property<T>
        for (child in children) {
            val c = getProp(child)
            c.addDisplayCondition { if (test(p.get())) Property.Display.SHOWN else Property.Display.HIDDEN }
            p.addCallback { c.revaluateDisplay(); false }
        }
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

    /** reverses the element along its length */
    open val mirrored: Boolean get() = false

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

    internal var movedByUser = false

    val pinned: Boolean
        get() = !movedByUser && (ForceDefaultPosition.customized(this) != true || matchesDefaultPosition(fallback = false))

    override fun updateRelativeX(absX: Float) {
        super.updateRelativeX(absX)
        noteUserMove()
    }

    override fun updateRelativeY(absY: Float) {
        super.updateRelativeY(absY)
        noteUserMove()
    }

    private fun noteUserMove() {
        if (tree != null && !HudInternals.systemReposition() && previewing()) movedByUser = true
    }

    private fun ensureSection() {
        if (tree == null || !positionSchemaReady) return
        val wantedAnchor = preferredGrowthAnchor.takeIf { growthAnchor == HudAnchor.Auto && it != HudAnchor.Auto }
        if (sectionChecked && wantedAnchor == null) return
        val customized = ForceDefaultPosition.customized(this)
        if (!pinned || isAnchored || customized != false) {
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
        positionRevision++
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
        defaultOriginX(screenWidth, screenHeight) + (1f - scale * elementScale) * width * anchorX - contentOffsetX * scale

    fun scaledOriginY(screenWidth: Int, screenHeight: Int, scale: Float = effectiveScale): Float =
        defaultOriginY(screenWidth, screenHeight) + (1f - scale * elementScale) * height * anchorY - contentOffsetY * scale

    override fun multipleInstancesAllowed() = false
    override fun deletable() = false
    override fun showByDefault() = true

    private data class Seed(val w: Int, val h: Int, val hudW: Float, val hudH: Float, val turns: Int, val anchor: HudAnchor?)

    private var seed: Seed? = null
    private var forcePending = false

    private fun forgetProfileState() {
        sectionChecked = false
        forcePending = false
        measureOwed = false
        seed = null
        measuredValid = false
        measuredFrame = -1L
    }

    fun queueForceDefault() {
        forcePending = true
        refreshDue = true
    }

    fun cancelForceDefault() {
        forcePending = false
    }

    /** the HUD this one follows until the user moves it, as the status bars follow the hotbar in vanilla */
    protected open val defaultAnchorTarget: VanillaHud? get() = null

    /** true while this HUD holds its default anchor, false once the user anchors it by hand */
    val onDefaultAnchor: Boolean
        get() = pinned && isAnchored && anchorParent === defaultAnchorTarget

    val anchoredByHand: Boolean
        get() = isAnchored && anchorParent !== defaultAnchorTarget

    val backAtDefault: Boolean
        get() = !anchoredByHand && matchesDefaultPosition(fallback = false)

    private fun holdDefaultAnchor(target: VanillaHud, screenWidth: Int, screenHeight: Int) {
        asSystemWrite {
            positionRevision++
            anchorTo(target, HudAnchor.TopLeft, HudAnchor.TopLeft)
            anchorOffsetX = defaultOriginX(screenWidth, screenHeight) - target.defaultOriginX(screenWidth, screenHeight)
            anchorOffsetY = defaultOriginY(screenWidth, screenHeight) - target.defaultOriginY(screenWidth, screenHeight)
        }
    }

    /**
     * OneConfig's reset only knows about the screen position, so it either clears the anchor or
     * leaves a dragged offset in place. A stored position back on its default is how a reset shows up.
     */
    fun restoreDefaultAnchor() {
        val target = defaultAnchorTarget?.takeIf { it.tree != null } ?: return
        if (tree == null || !positionSchemaReady || !matchesDefaultPosition(fallback = false)) return
        if (isAnchored && anchorParent !== target) return
        val w = HudManager.guiScreenWidth.toInt()
        val h = HudManager.guiScreenHeight.toInt()
        if (w <= 0 || h <= 0) return
        val offsetX = defaultOriginX(w, h) - target.defaultOriginX(w, h)
        val offsetY = defaultOriginY(w, h) - target.defaultOriginY(w, h)
        if (isAnchored && abs(anchorOffsetX - offsetX) < 1e-3f && abs(anchorOffsetY - offsetY) < 1e-3f) return
        holdDefaultAnchor(target, w, h)
    }

    fun applyForceDefault() {
        if (!forcePending || tree == null || !positionSchemaReady) return
        if (!pinned || (isAnchored && !onDefaultAnchor)) {
            forcePending = false
            return
        }
        val w = HudManager.guiScreenWidth.toInt()
        val h = HudManager.guiScreenHeight.toInt()
        if (w <= 0 || h <= 0) return
        try {
            syncRenderedSize()
            captureDefaults()
            val target = defaultAnchorTarget?.takeIf { it.tree != null }
            if (target != null) {
                holdDefaultAnchor(target, w, h)
            } else {
                if (isAnchored) asSystemWrite { clearAnchor() }
                val (dx, dy) = defaultPosition()
                placeAt(dx, dy)
            }
            forcePending = false
        } catch (_: Throwable) {
            measureOwed = true
        }
    }

    private fun isAtDefaultPosition(fallback: Boolean = false): Boolean =
        !isAnchored && matchesDefaultPosition(fallback)

    private fun matchesDefaultPosition(fallback: Boolean): Boolean {
        return try {
            val relXDef = getProperty("relativeX").getMetadata<Float?>("default") ?: return fallback
            val relYDef = getProperty("relativeY").getMetadata<Float?>("default") ?: return fallback
            val sectionDef = getProperty("section").getMetadata<Any?>("default") ?: return fallback
            sectionDef == section &&
                abs(relXDef - relativeX) < 1e-4f &&
                abs(relYDef - relativeY) < 1e-4f
        } catch (_: Throwable) {
            fallback
        }
    }

    fun anchorsToVanillaOrigin(): Boolean =
        tree != null && pinned && positionSchemaReady && isAtDefaultPosition(fallback = true)

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
        val seed = seed
        val screenSame = seed != null && w == seed.w && h == seed.h && quarterTurns == seed.turns &&
            growthAnchor == seed.anchor
        if (screenSame && !measure) return
        try {
            val scale = effectiveScale
            if (screenSame && (boxWidth * scale).coerceAtLeast(1f) == seed!!.hudW &&
                (boxHeight * scale).coerceAtLeast(1f) == seed.hudH
            ) {
                measureOwed = false
                return
            }
            syncRenderedSize()
            if (screenSame && renderedW == seed!!.hudW && renderedH == seed.hudH) return
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
        seed = Seed(w, h, hudW, hudH, quarterTurns, growthAnchor)
    }

    val previewing: Boolean get() = HudManager.isConfigUiOpen || HudManager.isEditorOpen && !liveThisFrame()

    private var liveFrame = -1L
    private var live = false

    private fun liveThisFrame(): Boolean {
        if (mc.player == null) return false
        if (liveFrame != frame) {
            live = try {
                hasLiveData()
            } catch (_: Throwable) {
                false
            }
            liveFrame = frame
        }
        return live
    }

    fun shouldDraw(): Boolean {
        if (!HudManager.isEditing && hidden) return false
        if (HudManager.isDebugScreenVisible && !showInF3) return false
        if (HudManager.isTabListVisible && !showInTab) return false
        if (!HudManager.overrideShowInScreens && !HudManager.isEditing) {
            if (HudManager.isChatScreenOpen) {
                if (!showInChat) return false
            } else if (HudManager.isGuiScreenOpen && !showInScreens) return false
        }
        return true
    }

    open fun hasContent(): Boolean = true

    protected open fun hasLiveData(): Boolean = false

    protected open val exampleText: String? get() = null

    /** content size before [quarterTurns] is applied which is what the render transform works in */
    val unrotatedWidth: Float get() = measuredWidth()
    val unrotatedHeight: Float get() = measuredHeight()

    open val width: Float get() = if (turned) measuredHeight() else measuredWidth()
    open val height: Float get() = if (turned) measuredWidth() else measuredHeight()

    /** whether Compose paints this HUD's background, the others draw theirs in the game's own pass */
    protected open val composeBackground: Boolean get() = false

    /** the Designer's size and resize handles change [textScale], and here that sizes the whole element with its background */
    open val elementScale: Float get() = textScale

    /** text whose colour the game's pass already resolved from [textColor] */
    open val managesTextColor: Boolean get() = false

    /** the background the game draws for this element stretches to the box, which only matters once the box differs from the content */
    open val stretchesBackground: Boolean get() = false

    open val everyFillIsBackground: Boolean get() = false

    val customBackgroundBox: Boolean get() = bgRadius > 0f || pads || staticWidth

    /** the scale the game applies itself inside this element's pose, as a title does */
    open val innerScale: Float get() = 1f

    val bgArgb: Int get() = PolyColor(bgColor, bgChroma, bgChromaSpeed).argb

    private val pads: Boolean get() = padLeft != 0f || padRight != 0f || padTop != 0f || padBottom != 0f

    private val drawnWidth: Float get() = width * elementScale

    private val drawnHeight: Float get() = height * elementScale

    val boxWidth: Float
        get() = if (staticWidth) maxOf(staticW, drawnWidth) else drawnWidth + (padLeft + padRight) * elementScale

    val boxHeight: Float
        get() = if (staticWidth) maxOf(staticH, drawnHeight) else drawnHeight + (padTop + padBottom) * elementScale

    /** where the content sits inside the box, zero unless the Designer's padding or static size is in use */
    val contentOffsetX: Float
        get() = when {
            staticWidth -> (boxWidth - drawnWidth) * alignment.name.let { if ("Left" in it) 0f else if ("Right" in it) 1f else 0.5f }
            else -> padLeft * elementScale
        }

    val contentOffsetY: Float
        get() = when {
            staticWidth -> (boxHeight - drawnHeight) * alignment.name.let { if ("Top" in it) 0f else if ("Bottom" in it) 1f else 0.5f }
            else -> padTop * elementScale
        }

    private fun syncRenderedSize() {
        val scale = effectiveScale
        val w = (boxWidth * scale).coerceAtLeast(1f)
        val h = (boxHeight * scale).coerceAtLeast(1f)
        if (w != renderedW || h != renderedH) {
            positionRevision++
            renderedW = w
            renderedH = h
        }
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
            text()?.let { HudCapture.measuring(this) { mc.font.width(it).toFloat() } } ?: naturalWidth
        } catch (_: Throwable) {
            naturalWidth
        }

    protected open fun measuredWidth(): Float = textWidth { exampleText }

    protected open fun measuredHeight(): Float = naturalHeight

    private var measuredFrame = -1L
    private var measuredBase = 0
    private var measuredStamp = 0L
    private var measuredEpoch = 0L
    private var measuredValid = false
    private var measured: Any? = null

    @Suppress("UNCHECKED_CAST")
    protected fun <T : Any> measureOnce(key: () -> Long, measure: () -> T?): T? {
        val base = measureKey()
        if (measuredFrame == frame && measuredBase == base) return measured as T?
        val stamp = key()
        val epoch = clock() shr 10
        if (!measuredValid || HudManager.isEditing || measuredBase != base || measuredStamp != stamp ||
            measuredEpoch != epoch
        ) {
            measured = HudCapture.measuring(this, measure)
            measuredValid = true
            measuredStamp = stamp
            measuredEpoch = epoch
        }
        measuredBase = base
        measuredFrame = frame
        return measured as T?
    }

    protected fun mix(h: Long, o: Any?): Long = h * 31 + System.identityHashCode(o)

    protected fun mix(h: Long, v: Int): Long = h * 31 + v

    protected fun mix(h: Long, v: Boolean): Long = h * 31 + if (v) 1 else 0

    private fun measureKey(): Int = HudManager.guiScreenWidth.toInt() * 31 + if (previewing) 1 else 0

    override fun update(): Boolean {
        val size = boxWidth.toBits().toLong() * 31 + boxHeight.toBits()
        if (size != shownSize) {
            shownSize = size
            recompose++
            contentChanged()
        }
        return true
    }

    /** repaints only this HUD, since OneConfig can't see what the game's pass drew */
    fun capturedDrawsChanged() = contentChanged()

    override fun minimumSize(): Pair<Float, Float> = boxWidth to boxHeight

    override fun defaultPosition(): Pair<Float, Float> {
        if (tree != null) syncRenderedSize()
        val w = HudManager.guiScreenWidth.toInt()
        val h = HudManager.guiScreenHeight.toInt()
        return Pair(scaledOriginX(w, h), scaledOriginY(w, h))
    }

    @Composable
    override fun Content() {
        key(recompose) { HudCapture.Replay(this, boxWidth, boxHeight) }
    }

    override fun hasBackground() = composeBackground
    override fun canMergeBackground() = composeBackground
    override fun shouldShow() = hasContent()

    companion object {
        private var currentSchema: Int? = null

        internal var frame = 0L
            private set

        internal var positionRevision = 0
            private set

        internal var clock: () -> Long = System::currentTimeMillis

        private var seenRevision = HudManager.revision

        private var refreshDue = true
        private var refreshedWidth = -1f
        private var refreshedHeight = -1f

        @JvmStatic
        fun markRefreshDue() {
            refreshDue = true
        }

        @JvmStatic
        fun beginFrame(graphics: GuiGraphicsExtractor) {
            HudTransform.resetFrame()
            HudCapture.beginFrame()
            if (!HudManager.isEditing) {
                HudManager.guiScreenWidth = graphics.guiWidth().toFloat()
                HudManager.guiScreenHeight = graphics.guiHeight().toFloat()
            }
            if (refreshDue || HudManager.isEditing || HudManager.guiScreenWidth != refreshedWidth ||
                HudManager.guiScreenHeight != refreshedHeight || HudManager.revision != seenRevision
            ) refreshAll() else frame++
        }

        @JvmStatic
        internal fun refreshAll() {
            frame++
            refreshDue = false
            refreshedWidth = HudManager.guiScreenWidth
            refreshedHeight = HudManager.guiScreenHeight
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

        /** the level overlaps the bar, and OneConfig paints HUDs in list order with no z setting to ask for */
        fun keepLevelAboveBar() {
            val huds = HudManager.activeInstances
            val bar = huds.indexOfFirst { it is ExperienceBarHud }
            val level = huds.indexOfFirst { it is ExperienceLevelHud }
            if (bar < 0 || level < 0 || level > bar) return
            huds.add(huds.removeAt(level))
        }

        fun forgetProfileStateAll() {
            positionRevision++
            for (hud in HudManager.activeInstances) {
                if (hud is VanillaHud) hud.forgetProfileState()
            }
        }

        /**
         * Whether HUDs should be drawing demo content instead of whatever the game has.
         *
         * The editor and the settings page both want it.
         */
        @JvmStatic
        fun previewing(): Boolean = HudManager.isEditorOpen || HudManager.isConfigUiOpen
    }
}

private fun cell(f: Float) = if (f < 0.25f) 0 else if (f < 0.75f) 1 else 2

private val SECTIONS = listOf(
    Section.TopLeft, Section.TopCenter, Section.TopRight,
    Section.CenterLeft, Section.Center, Section.CenterRight,
    Section.BottomLeft, Section.BottomCenter, Section.BottomRight,
)

private val GROWTH_ANCHORS = listOf(
    HudAnchor.TopLeft, HudAnchor.Top, HudAnchor.TopRight,
    HudAnchor.Left, HudAnchor.Center, HudAnchor.Right,
    HudAnchor.BottomLeft, HudAnchor.Bottom, HudAnchor.BottomRight,
)

internal fun sectionFor(fx: Float, fy: Float): Section = SECTIONS[cell(fy) * 3 + cell(fx)]

internal fun growthAnchorFor(fx: Float, fy: Float): HudAnchor = GROWTH_ANCHORS[cell(fy) * 3 + cell(fx)]
