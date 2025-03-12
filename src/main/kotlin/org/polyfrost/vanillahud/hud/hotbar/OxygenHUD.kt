package org.polyfrost.vanillahud.hud.hotbar

import org.polyfrost.oneconfig.api.config.v1.annotations.Checkbox
import org.polyfrost.polyui.unit.Vec2

class OxygenHUD() : HotbarHUDWrapper(Vec2(180f, 18f)) {
    @Checkbox(title = "Link with Health Position")
    var healthLink: Boolean = false

    @Checkbox(title = "Link with Mount Health Position")
    var mountLink: Boolean = true

    override fun title() = "Oxygen HUD"

    override fun defaultPosition() = Vec2(1920f / 2f + 182f / 2f - 81f, 1080f - 49f)
}