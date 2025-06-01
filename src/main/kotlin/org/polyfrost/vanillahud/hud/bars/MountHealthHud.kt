package org.polyfrost.vanillahud.hud.bars

import net.minecraft.client.Minecraft
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MathHelper
import org.polyfrost.oneconfig.api.config.v1.annotations.Checkbox
import org.polyfrost.oneconfig.api.config.v1.annotations.RadioButton
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.polyui.unit.Vec2
import org.polyfrost.vanillahud.hud.bars.wrapper.HotbarHUDWrapper

class MountHealthHud : HotbarHUDWrapper(Vec2(180f, 18f)) {
    @Checkbox(title = "Link with health")
    var healthLink: Boolean = false

    @RadioButton(title = "Mode", options = ["Down", "Up"])
    var mode: Int = 1

    override fun title() = "Mount Health HUD"

    override fun defaultPosition() = Vec2(1920f / 2f + 182f / 2f - 81f, 1080f - 39f)

    val mountLink: Int
        get() {
            if (HudManager.panelOpen) return 0
            val player = Minecraft.getMinecraft().renderViewEntity as EntityPlayer
            val tmp = player.ridingEntity as? EntityLivingBase ?: return 0
            val hearts = ((tmp.maxHealth + 0.5f).toInt() / 2).coerceAtMost(30)
            val rows = MathHelper.ceiling_float_int(hearts / 10f) - 1
            return (rows * 10 * (if (mode == 1) 1 else -1) * scale).toInt()
        }
}