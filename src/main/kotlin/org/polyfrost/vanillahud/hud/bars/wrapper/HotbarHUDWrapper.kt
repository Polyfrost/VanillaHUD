package org.polyfrost.vanillahud.hud.bars.wrapper

import org.polyfrost.oneconfig.api.config.v1.annotations.RadioButton
import org.polyfrost.polyui.unit.Vec2
import org.polyfrost.vanillahud.hud.wrapper.WrapperHUD

abstract class HotbarHUDWrapper(size: Vec2) : WrapperHUD(size) {
    @RadioButton(
        title = "Icon Alignment",
        options = ["Left", "Right"]
    )
    var alignment = 0

    override fun category() = Category.PLAYER
}