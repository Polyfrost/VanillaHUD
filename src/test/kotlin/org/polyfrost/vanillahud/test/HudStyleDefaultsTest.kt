package org.polyfrost.vanillahud.test

//? if > 1.8.9
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import com.google.gson.JsonParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.polyfrost.oneconfig.api.config.v1.ConfigManager
import org.polyfrost.oneconfig.api.hud.v1.Font
import org.polyfrost.vanillahud.hud.ActionBarHud
import org.polyfrost.vanillahud.hud.BossBarHud
import org.polyfrost.vanillahud.hud.HealthHud
import org.polyfrost.vanillahud.hud.HeldItemTooltipHud
import org.polyfrost.vanillahud.hud.HotbarHud
import org.polyfrost.vanillahud.hud.ScoreboardHud
import org.polyfrost.vanillahud.hud.SubtitleHud
import org.polyfrost.vanillahud.hud.TitleHud
import org.polyfrost.vanillahud.hud.VanillaHud
import org.polyfrost.vanillahud.render.fontColor
import org.polyfrost.vanillahud.util.HudConfigMigrator
import java.nio.file.Files

class HudStyleDefaultsTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun boot() {
            //? if > 1.8.9 {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
            //?} else
            //LegacyBootstrap.once()
        }
    }

    private fun assertVanillaBox(hud: VanillaHud) {
        assertEquals(0f, hud.bgRadius, "${hud.title} radius")
        assertEquals(Font.Minecraft, hud.font, "${hud.title} font")
        assertEquals(-1, hud.textColor, "${hud.title} text colour")
        assertFalse(hud.staticWidth, "${hud.title} static size")
        assertEquals(hud.width, hud.boxWidth, "${hud.title} box width")
        assertEquals(hud.height, hud.boxHeight, "${hud.title} box height")
        assertEquals(0f, hud.contentOffsetX, "${hud.title} content x")
        assertEquals(0f, hud.contentOffsetY, "${hud.title} content y")
    }

    @Test
    fun `elements vanilla draws bare have no background and keep their box`() {
        for (hud in listOf(HotbarHud(), HealthHud(), BossBarHud())) {
            assertFalse(hud.showBackground, "${hud.title} background")
            assertVanillaBox(hud)
        }
    }

    @Test
    fun `text boxes hug the letters and leave the shadow to the right and bottom padding`() {
        for ((hud, shadow) in listOf(ActionBarHud() to 1f, HeldItemTooltipHud() to 1f, TitleHud() to 4f, SubtitleHud() to 2f)) {
            assertFalse(hud.showBackground, "${hud.title} background")
            assertEquals(7f * hud.innerScale, hud.height, "${hud.title} height stops at the baseline")
            assertEquals(hud.width + shadow, hud.boxWidth, "${hud.title} box width")
            assertEquals(hud.height + shadow, hud.boxHeight, "${hud.title} box height")
            assertEquals(0f, hud.contentOffsetX, "${hud.title} content x")
            assertEquals(0f, hud.contentOffsetY, "${hud.title} content y")
        }
    }

    @Test
    fun `vanilla's own backgrounds, shadows and title sizes are the defaults`() {
        val board = ScoreboardHud()
        assertEquals(0x4C000000, board.bodyBgColor)
        assertFalse(board.textShadow)
        assertVanillaBox(board)
        assertEquals(true, ActionBarHud().textShadow)
        assertEquals(true, HotbarHud().showShadow)
        assertEquals(4f, TitleHud().titleScale)
        assertEquals(2f, SubtitleHud().subtitleScale)
    }

    @Test
    fun `the designer's size scales the whole box, except a title which already sizes itself`() {
        val hud = ActionBarHud()
        val natural = hud.height
        hud.padTop = 2f
        hud.padBottom = 3f
        assertEquals(natural + 5f, hud.boxHeight, "padding counts without a background too")
        assertEquals(2f, hud.contentOffsetY)
        hud.textScale = 2f
        assertEquals((natural + 5f) * 2f, hud.boxHeight)
        assertEquals(4f, hud.contentOffsetY)

        val title = TitleHud()
        val before = title.elementScale
        title.textScale = 6f
        assertEquals(before, title.elementScale)
    }

    @Test
    fun `text keeps the opacity the game's font would give it`() {
        assertEquals(0xFFFFFFFF.toInt(), fontColor(0x00FFFFFF, blending = true), "the 1.8.9 boss bar passes no alpha")
        assertEquals(0xFFFFFFFF.toInt(), fontColor(0x20FFFFFF, blending = false), "the 1.8.9 scoreboard draws with blending off")
        assertEquals(0xFFFFFFFF.toInt(), fontColor(0x20FFFFFF, blending = true), "nor does the editor's preview change that")
        assertEquals(0x80FFFFFF.toInt(), fontColor(0x80FFFFFF.toInt(), blending = true), "a fade keeps its alpha")
    }

    @Test
    fun `stored options move onto the designer's keys and its stored defaults give way to vanilla's`() {
        val folder = ConfigManager.active().folder
        val huds = folder.resolve("huds")
        Files.createDirectories(huds)
        val stamp = folder.resolve("vanillahud-migration")
        val stampWas = if (Files.exists(stamp)) Files.readString(stamp) else null
        val board = huds.resolve("vanillahud-scoreboard.json")
        val bar = huds.resolve("vanillahud-actionbar.json")
        val hotbar = huds.resolve("vanillahud-hotbar.json")
        try {
            Files.writeString(stamp, "8")
            Files.writeString(board, """{"showBackground":true,"bgRadius":4.0,"rounded":true,"cornerRadius":3.0,"textType":1,"backgroundColor":{"value":[1,2,3,64],"class":"org.polyfrost.compose.render.PolyColor"},"font":{"class":"org.polyfrost.oneconfig.api.hud.v1.Font","value":"Poppins"}}""")
            Files.writeString(bar, """{"showBackground":true,"bgRadius":4.0,"background":false,"paddingX":5.0}""")
            Files.writeString(hotbar, """{"id":"vanillahud-hotbar.json","showBackground":true,"bgRadius":4.0,"style":1}""")

            HudConfigMigrator.migrate()

            val b = JsonParser.parseString(Files.readString(board)).asJsonObject
            assertEquals(0x40010203, b.get("bgColor").asInt)
            assertEquals(3f, b.get("bgRadius").asFloat)
            assertEquals(true, b.get("showShadow").asBoolean)
            assertEquals("Minecraft", b.getAsJsonObject("font").get("value").asString)
            assertFalse(b.has("backgroundColor"))

            val a = JsonParser.parseString(Files.readString(bar)).asJsonObject
            assertEquals(false, a.get("showBackground").asBoolean)
            assertEquals(0f, a.get("bgRadius").asFloat)
            assertEquals(0f, a.get("padLeft").asFloat, "no background to pad, so it must not move the HUD")

            val h = JsonParser.parseString(Files.readString(hotbar)).asJsonObject
            assertEquals(false, h.get("showBackground").asBoolean)
            assertFalse(h.has("style"))
        } finally {
            for (p in listOf(board, bar, hotbar)) Files.deleteIfExists(p)
            if (stampWas != null) Files.writeString(stamp, stampWas) else Files.deleteIfExists(stamp)
        }
    }
}
