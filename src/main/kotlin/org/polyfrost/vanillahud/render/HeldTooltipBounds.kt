package org.polyfrost.vanillahud.render

//? if >=1.21.8 {
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import org.joml.Matrix3x2f
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.vanillahud.hud.BackdropHud
import org.polyfrost.vanillahud.hud.Huds
import kotlin.math.max
import kotlin.math.min

object HeldTooltipBounds {
    @JvmField
    var recording = false

    private var drawn = false
    private var minX = 0f
    private var minY = 0f
    private var maxX = 0f
    private var maxY = 0f

    @JvmStatic
    fun begin() {
        recording = true
        drawn = false
    }

    @JvmStatic
    fun end() {
        recording = false
    }

    @JvmStatic
    fun text(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int, color: Int) {
        if (width > 0 && color ushr 24 != 0) add(graphics, x.toFloat(), y.toFloat(), (x + width).toFloat(), (y + LINE_HEIGHT).toFloat())
    }

    @JvmStatic
    fun fill(graphics: GuiGraphicsExtractor, x0: Int, y0: Int, x1: Int, y1: Int, color: Int) {
        if (x0 != x1 && y0 != y1 && color ushr 24 != 0) add(graphics, x0.toFloat(), y0.toFloat(), x1.toFloat(), y1.toFloat())
    }

    private fun add(graphics: GuiGraphicsExtractor, x0: Float, y0: Float, x1: Float, y1: Float) {
        val pose = graphics.pose()
        val sx0 = screenX(pose, x0, y0)
        val sx1 = screenX(pose, x1, y1)
        val sy0 = screenY(pose, x0, y0)
        val sy1 = screenY(pose, x1, y1)
        if (!drawn) {
            drawn = true
            minX = min(sx0, sx1); maxX = max(sx0, sx1)
            minY = min(sy0, sy1); maxY = max(sy0, sy1)
            return
        }
        minX = min(minX, min(sx0, sx1)); maxX = max(maxX, max(sx0, sx1))
        minY = min(minY, min(sy0, sy1)); maxY = max(maxY, max(sy0, sy1))
    }

    @JvmStatic
    fun liftClear(graphics: GuiGraphicsExtractor, laidOut: Matrix3x2f, hud: BackdropHud, x: Int, y: Int, width: Int) {
        val tooltip = Huds.heldItemTooltip
        if (!drawn || HudManager.isPreviewing(tooltip)) return
        if (tooltip.isDrawnAsVanilla || !tooltip.shouldDraw() || !tooltip.hasContent()) return
        val pose = graphics.pose()
        if (!dropLift(laidOut, pose)) return
        val margin = if (Minecraft.getInstance().options.getBackgroundColor(0f) != 0) VANILLA_BACKDROP_MARGIN else 0
        var bx0 = (x - margin).toFloat()
        var by0 = (y - margin).toFloat()
        var bx1 = (x + width + margin).toFloat()
        var by1 = (y + LINE_HEIGHT + margin).toFloat()
        TextBackdrop.box(hud, x, y, width, LINE_HEIGHT)?.let { b ->
            val k = hud.innerScale
            bx0 = min(bx0, b[0] / k); by0 = min(by0, b[1] / k)
            bx1 = max(bx1, b[2] / k); by1 = max(by1, b[3] / k)
        }
        val x0 = screenX(pose, bx0, by0)
        val x1 = screenX(pose, bx1, by1)
        val top = screenY(pose, bx0, by0)
        val bottom = screenY(pose, bx1, by1)
        val lift = clearance(min(x0, x1), max(x0, x1), min(top, bottom), max(top, bottom), minX, minY, maxX, maxY)
        if (lift > 0f) pose.translateLocal(0f, -lift)
    }

    fun dropLift(laidOut: Matrix3x2f, pose: Matrix3x2f): Boolean {
        if (laidOut.determinant() == 0f) return false
        val added = laidOut.invert(scratch).mul(pose)
        added.m21 = 0f
        pose.set(laidOut).mul(added)
        return true
    }

    fun clearance(x0: Float, x1: Float, top: Float, bottom: Float, tipX0: Float, tipTop: Float, tipX1: Float, tipBottom: Float): Float =
        if (x1 <= tipX0 || x0 >= tipX1 || bottom <= tipTop || top >= tipBottom) 0f else bottom - tipTop

    private fun screenX(m: Matrix3x2f, x: Float, y: Float) = m.m00 * x + m.m10 * y + m.m20
    private fun screenY(m: Matrix3x2f, x: Float, y: Float) = m.m01 * x + m.m11 * y + m.m21

    private val scratch = Matrix3x2f()

    private const val LINE_HEIGHT = 9
    private const val VANILLA_BACKDROP_MARGIN = 2
}
//?}
