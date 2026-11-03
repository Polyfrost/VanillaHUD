package org.polyfrost.vanillahud.test

//? if > 1.8.9
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.polyfrost.oneconfig.api.config.v1.ConfigManager
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.oneconfig.internal.ui.hud.resetAllHudProperties
import org.polyfrost.oneconfig.internal.ui.hud.resetHudPosition
import org.polyfrost.vanillahud.hud.AirHud
import org.polyfrost.vanillahud.hud.ArmorHud
import org.polyfrost.vanillahud.hud.ExperienceBarHud
import org.polyfrost.vanillahud.hud.ExperienceLevelHud
import org.polyfrost.vanillahud.hud.HealthHud
import org.polyfrost.vanillahud.hud.HeldItemTooltipHud
import org.polyfrost.vanillahud.hud.HotbarHud
import org.polyfrost.vanillahud.hud.HungerHud
import org.polyfrost.vanillahud.hud.StatRows
import org.polyfrost.vanillahud.hud.VanillaHud
import org.polyfrost.vanillahud.render.HudTransform
import org.polyfrost.vanillahud.util.ForceDefaultPosition
import java.nio.file.Files

class HudResizeTest {
    companion object {
        private val SCREENS = listOf(320 to 240, 427 to 240, 504 to 317, 640 to 360, 641 to 361, 960 to 540, 1920 to 1080)

        @JvmStatic
        @BeforeAll
        fun boot() {
            //? if > 1.8.9 {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
            //?} else
            //LegacyBootstrap.once()
            TitlePositionTest.clearStoredTrees()
            ForceDefaultPosition.invalidate()
        }
    }

    private val made = ArrayList<VanillaHud>()

    @AfterEach
    fun release() {
        HudManager.activeInstances.removeAll(made)
        made.clear()
        StatRows.healthRowsOverride = null
        StatRows.mountRowsOverride = null
        HudTransform.resetFrame()
        Files.deleteIfExists(ConfigManager.active().folder.resolve("vanillahud-customized"))
        ForceDefaultPosition.invalidate()
    }

    private fun screen(w: Int, h: Int) {
        HudManager.guiScreenWidth = w.toFloat()
        HudManager.guiScreenHeight = h.toFloat()
        VanillaHud.refreshAll()
    }

    private fun <T : VanillaHud> fresh(hud: T): T {
        HudManager.activeInstances.removeAll { it::class == hud::class }
        ConfigManager.active().delete("huds/${hud.id}")
        @Suppress("UNCHECKED_CAST")
        val instance = hud.make() as T
        instance.useGuiScale = true
        instance.customScale = 1f
        HudManager.activeInstances.add(instance)
        made.add(instance)
        HudTransform.resetFrame()
        return instance
    }

    private fun settled(hud: VanillaHud) {
        hud.queueForceDefault()
        VanillaHud.refreshAll()
    }

    private fun assertAt(hud: VanillaHud, x: Float, y: Float, w: Int, h: Int) {
        assertEquals(x, hud.x, 0.01f, "${hud.title} x on a ${w}x$h screen")
        assertEquals(y, hud.y, 0.01f, "${hud.title} y on a ${w}x$h screen")
    }

    private class Cluster {
        val hotbar = HotbarHud()
        val health = HealthHud()
        val armor = ArmorHud()
        val hunger = HungerHud()
        val air = AirHud()
        val xp = ExperienceBarHud()
        val level = ExperienceLevelHud()
    }

    private fun cluster(): List<VanillaHud> {
        val c = Cluster()
        return listOf(fresh(c.hotbar), fresh(c.health), fresh(c.armor), fresh(c.hunger), fresh(c.air), fresh(c.xp), fresh(c.level))
    }

    /** what drawing a frame does to a hud whose content changed size */
    private fun measured(huds: List<VanillaHud>) {
        screen(640, 360)
        huds.forEach { it.reseedDefaultForScreen() }
    }

    private fun vanillaLayout(w: Int, h: Int): List<Pair<Float, Float>> {
        val cx = w / 2f
        return listOf(
            cx - 91f to h - 22f,
            cx - 91f to h - 39f,
            cx - 91f to h - 49f,
            cx + 10f to h - 39f,
            cx + 10f to h - 49f,
            cx - 91f to h - 29f,
            cx - 9f to h - 35f,
        )
    }

    @Test
    fun `the hotbar cluster sits where vanilla draws it on every screen and stays there through resizes`() {
        screen(640, 360)
        val huds = cluster()
        huds.forEach(::settled)
        repeat(3) {
            for ((w, h) in SCREENS + SCREENS.reversed()) {
                screen(w, h)
                huds.zip(vanillaLayout(w, h)).forEach { (hud, at) -> assertAt(hud, at.first, at.second, w, h) }
            }
        }
    }

    @Test
    fun `the status bars hang off the bar below them by default and follow the hotbar when it moves`() {
        screen(640, 360)
        val huds = cluster()
        huds.forEach(::settled)
        val hotbar = huds[0]
        val health = huds[1]
        val armor = huds[2]
        val hunger = huds[3]
        val air = huds[4]
        val xp = huds[5]
        val level = huds[6]
        val parents = listOf(health to xp, armor to health, hunger to xp, air to hunger, xp to hotbar, level to xp)
        for ((bar, parent) in parents) {
            assertSame(parent, bar.anchorParent, "${bar.title} should be anchored to ${parent.title}")
            assertTrue(bar.onDefaultAnchor, "${bar.title} should still count as unmoved")
        }

        hotbar.cancelForceDefault()
        hotbar.movedByUser = true
        hotbar.x = 20f
        hotbar.y = 50f
        val expected = vanillaLayout(640, 360)
        for ((i, bar) in huds.withIndex().drop(1)) {
            assertEquals(expected[i].first - expected[0].first + 20f, bar.x, 0.01f, "${bar.title} x")
            assertEquals(expected[i].second - expected[0].second + 50f, bar.y, 0.01f, "${bar.title} y")
        }
    }

    @Test
    fun `extra heart rows grow the health box and leave the armor hanging off it`() {
        screen(640, 360)
        val huds = cluster()
        huds.forEach(::settled)
        val health = huds[1]
        val armor = huds[2]
        val gap = health.y - armor.y
        val bottom = health.y + health.height

        StatRows.healthRowsOverride = 3
        measured(huds)
        // three rows of 9 apart, so two more than the first
        assertEquals(9f + 2 * 9f, health.height, 0.01f, "health height")
        assertEquals(bottom, health.y + health.height, 0.01f, "health keeps its bottom")
        assertEquals(gap, health.y - armor.y, 0.01f, "armor keeps hanging off the top of health")
        assertSame(health, armor.anchorParent)

        StatRows.healthRowsOverride = 1
        measured(huds)
        assertEquals(9f, health.height, 0.01f)
        assertEquals(gap, health.y - armor.y, 0.01f)
    }

    @Test
    fun `a mount's hearts grow the hunger box and leave the air hanging off it`() {
        screen(640, 360)
        val huds = cluster()
        huds.forEach(::settled)
        val hunger = huds[3]
        val air = huds[4]
        val gap = hunger.y - air.y
        val bottom = hunger.y + hunger.height

        StatRows.mountRowsOverride = 2
        measured(huds)
        assertEquals(19f, hunger.height, 0.01f, "hunger height")
        assertEquals(bottom, hunger.y + hunger.height, 0.01f, "hunger keeps its bottom")
        assertEquals(gap, hunger.y - air.y, 0.01f, "air keeps hanging off the top of hunger")
        // the game draws the mount's top row 10 up and the bubbles above it, and the box starts where the content does
        assertEquals(360f - 39f - 10f, hunger.vanillaOriginY(640, 360), 0.01f, "hunger content origin")
        assertEquals(360f - 49f - 10f, air.vanillaOriginY(640, 360), 0.01f, "air content origin")
        assertEquals(hunger.vanillaOriginY(640, 360), hunger.y, 0.01f, "hunger box starts at its content")
    }

    @Test
    fun `vanilla's row counts`() {
        assertEquals(1, StatRows.healthRows(20f, 20f, 0f))
        assertEquals(2, StatRows.healthRows(20f, 20f, 1f))
        assertEquals(2, StatRows.healthRows(20f, 20f, 20f))
        assertEquals(3, StatRows.healthRows(40f, 40f, 1f))
        assertEquals(10, StatRows.healthRowHeight(2))
        assertEquals(9, StatRows.healthRowHeight(3))
        assertEquals(3, StatRows.healthRowHeight(20))
        assertEquals(1, StatRows.mountRows(10))
        assertEquals(2, StatRows.mountRows(11))
        assertEquals(3, StatRows.mountRows(99))
    }

    @Test
    fun `the level always paints after the experience bar`() {
        screen(640, 360)
        val level = fresh(ExperienceLevelHud())
        val bar = fresh(ExperienceBarHud())
        val huds = HudManager.activeInstances
        assertTrue(huds.indexOf(level) < huds.indexOf(bar), "this order is the case under test")
        VanillaHud.keepLevelAboveBar()
        assertTrue(huds.indexOf(level) > huds.indexOf(bar))
    }

    private fun assertBackOnHotbar(hotbar: VanillaHud, bar: VanillaHud, parent: VanillaHud) {
        ForceDefaultPosition.tick()
        assertSame(parent, bar.anchorParent, "${bar.title} should be anchored to ${parent.title} again")
        assertEquals(hotbar.x, bar.x, 0.01f, "${bar.title} x")
        assertEquals(hotbar.y - 17f, bar.y, 0.01f, "${bar.title} y")
    }

    @Test
    fun `resetting a status bar puts it back on the hotbar`() {
        screen(640, 360)
        val huds = cluster()
        huds.forEach(::settled)
        val hotbar = huds[0]
        val health = huds[1]
        val xp = huds[5]

        health.movedByUser = true
        health.x = 12f
        health.y = 34f
        resetHudPosition(health)
        assertBackOnHotbar(hotbar, health, xp)

        health.clearAnchor()
        health.x = 12f
        health.y = 34f
        resetHudPosition(health)
        assertBackOnHotbar(hotbar, health, xp)

        resetAllHudProperties(health)
        assertBackOnHotbar(hotbar, health, xp)

        hotbar.movedByUser = true
        hotbar.x = 20f
        hotbar.y = 50f
        health.x = 12f
        resetHudPosition(health)
        assertBackOnHotbar(hotbar, health, xp)
    }

    @Test
    fun `a status bar the user placed elsewhere stays there`() {
        screen(640, 360)
        val huds = cluster()
        huds.forEach(::settled)
        val health = huds[1]
        health.movedByUser = true
        health.clearAnchor()
        health.x = 12f
        health.y = 34f
        ForceDefaultPosition.tick()
        assertEquals(null, health.anchorParent)
        assertEquals(12f, health.x, 0.5f)
    }

    @Test
    fun `an element the user placed keeps its place against its section through resizes`() {
        screen(640, 360)
        val hud = fresh(HeldItemTooltipHud())
        settled(hud)
        hud.cancelForceDefault()
        hud.movedByUser = true
        hud.x = 600f
        hud.y = 340f
        val fromCentre = hud.x - 320f
        val fromBottom = 360f - hud.y
        repeat(3) {
            for ((w, h) in SCREENS + SCREENS.reversed()) {
                screen(w, h)
                if (w < 640 || h < 360) continue
                assertEquals(w / 2f + fromCentre, hud.x, 1f, "x on a ${w}x$h screen")
                assertEquals(h - fromBottom, hud.y, 1f, "y on a ${w}x$h screen")
            }
        }
        screen(640, 360)
        assertEquals(600f, hud.x, 0.5f, "a resize round trip must not drift")
        assertEquals(340f, hud.y, 0.5f, "a resize round trip must not drift")
    }
}
