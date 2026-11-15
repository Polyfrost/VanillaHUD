package org.polyfrost.vanillahud.hud

import org.polyfrost.compose.render.PolyColor
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.config.v1.Tree
import org.polyfrost.oneconfig.api.config.v1.showWhen
import org.polyfrost.oneconfig.api.ui.v1.ExperimentalOneConfigUiApi
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

    @OptIn(ExperimentalOneConfigUiApi::class)
    val textBgColor: PolyColor get() = bgPolyColor

    val radius: Int get() = bgRadius.roundToInt()

    val textShadow: Boolean get() = showShadow

    /**
     * The box holds the letters alone, without the font's trailing advance or descender row, and
     * the shadow sits in the default padding. More padding then centres the text, not text and shadow.
     */
    open val hugsGlyphs: Boolean get() = false

    /** pads the right and bottom by [reach], how far the shadow falls past the letters in the element's own units */
    protected fun padForShadow(reach: Float) {
        padRight = reach
        padBottom = reach
    }

    companion object {
        /** rows from the top of a capital to its baseline, below which only descenders and the shadow go */
        const val GLYPH_HEIGHT = 7
    }

    @OptIn(ExperimentalOneConfigUiApi::class)
    override fun migrateLoaded(saved: Tree): Boolean {
        var changed = false
        val enabled = saved.getProp("border")?.get() as? Boolean
        if (enabled != null && saved.get("borderEnabled") == null) {
            borderEnabled = enabled
            changed = true
        }
        val color = legacyBorderColor(saved.get("borderColor")) ?: return changed
        borderColor = color.rawArgb
        if (color.chroma && saved.get("borderChroma") == null && saved.get("borderChromaSpeed") == null) {
            borderChroma = true
            borderChromaSpeed = color.chromaSpeed
        }
        return true
    }

    private fun legacyBorderColor(node: Any?): PolyColor? {
        if (node is Property<*>) return node.get() as? PolyColor
        if (node !is Tree) return null
        val rgba = (node.getProp("value") ?: node.getProp("rgba"))?.get() as? List<*> ?: return null
        val parts = rgba.map { it as? Number ?: return null }
        if (parts.size != 4) return null
        val color = PolyColor.rgba(parts[0].toInt(), parts[1].toInt(), parts[2].toInt(), parts[3].toInt())
        val chroma = node.getProp("chroma")?.get() as? Boolean ?: return color
        val speed = (node.getProp("chromaSpeed")?.get() as? Number)?.toFloat() ?: color.chromaSpeed
        return color.withChroma(chroma, speed)
    }

    override fun Tree.addDependencies() = backdropDependencies()

    protected fun Tree.backdropDependencies() {
        showWhen("showBackground", "borderEnabled", "borderSize", "borderColor", "borderChroma", "borderChromaSpeed")
        showWhen("borderEnabled", "borderSize", "borderColor", "borderChroma", "borderChromaSpeed")
    }
}
