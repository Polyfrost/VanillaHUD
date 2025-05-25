package org.polyfrost.vanillahud.hud

import dev.deftu.omnicore.client.render.OmniMatrixStack
import org.polyfrost.oneconfig.api.hud.v1.LegacyHud
import org.polyfrost.vanillahud.utils.drawScaledString

class PlayerListHud : LegacyHud() {
    var isGuiIngame: Boolean = false

    override var width: Float = 0f
    override var height: Float = 0f

    override fun render(
        stack: OmniMatrixStack,
        x: Float,
        y: Float,
        scaleX: Float,
        scaleY: Float
    ) {
        drawScaledString("meow", 1f, 1f, 0xa5e1ff, 1, 1f)
    }

    override fun title(): String {
        return "Player List"
    }

    override fun category(): Category {
        return Category.PLAYER
    }

    override fun update() = true

}