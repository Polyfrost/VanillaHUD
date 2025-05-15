package org.polyfrost.vanillahud

import net.minecraftforge.fml.common.Mod
import org.polyfrost.vanillahud.hud.bars.HungerHud

@Mod(modid = Constants.ID, name = Constants.NAME, version = Constants.VERSION)
object VanillaHUD {
    @JvmStatic
    val hunger = HungerHud()
}