package org.polyfrost.vanillahud.hud.bars

import org.polyfrost.oneconfig.api.config.v1.annotations.Checkbox
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch
import org.polyfrost.polyui.unit.Vec2
import org.polyfrost.vanillahud.hud.bars.wrapper.HotbarHUDWrapper

class HungerHud : HotbarHUDWrapper(Vec2(180f, 18f)) {
    @Checkbox(title = "Link with health")
    var healthLink: Boolean = false

    @Checkbox(title = "Link with mount health")
    var mountLink: Boolean = false

    @Switch(title = "Animation")
    var animation: Boolean = true

    override fun defaultPosition() = Vec2(1920f / 2f + 182f / 2f - 81f, 1080f - 59f)

    override fun title() = "Hunger HUD"
}