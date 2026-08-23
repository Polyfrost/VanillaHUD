package org.polyfrost.vanillahud.util

import org.polyfrost.oneconfig.api.config.v1.ConfigManager
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.vanillahud.hud.VanillaHud
import java.nio.file.Files
import java.nio.file.Path

object ForceDefaultPosition {
    private const val FILE = "vanillahud-customized"

    private val file: Path get() = ConfigManager.active().folder.resolve(FILE)

    private var cache: MutableSet<String>? = null

    /** ids queued this launch so each HUD is only forced once per profile */
    private val forced = HashSet<String>()

    private val customized: MutableSet<String>
        get() = cache ?: read().also { cache = it }

    fun invalidate() {
        cache = null
        forced.clear()
    }

    fun isCustomized(hud: VanillaHud): Boolean = customized.contains(hud.hudId)

    private fun markCustomized(hud: VanillaHud) {
        if (!customized.add(hud.hudId)) return
        hud.cancelForceDefault()
        write()
    }

    fun tick() {
        for (hud in HudManager.activeInstances.filterIsInstance<VanillaHud>()) {
            if (!hud.locked) {
                markCustomized(hud)
                continue
            }
            if (isCustomized(hud)) continue
            if (forced.add(hud.hudId)) hud.queueForceDefault()
        }
    }

    private fun read(): MutableSet<String> = try {
        val p = file
        if (Files.isRegularFile(p)) {
            Files.readAllLines(p).mapNotNullTo(LinkedHashSet()) { it.trim().ifEmpty { null } }
        } else {
            LinkedHashSet()
        }
    } catch (_: Throwable) {
        LinkedHashSet()
    }

    private fun write() {
        try {
            val p = file
            Files.createDirectories(p.parent)
            Files.write(p, customized.sorted())
        } catch (_: Throwable) {
        }
    }
}
