package org.polyfrost.vanillahud

import net.fabricmc.api.ClientModInitializer
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.vanillahud.config.VanillaHUDConfig
import org.polyfrost.vanillahud.hud.Huds

object VanillaHUDClient : ClientModInitializer {
    override fun onInitializeClient() {
        VanillaHUDConfig.tooltipStartAtTop

        // Learn kotlin cus wtf is this *Huds shit :sob:
        HudManager.register(*Huds.all)
    }
}
