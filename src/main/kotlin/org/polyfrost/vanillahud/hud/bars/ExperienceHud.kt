package org.polyfrost.vanillahud.hud.bars

import org.polyfrost.oneconfig.api.config.v1.annotations.Slider
import org.polyfrost.polyui.unit.Vec2
import org.polyfrost.vanillahud.oldhuds.hotbar.HotbarHUDWrapper

class ExperienceHud : HotbarHUDWrapper(Vec2(182f, 5f)) {
    @Slider(title = "Level Text Height", min = -10f, max = 10f)
    var expHeight: Float = 4f

    override fun defaultPosition() = Vec2(1920f / 2f - 182f / 2f, 1080f - 29f)

    override fun title() = "Experience Bar"
}