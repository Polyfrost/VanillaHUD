package org.polyfrost.vanillahud

import net.fabricmc.api.ClientModInitializer
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.vanillahud.compat.HudElementCompat
import org.polyfrost.vanillahud.config.VanillaHUDConfig
import org.polyfrost.vanillahud.hud.Huds

object VanillaHUDClient : ClientModInitializer {
    override fun onInitializeClient() {
        VanillaHUDConfig.preload()

        HudManager.register(*Huds.all)

        HudElementCompat.init()
    }
}
