package org.polyfrost.vanillahud.hud

import org.polyfrost.oneconfig.api.hud.v1.TextHud

class ActionBarHud : TextHud("") {


    override fun category() = Category.INFO

    override fun getText(): String? {
        TODO("Not yet implemented")
    }

    override fun title() = "Action Bar"
}