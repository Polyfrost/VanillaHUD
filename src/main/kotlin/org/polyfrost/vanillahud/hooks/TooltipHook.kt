package org.polyfrost.vanillahud.hooks

import org.polyfrost.polyui.animate.Animation

object TooltipHook {
    @JvmField
    var isScrolling: Boolean = false

    @JvmField
    var scrollY: Int = 0

    @JvmField
    var animationY: Animation? = null

    @JvmStatic
    fun resetScrolling() {
        scrollY = 0
    }
}