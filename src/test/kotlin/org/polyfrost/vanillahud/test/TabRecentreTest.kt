package org.polyfrost.vanillahud.test

//? if > 1.8.9
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.polyfrost.oneconfig.api.config.v1.ConfigManager
import org.polyfrost.oneconfig.api.hud.v1.Hud
import org.polyfrost.oneconfig.api.hud.v1.HudAnchor
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.oneconfig.api.hud.v1.Section
import org.polyfrost.vanillahud.hud.VanillaHud
import org.polyfrost.vanillahud.util.HudConfigMigrator
import java.nio.file.Files
import java.nio.file.Path

class TabRecentreTest {
    private class ProbeTab : VanillaHud("vanillahud-tab.json", "Probe Tab", Hud.Category.INFO) {
        override val naturalWidth get() = 200f
        override val naturalHeight get() = 50f
        override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - width / 2f
        override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = 40f
        override val anchorX get() = 0.5f
    }

    private fun tab(
        locked: Boolean = true,
        relativeY: Float = 10f,
        section: String = "TopCenter",
        growthAnchor: String = "Auto",
        anchorTargetId: String = "",
        wrapped: Boolean = true,
    ): String {
        val sectionJson = if (wrapped) """{ "class": "org.polyfrost.oneconfig.api.hud.v1.Section", "value": "$section" }""" else "\"$section\""
        val anchorJson = if (wrapped) """{ "class": "org.polyfrost.oneconfig.api.hud.v1.HudAnchor", "value": "$growthAnchor" }""" else "\"$growthAnchor\""
        return """
            {
                "locked": $locked,
                "relativeX": 85.0,
                "relativeY": $relativeY,
                "section": $sectionJson,
                "growthAnchor": $anchorJson,
                "anchorTargetId": "$anchorTargetId"
            }
        """.trimIndent()
    }

    private fun repaired(json: String): JsonObject {
        val folder = Files.createTempDirectory("vanillahud-tab")
        val file = folder.resolve("huds").resolve("vanillahud-tab.json")
        Files.createDirectories(file.parent)
        Files.writeString(file, json)
        HudConfigMigrator.recentreTabList(folder)
        return read(file)
    }

    private fun read(file: Path): JsonObject = JsonParser.parseString(Files.readString(file)).asJsonObject

    private fun keptX(json: String) = assertEquals(85f, repaired(json).get("relativeX").asFloat)

    @Test
    fun `a locked tab list at its default height drops the baked in x offset`() {
        val obj = repaired(tab())
        assertFalse(obj.has("relativeX"))
        assertEquals(10f, obj.get("relativeY").asFloat)
    }

    @Test
    fun `enums stored as plain strings are read too`() {
        assertFalse(repaired(tab(wrapped = false)).has("relativeX"))
    }

    @Test
    fun `an unlocked tab list keeps its x`() = keptX(tab(locked = false))

    @Test
    fun `a tab list moved off its default height keeps its x`() = keptX(tab(relativeY = 40f))

    @Test
    fun `a tab list in another section keeps its x`() = keptX(tab(section = "TopLeft"))

    @Test
    fun `a tab list on a growth edge the user picked keeps its x`() = keptX(tab(growthAnchor = "Left"))

    @Test
    fun `a tab list anchored to another hud keeps its x`() = keptX(tab(anchorTargetId = "vanillahud-scoreboard.json"))

    @Test
    fun `oneconfig loads the dropped x as dead centre`() {
        //? if > 1.8.9 {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
        //?} else
        //LegacyBootstrap.once()
        HudManager.guiScreenWidth = 504f
        HudManager.guiScreenHeight = 317f
        val folder = ConfigManager.active().folder
        val file = folder.resolve("huds").resolve("vanillahud-tab.json")
        try {
            Files.createDirectories(file.parent)
            Files.writeString(file, tab())
            HudConfigMigrator.recentreTabList(folder)

            val hud = ProbeTab().make()

            assertEquals(10f, hud.relativeY, "the stored height was read")
            assertEquals(Section.TopCenter, hud.section)
            assertEquals(HudAnchor.Auto, hud.growthAnchor)
            assertEquals(0f, hud.relativeX, "a missing relativeX is 0 which is the centre of TopCenter")
        } finally {
            Files.deleteIfExists(file)
        }
    }
}
