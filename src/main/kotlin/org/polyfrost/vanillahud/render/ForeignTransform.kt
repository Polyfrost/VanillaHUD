package org.polyfrost.vanillahud.render

//? if > 1.8.9 {
import net.minecraft.client.gui.GuiGraphicsExtractor
//?} else {
/*import org.polyfrost.vanillahud.compat.GuiGraphicsExtractor
*///?}
//? if >=1.21.8 {
import org.joml.Matrix3x2f
import org.polyfrost.oneconfig.utils.v1.dsl.mc
import org.polyfrost.vanillahud.mixin.access.IWindow
//?}
import org.polyfrost.vanillahud.hud.VanillaHud
import kotlin.math.abs
//? if >=1.21.8 {
import kotlin.math.hypot
import kotlin.math.atan2
//?}

private fun exceedsTolerance(delta: Float) = abs(delta) > ForeignTransform.TOLERANCE

/**
 * What another mod does to a HUD element: the pose it puts on, the screen size it makes the game
 * report, and logical coordinates it changes without touching the pose.
 *
 * A pose maps a point to `scale * point + (tx, ty)`. [screenW] and [screenH] are the size the
 * element laid itself out against, zero for the real one.
 */
data class ForeignPlacement(
    val scale: Float,
    val tx: Float,
    val ty: Float,
    val screenW: Int,
    val screenH: Int,
    val logicalX: Float,
    val logicalY: Float,
    /** false for a rotation or a non-uniform scale, which is reported and left alone */
    val applied: Boolean = true,
) {
    val scaled: Boolean get() = exceedsTolerance(scale - 1f)

    val moved: Boolean
        get() = !applied || scaled || exceedsTolerance(tx) || exceedsTolerance(ty) ||
            exceedsTolerance(logicalX) || exceedsTolerance(logicalY)

    /** this one outside, [inner] applied inside it, in the units [inner] was measured in */
    fun then(inner: ForeignPlacement) = ForeignPlacement(
        scale * inner.scale, tx + scale * inner.tx, ty + scale * inner.ty,
        inner.screenW, inner.screenH, 0f, 0f, applied && inner.applied,
    )

    fun withLogical(x: Float, y: Float) = ForeignPlacement(scale, tx, ty, screenW, screenH, x, y, applied)

    /** whether anything that changes where the HUD lands moved by more than [ForeignTransform.TOLERANCE] */
    fun differs(o: ForeignPlacement?): Boolean =
        o == null || applied != o.applied || screenW != o.screenW || screenH != o.screenH ||
            exceedsTolerance(scale - o.scale) || exceedsTolerance(tx - o.tx) || exceedsTolerance(ty - o.ty) ||
            exceedsTolerance(logicalX - o.logicalX) || exceedsTolerance(logicalY - o.logicalY)
}

/** Cancels what other mods do to a HUD element, so the HUD's box can show it instead. */
object ForeignTransform {
    const val TOLERANCE = 1e-3f

    @JvmField
    var realWindow = false

    var revision = 0
        private set

    internal fun bump() {
        revision++
        VanillaHud.markRefreshDue()
    }

    fun origin(
        realOrigin: Float,
        laidOutOrigin: Float,
        shift: Float,
        logical: Float,
        extent: Float,
        anchor: Float,
        contentOffset: Float,
        userScale: Float,
        ownScale: Float,
        foreignScale: Float,
        ignorePosition: Boolean,
        ignoreScale: Boolean,
    ): Float {
        val appliedForeignScale = if (ignoreScale) 1f else foreignScale
        return if (ignorePosition) {
            realOrigin + (1f - userScale * ownScale * appliedForeignScale) * extent * anchor - contentOffset * userScale
        } else {
            shift + appliedForeignScale * (laidOutOrigin + logical + (1f - userScale * ownScale) * extent * anchor) - contentOffset * userScale
        }
    }

    fun anchorOffset(origin: Float, extent: Float, fraction: Float, targetOrigin: Float, targetExtent: Float, targetFraction: Float): Float =
        origin + extent * fraction - (targetOrigin + targetExtent * targetFraction)

    //? if >=1.21.8 {
    private const val POSE_SIZE = 6
    private const val POSE_EPSILON = 1e-5f

    private class Frame {
        var hud: VanillaHud? = null
        var probed = false
        var hasPlacedPose = false
        var cancelsOuterPose = false
        var wasRealWindow = false
        var outerScale = 1f
        val startPose = FloatArray(POSE_SIZE)
        val placedPose = FloatArray(POSE_SIZE)
    }

    private val frames = ArrayList<Frame>()
    private var depth = 0
    private val rootPose = FloatArray(POSE_SIZE)
    private var hasRootPose = false
    private val relativePose = Matrix3x2f()

    private var measuredScale = 1f
    private var measuredTx = 0f
    private var measuredTy = 0f
    private var measuredApplied = true

    private fun push(hud: VanillaHud?): Frame {
        if (depth == frames.size) frames.add(Frame())
        val frame = frames[depth++]
        frame.hud = hud
        frame.probed = false
        frame.hasPlacedPose = false
        frame.cancelsOuterPose = false
        frame.wasRealWindow = realWindow
        frame.outerScale = 1f
        return frame
    }

    private fun savePose(pose: Matrix3x2f, into: FloatArray) {
        into[0] = pose.m00
        into[1] = pose.m01
        into[2] = pose.m10
        into[3] = pose.m11
        into[4] = pose.m20
        into[5] = pose.m21
    }

    private fun isSamePose(pose: Matrix3x2f, saved: FloatArray) =
        abs(pose.m00 - saved[0]) < POSE_EPSILON && abs(pose.m01 - saved[1]) < POSE_EPSILON &&
            abs(pose.m10 - saved[2]) < POSE_EPSILON && abs(pose.m11 - saved[3]) < POSE_EPSILON &&
            abs(pose.m20 - saved[4]) < POSE_EPSILON && abs(pose.m21 - saved[5]) < POSE_EPSILON

    private fun restorePose(pose: Matrix3x2f, saved: FloatArray) {
        pose.set(saved[0], saved[1], saved[2], saved[3], saved[4], saved[5])
    }

    private fun measureChange(pose: Matrix3x2f, from: FloatArray): Boolean {
        if (isSamePose(pose, from)) return false
        relativePose.set(from[0], from[1], from[2], from[3], from[4], from[5]).invert().mul(pose)
        val scaleX = hypot(relativePose.m00, relativePose.m01)
        val scaleY = hypot(relativePose.m10, relativePose.m11)
        if (!scaleX.isFinite() || !scaleY.isFinite() || !relativePose.m20.isFinite() || !relativePose.m21.isFinite()) return false
        val isUniform = abs(scaleX - scaleY) <= TOLERANCE * scaleX
        val isUnrotated = abs(atan2(relativePose.m01, relativePose.m00)) <= TOLERANCE
        measuredScale = scaleX
        measuredTx = relativePose.m20
        measuredTy = relativePose.m21
        measuredApplied = scaleX > TOLERANCE && isUniform && isUnrotated
        return true
    }

    private fun ForeignPlacement?.differsFromMeasured(screenW: Int, screenH: Int) =
        this == null || this.screenW != screenW || this.screenH != screenH || applied != measuredApplied ||
            exceedsTolerance(scale - measuredScale) || exceedsTolerance(tx - measuredTx) || exceedsTolerance(ty - measuredTy)

    private fun measuredPlacement(screenW: Int, screenH: Int) =
        ForeignPlacement(measuredScale, measuredTx, measuredTy, screenW, screenH, 0f, 0f, measuredApplied)

    private fun loadStartPose(frame: Frame): Boolean {
        for (i in depth - 2 downTo 0) {
            val enclosing = frames[i]
            if (enclosing.hud == null || !enclosing.hasPlacedPose) continue
            System.arraycopy(enclosing.placedPose, 0, frame.startPose, 0, POSE_SIZE)
            return true
        }
        if (!hasRootPose) return false
        System.arraycopy(rootPose, 0, frame.startPose, 0, POSE_SIZE)
        return true
    }

    private fun screenSizeSeenByInnerMod(size: Int, totalScale: Float) =
        if (realWindow) (size / totalScale).toInt() else size
    //?}

    @JvmStatic
    fun realGuiWidth(graphics: GuiGraphicsExtractor): Int {
        //? if >=1.21.8 {
        return (mc.window as? IWindow)?.realWidth() ?: graphics.guiWidth()
        //?} else
        //return graphics.guiWidth()
    }

    @JvmStatic
    fun realGuiHeight(graphics: GuiGraphicsExtractor): Int {
        //? if >=1.21.8 {
        return (mc.window as? IWindow)?.realHeight() ?: graphics.guiHeight()
        //?} else
        //return graphics.guiHeight()
    }

    @JvmStatic
    fun beginFrame(graphics: GuiGraphicsExtractor) {
        //? if >=1.21.8 {
        depth = 0
        realWindow = false
        val pose: Matrix3x2f? = graphics.pose()
        hasRootPose = pose != null
        if (pose != null) savePose(pose, rootPose)
        //?}
    }

    fun enterSkipped() {
        //? if >=1.21.8
        push(null)
    }

    fun enter(graphics: GuiGraphicsExtractor, hud: VanillaHud) {
        //? if >=1.21.8 {
        val frame = push(hud)
        if (!loadStartPose(frame)) return
        if (!measureChange(graphics.pose(), frame.startPose)) {
            if (hud.foreignOuter != null) {
                hud.foreignOuter = null
                hud.foreignUpdated()
            }
            return
        }
        val screenW = graphics.guiWidth()
        val screenH = graphics.guiHeight()
        if (hud.foreignOuter.differsFromMeasured(screenW, screenH)) {
            hud.foreignOuter = measuredPlacement(screenW, screenH)
            hud.foreignUpdated()
        }
        if (measuredApplied) {
            frame.cancelsOuterPose = true
            frame.outerScale = measuredScale
            realWindow = true
        }
        //?}
    }

    fun rebase(graphics: GuiGraphicsExtractor) {
        //? if >=1.21.8 {
        if (depth == 0) return
        val frame = frames[depth - 1]
        if (frame.cancelsOuterPose) restorePose(graphics.pose(), frame.startPose)
        //?}
    }

    fun recordPlacedPose(graphics: GuiGraphicsExtractor) {
        //? if >=1.21.8 {
        if (depth == 0) return
        val frame = frames[depth - 1]
        savePose(graphics.pose(), frame.placedPose)
        frame.hasPlacedPose = true
        //?}
    }

    @JvmStatic
    fun probe(graphics: GuiGraphicsExtractor) {
        //? if >=1.21.8 {
        if (depth == 0) return
        val frame = frames[depth - 1]
        val hud = frame.hud
        if (hud == null || frame.probed || !frame.hasPlacedPose) return
        frame.probed = true
        val pose = graphics.pose()
        if (!measureChange(pose, frame.placedPose)) {
            if (hud.foreignInner != null) {
                hud.foreignInner = null
                hud.foreignUpdated()
            }
            return
        }
        val totalScale = frame.outerScale * measuredScale
        val screenW = screenSizeSeenByInnerMod(graphics.guiWidth(), totalScale)
        val screenH = screenSizeSeenByInnerMod(graphics.guiHeight(), totalScale)
        if (hud.foreignInner.differsFromMeasured(screenW, screenH)) {
            hud.foreignInner = measuredPlacement(screenW, screenH)
            hud.foreignUpdated()
        }
        if (measuredApplied) {
            restorePose(pose, frame.placedPose)
            realWindow = true
        }
        //?}
    }

    @JvmStatic
    fun observeY(graphics: GuiGraphicsExtractor, hud: VanillaHud, actualY: Int, expectedY: Float) {
        //? if >=1.21.8 {
        if (expectedY.isNaN()) return
        val live = HudTransform.live(hud)
        val shift = actualY - expectedY
        val shifted = exceedsTolerance(shift)
        if (!shifted && live.foreignLogicalY == 0f) return
        if (exceedsTolerance(shift - live.foreignLogicalY)) {
            live.foreignLogicalY = if (shifted) shift else 0f
            live.foreignUpdated()
        }
        if (shifted) graphics.pose().translate(0f, -shift)
        //?}
    }

    fun leave() {
        //? if >=1.21.8 {
        if (depth == 0) return
        val frame = frames[--depth]
        realWindow = frame.wasRealWindow
        frame.hud = null
        //?}
    }

    fun reset() {
        //? if >=1.21.8 {
        depth = 0
        realWindow = false
        hasRootPose = false
        //?}
    }
}
