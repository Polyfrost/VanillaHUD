package org.polyfrost.vanillahud.render

import kotlin.math.roundToInt

internal class BackgroundRect(val x: Int, val y: Int, val w: Int, val h: Int, val u: Int, val v: Int, val uw: Int, val vh: Int)

internal fun fit(mode: Int, x: Int, y: Int, w: Int, h: Int, tw: Int, th: Int): BackgroundRect = when (mode) {
    1 -> {
        val scale = maxOf(w.toFloat() / tw, h.toFloat() / th)
        val uw = minOf(tw, (w / scale).roundToInt())
        val vh = minOf(th, (h / scale).roundToInt())
        BackgroundRect(x, y, w, h, (tw - uw) / 2, (th - vh) / 2, uw, vh)
    }
    2 -> {
        val scale = minOf(w.toFloat() / tw, h.toFloat() / th)
        val dw = (tw * scale).roundToInt()
        val dh = (th * scale).roundToInt()
        BackgroundRect(x + (w - dw) / 2, y + (h - dh) / 2, dw, dh, 0, 0, tw, th)
    }
    3 -> {
        val cw = minOf(w, tw)
        val ch = minOf(h, th)
        BackgroundRect(x + (w - cw) / 2, y + (h - ch) / 2, cw, ch, (tw - cw) / 2, (th - ch) / 2, cw, ch)
    }
    else -> BackgroundRect(x, y, w, h, 0, 0, tw, th)
}
