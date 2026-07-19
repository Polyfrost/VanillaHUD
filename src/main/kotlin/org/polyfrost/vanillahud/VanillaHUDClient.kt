package org.polyfrost.vanillahud

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.vanillahud.compat.CustomScoreboardBridge
import org.polyfrost.vanillahud.compat.HudElementCompat
import org.polyfrost.vanillahud.hud.Huds
import org.polyfrost.vanillahud.util.HudConfigMigrator

object VanillaHUDClient : ClientModInitializer {
    override fun onInitializeClient() {
        HudConfigMigrator.migrate()

        HudManager.register(*Huds.all)

        if (CustomScoreboardBridge.present) {
            HudManager.register(Huds.customScoreboard)
            ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
                CustomScoreboardBridge.syncVisibility()
                Huds.customScoreboard.stealOptions()
            })
        }

        HudElementCompat.init()
    }
}
