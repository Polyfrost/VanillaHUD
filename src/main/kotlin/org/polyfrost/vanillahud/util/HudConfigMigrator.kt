package org.polyfrost.vanillahud.util

import org.polyfrost.oneconfig.api.config.v1.ConfigManager
import java.nio.file.Files

// HOPEFULLY fixes old hud configs
object HudConfigMigrator {
    fun migrate() {
        try {
            val hudsDir = ConfigManager.active().folder.resolve("huds")
            val legacyDir = hudsDir.resolve("vanillahud")
            if (!Files.isDirectory(legacyDir)) return
            Files.newDirectoryStream(legacyDir).use { stream ->
                for (p in stream) {
                    if (!Files.isRegularFile(p)) continue
                    val target = hudsDir.resolve("vanillahud-${p.fileName}")
                    if (Files.exists(target)) continue
                    Files.move(p, target)
                }
            }
            try { Files.deleteIfExists(legacyDir) } catch (_: Throwable) {}
        } catch (_: Throwable) {
        }
    }
}
