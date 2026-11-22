@file:OptIn(ExperimentalOneConfigUiApi::class)

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.polyfrost.compose.layout.PolyAlign
import org.polyfrost.compose.render.PolyColor
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.config.v1.Tree
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch
import org.polyfrost.oneconfig.api.hud.v1.ExternalPlacement
import org.polyfrost.oneconfig.api.hud.v1.Font
import org.polyfrost.oneconfig.api.hud.v1.HudAnchor
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.oneconfig.api.hud.v1.Hud
import org.polyfrost.oneconfig.api.hud.v1.Section
import org.polyfrost.oneconfig.api.ui.v1.ExperimentalOneConfigUiApi
import org.polyfrost.oneconfig.utils.v1.dsl.mc
import org.polyfrost.vanillahud.api.VanillaHudApi
import org.polyfrost.vanillahud.compat.ForeignAttribution
import org.polyfrost.vanillahud.mixin.access.IGui
import org.polyfrost.vanillahud.render.ForeignPlacement
import org.polyfrost.vanillahud.render.ForeignTransform
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

    @Switch(
        title = "Ignore Position From Other Mods",
        subcategory = "Other Mods",
        description = "Keep this HUD where the game puts it when another mod moves it."
    )
    var ignoreModPosition = false

    @Switch(
        title = "Ignore Scale From Other Mods",
        subcategory = "Other Mods",
        description = "Draw this HUD at its own size when another mod scales it."
    )
    var ignoreModScale = false

    private var detectedPlacement by mutableStateOf<ExternalPlacement?>(null)

    override val externalPlacement: ExternalPlacement? get() = detectedPlacement

    internal var foreignOuter: ForeignPlacement? = null
    internal var foreignInner: ForeignPlacement? = null
    internal var foreignLogicalX = 0f
    internal var foreignLogicalY = 0f

    /** what other mods do to this HUD, null while nothing does */
    var foreign: ForeignPlacement? = null
        private set

    private var foreignMod: String? = null
    private var foreignAttributedAt = Long.MIN_VALUE
    private var seenForeignRevision = ForeignTransform.revision
    private var ignorePositionProp: Property<*>? = null
    private var ignoreScaleProp: Property<*>? = null

    private val activeForeign: ForeignPlacement? get() = foreign?.takeIf { it.applied }

    private val appliedForeignScale: Float get() = if (ignoreModScale) 1f else activeForeign?.scale ?: 1f

    private fun wrappedForeign(): ForeignPlacement? {
        val outer = foreignOuter
        val inner = foreignInner
        return if (outer != null && inner != null) outer.then(inner) else outer ?: inner
    }

    internal fun foreignUpdated() {
        val wrapped = wrappedForeign()
        val hasLogicalShift = foreignLogicalX != 0f || foreignLogicalY != 0f
        val next = when {
            wrapped != null -> wrapped.withLogical(foreignLogicalX, foreignLogicalY)
            hasLogicalShift -> ForeignPlacement(1f, 0f, 0f, 0, 0, foreignLogicalX, foreignLogicalY)
            else -> null
        }
        val before = foreign
        if (next == null && before == null) return
        if (next != null && !next.differs(before)) return
        foreign = next
        ForeignTransform.bump()
        if (before == null) attributeForeign()
        refreshDetection()
    }

    private fun attributeForeign() {
        val now = clock()
        if (foreignMod != null && now - foreignAttributedAt < ATTRIBUTION_REUSE_MILLIS) return
        foreignAttributedAt = now
        foreignMod = ForeignAttribution.attribute()
    }

    private var displayKey = -1
    private var displayedPositionProp: Property<*>? = null
    private var displayedScaleProp: Property<*>? = null

    internal fun refreshDetection() {
        val f = foreign
        val moved = f != null && f.moved && !ignoreModPosition && try { pinned } catch (_: Throwable) { false }
        val scaled = f != null && f.scaled && !ignoreModScale
        val current = detectedPlacement
        if (moved || scaled) {
            if (current == null || current.mod != foreignMod || current.moved != moved || current.scaled != scaled) {
                detectedPlacement = ExternalPlacement(foreignMod, moved, scaled)
            }
        } else if (current != null) {
            detectedPlacement = null
        }
        val key = (if (ignoreModPosition) 1 else 0) or (if (f?.moved == true) 2 else 0) or
            (if (ignoreModScale) 4 else 0) or (if (f?.scaled == true) 8 else 0)
        val position = ignorePositionProp
        val scale = ignoreScaleProp
        if (key == displayKey && position === displayedPositionProp && scale === displayedScaleProp) return
        displayKey = key
        displayedPositionProp = position
        displayedScaleProp = scale
        position?.revaluateDisplay()
        scale?.revaluateDisplay()
    }

    private fun foreignOptionChanged() {
        ForeignTransform.bump()
        queueForceDefault()
        refreshDetection()
    }

    override fun addCallbacks(tree: Tree) {
        super.addCallbacks(tree)
        tree.addDependencies()
        tree.addForeignControls()
    }

    // here and not in addDependencies, which some HUDs override without calling up
    private fun Tree.addForeignControls() {
        val position = getProp("ignoreModPosition")
        val scale = getProp("ignoreModScale")
        position.addDisplayCondition {
            if (ignoreModPosition || foreign?.moved == true) Property.Display.SHOWN else Property.Display.HIDDEN
        }
        scale.addDisplayCondition {
            if (ignoreModScale || foreign?.scaled == true) Property.Display.SHOWN else Property.Display.HIDDEN
        }
        position.addCallback { foreignOptionChanged(); false }
        scale.addCallback { foreignOptionChanged(); false }
        ignorePositionProp = position
        ignoreScaleProp = scale
    }

    protected open fun Tree.addDependencies() {}

    abstract val naturalWidth: Float
    abstract val naturalHeight: Float

    /**
     * Half of [size] rounded down, the way vanilla centres with integer division. On an odd GUI
     * size halving as a float puts the box half a pixel off what the game draws.
     */
    protected fun half(size: Int): Float = (size / 2).toFloat()

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

    private fun placeAt(absX: Float, absY: Float) = withSystemPosition {
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
        if (tree != null && !HudManager.isSystemPosition && previewing()) {
            movedByUser = true
            refreshDetection()
        }
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
            if (wantedAnchor != null) withSystemPosition {
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

    private val followsForeignLayout: Boolean get() = !ignoreModScale && !ignoreModPosition

    private fun laidOutWidth(f: ForeignPlacement, screenWidth: Int) =
        if (followsForeignLayout && f.screenW > 0) f.screenW else screenWidth

    private fun laidOutHeight(f: ForeignPlacement, screenHeight: Int) =
        if (followsForeignLayout && f.screenH > 0) f.screenH else screenHeight

    private fun foreignOriginX(f: ForeignPlacement, w: Int, h: Int, scale: Float, own: Float, offset: Float) = ForeignTransform.origin(
        defaultOriginX(w, h), defaultOriginX(laidOutWidth(f, w), laidOutHeight(f, h)), f.tx, f.logicalX,
        width, anchorX, offset, scale, own, f.scale, ignoreModPosition, ignoreModScale,
    )

    private fun foreignOriginY(f: ForeignPlacement, w: Int, h: Int, scale: Float, own: Float, offset: Float) = ForeignTransform.origin(
        defaultOriginY(w, h), defaultOriginY(laidOutWidth(f, w), laidOutHeight(f, h)), f.ty, f.logicalY,
        height, anchorY, offset, scale, own, f.scale, ignoreModPosition, ignoreModScale,
    )

    fun scaledOriginX(screenWidth: Int, screenHeight: Int, scale: Float = effectiveScale): Float {
        val f = activeForeign
            ?: return defaultOriginX(screenWidth, screenHeight) + (1f - scale * elementScale) * width * anchorX - contentOffsetX * scale
        return foreignOriginX(f, screenWidth, screenHeight, scale, ownScale, contentOffsetX)
    }

    fun scaledOriginY(screenWidth: Int, screenHeight: Int, scale: Float = effectiveScale): Float {
        val f = activeForeign
            ?: return defaultOriginY(screenWidth, screenHeight) + (1f - scale * elementScale) * height * anchorY - contentOffsetY * scale
        return foreignOriginY(f, screenWidth, screenHeight, scale, ownScale, contentOffsetY)
    }

    private fun placedOriginX(screenWidth: Int, screenHeight: Int): Float =
        activeForeign?.let { foreignOriginX(it, screenWidth, screenHeight, 1f, 1f, 0f) } ?: defaultOriginX(screenWidth, screenHeight)

    private fun placedOriginY(screenWidth: Int, screenHeight: Int): Float =
        activeForeign?.let { foreignOriginY(it, screenWidth, screenHeight, 1f, 1f, 0f) } ?: defaultOriginY(screenWidth, screenHeight)

    override fun multipleInstancesAllowed() = false
    override fun deletable() = false
    override fun showByDefault() = true

    private data class Seed(val w: Int, val h: Int, val hudW: Float, val hudH: Float, val turns: Int, val anchor: HudAnchor?, val foreignRevision: Int)

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

    /** the point of this HUD that sits on [defaultAnchorTargetPoint] of [defaultAnchorTarget] */
    protected open val defaultAnchorPoint: HudAnchor get() = HudAnchor.TopLeft

    /** the point of [defaultAnchorTarget] this HUD hangs from */
    protected open val defaultAnchorTargetPoint: HudAnchor get() = HudAnchor.TopLeft

    private fun HudAnchor.fractionX() = if (name.endsWith("Left")) 0f else if (name.endsWith("Right")) 1f else 0.5f

    private fun HudAnchor.fractionY() = if (name.startsWith("Top")) 0f else if (name.startsWith("Bottom")) 1f else 0.5f

    private fun defaultAnchorOffsetX(target: VanillaHud, screenWidth: Int, screenHeight: Int) = ForeignTransform.anchorOffset(
        placedOriginX(screenWidth, screenHeight), width * appliedForeignScale, defaultAnchorPoint.fractionX(),
        target.placedOriginX(screenWidth, screenHeight), target.width * target.appliedForeignScale, defaultAnchorTargetPoint.fractionX(),
    )

    private fun defaultAnchorOffsetY(target: VanillaHud, screenWidth: Int, screenHeight: Int) = ForeignTransform.anchorOffset(
        placedOriginY(screenWidth, screenHeight), height * appliedForeignScale, defaultAnchorPoint.fractionY(),
        target.placedOriginY(screenWidth, screenHeight), target.height * target.appliedForeignScale, defaultAnchorTargetPoint.fractionY(),
    )

    private fun holdDefaultAnchor(target: VanillaHud, screenWidth: Int, screenHeight: Int) {
        withSystemPosition {
            positionRevision++
            anchorTo(target, defaultAnchorTargetPoint, defaultAnchorPoint)
            anchorOffsetX = defaultAnchorOffsetX(target, screenWidth, screenHeight)
            anchorOffsetY = defaultAnchorOffsetY(target, screenWidth, screenHeight)
        }
    }

    /**
     * OneConfig's reset only knows about the screen position, so it either clears the anchor or
     * leaves a dragged offset in place. A stored position back on its default is how a reset shows up.
     */
    open fun restoreDefaultAnchor() {
        val target = defaultAnchorTarget?.takeIf { it.tree != null } ?: return
        if (tree == null || !positionSchemaReady || !matchesDefaultPosition(fallback = false)) return
        if (isAnchored && anchorParent !== target) return
        val w = HudManager.guiScreenWidth.toInt()
        val h = HudManager.guiScreenHeight.toInt()
        if (w <= 0 || h <= 0) return
        val offsetX = defaultAnchorOffsetX(target, w, h)
        val offsetY = defaultAnchorOffsetY(target, w, h)
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
        placing++
        try {
            syncRenderedSize()
            captureDefaults()
            val target = defaultAnchorTarget?.takeIf { it.tree != null }
            if (target != null) {
                holdDefaultAnchor(target, w, h)
            } else {
                if (isAnchored) withSystemPosition { clearAnchor() }
                val (dx, dy) = defaultPosition()
                placeAt(dx, dy)
            }
            forcePending = false
        } catch (_: Throwable) {
            measureOwed = true
        } finally {
            placing--
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
        placing++
        try {
            syncRenderedSize()
            placeAt(scaledOriginX(screenWidth, screenHeight, scale), scaledOriginY(screenWidth, screenHeight, scale))
        } catch (_: Throwable) {
            measureOwed = true
        } finally {
            placing--
        }
    }

    fun reseedDefaultForScreen(measure: Boolean = true) {
        if (tree == null || !positionSchemaReady) return
        val w = HudManager.guiScreenWidth.toInt()
        val h = HudManager.guiScreenHeight.toInt()
        if (w <= 0 || h <= 0) return
        val seed = seed
        val screenSame = seed != null && w == seed.w && h == seed.h && quarterTurns == seed.turns &&
            growthAnchor == seed.anchor && seed.foreignRevision == ForeignTransform.revision
        if (screenSame && !measure) return
        placing++
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
        } finally {
            placing--
        }
    }

    private fun commitSeed(w: Int, h: Int, hudW: Float, hudH: Float) {
        seed = Seed(w, h, hudW, hudH, quarterTurns, growthAnchor, ForeignTransform.revision)
    }

    val previewing: Boolean get() = previewing()

    val isDrawnAsVanilla: Boolean get() = !HudManager.isEditing && VanillaHudApi.isDrawnAsVanilla(hudId)

    fun shouldDraw(): Boolean {
        val editing = HudManager.isEditing
        if (!editing && VanillaHudApi.isDrawnAsVanilla(hudId)) return true
        if (!editing && hidden) return false
        if (HudManager.isDebugScreenVisible && !showInF3) return false
        if (HudManager.isTabListVisible && !showInTab) return false
        if (!HudManager.overrideShowInScreens && !editing) {
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
    val unrotatedWidth: Float
        get() {
            if (!sizeCached(widthFrame)) {
                cachedWidth = measuredWidth()
                widthFrame = frame
            }
            return cachedWidth
        }

    val unrotatedHeight: Float
        get() {
            if (!sizeCached(heightFrame)) {
                cachedHeight = measuredHeight()
                heightFrame = frame
            }
            return cachedHeight
        }

    private var placing = 0

    private var sizeKey = 0

    private fun sizeCached(stored: Long): Boolean {
        if (placing > 0 || HudManager.isEditing) return false
        val key = measureKey()
        if (key != sizeKey) {
            sizeKey = key
            widthFrame = -1L
            heightFrame = -1L
            return false
        }
        return stored == frame
    }

    private var widthFrame = -1L
    private var cachedWidth = 0f
    private var heightFrame = -1L
    private var cachedHeight = 0f

    private val mirror = HudStyle()
    private var layoutFrame = -1L
    private var textFrame = -1L
    private var backgroundFrame = -1L

    private fun styleStale(stamp: Long): Boolean = stamp != frame || placing > 0 || HudManager.isEditing

    val layoutStyle: HudStyle
        get() {
            val s = mirror
            if (styleStale(layoutFrame)) {
                s.textScale = textScale
                s.padLeft = padLeft
                s.padRight = padRight
                s.padTop = padTop
                s.padBottom = padBottom
                s.staticWidth = staticWidth
                s.staticW = staticW
                s.staticH = staticH
                s.alignment = alignment
                s.bgRadius = bgRadius
                layoutFrame = frame
            }
            return s
        }

    val textStyle: HudStyle
        get() {
            val s = mirror
            if (styleStale(textFrame)) {
                s.caseType = caseType
                s.textBold = textBold
                s.textItalic = textItalic
                s.textUnderline = textUnderline
                s.font = font
                s.textColor = textColor
                s.textChroma = textChroma
                s.textChromaSpeed = textChromaSpeed
                s.showShadow = showShadow
                s.fullShadow = fullShadow
                s.shadowColor = shadowColor
                s.shadowChroma = shadowChroma
                s.shadowChromaSpeed = shadowChromaSpeed
                s.shadowOffsetX = shadowOffsetX
                s.shadowOffsetY = shadowOffsetY
                textFrame = frame
            }
            return s
        }

    val backgroundStyle: HudStyle
        get() {
            val s = mirror
            if (styleStale(backgroundFrame)) {
                s.showBackground = showBackground
                s.bgColor = bgColor
                s.bgChroma = bgChroma
                s.bgChromaSpeed = bgChromaSpeed
                s.borderEnabled = borderEnabled
                s.borderColor = borderColor
                s.borderChroma = borderChroma
                s.borderChromaSpeed = borderChromaSpeed
                backgroundFrame = frame
            }
            return s
        }

    open val width: Float get() = if (turned) unrotatedHeight else unrotatedWidth
    open val height: Float get() = if (turned) unrotatedWidth else unrotatedHeight

    /** whether Compose paints this HUD's background, the others draw theirs in the game's own pass */
    protected open val composeBackground: Boolean get() = false

    /** the Designer's size and resize handles change [textScale], and here that sizes the whole element with its background */
    open val ownScale: Float get() = layoutStyle.textScale

    val elementScale: Float get() = ownScale * appliedForeignScale

    /** the size padding is drawn at, which grows with the element */
    open val padScale: Float get() = elementScale

    /** text whose colour the game's pass already resolved from [textColor] */
    open val managesTextColor: Boolean get() = false

    /** the background the game draws for this element stretches to the box, which only matters once the box differs from the content */
    open val stretchesBackground: Boolean get() = false

    open val everyFillIsBackground: Boolean get() = false

    val customBackgroundBox: Boolean
        get() {
            val s = layoutStyle
            return s.bgRadius > 0f || pads(s) || s.staticWidth
        }

    /** the scale the game applies itself inside this element's pose, as a title does */
    open val innerScale: Float get() = 1f

    val bgArgb: Int get() = HudCapture.backgroundTint(this, bgRawArgb)

    private val bgRawArgb: Int
        get() {
            val s = backgroundStyle
            val argb = s.bgColor
            return if (s.bgChroma) PolyColor(argb, true, s.bgChromaSpeed).argb else argb
        }

    private fun pads(s: HudStyle): Boolean = s.padLeft != 0f || s.padRight != 0f || s.padTop != 0f || s.padBottom != 0f

    private val drawnWidth: Float get() = width * elementScale

    private val drawnHeight: Float get() = height * elementScale

    val boxWidth: Float
        get() {
            val s = layoutStyle
            return if (s.staticWidth) maxOf(s.staticW, drawnWidth) else drawnWidth + (s.padLeft + s.padRight) * padScale
        }

    val boxHeight: Float
        get() {
            val s = layoutStyle
            return if (s.staticWidth) maxOf(s.staticH, drawnHeight) else drawnHeight + (s.padTop + s.padBottom) * padScale
        }

    /** where the content sits inside the box, zero unless the Designer's padding or static size is in use */
    val contentOffsetX: Float
        get() {
            val s = layoutStyle
            if (!s.staticWidth) return s.padLeft * padScale
            val drawn = drawnWidth
            return (maxOf(s.staticW, drawn) - drawn) * alignmentFractionX(s.alignment)
        }

    val contentOffsetY: Float
        get() {
            val s = layoutStyle
            if (!s.staticWidth) return s.padTop * padScale
            val drawn = drawnHeight
            return (maxOf(s.staticH, drawn) - drawn) * alignmentFractionY(s.alignment)
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
    protected inline fun <T : Any> measureOnce(key: () -> Long, crossinline measure: () -> T?): T? {
        val base = measureKey()
        if (!measuredAt(base)) {
            val stamp = key()
            val epoch = measureEpoch()
            if (measureStale(base, stamp, epoch)) storeMeasured(HudCapture.measuring(this) { measure() }, stamp, epoch)
            markMeasured(base)
        }
        return measuredValue() as T?
    }

    @PublishedApi
    internal fun measuredAt(base: Int): Boolean = measuredFrame == frame && measuredBase == base

    @PublishedApi
    internal fun measureEpoch(): Long = clock() shr 10

    @PublishedApi
    internal fun measureStale(base: Int, stamp: Long, epoch: Long): Boolean =
        !measuredValid || HudManager.isEditing || measuredBase != base || measuredStamp != stamp || measuredEpoch != epoch

    @PublishedApi
    internal fun storeMeasured(value: Any?, stamp: Long, epoch: Long) {
        measured = value
        measuredValid = true
        measuredStamp = stamp
        measuredEpoch = epoch
    }

    @PublishedApi
    internal fun markMeasured(base: Int) {
        measuredBase = base
        measuredFrame = frame
    }

    @PublishedApi
    internal fun measuredValue(): Any? = measured

    protected fun mix(h: Long, o: Any?): Long = h * 31 + System.identityHashCode(o)

    protected fun mix(h: Long, v: Int): Long = h * 31 + v

    protected fun mix(h: Long, v: Boolean): Long = h * 31 + if (v) 1 else 0

    @PublishedApi
    internal fun measureKey(): Int = HudManager.guiScreenWidth.toInt() * 31 + if (previewing) 1 else 0

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
        private const val ATTRIBUTION_REUSE_MILLIS = 5000L

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
            ForeignTransform.beginFrame(graphics)
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
            val huds = HudManager.activeInstances
            for (i in 0 until huds.size) {
                val hud = huds[i]
                if (hud !is VanillaHud || !hud.shouldDraw()) continue
                try {
                    if (hud.seenForeignRevision != ForeignTransform.revision) {
                        hud.seenForeignRevision = ForeignTransform.revision
                        hud.queueForceDefault()
                    }
                    hud.refreshDetection()
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
        fun previewing(): Boolean = (HudManager.isEditorOpen || HudManager.isConfigUiOpen) && !HudManager.isSamplingVanilla
    }
}

class HudStyle internal constructor() {
    @JvmField var textScale = 1f
    @JvmField var padLeft = 0f
    @JvmField var padRight = 0f
    @JvmField var padTop = 0f
    @JvmField var padBottom = 0f
    @JvmField var staticWidth = false
    @JvmField var staticW = 0f
    @JvmField var staticH = 0f
    @JvmField var alignment = PolyAlign.Center
    @JvmField var bgRadius = 0f

    @JvmField var caseType = 0
    @JvmField var textBold = false
    @JvmField var textItalic = false
    @JvmField var textUnderline = false
    @JvmField var font = Font.Minecraft
    @JvmField var textColor = 0
    @JvmField var textChroma = false
    @JvmField var textChromaSpeed = 1f
    @JvmField var showShadow = false
    @JvmField var fullShadow = false
    @JvmField var shadowColor = 0
    @JvmField var shadowChroma = false
    @JvmField var shadowChromaSpeed = 1f
    @JvmField var shadowOffsetX = 0f
    @JvmField var shadowOffsetY = 0f

    @JvmField var showBackground = false
    @JvmField var bgColor = 0
    @JvmField var bgChroma = false
    @JvmField var bgChromaSpeed = 1f
    @JvmField var borderEnabled = false
    @JvmField var borderColor = 0
    @JvmField var borderChroma = false
    @JvmField var borderChromaSpeed = 1f
}

private fun cell(f: Float) = if (f < 0.25f) 0 else if (f < 0.75f) 1 else 2

private fun alignmentFractionX(align: PolyAlign): Float = when (align) {
    PolyAlign.TopLeft, PolyAlign.Left, PolyAlign.BottomLeft -> 0f
    PolyAlign.TopRight, PolyAlign.Right, PolyAlign.BottomRight -> 1f
    else -> 0.5f
}

private fun alignmentFractionY(align: PolyAlign): Float = when (align) {
    PolyAlign.TopLeft, PolyAlign.Top, PolyAlign.TopRight -> 0f
    PolyAlign.BottomLeft, PolyAlign.Bottom, PolyAlign.BottomRight -> 1f
    else -> 0.5f
}

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
