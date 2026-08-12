package org.polyfrost.vanillahud.util

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.polyfrost.oneconfig.api.config.v1.ConfigManager
import java.nio.file.Files
import java.nio.file.Path

// HOPEFULLY fixes old hud configs
object HudConfigMigrator {
    private const val SCHEMA_VERSION = 6

    private const val CUSTOM_SCOREBOARD_FILE = "vanillahud-customscoreboard.json"

    private const val HOTBAR_FILE = "vanillahud-hotbar.json"

    /** hud files folded into [HOTBAR_FILE] mapped to the keys they hand over */
    private val MERGED_INTO_HOTBAR = mapOf(
        "vanillahud-health.json" to mapOf("animation" to "healthAnimation", "hardcoreHearts" to "hardcoreHearts"),
        "vanillahud-hunger.json" to mapOf("animation" to "hungerAnimation"),
        "vanillahud-armor.json" to emptyMap(),
        "vanillahud-air.json" to emptyMap(),
        "vanillahud-mount.json" to emptyMap(),
        "vanillahud-experience.json" to emptyMap(),
        "vanillahud-experience-level.json" to emptyMap(),
    )

    private const val LEGACY_UNLOCKED_FILE = "vanillahud-unlocked"
    private const val CUSTOMIZED_FILE = "vanillahud-customized"

    private const val SUBTITLES_FILE = "vanillahud-subtitles.json"
    private const val CLOSED_CAPTIONS_FILE = "vanillahud-closedcaptions.json"

    private val POSITION_KEYS = arrayOf("relativeX", "relativeY", "section")

    private val gson = GsonBuilder().setPrettyPrinting().create()

    private var listening = false

    fun migrate() {
        for (folder in configFolders()) migrateFolder(folder)
        listenForProfileChanges()
    }

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

        if (from < 3) dropCustomScoreboard(folder)
        if (from < 4) renameSubtitles(folder)
        if (from < 5) resetPositions(folder)
        if (from < 6) mergeHotbarCluster(folder)

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
            val hudsDir = folder.resolve("huds")
            if (Files.isDirectory(hudsDir)) {
                Files.newDirectoryStream(hudsDir, "vanillahud-*.json").use { stream ->
                    for (p in stream) {
                        if (!Files.isRegularFile(p)) continue
                        resetFile(p)
                    }
                }
            }
        } catch (_: Throwable) {
        }

        try { Files.deleteIfExists(folder.resolve(LEGACY_UNLOCKED_FILE)) } catch (_: Throwable) {}
        try { Files.deleteIfExists(folder.resolve(CUSTOMIZED_FILE)) } catch (_: Throwable) {}
    }

    /** folds the old status bar and experience settings onto the hotbar and drops its now stale position */
    private fun mergeHotbarCluster(folder: Path) {
        try {
            val hudsDir = folder.resolve("huds")
            if (!Files.isDirectory(hudsDir)) return

            val carried = JsonObject()
            for ((file, keys) in MERGED_INTO_HOTBAR) {
                val path = hudsDir.resolve(file)
                if (keys.isNotEmpty()) {
                    readObject(path)?.let { old ->
                        for ((from, to) in keys) old.get(from)?.let { carried.add(to, it) }
                    }
                }
                try { Files.deleteIfExists(path) } catch (_: Throwable) {}
            }

            val hotbar = hudsDir.resolve(HOTBAR_FILE)
            val obj = readObject(hotbar) ?: JsonObject().apply { addProperty("id", HOTBAR_FILE) }
            for ((key, value) in carried.entrySet()) obj.add(key, value)
            // the old mode became the side dropdown where Vertical already lines up with Left
            obj.remove("hotbarMode")?.let { obj.add("side", it) }
            for (key in POSITION_KEYS) obj.remove(key)
            Files.newBufferedWriter(hotbar).use { gson.toJson(obj, it) }
        } catch (_: Throwable) {
        }

        unmarkCustomized(folder, HOTBAR_FILE)
    }

    /** lets [ForceDefaultPosition] re-seed the hud from its new origin on the next tick */
    private fun unmarkCustomized(folder: Path, hudId: String) {
        try {
            val path = folder.resolve(CUSTOMIZED_FILE)
            if (!Files.isRegularFile(path)) return
            val kept = Files.readAllLines(path).filter { it.trim() != hudId }
            Files.write(path, kept)
        } catch (_: Throwable) {
        }
    }

    private fun readObject(path: Path): JsonObject? = try {
        if (!Files.isRegularFile(path)) null
        else Files.newBufferedReader(path).use { JsonParser.parseReader(it) }.takeIf { it.isJsonObject }?.asJsonObject
    } catch (_: Throwable) {
        null
    }

    private fun dropCustomScoreboard(folder: Path) {
        try {
            Files.deleteIfExists(folder.resolve("huds").resolve(CUSTOM_SCOREBOARD_FILE))
        } catch (_: Throwable) {
        }
    }

    private fun renameSubtitles(folder: Path) {
        try {
            val hudsDir = folder.resolve("huds")
            val old = hudsDir.resolve(SUBTITLES_FILE)
            if (!Files.isRegularFile(old)) return
            val target = hudsDir.resolve(CLOSED_CAPTIONS_FILE)
            if (Files.exists(target)) {
                Files.deleteIfExists(old)
                return
            }
            Files.move(old, target)
            retagId(target)
        } catch (_: Throwable) {
        }
    }

    private fun retagId(path: Path) {
        try {
            val json = Files.newBufferedReader(path).use { JsonParser.parseReader(it) }
            if (!json.isJsonObject) return
            val obj = json.asJsonObject
            if (obj.get("id")?.asString != SUBTITLES_FILE) return
            obj.addProperty("id", CLOSED_CAPTIONS_FILE)
            Files.newBufferedWriter(path).use { gson.toJson(obj, it) }
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
