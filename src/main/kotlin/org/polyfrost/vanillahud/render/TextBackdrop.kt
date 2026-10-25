package org.polyfrost.vanillahud.render

//? if > 1.8.9 {
import net.minecraft.client.gui.GuiGraphicsExtractor
//?} else {
/*import net.minecraft.client.gui.GuiElement
import org.polyfrost.vanillahud.compat.GuiGraphicsExtractor
*///?}
import org.polyfrost.vanillahud.hud.BackdropHud
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

object TextBackdrop {
    @JvmStatic
    fun draw(graphics: GuiGraphicsExtractor, hud: BackdropHud, x: Int, y: Int, width: Int, height: Int, textColor: Int) {
        if (!hud.background || width <= 0) return
        val textAlpha = if (textColor ushr 24 == 0) 255 else textColor ushr 24
        val x0 = x - hud.paddingX.roundToInt()
        val y0 = y - hud.paddingY.roundToInt()
        val x1 = x + width + hud.paddingX.roundToInt()
        val y1 = y + height + hud.paddingY.roundToInt()
        val radius = if (hud.rounded) min(hud.cornerRadius.roundToInt(), min(x1 - x0, y1 - y0) / 2) else 0
        val border = if (hud.border) min(hud.borderSize.roundToInt(), min(x1 - x0, y1 - y0) / 2) else 0
        val fill = withAlpha(hud.textBgColor.argb, textAlpha)
        if (border <= 0) {
            rounded(graphics, x0, y0, x1, y1, radius, fill)
            return
        }
        val inner = max(radius - border, 0)
        rounded(graphics, x0 + border, y0 + border, x1 - border, y1 - border, inner, fill)
        ring(graphics, x0, y0, x1, y1, radius, border, inner, withAlpha(hud.borderColor.argb, textAlpha))
    }

    fun interface RectFill {
        fun fill(x0: Int, y0: Int, x1: Int, y1: Int)
    }

    private const val SUBPIXELS = 8

    private fun interface Run {
        fun emit(y0: Int, y1: Int, inset: Int)
    }

    @JvmStatic
    fun roundedSegment(
        graphics: GuiGraphicsExtractor, x0: Int, y0: Int, x1: Int, y1: Int, radius: Int,
        roundTop: Boolean, roundBottom: Boolean, fill: RectFill,
    ) {
        if (x1 <= x0 || y1 <= y0) return
        val span = if (roundTop && roundBottom) (y1 - y0) / 2 else y1 - y0
        val r = if (roundTop || roundBottom) min(radius, min((x1 - x0) / 2, span)) else 0
        if (r <= 0) {
            fill.fill(x0, y0, x1, y1)
            return
        }
        scaled(graphics) {
            val sx0 = x0 * SUBPIXELS
            val sx1 = x1 * SUBPIXELS
            strips(y0 * SUBPIXELS, y1 * SUBPIXELS, r * SUBPIXELS, roundTop, roundBottom, y0 * SUBPIXELS, y1 * SUBPIXELS) { a, b, c ->
                fill.fill(sx0 + c, a, sx1 - c, b)
            }
        }
    }

    @JvmStatic
    fun roundedClip(
        graphics: GuiGraphicsExtractor, x0: Int, y0: Int, x1: Int, y1: Int,
        bx0: Int, by0: Int, bx1: Int, by1: Int, radius: Int, fill: RectFill,
    ) {
        if (x1 <= x0 || y1 <= y0) return
        val r = min(radius, min((bx1 - bx0) / 2, (by1 - by0) / 2))
        val touchesCorner = y0 < by0 + r || y1 > by1 - r
        if (r <= 0 || !touchesCorner) {
            fill.fill(x0, y0, x1, y1)
            return
        }
        scaled(graphics) {
            val sx0 = x0 * SUBPIXELS
            val sx1 = x1 * SUBPIXELS
            val sbx0 = bx0 * SUBPIXELS
            val sbx1 = bx1 * SUBPIXELS
            strips(by0 * SUBPIXELS, by1 * SUBPIXELS, r * SUBPIXELS, true, true, y0 * SUBPIXELS, y1 * SUBPIXELS) { a, b, c ->
                val left = max(sx0, sbx0 + c)
                val right = min(sx1, sbx1 - c)
                if (right > left) fill.fill(left, a, right, b)
            }
        }
    }

    private fun withAlpha(argb: Int, textAlpha: Int): Int {
        val alpha = min(argb ushr 24, textAlpha)
        return (alpha shl 24) or (argb and 0xFFFFFF)
    }

    private inline fun scaled(graphics: GuiGraphicsExtractor, body: () -> Unit) {
        val pose = graphics.pose()
        //? if <=1.21.5 && >1.8.9 {
        /*pose.pushPose()
        pose.scale(1f / SUBPIXELS, 1f / SUBPIXELS, 1f)
        *///?} else {
        pose.pushMatrix()
        pose.scale(1f / SUBPIXELS, 1f / SUBPIXELS)
        //?}
        try {
            body()
        } finally {
            //? if <=1.21.5 && >1.8.9 {
            /*pose.popPose()
            *///?} else
            pose.popMatrix()
        }
    }

    // circle x inset for row i of a radius r corner, sampled at the row centre with row 0 outermost
    private fun inset(r: Int, i: Int): Int {
        if (r <= 0 || i >= r) return 0
        val d = r - i - 0.5
        return max(0, (r - sqrt(r.toDouble() * r - d * d)).roundToInt())
    }

    /**
     * Splits rows [from, to) into runs of equal inset for a box spanning [top, bottom) with corner
     * radius r. Rows outside the box get no inset.
     */
    private fun strips(
        top: Int, bottom: Int, r: Int, roundTop: Boolean, roundBottom: Boolean,
        from: Int, to: Int, run: Run,
    ) {
        val topR = if (roundTop) r else 0
        val botR = if (roundBottom) r else 0
        fun insetAt(y: Int): Int = when {
            y < top || y >= bottom -> 0
            y < top + topR -> inset(r, y - top)
            y >= bottom - botR -> inset(r, bottom - 1 - y)
            else -> 0
        }
        var y = from
        while (y < to) {
            val c = insetAt(y)
            var end = y + 1
            if (c == 0) {
                val flatEnd = when {
                    y < top -> top
                    y >= bottom -> to
                    y >= top + topR && y < bottom - botR -> bottom - botR
                    else -> y + 1
                }
                end = max(end, min(to, flatEnd))
            }
            while (end < to && insetAt(end) == c) end++
            run.emit(y, end, c)
            y = end
        }
    }

    private fun rounded(graphics: GuiGraphicsExtractor, x0: Int, y0: Int, x1: Int, y1: Int, radius: Int, color: Int) {
        if (x1 <= x0 || y1 <= y0 || color ushr 24 == 0) return
        roundedSegment(graphics, x0, y0, x1, y1, radius, true, true) { a, b, c, d -> fill(graphics, a, b, c, d, color) }
    }

    private fun ring(
        graphics: GuiGraphicsExtractor, x0: Int, y0: Int, x1: Int, y1: Int,
        radius: Int, border: Int, inner: Int, color: Int,
    ) {
        if (color ushr 24 == 0) return
        scaled(graphics) {
            val sx0 = x0 * SUBPIXELS
            val sx1 = x1 * SUBPIXELS
            val sy0 = y0 * SUBPIXELS
            val sy1 = y1 * SUBPIXELS
            val b = border * SUBPIXELS
            val r = radius * SUBPIXELS
            val ir = inner * SUBPIXELS
            strips(sy0, sy1, r, true, true, sy0, sy0 + b) { a, e, c -> fill(graphics, sx0 + c, a, sx1 - c, e, color) }
            strips(sy0, sy1, r, true, true, sy1 - b, sy1) { a, e, c -> fill(graphics, sx0 + c, a, sx1 - c, e, color) }
            strips(sy0, sy1, r, true, true, sy0 + b, sy1 - b) { a, e, c ->
                strips(sy0 + b, sy1 - b, ir, true, true, a, e) { ia, ie, ic ->
                    fill(graphics, sx0 + c, ia, sx0 + b + ic, ie, color)
                    fill(graphics, sx1 - b - ic, ia, sx1 - c, ie, color)
                }
            }
        }
    }

    private fun fill(graphics: GuiGraphicsExtractor, x0: Int, y0: Int, x1: Int, y1: Int, color: Int) {
        if (x1 <= x0 || y1 <= y0) return
        //? if > 1.8.9 {
        graphics.fill(x0, y0, x1, y1, color)
        //?} else
        //GuiElement.fill(x0, y0, x1, y1, color)
    }
}
