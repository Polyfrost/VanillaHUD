package org.polyfrost.vanillahud

import net.fabricmc.api.ClientModInitializer
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.vanillahud.compat.HudElementCompat
import org.polyfrost.vanillahud.hud.Huds
import org.polyfrost.vanillahud.util.ForceDefaultPosition
import org.polyfrost.vanillahud.util.HudConfigMigrator

object VanillaHUDClient : ClientModInitializer {
    override fun onInitializeClient() {
        HudConfigMigrator.migrate()

        HudManager.register(*Huds.all)

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            ForceDefaultPosition.tick()
        })

        HudElementCompat.init()
    }
}
