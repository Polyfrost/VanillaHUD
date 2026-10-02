package org.polyfrost.vanillahud.test

//? if = 1.8.9 {
/*import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.Bootstrap
import net.ornithemc.osl.entrypoints.api.ModInitializer

/**
 * Ornithe runs registry bootstrap through OSL's entrypoints, and OSL throws on a second pass,
 * so every test class shares this one shot.
 */
internal object LegacyBootstrap {
    private var done = false

    fun once() {
        if (done) return
        done = true
        FabricLoader.getInstance().invokeEntrypoints(
            ModInitializer.ENTRYPOINT_KEY,
            ModInitializer::class.java,
            ModInitializer::init,
        )
        Bootstrap.init()
    }
}
*///?}
