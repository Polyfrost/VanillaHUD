package org.polyfrost.vanillahud.util

import org.polyfrost.oneconfig.api.config.v1.ConfigManager
import org.polyfrost.oneconfig.utils.v1.dsl.mc
import org.polyfrost.vanillahud.hud.Huds
import java.nio.file.Files

object ForceDefaultPosition {
    private val marker get() = ConfigManager.active().folder.resolve("vanillahud-unlocked")

    private var optedOutCache: Boolean? = null
    private var wasInWorld = false

    val optedOut: Boolean
        get() = optedOutCache ?: Files.exists(marker).also { optedOutCache = it }

    private fun markOptedOut() {
        if (optedOut) return
        optedOutCache = true
        try {
            Files.createDirectories(marker.parent)
            if (!Files.exists(marker)) Files.createFile(marker)
        } catch (_: Throwable) {
        }
    }

    fun tick() {
        if (!optedOut && Huds.all.any { !it.locked }) {
            markOptedOut()
            for (hud in Huds.all) hud.cancelForceDefault()
        }

        val inWorld = try { mc.level != null } catch (_: Throwable) { false }
        if (inWorld && !wasInWorld && !optedOut) {
            for (hud in Huds.all) hud.queueForceDefault()
        }
        wasInWorld = inWorld
    }
}
