package org.polyfrost.vanillahud.hud.commands

import org.polyfrost.oneconfig.api.config.v1.annotations.Switch
import org.polyfrost.oneconfig.api.hud.v1.TextHud

object TitleHud : TextHud("") {
    @Switch(title = "Instant Fade")
    private val instantFade: Boolean = false

    override fun getText(): String {
        if (!isReal) return "Title"

        return "Not Implemented"
    }

    override fun title(): String {
        return "Title"
    }

    override fun category(): Category {
        return Category.INFO
    }
}

object SubTitleHud : TextHud("") {
    @Switch(title = "Instant Fade")
    private val instantFade: Boolean = false

    override fun getText(): String {
        if (!isReal) return "Subtitle"

        return "Not Implemented"
    }

    override fun title(): String {
        return "Subtitle"
    }

    override fun category(): Category {
        return Category.INFO
    }
}

object ActionBarHud : TextHud("") {
    var hue: Float = 0f
    var opacity: Int = 0

    @Switch(title = "Jukebox Rainbow Timer", description = "Use the rainbow timer color when a jukebox begins playing.")
    var rainbow = true

    override fun getText(): String {
        if (!isReal) return "Action Bar"

        return "Not Implemented"
    }

    override fun title(): String {
        return "Action Bar"
    }

    override fun category(): Category {
        return Category.INFO
    }
}