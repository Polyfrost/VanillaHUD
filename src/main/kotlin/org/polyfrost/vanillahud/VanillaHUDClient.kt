package org.polyfrost.vanillahud

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.vanillahud.compat.HudElementCompat
import org.polyfrost.vanillahud.hud.Huds
import org.polyfrost.vanillahud.util.ForceDefaultPosition
import org.polyfrost.vanillahud.util.HudConfigMigrator

object VanillaHUDClient : ClientModInitializer {
    override fun onInitializeClient() {
        HudConfigMigrator.migrate()

        Huds.all.forEach {
            HudManager.register(it, "vanillahud", "/assets/vanillahud/vanillahud_dark.svg")
        }

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            ForceDefaultPosition.tick()
        })

        HudElementCompat.init()
    }
}
