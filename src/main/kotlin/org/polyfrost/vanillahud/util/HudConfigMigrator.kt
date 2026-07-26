package org.polyfrost.vanillahud.util

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.polyfrost.oneconfig.api.config.v1.ConfigManager
import java.nio.file.Files
import java.nio.file.Path

// HOPEFULLY fixes old hud configs
object HudConfigMigrator {
    private const val SCHEMA_VERSION = 3

    /** Left behind by the removed Custom Scoreboard compat layer; no hud claims it anymore. */
    private const val CUSTOM_SCOREBOARD_FILE = "vanillahud-customscoreboard.json"

    private val POSITION_KEYS = arrayOf("relativeX", "relativeY", "section")

    private val gson = GsonBuilder().setPrettyPrinting().create()

    private var listening = false

    fun migrate() {
        for (folder in configFolders()) migrateFolder(folder)
        listenForProfileChanges()
    }

    /**
     * Every profile folder, including the root (profile-less) one. OneConfig only mounts the active
     * profile, so inactive profiles keep their own copy of every hud config and need migrating too.
     */
    private fun configFolders(): List<Path> {
        val out = LinkedHashMap<Path, Path>()

        fun add(folder: Path?) {
            if (folder == null) return
            val key = try { folder.toAbsolutePath().normalize() } catch (_: Throwable) { return }
            out.putIfAbsent(key, folder)
        }

        try {
            for (profile in ConfigManager.profiles()) {
                add(try { ConfigManager.profileDir(profile) } catch (_: Throwable) { null })
            }
        } catch (_: Throwable) {
        }
        add(try { ConfigManager.active().folder } catch (_: Throwable) { null })

        return out.values.toList()
    }

    /**
     * Profiles created or dropped in while the game is running never went through [migrate].
     * Switching onto one re-runs it; the stamp makes already-migrated folders a no-op.
     */
    private fun listenForProfileChanges() {
        if (listening) return
        try {
            ConfigManager.addProfileChangeListener {
                try { migrateFolder(ConfigManager.active().folder) } catch (_: Throwable) {}
                ForceDefaultPosition.invalidate()
            }
            listening = true
        } catch (_: Throwable) {
        }
    }

    private fun migrateFolder(folder: Path) {
        if (!Files.isDirectory(folder)) return

        moveLegacyFolder(folder)

        val stamp = folder.resolve("vanillahud-migration")
        val from = readStamp(stamp)
        if (from >= SCHEMA_VERSION) return

        if (from < 1) resetPositions(folder)
        if (from < 3) dropCustomScoreboard(folder)

        writeStamp(stamp)
    }

    private fun moveLegacyFolder(folder: Path) {
        try {
            val hudsDir = folder.resolve("huds")
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

    private fun resetPositions(folder: Path) {
        try {
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
        } catch (_: Throwable) {
        }
    }

    private fun dropCustomScoreboard(folder: Path) {
        try {
            Files.deleteIfExists(folder.resolve("huds").resolve(CUSTOM_SCOREBOARD_FILE))
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
