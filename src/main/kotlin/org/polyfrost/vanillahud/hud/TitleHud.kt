package org.polyfrost.vanillahud.hud

import org.polyfrost.oneconfig.api.config.v1.annotations.Switch
import org.polyfrost.oneconfig.api.hud.v1.TextHud

open class TitleHud : TextHud("") {
    @Switch(title = "Instant Fade")
    var instantFade = false

    override fun getText(): String {
        return "Hi"
    }

    override fun title(): String {
        return "Title"
    }

    override fun category(): Category {
        return Category.INFO
    }
}

class SubTitleHud : TitleHud() {
    override fun getText(): String {
        return "Bye"
    }
}