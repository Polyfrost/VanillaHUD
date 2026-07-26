package org.polyfrost.vanillahud.util

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.polyfrost.oneconfig.api.config.v1.ConfigManager
import java.nio.file.Files
import java.nio.file.Path

// HOPEFULLY fixes old hud configs
object HudConfigMigrator {
    private const val SCHEMA_VERSION = 1

    private val POSITION_KEYS = arrayOf("relativeX", "relativeY", "section")

    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun migrate() {
        moveLegacyFolder()
        resetPositions()
    }

    private fun moveLegacyFolder() {
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

    private fun resetPositions() {
        try {
            val folder = ConfigManager.active().folder
            val stamp = folder.resolve("vanillahud-migration")
            if (readStamp(stamp) >= SCHEMA_VERSION) return

            var touched = false
            val hudsDir = folder.resolve("huds")
            if (Files.isDirectory(hudsDir)) {
                Files.newDirectoryStream(hudsDir, "vanillahud-*.json").use { stream ->
                    for (p in stream) {
                        if (!Files.isRegularFile(p)) continue
                        if (resetFile(p)) touched = true
                    }
                }
            }

            if (touched) {
                try { Files.deleteIfExists(folder.resolve("vanillahud-unlocked")) } catch (_: Throwable) {}
            }

            writeStamp(stamp)
        } catch (_: Throwable) {
        }
    }

    private fun resetFile(path: Path): Boolean {
        return try {
            val json = Files.newBufferedReader(path).use { JsonParser.parseReader(it) }
            if (!json.isJsonObject) return false
            val obj = json.asJsonObject

            var changed = false
            for (key in POSITION_KEYS) if (obj.remove(key) != null) changed = true
            if (!isLocked(obj)) {
                obj.addProperty("locked", true)
                changed = true
            }
            if (!changed) return false

            Files.newBufferedWriter(path).use { gson.toJson(obj, it) }
            true
        } catch (_: Throwable) {
            false
        }
    }

    private fun isLocked(obj: JsonObject): Boolean = try {
        obj.get("locked")?.asBoolean == true
    } catch (_: Throwable) {
        false
    }

    private fun readStamp(path: Path): Int = try {
        if (Files.isRegularFile(path)) Files.readString(path).trim().toIntOrNull() ?: 0 else 0
    } catch (_: Throwable) {
        0
    }

    private fun writeStamp(path: Path) {
        try {
            Files.createDirectories(path.parent)
            Files.writeString(path, SCHEMA_VERSION.toString())
        } catch (_: Throwable) {
        }
    }
}
