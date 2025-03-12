package org.polyfrost.vanillahud.hud

import org.polyfrost.oneconfig.api.event.v1.eventHandler
import org.polyfrost.oneconfig.api.hud.v1.TextHud
import org.polyfrost.polyui.animate.Animations
import org.polyfrost.polyui.operations.Fade
import org.polyfrost.polyui.unit.Vec2
import org.polyfrost.polyui.unit.seconds
import org.polyfrost.polyui.utils.Clock
import org.polyfrost.vanillahud.events.RecordPlayingEvent

class ActionBarHud : TextHud("") {
    override fun initialize() {
        eventHandler { (message, isPlaying): RecordPlayingEvent ->
            val hud = getBackground() ?: get()
            if (isPlaying) {
                sb.append(message)
                hud.alpha = 1f
                hud.polyUI.addExecutor(Clock.Bomb(2.seconds) {
                    Fade(hud, 0f, false, Animations.Default.create(1.seconds)).add()
                })
                updateAndRecalculate()
            } else {
                hud.alpha = 0f
            }
        }
        super.initialize()
    }

    override fun defaultPosition() = Vec2(1920f / 2f, 1080f - 62f)

    override fun category() = Category.INFO

    override fun getText() = null

    override fun title() = "Action Bar"
}