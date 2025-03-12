package org.polyfrost.vanillahud.hud

import org.polyfrost.oneconfig.api.hud.v1.Hud
import org.polyfrost.polyui.component.Drawable
import org.polyfrost.polyui.unit.Vec2

abstract class WrapperHUD(val size: Vec2) : Hud<Drawable>() {
    val scale: Float
        get() = get().scaleX

    var x: Float
        get() = get().x
        set(value) {
            get().x = value
        }

    var y: Float
        get() = get().y
        set(value) {
            get().y = value
        }

    val width: Float
        get() = get().width

    val height: Float
        get() = get().height

    override fun create(): Drawable {
        return object : Drawable(size = size) {
            override fun draw() {
            }

            override fun render() {
            }

        }
    }

    override fun update() = false

    override fun multipleInstancesAllowed() = false

    override fun hasBackground() = false

    abstract override fun defaultPosition(): Vec2
}