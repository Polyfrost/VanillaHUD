package org.polyfrost.vanillahud

import org.polyfrost.vanillahud.hud.ActionBarHud
import org.polyfrost.vanillahud.hud.hotbar.*

object VanillaHUD {
    @JvmStatic
    val oxygen = OxygenHUD()
    @JvmStatic
    val armor = ArmorHUD()
    @JvmStatic
    val experience = ExperienceHUD()
    @JvmStatic
    val health = HealthHUD()
    @JvmStatic
    val hunger = HungerHUD()
    @JvmStatic
    val mount = MountHealthHUD()

    @JvmStatic
    val actionBar = ActionBarHud()

    @JvmStatic
    val healthLinkAmount
        get() = health.healthLink

    @JvmStatic
    val mountLinkAmount
        get() = mount.mountLink
}