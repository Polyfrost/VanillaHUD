package org.polyfrost.vanillahud

import net.fabricmc.api.ClientModInitializer
//? if > 1.8.9 {
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
//?} else {
/*import org.polyfrost.oneconfig.api.event.v1.EventManager
import org.polyfrost.oneconfig.api.event.v1.events.InitializationEvent
import org.polyfrost.oneconfig.api.event.v1.events.TickEvent
import org.polyfrost.oneconfig.api.event.v1.invoke.EventHandler
import java.util.function.Consumer
*///?}
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.vanillahud.compat.HudElementCompat
import org.polyfrost.vanillahud.hud.Huds
import org.polyfrost.vanillahud.hud.VanillaHud
import org.polyfrost.vanillahud.util.ForceDefaultPosition
import org.polyfrost.vanillahud.util.HudConfigMigrator

object VanillaHUDClient : ClientModInitializer {
    override fun onInitializeClient() {
        //? if = 1.8.9 {
        /*// 1.8.9 runs entrypoints before Minecraft.init, so the HUDs cannot be built yet: the font
        // and the language files they measure against do not exist. Defer to the end of init, at
        // the lowest priority so OneConfig's own setup has already run.
        object : EventHandler<InitializationEvent>() {
            override fun handle(event: InitializationEvent): Boolean {
                setup()
                return true
            }

            override fun getEventClass(): Class<InitializationEvent> = InitializationEvent::class.java

            override fun getPriority(): Int = Int.MIN_VALUE
        }.register()
        *///?} else
        setup()
    }

    private fun setup() {
        HudConfigMigrator.migrate()

        Huds.all.forEach {
            HudManager.register(it, "vanillahud", "/assets/vanillahud/vanillahud_dark.svg")
        }

        //? if > 1.8.9 {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            ForceDefaultPosition.tick()
            VanillaHud.markRefreshDue()
            if (HudManager.isEditing) VanillaHud.refreshAll()
        })
        //?} else {
        /*// same per tick refresh the newer versions get from the Fabric end tick event: without it
        // the editor previews keep whatever they measured when the editor opened
        EventManager.register(TickEvent.End::class.java, Consumer {
            ForceDefaultPosition.tick()
            VanillaHud.markRefreshDue()
            if (HudManager.isEditing) VanillaHud.refreshAll()
        })
        *///?}

        HudElementCompat.init()
    }
}
