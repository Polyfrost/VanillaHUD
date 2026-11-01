package org.polyfrost.vanillahud.hud

import org.polyfrost.compose.render.PolyColor
import org.polyfrost.oneconfig.api.config.v1.Tree
import org.polyfrost.oneconfig.api.config.v1.annotations.Color
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch
import kotlin.math.roundToInt

abstract class BackdropHud(
    hudId: String,
    title: String,
    category: Category,
    defaultBgColor: Int = 0x50000000,
) : VanillaHud(hudId, title, category) {
    init {
        showShadow = true
        bgColor = defaultBgColor
    }

    val background: Boolean get() = showBackground

    val textBgColor: PolyColor get() = PolyColor(bgColor, bgChroma, bgChromaSpeed)

    val radius: Int get() = bgRadius.roundToInt()

    @Switch(title = "Border", subcategory = "Background")
    var border = false

    @Slider(title = "Border Size", subcategory = "Background", min = 0f, max = 10f)
    var borderSize = 1f

    @Color(title = "Border Color", subcategory = "Background")
    var borderColor = PolyColor(0xFF000000.toInt())

    val textShadow: Boolean get() = showShadow

    override fun Tree.addDependencies() = backdropDependencies()

    protected fun Tree.backdropDependencies() {
        hideUnless("showBackground", "border", "borderSize", "borderColor")
        hideUnless("border", "borderSize", "borderColor")
    }
}
