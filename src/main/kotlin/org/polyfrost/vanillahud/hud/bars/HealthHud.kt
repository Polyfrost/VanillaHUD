package org.polyfrost.vanillahud.hud.bars

import net.minecraft.client.Minecraft
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.util.MathHelper
import org.polyfrost.oneconfig.api.config.v1.annotations.Checkbox
import org.polyfrost.oneconfig.api.config.v1.annotations.RadioButton
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.polyui.unit.Vec2
import org.polyfrost.vanillahud.hud.bars.wrapper.HotbarHUDWrapper
import kotlin.math.max

object HealthHud : HotbarHUDWrapper(Vec2(180f, 18f)) {
    @RadioButton(title = "Mode", options = ["Down", "Up"])
    var mode: Int = 1

    @Checkbox(title = "Link with mount health")
    var mountLink: Boolean = false

    @Switch(title = "Animation")
    var animation: Boolean = true

    override fun title() = "Health HUD"

    override fun defaultPosition() = Vec2(1920f / 2f - 182f / 2f, 1080f - 39f)

    val healthLink: Int
        get() {
            val player = Minecraft.getMinecraft().thePlayer
            val attrMaxHealth = player.getEntityAttribute(SharedMonsterAttributes.maxHealth)
            val healthMax = attrMaxHealth.attributeValue.toFloat()
            val absorb = player.absorptionAmount
            val healthRows = MathHelper.ceiling_float_int((healthMax + absorb) / 2.0f / 10.0f)
            val rowHeight = max((10 - (healthRows - 2)), 3)
            var height = healthRows * rowHeight
            if (rowHeight != 10) height += 10 - rowHeight
            return if (HudManager.panelOpen) 0 else ((height - 10) * (if (mode == 1) 1 else -1) * scale).toInt()
        }
}