package org.polyfrost.vanillahud

import net.fabricmc.api.ClientModInitializer
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.vanillahud.config.VanillaHUDConfig
import org.polyfrost.vanillahud.hud.Huds

object VanillaHUDClient : ClientModInitializer {
    override fun onInitializeClient() {
        VanillaHUDConfig.tooltipStartAtTop

        HudManager.register(*Huds.all)
    }
}
