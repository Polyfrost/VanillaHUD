package org.polyfrost.vanillahud.hud

import org.polyfrost.compose.render.PolyColor
import org.polyfrost.oneconfig.api.config.v1.Tree
import org.polyfrost.oneconfig.api.config.v1.annotations.Color
import org.polyfrost.oneconfig.api.config.v1.annotations.Dropdown
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch

abstract class BackdropHud(
    hudId: String,
    title: String,
    category: Category,
    defaultBgColor: Int = 0x50000000,
) : VanillaHud(hudId, title, category) {
    @Dropdown(title = "Text Type", subcategory = "Text", options = ["No Shadow", "Shadow"])
    var textType: Int = 1

    @Switch(title = "Background", subcategory = "Background")
    var background = false

    @Color(title = "Background Color", subcategory = "Background")
    var textBgColor = PolyColor(defaultBgColor)

    @Slider(title = "Padding X", subcategory = "Background", min = 0f, max = 10f)
    var paddingX = 2f

    @Slider(title = "Padding Y", subcategory = "Background", min = 0f, max = 10f)
    var paddingY = 2f

    @Switch(title = "Rounded Corners", subcategory = "Background")
    var rounded = false

    @Slider(title = "Corner Radius", subcategory = "Background", min = 0f, max = 10f)
    var cornerRadius = 2f

    @Switch(title = "Border", subcategory = "Background")
    var border = false

    @Slider(title = "Border Size", subcategory = "Background", min = 0f, max = 10f)
    var borderSize = 1f

    @Color(title = "Border Color", subcategory = "Background")
    var borderColor = PolyColor(0xFF000000.toInt())

    val textShadow: Boolean get() = textType == 1

    override fun Tree.addDependencies() = backdropDependencies()

    protected fun Tree.backdropDependencies() {
        hideUnless("background", "textBgColor", "paddingX", "paddingY", "rounded", "cornerRadius", "border", "borderSize", "borderColor")
        hideUnless("rounded", "cornerRadius")
        hideUnless("border", "borderSize", "borderColor")
    }
}
