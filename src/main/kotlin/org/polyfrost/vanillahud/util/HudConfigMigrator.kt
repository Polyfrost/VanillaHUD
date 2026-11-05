package org.polyfrost.vanillahud.util

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.polyfrost.oneconfig.api.config.v1.ConfigManager
import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.abs

// HOPEFULLY fixes old hud configs
object HudConfigMigrator {
    private const val SCHEMA_VERSION = 15

    private const val CUSTOM_SCOREBOARD_FILE = "vanillahud-customscoreboard.json"

    private const val HOTBAR_FILE = "vanillahud-hotbar.json"

    private const val TAB_FILE = "vanillahud-tab.json"

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

    /** hotbar keys handed back to the bar huds that were split out of it again */
    private val SPLIT_FROM_HOTBAR = mapOf(
        "vanillahud-health.json" to mapOf(
            "healthAnimation" to "animation", "hardcoreHearts" to "hardcoreHearts", "healthAlignment" to "fillSide",
        ),
        "vanillahud-armor.json" to mapOf("armorAlignment" to "fillSide"),
        "vanillahud-hunger.json" to mapOf("hungerAnimation" to "animation", "hungerAlignment" to "fillSide"),
        "vanillahud-air.json" to mapOf("airAlignment" to "fillSide"),
        "vanillahud-experience.json" to mapOf("overlayLocatorBar" to "overlayLocatorBar"),
        "vanillahud-experience-level.json" to emptyMap(),
    )

    /** hotbar switches that became hiding the experience huds */
    private val HIDDEN_BY_HOTBAR_SWITCH = mapOf(
        "experienceBar" to "vanillahud-experience.json",
        "experienceLevel" to "vanillahud-experience-level.json",
    )

    private val ROTATION_KEYS = listOf("side", "reverseDirection")

    private val DROPPED_STYLE_KEYS = listOf(
        "style", "background", "textBgColor", "paddingX", "paddingY", "rounded", "cornerRadius", "textType",
        "backgroundColor", "bodyBgColor", "captionBgColor", "captionTextColor", "titleScale", "subtitleScale",
    )

    private val DROPPED_HOTBAR_KEYS = listOf("experienceBar", "experienceLevel", "levelTextHeight")

    private const val LEVEL_FILE = "vanillahud-experience-level.json"

    /** how far each text hud's shadow falls past its letters, null for those at the game's own size */
    private val SHADOW_REACH = mapOf(
        "vanillahud-actionbar.json" to null,
        "vanillahud-itemtooltip.json" to null,
        "vanillahud-title.json" to 4f,
        "vanillahud-subtitle.json" to 2f,
    )

    private const val LEGACY_UNLOCKED_FILE = "vanillahud-unlocked"
    private const val CUSTOMIZED_FILE = "vanillahud-customized"

    private const val SUBTITLES_FILE = "vanillahud-subtitles.json"
    private const val CLOSED_CAPTIONS_FILE = "vanillahud-closedcaptions.json"

    private val LEGACY_CAPTION_IDS = setOf(SUBTITLES_FILE, "vanillahud/subtitles.json")

    private const val LEGACY_CAPTIONS_CLASS = "org.polyfrost.vanillahud.hud.SubtitlesHud"
    private const val CLOSED_CAPTIONS_CLASS = "org.polyfrost.vanillahud.hud.ClosedCaptionsHud"

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
        renameSubtitles(folder)

        val stamp = folder.resolve("vanillahud-migration")
        val from = readStamp(stamp)
        if (from >= SCHEMA_VERSION) return

        if (from < 3) try { Files.deleteIfExists(folder.resolve("huds").resolve(CUSTOM_SCOREBOARD_FILE)) } catch (_: Throwable) {}
        if (from < 5) resetPositions(folder)
        if (from < 6) mergeHotbarCluster(folder)
        if (from < 7) recentreTabList(folder)
        if (from < 8) splitHotbarCluster(folder)
        if (from < 9) adoptDesignerOptions(folder)
        if (from < 10) dropUnusedPadding(folder)
        if (from < 11) {
            try { Files.deleteIfExists(folder.resolve("huds").resolve("vanillahud-mount.json")) } catch (_: Throwable) {}
            unlockAll(folder)
        }
        if (from < 12) resetLevelText(folder)
        if (from < 13) padTextShadows(folder)
        // a folder below 9 has only just been given the designer migration above
        if (from in 9..13) redoLostDesignerMigration(folder)
        if (from < 15) rescaleTitlePadding(folder)

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
                    val name = "vanillahud-${p.fileName}"
                    val target = hudsDir.resolve(name)
                    if (Files.exists(target)) continue
                    Files.move(p, target)
                    retagId(target, name)
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
            val obj = readObject(hotbar) ?: JsonObject()
            obj.addProperty("id", HOTBAR_FILE)
            for ((key, value) in carried.entrySet()) obj.add(key, value)
            // the old mode became the side dropdown where Vertical already lines up with Left
            obj.remove("hotbarMode")?.let { obj.add("side", it) }
            for (key in POSITION_KEYS) obj.remove(key)
            writeObject(hotbar, obj)
        } catch (_: Throwable) {
        }

        unmarkCustomized(folder, HOTBAR_FILE)
    }

    /** the bars left the hotbar again so each gets its settings back and the hotbar drops its stale position */
    private fun splitHotbarCluster(folder: Path) {
        try {
            val hudsDir = folder.resolve("huds")
            val hotbarPath = hudsDir.resolve(HOTBAR_FILE)
            val hotbar = readObject(hotbarPath) ?: return

            for ((file, keys) in SPLIT_FROM_HOTBAR) {
                val path = hudsDir.resolve(file)
                val obj = readObject(path) ?: JsonObject()
                obj.addProperty("id", file)
                for ((from, to) in keys) hotbar.get(from)?.let { obj.add(to, it) }
                // the old hotbar rotated the whole cluster so every part inherits it
                for (key in ROTATION_KEYS) hotbar.get(key)?.let { obj.add(key, it) }
                if (HIDDEN_BY_HOTBAR_SWITCH.any { (switch, target) ->
                        target == file && hotbar.get(switch)?.let { it.isJsonPrimitive && !it.asBoolean } == true
                    }
                ) obj.addProperty("hidden", true)
                writeObject(path, obj)
                unmarkCustomized(folder, file)
            }

            for (keys in SPLIT_FROM_HOTBAR.values) for (from in keys.keys) hotbar.remove(from)
            for (key in DROPPED_HOTBAR_KEYS) hotbar.remove(key)
            for (key in POSITION_KEYS) hotbar.remove(key)
            writeObject(hotbarPath, hotbar)
        } catch (_: Throwable) {
        }

        unmarkCustomized(folder, HOTBAR_FILE)
    }

    internal fun recentreTabList(folder: Path) {
        try {
            val path = folder.resolve("huds").resolve(TAB_FILE)
            val obj = readObject(path) ?: return
            if (!isLocked(obj)) return
            if (enumValue(obj, "section") != "TopCenter") return
            if ((enumValue(obj, "growthAnchor") ?: "Auto") != "Auto") return
            if (!obj.get("anchorTargetId")?.asString.isNullOrEmpty()) return
            val y = obj.get("relativeY")?.asFloat ?: return
            if (abs(y - 10f) > 1e-3f) return
            if (obj.remove("relativeX") == null) return
            writeObject(path, obj)
        } catch (_: Throwable) {
        }
    }

    /** moves the options OneConfig's Designer now owns onto its keys, and replaces its stored defaults with vanilla's */
    private fun adoptDesignerOptions(folder: Path) {
        val hudsDir = folder.resolve("huds")
        if (!Files.isDirectory(hudsDir)) return
        Files.newDirectoryStream(hudsDir, "vanillahud-*.json").use { files ->
            for (path in files) try {
                val obj = readObject(path) ?: continue
                adoptDesignerOptions(path.fileName.toString(), obj)
                writeObject(path, obj)
            } catch (_: Throwable) {
            }
        }
    }

    private fun adoptDesignerOptions(file: String, obj: JsonObject) {
        fun bool(key: String, default: Boolean) = obj.get(key)?.takeIf { it.isJsonPrimitive }?.asBoolean ?: default
        fun float(key: String, default: Float) = obj.get(key)?.takeIf { it.isJsonPrimitive }?.asFloat ?: default
        fun shadow(default: Int) = (obj.get("textType")?.takeIf { it.isJsonPrimitive }?.asInt ?: default) == 1
        fun color(key: String, default: Int): Int {
            val rgba = obj.get(key)?.takeIf { it.isJsonObject }?.asJsonObject?.getAsJsonArray("value") ?: return default
            if (rgba.size() != 4) return default
            return (rgba[3].asInt shl 24) or (rgba[0].asInt shl 16) or (rgba[1].asInt shl 8) or rgba[2].asInt
        }
        val radius = if (bool("rounded", false)) float("cornerRadius", 2f) else 0f
        when (file) {
            "vanillahud-actionbar.json", "vanillahud-bossbar.json", "vanillahud-itemtooltip.json",
            "vanillahud-title.json", "vanillahud-subtitle.json" -> {
                val default = if (file == "vanillahud-bossbar.json") 0x78000000 else 0x50000000
                obj.addProperty("showBackground", bool("background", false))
                obj.addProperty("bgColor", color("textBgColor", default))
                obj.addProperty("bgRadius", radius)
                obj.addProperty("showShadow", shadow(1))
                val padX = float("paddingX", 2f)
                val padY = float("paddingY", 2f)
                obj.addProperty("padLeft", padX)
                obj.addProperty("padRight", padX)
                obj.addProperty("padTop", padY)
                obj.addProperty("padBottom", padY)
                if (file == "vanillahud-title.json") obj.addProperty("textScale", float("titleScale", 4f))
                if (file == "vanillahud-subtitle.json") obj.addProperty("textScale", float("subtitleScale", 2f))
            }
            "vanillahud-scoreboard.json" -> {
                obj.addProperty("showBackground", true)
                obj.addProperty("bgColor", color("backgroundColor", 0x4C000000))
                obj.addProperty("bgRadius", radius)
                obj.addProperty("showShadow", shadow(0))
            }
            TAB_FILE -> {
                obj.addProperty("showBackground", true)
                obj.addProperty("bgColor", color("bodyBgColor", 0x80000000.toInt()))
                obj.addProperty("bgRadius", radius)
                obj.addProperty("showShadow", shadow(1))
            }
            "vanillahud-closedcaptions.json" -> {
                obj.addProperty("showBackground", true)
                obj.addProperty("bgColor", color("captionBgColor", 0xCC000000.toInt()))
                obj.addProperty("textColor", color("captionTextColor", -1))
                obj.addProperty("bgRadius", 0f)
                obj.addProperty("showShadow", shadow(1))
            }
            else -> {
                obj.addProperty("showBackground", false)
                obj.addProperty("bgRadius", 0f)
                obj.addProperty("showShadow", false)
            }
        }
        val font = JsonObject()
        font.addProperty("class", "org.polyfrost.oneconfig.api.hud.v1.Font")
        font.addProperty("value", "Minecraft")
        obj.add("font", font)
        for (key in DROPPED_STYLE_KEYS) obj.remove(key)
    }

    /** the customized file now tracks which HUDs follow vanilla, so locking is the user's choice again */
    private fun unlockAll(folder: Path) {
        val hudsDir = folder.resolve("huds")
        if (!Files.isDirectory(hudsDir)) return
        Files.newDirectoryStream(hudsDir, "vanillahud-*.json").use { files ->
            for (path in files) try {
                val obj = readObject(path) ?: continue
                if (!isLocked(obj)) continue
                obj.addProperty("locked", false)
                writeObject(path, obj)
            } catch (_: Throwable) {
            }
        }
    }

    /** padding now always grows the box, so what only ever applied behind a background must not move HUDs without one */
    private fun dropUnusedPadding(folder: Path) {
        val hudsDir = folder.resolve("huds")
        if (!Files.isDirectory(hudsDir)) return
        Files.newDirectoryStream(hudsDir, "vanillahud-*.json").use { files ->
            for (path in files) try {
                val obj = readObject(path) ?: continue
                if (!dropUnusedPadding(obj)) continue
                writeObject(path, obj)
            } catch (_: Throwable) {
            }
        }
    }

    private fun dropUnusedPadding(obj: JsonObject): Boolean {
        if (obj.get("showBackground")?.takeIf { it.isJsonPrimitive }?.asBoolean == true) return false
        for (key in listOf("padLeft", "padRight", "padTop", "padBottom")) obj.addProperty(key, 0f)
        return true
    }

    /**
     * Text boxes stopped covering the shadow, which the right and bottom padding holds instead.
     * Stored padding gains it so a saved box still fits its shadow.
     */
    private fun padTextShadows(folder: Path) {
        for (file in SHADOW_REACH.keys) try {
            val path = folder.resolve("huds").resolve(file)
            val obj = readObject(path) ?: continue
            padTextShadow(file, obj)
            writeObject(path, obj)
        } catch (_: Throwable) {
        }
    }

    private fun padTextShadow(file: String, obj: JsonObject) {
        if (file !in SHADOW_REACH) return
        val scale = SHADOW_REACH[file]
        val shadow = if (scale == null) 1f else obj.get("textScale")?.takeIf { it.isJsonPrimitive }?.asFloat ?: scale
        for (key in listOf("padRight", "padBottom")) {
            val pad = obj.get(key)?.takeIf { it.isJsonPrimitive }?.asFloat ?: continue
            obj.addProperty(key, pad + shadow)
        }
    }

    /**
     * Title padding now grows with the text from its default size, so what was saved at another size
     * is restated to keep its box. The shadow's own default is left to follow the text, that being
     * what was wrong with it.
     */
    private fun rescaleTitlePadding(folder: Path) {
        for ((file, default) in SHADOW_REACH) try {
            if (default == null) continue
            val path = folder.resolve("huds").resolve(file)
            val obj = readObject(path) ?: continue
            val scale = obj.get("textScale")?.takeIf { it.isJsonPrimitive }?.asFloat ?: continue
            if (scale <= 0f || scale == default) continue
            for (key in listOf("padLeft", "padRight", "padTop", "padBottom")) {
                val pad = obj.get(key)?.takeIf { it.isJsonPrimitive }?.asFloat ?: continue
                if (pad == default && (key == "padRight" || key == "padBottom")) continue
                obj.addProperty(key, pad * default / scale)
            }
            writeObject(path, obj)
        } catch (_: Throwable) {
        }
    }

    /** the experience level's outline moved from vanilla's draws to the hud's own shadow and colour options */
    private fun resetLevelText(folder: Path) {
        val path = folder.resolve("huds").resolve(LEVEL_FILE)
        try {
            val obj = readObject(path) ?: return
            resetLevelText(obj)
            writeObject(path, obj)
        } catch (_: Throwable) {
        }
    }

    private fun resetLevelText(obj: JsonObject) {
        for (key in listOf("showShadow", "fullShadow", "textColor", "shadowColor")) obj.remove(key)
    }

    /**
     * 1.8.9 ran the migrator after OneConfig had already loaded the HUDs, and OneConfig then saved what it
     * had read back over the migrated files while the stamp moved on. A hud still holding OneConfig's own
     * designer defaults, which no vanillahud element ships, never kept the designer migration, so it is
     * given it again along with the later steps built on it.
     */
    internal fun redoLostDesignerMigration(folder: Path) {
        val hudsDir = folder.resolve("huds")
        if (!Files.isDirectory(hudsDir)) return
        Files.newDirectoryStream(hudsDir, "vanillahud-*.json").use { files ->
            for (path in files) try {
                val obj = readObject(path) ?: continue
                if (!hasOneConfigDesignerDefaults(obj)) continue
                val file = path.fileName.toString()
                adoptDesignerOptions(file, obj)
                dropUnusedPadding(obj)
                // left over from when a locked hud meant one still at its vanilla origin
                obj.addProperty("locked", false)
                if (file == LEVEL_FILE) resetLevelText(obj)
                padTextShadow(file, obj)
                writeObject(path, obj)
            } catch (_: Throwable) {
            }
        }
    }

    private fun hasOneConfigDesignerDefaults(obj: JsonObject): Boolean = try {
        fun prim(key: String) = obj.get(key)?.takeIf { it.isJsonPrimitive }
        prim("showBackground")?.asBoolean == true &&
            prim("bgColor")?.asInt == 0x80000000.toInt() &&
            prim("bgRadius")?.asFloat == 4f &&
            prim("showShadow")?.asBoolean == false
    } catch (_: Throwable) {
        false
    }

    private fun enumValue(obj: JsonObject, key: String): String? {
        val e = obj.get(key) ?: return null
        return if (e.isJsonObject) e.asJsonObject.get("value")?.asString else e.asString
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

    private fun writeObject(path: Path, obj: JsonObject) = Files.newBufferedWriter(path).use { gson.toJson(obj, it) }

    private fun renameSubtitles(folder: Path) {
        try {
            val hudsDir = folder.resolve("huds")
            val target = hudsDir.resolve(CLOSED_CAPTIONS_FILE)
            val old = hudsDir.resolve(SUBTITLES_FILE)
            if (Files.isRegularFile(old)) {
                val legacy = readObject(old)
                if (legacy != null) {
                    val merged = readObject(target) ?: JsonObject()
                    for (key in POSITION_KEYS) legacy.remove(key)
                    for ((key, value) in legacy.entrySet()) merged.add(key, value)
                    writeObject(target, merged)
                }
                try { Files.deleteIfExists(old) } catch (_: Throwable) {}
            }
            if (Files.isRegularFile(target)) {
                retagId(target, CLOSED_CAPTIONS_FILE)
                retagCaptionClass(target)
            }
        } catch (_: Throwable) {
        }
    }

    private fun retagCaptionClass(path: Path) {
        try {
            val obj = readObject(path) ?: return
            val current = try { obj.get("hudClass")?.asString } catch (_: Throwable) { null }
            if (current != null && current != LEGACY_CAPTIONS_CLASS) return
            obj.addProperty("hudClass", CLOSED_CAPTIONS_CLASS)
            writeObject(path, obj)
        } catch (_: Throwable) {
        }
    }

    private fun retagId(path: Path, id: String) {
        try {
            val obj = readObject(path) ?: return
            val current = try { obj.get("id")?.asString } catch (_: Throwable) { null } ?: return
            if (current == id) return
            if (path.fileName.toString() == CLOSED_CAPTIONS_FILE && current !in LEGACY_CAPTION_IDS) return
            obj.addProperty("id", id)
            writeObject(path, obj)
        } catch (_: Throwable) {
        }
    }

    private fun resetFile(path: Path): Boolean {
        return try {
            val obj = readObject(path) ?: return false

            var changed = false
            for (key in POSITION_KEYS) if (obj.remove(key) != null) changed = true
            if (!isLocked(obj)) {
                obj.addProperty("locked", true)
                changed = true
            }
            if (!changed) return false

            writeObject(path, obj)
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
