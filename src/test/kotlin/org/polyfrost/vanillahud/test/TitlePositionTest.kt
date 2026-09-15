package org.polyfrost.vanillahud.test

import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.polyfrost.oneconfig.api.config.v1.Config
import org.polyfrost.oneconfig.api.config.v1.ConfigManager
import org.polyfrost.oneconfig.api.hud.v1.Hud
import org.polyfrost.oneconfig.api.hud.v1.HudAnchor
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.oneconfig.api.hud.v1.Section
import org.polyfrost.vanillahud.hud.ActionBarHud
import org.polyfrost.vanillahud.hud.HudInternals
import org.polyfrost.vanillahud.hud.ScoreboardHud
import org.polyfrost.vanillahud.hud.TitleHud
import org.polyfrost.vanillahud.hud.VanillaHud
import org.polyfrost.vanillahud.util.ForceDefaultPosition
import org.polyfrost.vanillahud.util.HudConfigMigrator
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions
import kotlin.math.abs

class TitlePositionTest {

    companion object {
        private const val LAPTOP_W = 504
        private const val LAPTOP_H = 317

        private const val MONITOR_W = 640
        private const val MONITOR_H = 360

        private const val WIDE_W = 900
        private const val WIDE_H = 506

        @JvmStatic
        @BeforeAll
        fun boot() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
            clearStoredTrees()
            HudConfigMigrator.migrate()
            clearStoredTrees()
            Files.deleteIfExists(ConfigManager.active().folder.resolve("vanillahud-customized"))
            ForceDefaultPosition.invalidate()
            probePermissions()
        }

        private var permissionsNotEnforced: String? = null

        @JvmStatic
        private fun probePermissions() {
            val probe = ConfigManager.active().folder.resolve("vanillahud-permission-probe")
            permissionsNotEnforced = try {
                Files.createDirectories(probe.parent)
                Files.write(probe, listOf("probe"))
                Files.setPosixFilePermissions(probe, PosixFilePermissions.fromString("-w--w----"))
                if (runCatching { Files.readAllLines(probe) }.isSuccess) {
                    "a chmod here does not deny a read, which it does not as root"
                } else {
                    Files.setPosixFilePermissions(probe, PosixFilePermissions.fromString("r--r-----"))
                    if (runCatching { Files.write(probe, listOf("probe")) }.isSuccess) {
                        "a chmod here does not deny a write"
                    } else {
                        null
                    }
                }
            } catch (t: Throwable) {
                "a chmod here is not supported at all: $t"
            } finally {
                runCatching { Files.setPosixFilePermissions(probe, PosixFilePermissions.fromString("rw-------")) }
                runCatching { Files.deleteIfExists(probe) }
            }
        }

        private fun requirePermissionEnforcement() {
            val reason = permissionsNotEnforced
            assertNull(reason, "this scenario cannot drive an unreadable file: $reason")
        }

        @JvmStatic
        fun clearStoredTrees() {
            val huds = ConfigManager.active().folder.resolve("huds")
            if (Files.isDirectory(huds)) Files.newDirectoryStream(huds, "vanillahud-*.json").use { dir ->
                dir.forEach { Files.deleteIfExists(it) }
            }
        }
    }

    @AfterEach
    fun releaseClock() {
        ForceDefaultPosition.nanos = System::nanoTime
    }

    private fun makeProfile(name: String) {
        runCatching { ConfigManager.deleteProfile(name) }
        ConfigManager.createProfile(name)
    }

    private fun screen(w: Int, h: Int) {
        HudManager.guiScreenWidth = w.toFloat()
        HudManager.guiScreenHeight = h.toFloat()
    }

    private fun vanillaY(h: Int) = h / 2f - 40f

    private fun <T : Hud> fresh(hud: T): T {
        HudManager.activeInstances.removeAll { it::class == hud::class }
        ConfigManager.active().delete("huds/${hud.id}")
        @Suppress("UNCHECKED_CAST")
        val made = hud.make() as T
        made.locked = true
        made.useGuiScale = true
        made.customScale = 1f
        HudManager.activeInstances.add(made)
        return made
    }

    @Test
    fun `the title follows the screen it is drawn on`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(TitleHud())

        hud.section = Section.TopCenter
        hud.growthAnchor = HudAnchor.Auto
        hud.x = LAPTOP_W / 2f - hud.width / 2f
        hud.y = vanillaY(LAPTOP_H)

        VanillaHud.refreshAll()
        assertEquals(Section.Center, hud.section, "the title should store against the screen centre")
        assertEquals(HudAnchor.Top, hud.growthAnchor, "but still grow from its top edge")
        assertEquals(vanillaY(LAPTOP_H), hud.y, 1.5f, "converting the section must not move the title")

        hud.queueForceDefault()
        VanillaHud.refreshAll()
        assertEquals(vanillaY(LAPTOP_H), hud.y, 0.5f)
        assertEquals(LAPTOP_W / 2f, hud.x + hud.width / 2f, 0.5f)

        screen(MONITOR_W, MONITOR_H)
        VanillaHud.refreshAll()
        assertEquals(vanillaY(MONITOR_H), hud.y, 0.5f, "the title should re-centre on the bigger screen")

        repeat(5) {
            screen(LAPTOP_W, LAPTOP_H)
            VanillaHud.refreshAll()
            assertEquals(vanillaY(LAPTOP_H), hud.y, 0.5f)
            screen(MONITOR_W, MONITOR_H)
            VanillaHud.refreshAll()
            assertEquals(vanillaY(MONITOR_H), hud.y, 0.5f)
        }

        for (h in listOf(240, 317, 361, 540, 1080)) {
            screen(MONITOR_W, h)
            VanillaHud.refreshAll()
            assertEquals(vanillaY(h), hud.y, 0.5f, "wrong origin at a screen height of $h")
        }

        screen(LAPTOP_W, LAPTOP_H)
        VanillaHud.refreshAll()
        hud.cancelForceDefault()
        hud.y = 12f
        screen(MONITOR_W, MONITOR_H)
        VanillaHud.refreshAll()
        assertTrue(abs(hud.y - vanillaY(MONITOR_H)) > 1f, "a moved title must not be re-centred")
    }

    @Test
    fun `converting a section re-states the position rather than moving the title`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(TitleHud())
        hud.section = Section.TopCenter
        hud.growthAnchor = HudAnchor.Auto
        hud.x = LAPTOP_W / 2f - hud.width / 2f
        hud.y = 12f
        hud.cancelForceDefault()

        VanillaHud.refreshAll()
        assertEquals(12f, hud.y, 0.5f, "the conversion must never move a hud the user placed")
    }

    @Test
    fun `a hud already in its section but still on the auto growth edge is converted`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(AutoAnchorHud())
        hud.cancelForceDefault()
        hud.pinToVanillaOrigin(LAPTOP_W, LAPTOP_H)
        hud.section = Section.Center
        hud.growthAnchor = HudAnchor.Auto
        hud.y = vanillaY(LAPTOP_H)
        hud.x = LAPTOP_W / 2f - hud.width / 2f

        VanillaHud.refreshAll()

        assertEquals(HudAnchor.Top, hud.growthAnchor, "the edge the offsets beside it have to be measured from")
        assertEquals(vanillaY(LAPTOP_H), hud.y, 1.5f, "and re-stating them must not move the hud")
        assertTrue(hud.anchorsToVanillaOrigin(), "a hud that never moved has to still read as being on its default")
    }

    private class AutoAnchorHud : CentredHud("vanillahud-auto-anchor.json", "Auto Anchor")

    @Test
    fun `edge anchored elements are left alone`() {
        screen(LAPTOP_W, LAPTOP_H)
        val bar = fresh(ActionBarHud())
        val board = fresh(ScoreboardHud())
        bar.queueForceDefault()
        board.queueForceDefault()
        VanillaHud.refreshAll()

        assertEquals(HudAnchor.Auto, bar.growthAnchor)
        assertEquals(HudAnchor.Auto, board.growthAnchor)
        assertEquals(Section.BottomCenter, bar.section)
        assertEquals(Section.CenterRight, board.section)

        screen(MONITOR_W, MONITOR_H)
        VanillaHud.refreshAll()
        assertEquals(bar.vanillaOriginY(MONITOR_W, MONITOR_H), bar.y, 0.5f)
    }

    @Test
    fun `a growth anchor the user picked survives a relaunch`() {
        screen(LAPTOP_W, LAPTOP_H)
        val bar = fresh(ActionBarHud())
        bar.queueForceDefault()
        VanillaHud.refreshAll()

        val again = fresh(ActionBarHud())
        again.cancelForceDefault()
        again.setGrowthAnchorKeepingPosition(HudAnchor.Top)
        val y = again.y

        VanillaHud.refreshAll()
        assertEquals(HudAnchor.Top, again.growthAnchor, "the conversion must not take the anchor back")
        assertEquals(y, again.y, 0.5f)
    }

    @Test
    fun `an unlocked hud keeps the section the user dropped it in`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(StrayHud())
        hud.locked = false
        hud.cancelForceDefault()
        hud.section = Section.BottomRight

        VanillaHud.refreshAll()
        assertEquals(Section.BottomRight, hud.section, "converting a hud the user placed would move it later")

        hud.locked = true
        VanillaHud.refreshAll()
        assertEquals(Section.BottomRight, hud.section, "locking it again must not revive the conversion")
    }

    @Test
    fun `reset restores the title section and its matching growth edge together`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(ResetTitleHud())
        hud.queueForceDefault()
        VanillaHud.refreshAll()

        assertEquals(Section.Center, hud.tree!!.getProp("section")!!.getMetadata<Section>("default"))
        assertEquals(HudAnchor.Top, hud.tree!!.getProp("growthAnchor")!!.getMetadata<HudAnchor>("default"))

        hud.section = Section.BottomRight
        hud.relativeY = 0f
        hud.growthAnchor = HudAnchor.Bottom
        Config.restoreCapturedDefaults(hud.tree!!)

        assertEquals(Section.Center, hud.section)
        assertEquals(HudAnchor.Top, hud.growthAnchor)
        assertEquals(vanillaY(LAPTOP_H), hud.y, 0.5f, "reset must not shift the title by half its height")
    }

    @Test
    fun `a hud still stored in grid units is left for oneconfig to migrate`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(SchemaHud())
        hud.cancelForceDefault()
        hud.section = Section.BottomRight
        hud.relativeX = 1f
        hud.relativeY = 1f
        hud.posSchema = 0

        VanillaHud.refreshAll()

        assertEquals(Section.BottomRight, hud.section, "the conversion must wait for the migration")
        assertEquals(1f, hud.relativeX, 0f)
        assertEquals(1f, hud.relativeY, 0f)
        assertEquals(0, hud.posSchema, "oneconfig must still see the hud as unconverted")
    }

    @Test
    fun `a centred hud in grid units keeps its growth edge until oneconfig has migrated it`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(SchemaGrowthHud())
        hud.cancelForceDefault()
        hud.section = Section.BottomRight
        hud.relativeX = 1f
        hud.relativeY = 1f
        hud.posSchema = 0

        VanillaHud.refreshAll()

        assertEquals(0, hud.posSchema, "oneconfig must still see the hud as unconverted")
        assertEquals(HudAnchor.Auto, hud.growthAnchor, "the growth edge has to wait for the migration too")
        assertEquals(Section.BottomRight, hud.section, "and so does the section")
        assertEquals(1f, hud.relativeX, 0f)
        assertEquals(1f, hud.relativeY, 0f)
    }

    private class SchemaGrowthHud : CentredHud("vanillahud-schema-growth.json", "Schema Growth")

    @Test
    fun `a position from a newer oneconfig is left entirely untouched`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(FutureSchemaHud())
        hud.cancelForceDefault()
        hud.section = Section.BottomRight
        hud.growthAnchor = HudAnchor.BottomRight
        hud.relativeX = 13f
        hud.relativeY = 17f
        hud.posSchema = 99

        VanillaHud.refreshAll()
        hud.pinToVanillaOrigin(LAPTOP_W, LAPTOP_H)
        hud.queueForceDefault()
        VanillaHud.refreshAll()

        assertEquals(99, hud.posSchema)
        assertEquals(Section.BottomRight, hud.section, "an unknown schema must not be converted")
        assertEquals(HudAnchor.BottomRight, hud.growthAnchor)
        assertEquals(13f, hud.relativeX, 0f, "an unknown X unit must not be rewritten")
        assertEquals(17f, hud.relativeY, 0f, "an unknown Y unit must not be rewritten")
    }

    @Test
    fun `a hud on a schema this build cannot read is not drawn on the vanilla origin`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(FutureDrawHud())
        hud.queueForceDefault()
        VanillaHud.refreshAll()
        assertTrue(hud.anchorsToVanillaOrigin(), "a hud on its default is drawn on the vanilla origin")

        hud.posSchema = 99

        assertFalse(
            hud.anchorsToVanillaOrigin(),
            "one stored in units this build cannot read has to stay where oneconfig lays it out",
        )
    }

    private class FutureDrawHud : CornerHud("vanillahud-future-draw.json", "Future Draw")

    @Test
    fun `the draw path never renumbers a position schema of its own accord`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(SchemaDrawHud())
        hud.queueForceDefault()
        VanillaHud.refreshAll()

        hud.posSchema = 0
        screen(MONITOR_W, MONITOR_H)
        hud.reseedDefaultForScreen()

        assertEquals(0, hud.posSchema, "the reseed runs outside the frame pass so it has to wait too")
        hud.pinToVanillaOrigin(MONITOR_W, MONITOR_H)
        assertEquals(0, hud.posSchema, "and so does the editor pin")

        hud.posSchema = 99
        hud.relativeX = 20f
        hud.reseedDefaultForScreen()

        assertEquals(99, hud.posSchema, "the default capture must leave a schema it does not know alone")
        assertEquals(20f, hud.relativeX, 0f, "and must leave the position it found alone")

        hud.pinToVanillaOrigin(MONITOR_W, MONITOR_H)
        assertEquals(99, hud.posSchema, "the editor pin must not renumber it either")

        hud.queueForceDefault()
        VanillaHud.refreshAll()
        assertEquals(99, hud.posSchema, "nor may a reset back to the default")
    }

    @Test
    fun `a steady screen costs no measurements`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(CountingHud())
        VanillaHud.refreshAll()
        val settled = hud.measures

        repeat(5) { VanillaHud.refreshAll() }
        assertEquals(settled, hud.measures, "a refresh on an unchanged screen must not measure")

        screen(MONITOR_W, MONITOR_H)
        VanillaHud.refreshAll()
        assertTrue(hud.measures > settled, "a screen change still has to measure")
    }

    private open class CornerHud(id: String, title: String) : VanillaHud(id, title, Hud.Category.INFO) {
        override val naturalWidth get() = 50f
        override val naturalHeight get() = 10f
        override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = 0f
        override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = 0f
    }

    private open class CentredHud(id: String, title: String) : VanillaHud(id, title, Hud.Category.INFO) {
        override val naturalWidth get() = 120f
        override val naturalHeight get() = 68f
        override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - width / 2f
        override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight / 2f - 40f
        override val anchorX get() = 0.5f
        override val anchorY get() = 0.5f
        override val positionAnchorY get() = 0f
        override val sectionAnchorY get() = 0.5f
    }

    private class CountingHud : CornerHud("vanillahud-counting.json", "Counting") {
        var measures = 0
        override fun measuredWidth(): Float {
            measures++
            return naturalWidth
        }
    }

    private class StrayHud : CornerHud("vanillahud-stray.json", "Stray")

    private class ForcedAnchorHud : CornerHud("vanillahud-forced-anchor.json", "Forced Anchor")

    private class SchemaHud : CornerHud("vanillahud-schema.json", "Schema")

    private class SchemaDrawHud : CornerHud("vanillahud-schema-draw.json", "Schema Draw")

    private class FutureSchemaHud : CornerHud("vanillahud-future-schema.json", "Future Schema")

    private class ResetTitleHud : CentredHud("vanillahud-reset-title.json", "Reset Title")

    private class PositionResetHud : CentredHud("vanillahud-position-reset.json", "Position Reset")

    @Test
    fun `capturing a default puts back the growth edge the offsets were stored under`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(GrowthRestoreHud())
        hud.cancelForceDefault()
        hud.pinToVanillaOrigin(LAPTOP_W, LAPTOP_H)
        hud.section = Section.Center
        hud.growthAnchor = HudAnchor.Auto
        hud.relativeY = -6f
        val before = hud.y

        hud.reseedDefaultForScreen()

        assertEquals(HudAnchor.Auto, hud.growthAnchor, "a default capture must leave the edge it found")
        assertEquals(before, hud.y, 0.5f, "a default capture must not move the hud by half its height")
    }

    private class GrowthRestoreHud : CentredHud("vanillahud-growth-restore.json", "Growth Restore")

    @Test
    fun `a growth anchor the user picked survives a screen change`() {
        screen(LAPTOP_W, LAPTOP_H)
        val bar = fresh(ActionBarHud())
        bar.queueForceDefault()
        VanillaHud.refreshAll()

        bar.setGrowthAnchorKeepingPosition(HudAnchor.Top)
        bar.y = bar.y - 37f
        val fromBottom = LAPTOP_H - bar.y

        screen(MONITOR_W, MONITOR_H)
        VanillaHud.refreshAll()
        assertEquals(HudAnchor.Top, bar.growthAnchor, "reseeding must not take the anchor back")
        assertEquals(fromBottom, MONITOR_H - bar.y, 0.5f, "and must not move the hud by its own height")
    }

    @Test
    fun `a growth anchor read back from the config survives on the default position`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(AnchorHud())
        hud.queueForceDefault()
        VanillaHud.refreshAll()
        assertEquals(HudAnchor.Auto, hud.growthAnchor, "nothing about this one needs an anchor of its own")

        hud.growthAnchor = HudAnchor.Top

        screen(MONITOR_W, MONITOR_H)
        VanillaHud.refreshAll()

        assertEquals(HudAnchor.Top, hud.growthAnchor, "a hud left on its default still keeps the user's edge")
        assertEquals(
            hud.vanillaOriginY(MONITOR_W, MONITOR_H), hud.y, 0.5f,
            "and keeping it must not stop the hud following the screen it is drawn on",
        )
    }

    @Test
    fun `resetting a whole hud restores its position under the edge the user picked`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(ResetAllHud())
        hud.queueForceDefault()
        VanillaHud.refreshAll()

        hud.setGrowthAnchorKeepingPosition(HudAnchor.Bottom)
        VanillaHud.refreshAll()

        hud.section = Section.BottomRight
        hud.relativeY += 90f
        Config.restoreCapturedDefaults(hud.tree!!)

        assertEquals(vanillaY(LAPTOP_H), hud.y, 0.5f, "the reset has to put the hud back on its default")
        assertEquals(HudAnchor.Bottom, hud.growthAnchor, "and leaves the edge the offsets were measured from")
    }

    private class ResetAllHud : CentredHud("vanillahud-reset-all.json", "Reset All")

    @Test
    fun `a reset to the default position keeps the growth edge the user picked`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(ForcedAnchorHud())
        hud.queueForceDefault()
        VanillaHud.refreshAll()
        hud.setGrowthAnchorKeepingPosition(HudAnchor.Top)

        hud.queueForceDefault()
        VanillaHud.refreshAll()

        assertEquals(HudAnchor.Top, hud.growthAnchor, "a reset must not drop the edge the user picked")
        assertEquals(0f, hud.x, 0.5f, "and must still land the hud on its default")
        assertEquals(0f, hud.y, 0.5f)
    }

    @Test
    fun `oneconfig's position only reset lands the hud back on its default`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(PositionResetHud())
        hud.queueForceDefault()
        VanillaHud.refreshAll()

        hud.setGrowthAnchorKeepingPosition(HudAnchor.Bottom)
        VanillaHud.refreshAll()

        hud.relativeY += 60f
        val tree = hud.tree!!
        hud.section = tree.getProp("section")!!.getMetadata<Section>("default")!!
        hud.relativeX = tree.getProp("relativeX")!!.getMetadata<Float>("default")!!
        hud.relativeY = tree.getProp("relativeY")!!.getMetadata<Float>("default")!!

        assertEquals(HudAnchor.Bottom, hud.growthAnchor, "the reset leaves the edge alone")
        assertEquals(vanillaY(LAPTOP_H), hud.y, 0.5f, "so the offsets it restores have to match it")
    }

    private class AnchorHud : VanillaHud("vanillahud-anchor.json", "Anchor", Hud.Category.INFO) {
        override val naturalWidth get() = 60f
        override val naturalHeight get() = 11f
        override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - width / 2f
        override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight - 72f
        override val anchorX get() = 0.5f
        override val anchorY get() = 1f
    }

    @Test
    fun `a hud customized in an earlier launch is not converted on the next one`() {
        screen(LAPTOP_W, LAPTOP_H)
        val file = ConfigManager.active().folder.resolve("vanillahud-customized")
        val had = if (Files.isRegularFile(file)) Files.readAllLines(file) else null
        try {
            Files.createDirectories(file.parent)
            Files.write(file, listOf("vanillahud-earlier-launch.json"))
            ForceDefaultPosition.invalidate()

            val hud = fresh(EarlierLaunchHud())
            hud.cancelForceDefault()
            hud.pinToVanillaOrigin(LAPTOP_W, LAPTOP_H)
            hud.section = Section.BottomRight
            hud.relativeX = 12f
            hud.relativeY = 8f

            VanillaHud.refreshAll()

            assertEquals(Section.BottomRight, hud.section, "the section the user dropped it in is theirs to keep")

            val fromRight = LAPTOP_W - hud.x
            val fromBottom = LAPTOP_H - hud.y
            screen(MONITOR_W, MONITOR_H)
            VanillaHud.refreshAll()

            assertEquals(fromRight, MONITOR_W - hud.x, 1.5f, "and converting it would drift the hud they placed")
            assertEquals(fromBottom, MONITOR_H - hud.y, 1.5f)
        } finally {
            if (had != null) Files.write(file, had) else Files.deleteIfExists(file)
            ForceDefaultPosition.invalidate()
        }
    }

    @Test
    fun `capturing a default does not move oneconfig's layout reference`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(LayoutRefHud())
        hud.cancelForceDefault()
        VanillaHud.refreshAll()

        hud.relativeX += 40f
        val refW = HudManager.layoutRefWidth
        val refH = HudManager.layoutRefHeight
        HudManager.layoutRefWidth = 111f
        HudManager.layoutRefHeight = 222f

        try {
            screen(MONITOR_W, MONITOR_H)
            hud.reseedDefaultForScreen()

            assertEquals(111f, HudManager.layoutRefWidth, 0f, "a capture must not restamp the layout reference")
            assertEquals(222f, HudManager.layoutRefHeight, 0f)

            hud.pinToVanillaOrigin(MONITOR_W, MONITOR_H)
            assertEquals(111f, HudManager.layoutRefWidth, 0f, "the editor pin must not restamp it either")
            assertEquals(222f, HudManager.layoutRefHeight, 0f)

            hud.queueForceDefault()
            hud.applyForceDefault()
            assertEquals(111f, HudManager.layoutRefWidth, 0f, "nor may a reset back to the default")
            assertEquals(222f, HudManager.layoutRefHeight, 0f)
        } finally {
            HudManager.layoutRefWidth = refW
            HudManager.layoutRefHeight = refH
        }
    }

    @Test
    fun `converting a section does not move oneconfig's layout reference`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(ConvertRefHud())
        hud.cancelForceDefault()
        hud.section = Section.BottomRight
        hud.relativeX = 40f
        hud.relativeY = 20f

        val refW = HudManager.layoutRefWidth
        val refH = HudManager.layoutRefHeight
        HudManager.layoutRefWidth = 111f
        HudManager.layoutRefHeight = 222f
        try {
            VanillaHud.refreshAll()

            assertEquals(Section.TopLeft, hud.section, "the conversion still has to run")
            assertEquals(111f, HudManager.layoutRefWidth, 0f, "a conversion must not restamp the layout reference")
            assertEquals(222f, HudManager.layoutRefHeight, 0f)
        } finally {
            HudManager.layoutRefWidth = refW
            HudManager.layoutRefHeight = refH
        }
    }

    @Test
    fun `capturing a default leaves the offset of a hud hung off another one alone`() {
        screen(LAPTOP_W, LAPTOP_H)
        val parent = fresh(AnchorParentHud())
        val child = fresh(AnchorChildHud())
        parent.cancelForceDefault()
        child.cancelForceDefault()
        VanillaHud.refreshAll()

        child.anchorTargetId = parent.tree!!.id
        child.anchorOffsetX = 123f
        child.anchorOffsetY = 456f
        child.relativeX += 40f

        try {
            screen(MONITOR_W, MONITOR_H)
            child.reseedDefaultForScreen()

            assertEquals(123f, child.anchorOffsetX, 0f, "the capture has to put the anchor offset back")
            assertEquals(456f, child.anchorOffsetY, 0f)
        } finally {
            unanchor(child)
        }
    }

    private class EarlierLaunchHud : CentredHud("vanillahud-earlier-launch.json", "Earlier Launch")

    private class LayoutRefHud : CornerHud("vanillahud-layout-ref.json", "Layout Ref")

    private class ConvertRefHud : CornerHud("vanillahud-convert-ref.json", "Convert Ref")

    private class AnchorParentHud : VanillaHud("vanillahud-anchor-parent.json", "Anchor Parent", Hud.Category.INFO) {
        override val naturalWidth get() = 40f
        override val naturalHeight get() = 12f
        override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth - 40f
        override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight - 12f
        override val anchorX get() = 1f
        override val anchorY get() = 1f
    }

    private class AnchorChildHud : CornerHud("vanillahud-anchor-child.json", "Anchor Child")

    @Test
    fun `a hud hung off another one follows its parent and keeps the offset the user chose`() {
        screen(LAPTOP_W, LAPTOP_H)
        val parent = fresh(PinParentHud())
        val child = fresh(PinChildHud())
        parent.cancelForceDefault()
        child.cancelForceDefault()
        VanillaHud.refreshAll()
        parent.x = 200f
        parent.y = 100f

        child.anchorTargetId = parent.tree!!.id
        child.anchorOffsetX = 123f
        child.anchorOffsetY = 456f
        try {
            assertFalse(child.anchorsToVanillaOrigin(), "an anchor is a position the user stored")
            assertEquals(323f, child.x, 0.5f, "the anchor is what the hud's own box is laid out against")

            parent.x = 300f
            parent.y = 150f
            assertEquals(423f, child.x, 0.5f, "the child has to follow the parent")
            assertEquals(606f, child.y, 0.5f)

            screen(MONITOR_W, MONITOR_H)
            VanillaHud.refreshAll()
            assertEquals(123f, child.anchorOffsetX, 0.5f, "a screen change must not re-state the offset")
            assertEquals(456f, child.anchorOffsetY, 0.5f)
        } finally {
            unanchor(child)
        }
    }

    private fun unanchor(hud: Hud) {
        hud.anchorTargetId = ""
        hud.anchorOffsetX = 0f
        hud.anchorOffsetY = 0f
    }

    private class PinParentHud : CornerHud("vanillahud-pin-parent.json", "Pin Parent")

    private class PinChildHud : CornerHud("vanillahud-pin-child.json", "Pin Child")

    @Test
    fun `a hud hung off another one is not converted out of the section it is stored in`() {
        screen(LAPTOP_W, LAPTOP_H)
        val parent = fresh(AnchorSectionParentHud())
        val child = fresh(AnchorSectionChildHud())
        parent.cancelForceDefault()
        child.cancelForceDefault()

        child.section = Section.TopCenter
        child.growthAnchor = HudAnchor.Auto
        child.anchorTargetId = parent.tree!!.id
        child.anchorOffsetX = 30f
        child.anchorOffsetY = 60f

        try {
            VanillaHud.refreshAll()

            assertEquals(Section.TopCenter, child.section, "an anchored hud is not the conversion's to move")
            assertEquals(HudAnchor.Auto, child.growthAnchor)
            assertEquals(30f, child.anchorOffsetX, 0.5f, "and its offset from the parent is the user's")
            assertEquals(60f, child.anchorOffsetY, 0.5f)
        } finally {
            unanchor(child)
        }
    }

    private class AnchorSectionParentHud : CornerHud("vanillahud-anchor-section-parent.json", "Anchor Section Parent")

    private class AnchorSectionChildHud : CentredHud("vanillahud-anchor-section-child.json", "Anchor Section Child")

    @Test
    fun `a hud hung off another one is recorded as the user's rather than reset to its default`() {
        screen(LAPTOP_W, LAPTOP_H)
        val file = ConfigManager.active().folder.resolve("vanillahud-customized")
        val had = if (Files.isRegularFile(file)) Files.readAllLines(file) else null
        try {
            Files.deleteIfExists(file)
            ForceDefaultPosition.invalidate()

            val parent = fresh(AnchorResetParentHud())
            val child = fresh(AnchorResetChildHud())
            parent.cancelForceDefault()
            child.cancelForceDefault()
            child.anchorTargetId = parent.tree!!.id
            child.anchorOffsetX = 80f
            child.anchorOffsetY = 40f

            ForceDefaultPosition.tick()
            VanillaHud.refreshAll()

            assertEquals(80f, child.anchorOffsetX, 0.5f, "a reset is not the anchor's to undo")
            assertEquals(40f, child.anchorOffsetY, 0.5f)
            assertTrue(
                Files.readAllLines(file).contains(child.hudId),
                "an anchor is recorded the way a drag is",
            )
        } finally {
            HudManager.activeInstances.filterIsInstance<AnchorResetChildHud>().forEach { unanchor(it) }
            if (had != null) Files.write(file, had) else Files.deleteIfExists(file)
            ForceDefaultPosition.invalidate()
        }
    }

    private class AnchorResetParentHud : CornerHud("vanillahud-anchor-reset-parent.json", "Anchor Reset Parent")

    private class AnchorResetChildHud : CornerHud("vanillahud-anchor-reset-child.json", "Anchor Reset Child")

    @Test
    fun `a reset already handed out is dropped once the user hangs the hud off another one`() {
        screen(LAPTOP_W, LAPTOP_H)
        val parent = fresh(AnchorQueuedParentHud())
        val child = fresh(AnchorQueuedChildHud())
        parent.cancelForceDefault()
        child.cancelForceDefault()
        VanillaHud.refreshAll()
        parent.x = 200f
        parent.y = 100f

        try {
            child.queueForceDefault()
            child.anchorTargetId = parent.tree!!.id
            child.anchorOffsetX = 123f
            child.anchorOffsetY = 456f

            VanillaHud.refreshAll()

            assertEquals(123f, child.anchorOffsetX, 0.5f, "a reset is not the anchor offset's to rewrite")
            assertEquals(456f, child.anchorOffsetY, 0.5f)
        } finally {
            unanchor(child)
        }
    }

    private class AnchorQueuedParentHud : CornerHud("vanillahud-anchor-queued-parent.json", "Anchor Queued Parent")

    private class AnchorQueuedChildHud : CornerHud("vanillahud-anchor-queued-child.json", "Anchor Queued Child")

    @Test
    fun `a pin whose own measure threw does not escape into the draw`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(PinMeasureHud())
        VanillaHud.refreshAll()

        try {
            hud.failMeasure = true
            assertDoesNotThrow { hud.pinToVanillaOrigin(LAPTOP_W, LAPTOP_H) }
        } finally {
            hud.failMeasure = false
        }
    }

    private class PinMeasureHud : ThrowingSeedHud("vanillahud-pin-measure.json", "Pin Measure")

    @Test
    fun `a profile switch drops everything an instance learned from the last one`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(ProfileStateHud())
        hud.cancelForceDefault()
        VanillaHud.refreshAll()

        hud.section = Section.BottomRight
        hud.x = 12f
        hud.y = 8f

        VanillaHud.forgetProfileStateAll()
        VanillaHud.refreshAll()

        assertEquals(Section.TopLeft, hud.section, "the latched conversion has to be retried on the new tree")

        hud.locked = false
        screen(MONITOR_W, MONITOR_H)
        VanillaHud.refreshAll()
        val settled = hud.measures

        VanillaHud.forgetProfileStateAll()
        VanillaHud.refreshAll()

        assertTrue(hud.measures > settled, "and the seed key named a screen the new position was never measured on")
        hud.locked = true

        hud.queueForceDefault()
        VanillaHud.forgetProfileStateAll()
        VanillaHud.refreshAll()

        assertEquals(12f, hud.x, 1.5f, "a reset queued against the old profile is not the new one's to apply")
        assertEquals(8f, hud.y, 1.5f)

        hud.section = Section.BottomRight
        hud.failMeasure = true
        hud.reseedDefaultForScreen()
        hud.failMeasure = false

        VanillaHud.forgetProfileStateAll()
        VanillaHud.refreshAll()

        assertEquals(
            Section.TopLeft, hud.section,
            "a measure the last profile could not take is not one the new one still owes",
        )
    }

    private class ProfileStateHud : CornerHud("vanillahud-profile-state.json", "Profile State") {
        var failMeasure = false
        var measures = 0
        override fun measuredWidth(): Float {
            measures++
            if (failMeasure) throw IllegalStateException("the size is not measurable yet")
            return naturalWidth
        }
    }

    @Test
    fun `a revision bump re-queues the reset it wiped`() {
        screen(LAPTOP_W, LAPTOP_H)
        ForceDefaultPosition.invalidate()
        val hud = fresh(RevisionHud())
        VanillaHud.refreshAll()

        ForceDefaultPosition.tick()

        hud.relativeX += 40f
        hud.relativeY += 20f
        val moved = hud.relativeX to hud.relativeY

        val bump = RevisionBumpHud()
        HudManager.register(bump)
        try {
            VanillaHud.refreshAll()

            assertEquals(moved.first, hud.relativeX, 0.01f, "a reset queued against the last profile is not this one's to apply")
            assertEquals(moved.second, hud.relativeY, 0.01f)

            ForceDefaultPosition.tick()
            VanillaHud.refreshAll()

            assertEquals(0f, hud.x, 0.5f, "a reset wiped by a revision bump has to be queued again")
            assertEquals(0f, hud.y, 0.5f)
        } finally {
            HudManager.unregister(bump)
            VanillaHud.refreshAll()
        }
    }

    private class RevisionHud : CornerHud("vanillahud-revision.json", "Revision")

    private class RevisionBumpHud : CornerHud("vanillahud-revision-bump.json", "Revision Bump")

    @Test
    fun `a list of customized huds that cannot be read is not taken for an empty one`() {
        requirePermissionEnforcement()
        screen(LAPTOP_W, LAPTOP_H)
        val file = ConfigManager.active().folder.resolve("vanillahud-customized")
        val had = if (Files.isRegularFile(file)) Files.readAllLines(file) else null
        try {
            Files.createDirectories(file.parent)
            Files.write(file, listOf(KEPT_ID))
            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("-w--w----"))
            assertTrue(runCatching { Files.readAllLines(file) }.isFailure, "the file has to actually fail to read")
            ForceDefaultPosition.invalidate()

            val hud = fresh(UnreadableHud())
            hud.cancelForceDefault()
            hud.section = Section.BottomRight
            hud.relativeX = 12f
            hud.relativeY = 8f

            VanillaHud.refreshAll()

            assertEquals(Section.BottomRight, hud.section, "an unreadable list must not license the conversion")
            assertEquals(12f, hud.relativeX, 0.01f, "which would rewrite the offsets along with it")

            val dragged = fresh(UnreadableWriteHud())
            dragged.locked = false
            ForceDefaultPosition.tick()

            dragged.locked = true
            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"))
            ForceDefaultPosition.invalidate()

            assertEquals(listOf(KEPT_ID), Files.readAllLines(file), "and must not write over the ids it never read")

            VanillaHud.refreshAll()
            assertEquals(Section.TopLeft, hud.section, "one failed read must not retire the conversion")
        } finally {
            if (Files.exists(file)) Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"))
            if (had != null) Files.write(file, had) else Files.deleteIfExists(file)
            ForceDefaultPosition.invalidate()
        }
    }

    private class UnreadableHud : CornerHud("vanillahud-unreadable.json", "Unreadable")

    private class UnreadableWriteHud : CornerHud("vanillahud-unreadable-write.json", "Unreadable Write")

    @Test
    fun `a list that cannot be read does not force every hud back to its default`() {
        requirePermissionEnforcement()
        screen(LAPTOP_W, LAPTOP_H)
        val file = ConfigManager.active().folder.resolve("vanillahud-customized")
        val had = if (Files.isRegularFile(file)) Files.readAllLines(file) else null
        try {
            Files.createDirectories(file.parent)
            Files.write(file, listOf(KEPT_ID))
            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("-w--w----"))
            assertTrue(runCatching { Files.readAllLines(file) }.isFailure, "the file has to actually fail to read")
            ForceDefaultPosition.invalidate()

            val hud = fresh(UnreadableForceHud())
            hud.cancelForceDefault()
            hud.relativeX = 40f
            hud.relativeY = 20f

            ForceDefaultPosition.tick()
            VanillaHud.refreshAll()

            assertEquals(40f, hud.relativeX, 0.01f, "a read that could not answer is no licence to reset")
            assertEquals(20f, hud.relativeY, 0.01f)
        } finally {
            if (Files.exists(file)) Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"))
            if (had != null) Files.write(file, had) else Files.deleteIfExists(file)
            ForceDefaultPosition.invalidate()
        }
    }

    private class UnreadableForceHud : CornerHud("vanillahud-unreadable-force.json", "Unreadable Force")

    @Test
    fun `a drag made while the list could not be read survives the next invalidate`() {
        requirePermissionEnforcement()
        screen(LAPTOP_W, LAPTOP_H)
        val file = ConfigManager.active().folder.resolve("vanillahud-customized")
        val had = if (Files.isRegularFile(file)) Files.readAllLines(file) else null
        try {
            Files.createDirectories(file.parent)
            Files.write(file, listOf(KEPT_ID))
            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("-w--w----"))
            assertTrue(runCatching { Files.readAllLines(file) }.isFailure, "the file has to actually fail to read")
            ForceDefaultPosition.invalidate()

            val hud = fresh(UnreadableDragHud())
            hud.cancelForceDefault()
            hud.relativeX = 40f
            hud.relativeY = 20f

            hud.locked = false
            ForceDefaultPosition.tick()
            hud.locked = true

            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"))
            ForceDefaultPosition.invalidate()
            ForceDefaultPosition.tick()
            VanillaHud.refreshAll()

            assertEquals(40f, hud.relativeX, 0.01f, "a drag the disk could not be asked about is still the user's")
            assertEquals(20f, hud.relativeY, 0.01f)
        } finally {
            if (Files.exists(file)) Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"))
            if (had != null) Files.write(file, had) else Files.deleteIfExists(file)
            ForceDefaultPosition.invalidate()
        }
    }

    private class UnreadableDragHud : CornerHud("vanillahud-unreadable-drag.json", "Unreadable Drag")

    @Test
    fun `taking hold of a hud drops its queued reset even when the customized list cannot be read`() {
        requirePermissionEnforcement()
        screen(LAPTOP_W, LAPTOP_H)
        val file = ConfigManager.active().folder.resolve("vanillahud-customized")
        val had = if (Files.isRegularFile(file)) Files.readAllLines(file) else null
        try {
            Files.createDirectories(file.parent)
            Files.write(file, listOf(KEPT_ID))
            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("-w--w----"))
            assertTrue(runCatching { Files.readAllLines(file) }.isFailure, "the file has to actually fail to read")
            ForceDefaultPosition.invalidate()

            val hud = fresh(DragCancelHud())
            hud.queueForceDefault()

            hud.locked = false
            hud.relativeX = 40f
            hud.relativeY = 20f

            ForceDefaultPosition.tick()

            hud.locked = true
            hud.applyForceDefault()

            assertEquals(40f, hud.relativeX, 0.01f, "a hud the user has hold of is not the reset's to move")
            assertEquals(20f, hud.relativeY, 0.01f)
        } finally {
            if (Files.exists(file)) Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"))
            if (had != null) Files.write(file, had) else Files.deleteIfExists(file)
            ForceDefaultPosition.invalidate()
        }
    }

    private class DragCancelHud : CornerHud("vanillahud-drag-cancel.json", "Drag Cancel")

    @Test
    fun `a reset that could not derive its default is still owed`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(ThrowingOriginHud())
        hud.relativeX = 40f
        hud.relativeY = 20f
        hud.queueForceDefault()

        hud.failOrigin = true
        hud.applyForceDefault()
        assertEquals(40f, hud.relativeX, 0.01f, "a default it could not work out is not one to place against")

        hud.applyForceDefault()

        assertEquals(0f, hud.x, 0.5f, "and the reset is owed until one of them lands")
        assertEquals(0f, hud.y, 0.5f)
    }

    private class ThrowingOriginHud : CornerHud("vanillahud-throwing-origin.json", "Throwing Origin") {
        var failOrigin = false
        override fun vanillaOriginX(screenWidth: Int, screenHeight: Int): Float {
            if (failOrigin) {
                failOrigin = false
                throw IllegalStateException("the origin is not readable yet")
            }
            return 0f
        }
    }

    @Test
    fun `a placement that threw part way through leaves the position it found`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(ThrowingPlaceHud())
        hud.cancelForceDefault()
        hud.section = Section.BottomRight
        hud.relativeX = 12f
        hud.relativeY = 8f

        try {
            hud.failOnCentre = true
            hud.pinToVanillaOrigin(LAPTOP_W, LAPTOP_H)

            assertEquals(Section.BottomRight, hud.section, "a placement that threw must put the section back")
            assertEquals(HudAnchor.Auto, hud.growthAnchor, "a placement must not touch the growth edge")
            assertEquals(12f, hud.relativeX, 0f, "leaving the offsets exactly as it found them")
            assertEquals(8f, hud.relativeY, 0f)
        } finally {
            hud.failOnCentre = false
        }
    }

    private class ThrowingPlaceHud : CentredHud("vanillahud-throwing-place.json", "Throwing Place") {
        var failOnCentre = false
        override fun measuredWidth(): Float {
            if (failOnCentre && section == Section.Center) throw IllegalStateException("not measurable yet")
            return naturalWidth
        }
    }

    @Test
    fun `a hud the user dragged still measures its offsets from the edge its defaults were captured under`() {
        screen(LAPTOP_W, LAPTOP_H)
        val file = ConfigManager.active().folder.resolve("vanillahud-customized")
        val had = if (Files.isRegularFile(file)) Files.readAllLines(file) else null
        try {
            Files.createDirectories(file.parent)
            Files.write(file, listOf("vanillahud-dragged-reset.json"))
            ForceDefaultPosition.invalidate()

            val hud = fresh(DraggedResetHud())
            hud.cancelForceDefault()
            hud.pinToVanillaOrigin(LAPTOP_W, LAPTOP_H)
            hud.section = Section.Center
            hud.growthAnchor = HudAnchor.Auto
            hud.y = vanillaY(LAPTOP_H)

            VanillaHud.refreshAll()

            assertTrue(hud.anchorsToVanillaOrigin(), "a hud sitting on its default has to read as being on it")

            val tree = hud.tree!!
            hud.section = tree.getProp("section")!!.getMetadata<Section>("default")!!
            hud.relativeX = tree.getProp("relativeX")!!.getMetadata<Float>("default")!!
            hud.relativeY = tree.getProp("relativeY")!!.getMetadata<Float>("default")!!

            assertEquals(vanillaY(LAPTOP_H), hud.y, 0.5f, "the reset must not shift the hud by half its height")
        } finally {
            if (had != null) Files.write(file, had) else Files.deleteIfExists(file)
            ForceDefaultPosition.invalidate()
        }
    }

    private class DraggedResetHud : CentredHud("vanillahud-dragged-reset.json", "Dragged Reset")

    @Test
    fun `a hud the user dropped against an edge keeps growing out of that edge`() {
        screen(LAPTOP_W, LAPTOP_H)
        val file = ConfigManager.active().folder.resolve("vanillahud-customized")
        val had = if (Files.isRegularFile(file)) Files.readAllLines(file) else null
        try {
            Files.createDirectories(file.parent)
            Files.write(file, listOf("vanillahud-dragged-growth.json"))
            ForceDefaultPosition.invalidate()

            val hud = fresh(DraggedGrowthHud())
            hud.cancelForceDefault()
            hud.pinToVanillaOrigin(LAPTOP_W, LAPTOP_H)
            hud.section = Section.BottomRight
            hud.growthAnchor = HudAnchor.Auto
            hud.x = LAPTOP_W - hud.width - 10f
            hud.y = LAPTOP_H - hud.height - 10f
            val gap = LAPTOP_H - (hud.y + hud.height)

            VanillaHud.refreshAll()

            hud.tall = true
            hud.reseedDefaultForScreen()
            VanillaHud.refreshAll()

            assertEquals(
                gap, LAPTOP_H - (hud.y + hud.height), 1.5f,
                "a hud dropped against the bottom has to keep growing off its bottom edge",
            )
        } finally {
            if (had != null) Files.write(file, had) else Files.deleteIfExists(file)
            ForceDefaultPosition.invalidate()
        }
    }

    private class DraggedGrowthHud : CentredHud("vanillahud-dragged-growth.json", "Dragged Growth") {
        var tall = false
        override val naturalHeight get() = if (tall) 136f else 68f
    }

    @Test
    fun `restating a growth edge does not move oneconfig's layout reference`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(GrowthRefHud())
        hud.cancelForceDefault()
        hud.pinToVanillaOrigin(LAPTOP_W, LAPTOP_H)
        hud.section = Section.Center
        hud.growthAnchor = HudAnchor.Auto

        val refW = HudManager.layoutRefWidth
        val refH = HudManager.layoutRefHeight
        HudManager.layoutRefWidth = 111f
        HudManager.layoutRefHeight = 222f
        try {
            VanillaHud.refreshAll()

            assertEquals(HudAnchor.Top, hud.growthAnchor, "the conversion still has to run")
            assertEquals(111f, HudManager.layoutRefWidth, 0f, "restating an edge must not restamp the layout reference")
            assertEquals(222f, HudManager.layoutRefHeight, 0f)
        } finally {
            HudManager.layoutRefWidth = refW
            HudManager.layoutRefHeight = refH
        }
    }

    private class GrowthRefHud : CentredHud("vanillahud-growth-ref.json", "Growth Ref")

    @Test
    fun `a reset the walk cannot land does not cost a measure a pass`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(ForcePaceHud())
        VanillaHud.refreshAll()
        hud.relativeX = 40f
        hud.relativeY = 20f

        hud.failOrigin = true
        hud.queueForceDefault()
        VanillaHud.refreshAll()
        val measured = hud.measures

        repeat(10) { VanillaHud.refreshAll() }
        assertEquals(measured, hud.measures, "a reset the walk cannot land must not re-measure on every pass")

        hud.failOrigin = false
        hud.reseedDefaultForScreen()
        VanillaHud.refreshAll()
        assertEquals(0f, hud.x, 0.5f, "a reset that could not land is owed until one does")
        assertEquals(0f, hud.y, 0.5f)
    }

    private class ForcePaceHud : CornerHud("vanillahud-force-pace.json", "Force Pace") {
        var failOrigin = false
        var measures = 0
        override fun measuredWidth(): Float {
            measures++
            return naturalWidth
        }

        override fun vanillaOriginX(screenWidth: Int, screenHeight: Int): Float {
            if (failOrigin) throw IllegalStateException("the origin is not readable yet")
            return 0f
        }
    }

    @Test
    fun `a drag the disk would not take is not lost to the next invalidate`() {
        requirePermissionEnforcement()
        screen(LAPTOP_W, LAPTOP_H)
        val file = ConfigManager.active().folder.resolve("vanillahud-customized")
        val had = if (Files.isRegularFile(file)) Files.readAllLines(file) else null
        try {
            Files.createDirectories(file.parent)
            Files.write(file, listOf(KEPT_ID))
            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("r--r-----"))
            assertTrue(
                runCatching { Files.write(file, listOf(KEPT_ID)) }.isFailure,
                "the file has to actually fail to write",
            )
            ForceDefaultPosition.invalidate()

            val hud = fresh(UnwritableHud())
            hud.cancelForceDefault()
            hud.relativeX = 40f
            hud.relativeY = 20f

            hud.locked = false
            ForceDefaultPosition.tick()
            hud.locked = true

            ForceDefaultPosition.invalidate()
            ForceDefaultPosition.tick()
            VanillaHud.refreshAll()

            assertEquals(40f, hud.relativeX, 0.01f, "a drag the disk would not take is still the user's")
            assertEquals(20f, hud.relativeY, 0.01f)
        } finally {
            if (Files.exists(file)) Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"))
            if (had != null) Files.write(file, had) else Files.deleteIfExists(file)
            ForceDefaultPosition.invalidate()
        }
    }

    private class UnwritableHud : CornerHud("vanillahud-unwritable.json", "Unwritable")

    @Test
    fun `a drag the disk would not take reaches it once it will`() {
        requirePermissionEnforcement()
        screen(LAPTOP_W, LAPTOP_H)
        val file = ConfigManager.active().folder.resolve("vanillahud-customized")
        val had = if (Files.isRegularFile(file)) Files.readAllLines(file) else null
        try {
            Files.createDirectories(file.parent)
            Files.write(file, listOf(KEPT_ID))
            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("r--r-----"))
            assertTrue(
                runCatching { Files.write(file, listOf(KEPT_ID)) }.isFailure,
                "the file has to actually fail to write",
            )
            ForceDefaultPosition.invalidate()
            ForceDefaultPosition.nanos = { 0L }

            val hud = fresh(WriteRetryHud())
            hud.locked = false
            ForceDefaultPosition.tick()

            hud.locked = true
            ForceDefaultPosition.tick()

            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"))
            hud.locked = false
            ForceDefaultPosition.tick()
            hud.locked = true

            val written = Files.readAllLines(file)
            assertTrue(hud.hudId in written, "a drag the disk refused has to reach it once it will take one")
            assertTrue(KEPT_ID in written, "without dropping the ids the retried write was merged into")
        } finally {
            ForceDefaultPosition.nanos = System::nanoTime
            if (Files.exists(file)) Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"))
            if (had != null) Files.write(file, had) else Files.deleteIfExists(file)
            ForceDefaultPosition.invalidate()
        }
    }

    private class WriteRetryHud : CornerHud("vanillahud-write-retry.json", "Write Retry")

    @Test
    fun `a push the disk would not take keeps the drag it was carrying`() {
        screen(LAPTOP_W, LAPTOP_H)
        val file = ConfigManager.active().folder.resolve("vanillahud-customized")
        val had = if (Files.isRegularFile(file)) Files.readAllLines(file) else null
        try {
            unwritableList(file)
            ForceDefaultPosition.nanos = { 0L }

            val hud = fresh(FlushKeepHud())
            hud.locked = false
            ForceDefaultPosition.tick()

            hud.locked = true
            ForceDefaultPosition.tick()

            Files.delete(file)
            ForceDefaultPosition.nanos = { RETRY_WINDOW_NANOS * 2 }
            ForceDefaultPosition.tick()

            assertTrue(
                Files.isRegularFile(file) && hud.hudId in Files.readAllLines(file),
                "a push the disk refused is not one to drop the drag it was carrying",
            )
        } finally {
            ForceDefaultPosition.nanos = System::nanoTime
            runCatching { Files.delete(file) }
            if (had != null) Files.write(file, had)
            ForceDefaultPosition.invalidate()
        }
    }

    private class FlushKeepHud : CornerHud("vanillahud-flush-keep.json", "Flush Keep")

    @Test
    fun `a disk that keeps refusing the drag list is not asked again on every tick`() {
        requirePermissionEnforcement()
        screen(LAPTOP_W, LAPTOP_H)
        val file = ConfigManager.active().folder.resolve("vanillahud-customized")
        val had = if (Files.isRegularFile(file)) Files.readAllLines(file) else null
        try {
            Files.createDirectories(file.parent)
            Files.write(file, listOf(KEPT_ID))
            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("r--r-----"))
            assertTrue(
                runCatching { Files.write(file, listOf(KEPT_ID)) }.isFailure,
                "the file has to actually fail to write",
            )
            ForceDefaultPosition.invalidate()
            ForceDefaultPosition.nanos = { 0L }

            val hud = fresh(WritePaceHud())
            hud.locked = false
            ForceDefaultPosition.tick()
            ForceDefaultPosition.tick()

            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"))
            ForceDefaultPosition.tick()
            hud.locked = true

            assertEquals(
                listOf(KEPT_ID), Files.readAllLines(file),
                "a write the disk refused must not be retried on the very next tick",
            )

            val other = fresh(WritePaceOtherHud())
            other.locked = false
            ForceDefaultPosition.tick()
            other.locked = true

            val written = Files.readAllLines(file)
            assertTrue(hud.hudId in written, "the drag the disk refused still has to reach it")
            assertTrue(other.hudId in written, "along with the one that carried it out")
        } finally {
            ForceDefaultPosition.nanos = System::nanoTime
            if (Files.exists(file)) Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"))
            if (had != null) Files.write(file, had) else Files.deleteIfExists(file)
            ForceDefaultPosition.invalidate()
        }
    }

    private class WritePaceHud : CornerHud("vanillahud-write-pace.json", "Write Pace")

    private class WritePaceOtherHud : CornerHud("vanillahud-write-pace-other.json", "Write Pace Other")

    @Test
    fun `a config folder that cannot be resolved does not take the client tick down with it`() {
        screen(LAPTOP_W, LAPTOP_H)
        val backend = ConfigManager::class.java.getDeclaredField("backend")
            .apply { isAccessible = true }
            .get(ConfigManager.active())
        val folderField = backend.javaClass.getField("folder").apply { isAccessible = true }
        val folder = folderField.get(backend)
        ForceDefaultPosition.invalidate()
        try {
            folderField.set(backend, null)
            assertDoesNotThrow { ForceDefaultPosition.tick() }
        } finally {
            folderField.set(backend, folder)
            ForceDefaultPosition.invalidate()
        }
    }

    @Test
    fun `a drag the disk would not take belongs to the profile it was made in`() {
        requirePermissionEnforcement()
        screen(LAPTOP_W, LAPTOP_H)
        val file = ConfigManager.active().folder.resolve("vanillahud-customized")
        val had = if (Files.isRegularFile(file)) Files.readAllLines(file) else null
        val was = ConfigManager.activeProfile()
        try {
            Files.createDirectories(file.parent)
            Files.write(file, listOf(KEPT_ID))
            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("r--r-----"))
            assertTrue(
                runCatching { Files.write(file, listOf(KEPT_ID)) }.isFailure,
                "the file has to actually fail to write",
            )
            ForceDefaultPosition.invalidate()

            val dragged = fresh(ProfileScopeDragHud())
            dragged.locked = false
            ForceDefaultPosition.tick()
            dragged.locked = true

            makeProfile(SCOPE_PROFILE)
            ForceDefaultPosition.invalidate()

            val other = fresh(ProfileScopeOtherHud())
            other.locked = false
            ForceDefaultPosition.tick()
            other.locked = true

            val written = Files.readAllLines(ConfigManager.active().folder.resolve("vanillahud-customized"))
            assertTrue(other.hudId in written, "a drag made in this profile has to be recorded in it")
            assertFalse(
                dragged.hudId in written,
                "a backlog collected against one profile is not one to write into another's file",
            )
        } finally {
            runCatching { ConfigManager.deleteProfile(SCOPE_PROFILE) }
            if (ConfigManager.activeProfile() != was) runCatching { ConfigManager.openProfile(was) }
            clearStoredTrees()
            if (Files.exists(file)) Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"))
            if (had != null) Files.write(file, had) else Files.deleteIfExists(file)
            ForceDefaultPosition.invalidate()
        }
    }

    private class ProfileScopeDragHud : CornerHud("vanillahud-profile-scope-drag.json", "Profile Scope Drag")

    private class ProfileScopeOtherHud : CornerHud("vanillahud-profile-scope-other.json", "Profile Scope Other")

    @Test
    fun `a reseed whose own measure threw is owed without costing a measure a frame`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(MeasureThrowHud())
        hud.queueForceDefault()
        VanillaHud.refreshAll()

        hud.failMeasure = true
        try {
            screen(MONITOR_W, MONITOR_H)
            VanillaHud.refreshAll()
            val measured = hud.measures

            repeat(5) { VanillaHud.refreshAll() }
            assertEquals(measured, hud.measures, "a seed whose measure threw must not re-measure on every frame")
        } finally {
            hud.failMeasure = false
        }

        hud.reseedDefaultForScreen()
        assertEquals(MONITOR_W / 3f, hud.x, 0.5f, "and is still owed until a measure lands")
    }

    private class MeasureThrowHud : ThrowingSeedHud("vanillahud-measure-throw.json", "Measure Throw")

    @Test
    fun `a reset queued while a hud was locked is dropped once the user takes hold of it`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(DragRaceHud())
        VanillaHud.refreshAll()

        hud.queueForceDefault()
        hud.locked = false
        hud.relativeX = 40f
        hud.relativeY = 20f

        VanillaHud.refreshAll()

        assertEquals(40f, hud.relativeX, 0.01f, "a hud the user has hold of is not the reset's to move")
        assertEquals(20f, hud.relativeY, 0.01f)

        hud.locked = true
        VanillaHud.refreshAll()
        assertEquals(
            40f, hud.relativeX, 0.01f,
            "a reset dropped while the user had hold of the hud does not come back when they let go",
        )
        assertEquals(20f, hud.relativeY, 0.01f)
    }

    private class DragRaceHud : CornerHud("vanillahud-drag-race.json", "Drag Race")

    @Test
    fun `a reseed that could not capture is owed without costing a measure a frame`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(ThrowingSeedHud())
        hud.queueForceDefault()
        VanillaHud.refreshAll()
        assertEquals(LAPTOP_W / 3f, hud.x, 0.5f)

        hud.failOrigin = true
        try {
            screen(MONITOR_W, MONITOR_H)
            VanillaHud.refreshAll()
            val measured = hud.measures

            repeat(5) { VanillaHud.refreshAll() }
            assertEquals(measured, hud.measures, "a seed that threw must not re-measure on every frame")
        } finally {
            hud.failOrigin = false
        }

        hud.reseedDefaultForScreen()
        assertEquals(MONITOR_W / 3f, hud.x, 0.5f, "a seed that could not finish is owed until one does")
    }

    private open class ThrowingSeedHud(
        id: String = "vanillahud-throwing-seed.json",
        title: String = "Throwing Seed",
    ) : VanillaHud(id, title, Hud.Category.INFO) {
        var failOrigin = false
        var failMeasure = false
        var measures = 0
        override val naturalWidth get() = 40f
        override val naturalHeight get() = 10f
        override fun measuredWidth(): Float {
            measures++
            if (failMeasure) throw IllegalStateException("the size is not measurable yet")
            return naturalWidth
        }

        override fun vanillaOriginX(screenWidth: Int, screenHeight: Int): Float {
            if (failOrigin) throw IllegalStateException("the origin is not readable yet")
            return screenWidth / 3f
        }

        override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = 0f
    }

    @Test
    fun `a conversion whose own measure threw does not cost a measure a pass`() {
        screen(LAPTOP_W, LAPTOP_H)
        val file = ConfigManager.active().folder.resolve("vanillahud-customized")
        val had = if (Files.isRegularFile(file)) Files.readAllLines(file) else null
        try {
            Files.createDirectories(file.parent)
            Files.write(file, listOf("vanillahud-convert-pace.json"))
            ForceDefaultPosition.invalidate()

            val hud = fresh(ConvertPaceHud())
            hud.cancelForceDefault()
            VanillaHud.refreshAll()
            assertEquals(HudAnchor.Auto, hud.growthAnchor, "a hud the user dragged keeps the edge it has")

            Files.write(file, listOf<String>())
            ForceDefaultPosition.invalidate()

            hud.failMeasure = true
            try {
                VanillaHud.refreshAll()
                val measured = hud.measures

                repeat(10) { VanillaHud.refreshAll() }
                assertEquals(measured, hud.measures, "a conversion the walk cannot measure for must not re-measure a pass")
                assertEquals(HudAnchor.Auto, hud.growthAnchor, "and must not have landed either")
            } finally {
                hud.failMeasure = false
            }

            hud.reseedDefaultForScreen()
            VanillaHud.refreshAll()
            assertEquals(HudAnchor.Top, hud.growthAnchor, "a conversion that could not measure is owed until one lands")
            assertEquals(Section.Center, hud.section)
        } finally {
            if (had != null) Files.write(file, had) else Files.deleteIfExists(file)
            ForceDefaultPosition.invalidate()
        }
    }

    private class ConvertPaceHud : CentredHud("vanillahud-convert-pace.json", "Convert Pace") {
        var failMeasure = false
        var measures = 0
        override fun measuredWidth(): Float {
            measures++
            if (failMeasure) throw IllegalStateException("the size is not measurable yet")
            return naturalWidth
        }
    }

    @Test
    fun `a reset whose own measure threw does not cost a measure a pass`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(ForceMeasurePaceHud())
        VanillaHud.refreshAll()
        hud.relativeX = 40f
        hud.relativeY = 20f

        hud.failMeasure = true
        try {
            hud.queueForceDefault()
            VanillaHud.refreshAll()
            val measured = hud.measures

            repeat(10) { VanillaHud.refreshAll() }
            assertEquals(measured, hud.measures, "a reset the walk cannot measure for must not re-measure a pass")
        } finally {
            hud.failMeasure = false
        }

        hud.reseedDefaultForScreen()
        VanillaHud.refreshAll()
        assertEquals(0f, hud.x, 0.5f, "a reset that could not measure is owed until one lands")
        assertEquals(0f, hud.y, 0.5f)
    }

    private class ForceMeasurePaceHud : CornerHud("vanillahud-force-measure-pace.json", "Force Measure Pace") {
        var failMeasure = false
        var measures = 0
        override fun measuredWidth(): Float {
            measures++
            if (failMeasure) throw IllegalStateException("the size is not measurable yet")
            return naturalWidth
        }
    }

    @Test
    fun `a pass that failed once still re-derives the element on the next screen change`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(UndrawnSeedHud())
        hud.queueForceDefault()
        VanillaHud.refreshAll()
        assertEquals(LAPTOP_W / 3f, hud.x, 0.5f)

        hud.failOrigin = true
        try {
            screen(MONITOR_W, MONITOR_H)
            VanillaHud.refreshAll()
        } finally {
            hud.failOrigin = false
        }
        assertEquals(LAPTOP_W / 3f, hud.x, 0.5f, "a seed that threw leaves the hud where it was")

        screen(WIDE_W, WIDE_H)
        repeat(5) { VanillaHud.refreshAll() }
        assertEquals(WIDE_W / 3f, hud.x, 0.5f, "one failed pass must not latch the element out of the refresh")
    }

    private class UndrawnSeedHud : ThrowingSeedHud("vanillahud-undrawn-seed.json", "Undrawn Seed")

    @Test
    fun `the scoreboard still lands on its default while the sidebar cannot be read`() {
        screen(LAPTOP_W, LAPTOP_H)
        val board = fresh(ScoreboardHud())
        board.queueForceDefault()
        VanillaHud.refreshAll()

        assertEquals(LAPTOP_H / 2f - 45f, board.y, 0.5f, "a reset has to land the scoreboard on its default")

        screen(MONITOR_W, MONITOR_H)
        VanillaHud.refreshAll()
        assertEquals(MONITOR_H / 2f - 45f, board.y, 0.5f, "and it has to follow the screen it is drawn on")
    }

    @Test
    fun `a conversion with nothing to convert leaves the offsets exactly as it found them`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(SectionHeldHud())
        hud.cancelForceDefault()
        hud.section = Section.TopLeft
        hud.growthAnchor = HudAnchor.Auto
        hud.relativeX = 12.5f
        hud.relativeY = 8.5f

        VanillaHud.refreshAll()

        assertEquals(12.5f, hud.relativeX, 0f, "a conversion with nothing to convert must not re-round the offsets")
        assertEquals(8.5f, hud.relativeY, 0f)
    }

    private class SectionHeldHud : CornerHud("vanillahud-section-held.json", "Section Held")

    @Test
    fun `a drag the disk would not take reaches it once the user has let go`() {
        requirePermissionEnforcement()
        screen(LAPTOP_W, LAPTOP_H)
        val file = ConfigManager.active().folder.resolve("vanillahud-customized")
        val had = if (Files.isRegularFile(file)) Files.readAllLines(file) else null
        try {
            val hud = fresh(RelockRetryHud())
            hud.locked = false
            ForceDefaultPosition.tick()
            hud.locked = true

            Files.createDirectories(file.parent)
            Files.write(file, listOf(KEPT_ID))
            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("r--r-----"))
            assertTrue(
                runCatching { Files.write(file, listOf(KEPT_ID)) }.isFailure,
                "the file has to actually fail to write",
            )
            ForceDefaultPosition.invalidate()

            hud.locked = false
            ForceDefaultPosition.tick()

            hud.locked = true
            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"))
            ForceDefaultPosition.tick()

            val written = Files.readAllLines(file)
            assertTrue(hud.hudId in written, "a drag the user has let go of still has to reach the disk")
            assertTrue(KEPT_ID in written, "without dropping the ids the flushed write was merged into")
        } finally {
            if (Files.exists(file)) Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"))
            if (had != null) Files.write(file, had) else Files.deleteIfExists(file)
            ForceDefaultPosition.invalidate()
        }
    }

    private class RelockRetryHud : CornerHud("vanillahud-relock-retry.json", "Relock Retry")

    @Test
    fun `a profile switch drops the customized list before the next client tick`() {
        val hud = ProfileListHud()
        val file = ConfigManager.active().folder.resolve("vanillahud-customized")
        val had = if (Files.isRegularFile(file)) Files.readAllLines(file) else null
        val was = ConfigManager.activeProfile()
        try {
            Files.createDirectories(file.parent)
            Files.write(file, listOf(hud.hudId))
            ForceDefaultPosition.invalidate()
            assertEquals(true, ForceDefaultPosition.customized(hud))

            makeProfile(LIST_PROFILE)

            assertEquals(
                false, ForceDefaultPosition.customized(hud),
                "a warm list belonging to the last profile is not this one's to answer from",
            )
        } finally {
            runCatching { ConfigManager.deleteProfile(LIST_PROFILE) }
            if (ConfigManager.activeProfile() != was) runCatching { ConfigManager.openProfile(was) }
            clearStoredTrees()
            if (had != null) Files.write(file, had) else Files.deleteIfExists(file)
            ForceDefaultPosition.invalidate()
        }
    }

    private class ProfileListHud : CornerHud("vanillahud-profile-list.json", "Profile List")

    @Test
    fun `a conversion that threw part way through puts back the growth edge it found`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(AnchorRestoreHud())
        hud.cancelForceDefault()
        hud.pinToVanillaOrigin(LAPTOP_W, LAPTOP_H)
        VanillaHud.refreshAll()
        hud.growthAnchor = HudAnchor.Auto
        val before = hud.y

        try {
            hud.failUpdate = true
            VanillaHud.refreshAll()

            assertEquals(HudAnchor.Auto, hud.growthAnchor, "a conversion that threw has to put the edge back")
            assertEquals(before, hud.y, 0.5f, "and leave the hud exactly where it found it")
        } finally {
            hud.failUpdate = false
        }
    }

    private class AnchorRestoreHud : CentredHud("vanillahud-anchor-restore.json", "Anchor Restore") {
        var failUpdate = false
        override fun updateRelativeX(absX: Float) {
            if (failUpdate) throw IllegalStateException("the position is not writable yet")
            super.updateRelativeX(absX)
        }
    }

    @Test
    fun `an editor pin whose own measure threw does not leave the walk re-measuring`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(PinPaceHud())
        VanillaHud.refreshAll()
        hud.relativeX = 40f
        hud.relativeY = 20f

        hud.failMeasure = true
        try {
            hud.queueForceDefault()
            hud.pinToVanillaOrigin(LAPTOP_W, LAPTOP_H)
            val measured = hud.measures

            repeat(10) { VanillaHud.refreshAll() }
            assertEquals(measured, hud.measures, "a pin that could not measure must not leave the walk re-measuring")
        } finally {
            hud.failMeasure = false
        }

        hud.reseedDefaultForScreen()
        VanillaHud.refreshAll()
        assertEquals(0f, hud.x, 0.5f, "a reset held off by a failed pin is owed until a measure lands")
        assertEquals(0f, hud.y, 0.5f)
    }

    private class PinPaceHud : CornerHud("vanillahud-pin-pace.json", "Pin Pace") {
        var failMeasure = false
        var measures = 0
        override fun measuredWidth(): Float {
            measures++
            if (failMeasure) throw IllegalStateException("the size is not measurable yet")
            return naturalWidth
        }
    }

    @Test
    fun `a reseed whose own measure threw holds the rest of the walk off as well`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(SeedPaceHud())
        VanillaHud.refreshAll()
        hud.relativeX = 40f
        hud.relativeY = 20f

        hud.failMeasure = true
        try {
            screen(MONITOR_W, MONITOR_H)
            VanillaHud.refreshAll()
            val measured = hud.measures

            hud.queueForceDefault()
            repeat(10) { VanillaHud.refreshAll() }
            assertEquals(measured, hud.measures, "a seed that could not measure must not leave the reset re-measuring")
        } finally {
            hud.failMeasure = false
        }

        hud.reseedDefaultForScreen()
        VanillaHud.refreshAll()
        assertEquals(0f, hud.x, 0.5f, "a reset held off by a failed seed is owed until a measure lands")
        assertEquals(0f, hud.y, 0.5f)
    }

    private class SeedPaceHud : CornerHud("vanillahud-seed-pace.json", "Seed Pace") {
        var failMeasure = false
        var measures = 0
        override fun measuredWidth(): Float {
            measures++
            if (failMeasure) throw IllegalStateException("the size is not measurable yet")
            return naturalWidth
        }
    }

    @Test
    fun `a hud dragged along its own row is not drawn back on the vanilla origin`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(SideDragHud())
        hud.cancelForceDefault()
        hud.pinToVanillaOrigin(LAPTOP_W, LAPTOP_H)
        VanillaHud.refreshAll()
        assertTrue(hud.anchorsToVanillaOrigin(), "a hud that never moved has to read as being on its default")

        val downwards = hud.relativeY
        hud.x = hud.x + 60f
        assertEquals(Section.BottomCenter, hud.section, "the drag has to stay in the section it started in")
        assertEquals(downwards, hud.relativeY, 0f, "and leave the offset down the screen alone")

        assertFalse(hud.anchorsToVanillaOrigin(), "a hud dragged sideways is no longer sitting on its default")

        val placed = hud.x
        screen(MONITOR_W, MONITOR_H)
        VanillaHud.refreshAll()
        screen(LAPTOP_W, LAPTOP_H)
        VanillaHud.refreshAll()
        assertEquals(placed, hud.x, 1.5f, "a sideways drag must not be undone by the next reseed")
    }

    @Test
    fun `a nudge along its own row is kept rather than pinned back`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(SideDragHud())
        hud.cancelForceDefault()
        hud.pinToVanillaOrigin(LAPTOP_W, LAPTOP_H)
        VanillaHud.refreshAll()
        assertTrue(hud.anchorsToVanillaOrigin(), "a hud that never moved has to read as being on its default")

        hud.x = hud.x + 6f
        val placed = hud.x

        hud.reseedDefaultForScreen()
        assertFalse(
            hud.anchorsToVanillaOrigin(),
            "a nudge of a few pixels is still a drag the user made",
        )
        VanillaHud.refreshAll()
        assertEquals(placed, hud.x, 0f, "and must not be taken back to the vanilla origin")
    }

    private class SideDragHud : VanillaHud("vanillahud-side-drag.json", "Side Drag", Hud.Category.INFO) {
        override val naturalWidth get() = 90f
        override val naturalHeight get() = 10f
        override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - width / 2f
        override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight - 60f - height
        override val anchorX get() = 0.5f
        override val anchorY get() = 1f
    }

    @Test
    fun `an editor pin for another screen is not stored against this one`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(PinScreenHud())
        hud.cancelForceDefault()
        hud.pinToVanillaOrigin(LAPTOP_W, LAPTOP_H)
        VanillaHud.refreshAll()
        val across = hud.relativeX
        val down = hud.relativeY

        hud.pinToVanillaOrigin(MONITOR_W, MONITOR_H)

        assertEquals(across, hud.relativeX, 0f, "a pin for a screen the manager is not on must not be stored")
        assertEquals(down, hud.relativeY, 0f)

        screen(MONITOR_W, MONITOR_H)
        hud.pinToVanillaOrigin(MONITOR_W, MONITOR_H)
        assertEquals(MONITOR_H / 2f - 40f, hud.y, 0.5f, "a pin for the screen the manager is on still has to land")
    }

    private class PinScreenHud : CentredHud("vanillahud-pin-screen.json", "Pin Screen")

    @Test
    fun `a hud drawn at less than full size stores the offset its scaled box gives`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(ScaledBoxHud())
        hud.cancelForceDefault()
        hud.customScale = 0.5f
        VanillaHud.refreshAll()

        hud.pinToVanillaOrigin(LAPTOP_W, LAPTOP_H)

        assertEquals(
            0f, hud.relativeX, 0.01f,
            "the stored offset has to be measured from the scaled box rather than the content",
        )
    }

    private class ScaledBoxHud : CentredHud("vanillahud-scaled-box.json", "Scaled Box")

    @Test
    fun `a reset landing on a hud that has grown captures the default it landed on`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(ForceCaptureHud())
        hud.cancelForceDefault()
        hud.customScale = 2f
        hud.pinToVanillaOrigin(LAPTOP_W, LAPTOP_H)
        VanillaHud.refreshAll()
        assertTrue(hud.anchorsToVanillaOrigin(), "a hud that never moved has to read as being on its default")

        hud.tall = true
        hud.queueForceDefault()
        VanillaHud.refreshAll()

        assertEquals(
            LAPTOP_H / 2f - 40f - hud.height / 2f, hud.y, 0.5f,
            "the reset has to land on the default the new size gives",
        )
        assertEquals(
            LAPTOP_W / 2f - hud.width, hud.x, 0.5f,
            "the same correction has to run across the screen",
        )
        assertEquals(
            0f, hud.relativeX, 0.5f,
            "a centred element on its default stores no offset from its section",
        )
        assertTrue(
            hud.anchorsToVanillaOrigin(),
            "a hud a reset just put on its default has to read as being on it",
        )
    }

    private class ForceCaptureHud : CentredHud("vanillahud-force-capture.json", "Force Capture") {
        var tall = false
        override val naturalWidth get() = if (tall) 240f else 120f
        override val naturalHeight get() = if (tall) 136f else 68f
    }

    @Test
    fun `a disk that keeps refusing to hand over the drag list is not read again on every tick`() {
        requirePermissionEnforcement()
        screen(LAPTOP_W, LAPTOP_H)
        val file = ConfigManager.active().folder.resolve("vanillahud-customized")
        val had = if (Files.isRegularFile(file)) Files.readAllLines(file) else null
        var now = 0L
        try {
            Files.createDirectories(file.parent)
            Files.write(file, listOf(KEPT_ID))
            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("-w--w----"))
            assertTrue(
                runCatching { Files.readAllLines(file) }.isFailure,
                "the file has to actually fail to read",
            )
            ForceDefaultPosition.invalidate()
            ForceDefaultPosition.nanos = { now }

            val hud = fresh(ReadPaceHud())
            assertNull(ForceDefaultPosition.customized(hud), "a list that cannot be read answers nothing")

            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"))
            now += RETRY_WINDOW_NANOS / 2
            assertNull(
                ForceDefaultPosition.customized(hud),
                "a read the disk refused must not be retried inside the window",
            )

            now += RETRY_WINDOW_NANOS
            assertEquals(false, ForceDefaultPosition.customized(hud), "and the first query past it has to ask")
        } finally {
            ForceDefaultPosition.nanos = System::nanoTime
            if (Files.exists(file)) Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"))
            if (had != null) Files.write(file, had) else Files.deleteIfExists(file)
            ForceDefaultPosition.invalidate()
        }
    }

    private class ReadPaceHud : CornerHud("vanillahud-read-pace.json", "Read Pace")

    @Test
    fun `an element the walk is not drawing costs it nothing`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(HiddenWalkHud())
        VanillaHud.refreshAll()
        val settled = hud.measures

        hud.hidden = true
        try {
            repeat(5) {
                screen(MONITOR_W, MONITOR_H)
                VanillaHud.refreshAll()
                screen(LAPTOP_W, LAPTOP_H)
                VanillaHud.refreshAll()
            }
            assertEquals(settled, hud.measures, "an element nothing is drawing must not be measured by the walk")
        } finally {
            hud.hidden = false
        }

        screen(MONITOR_W, MONITOR_H)
        VanillaHud.refreshAll()
        assertTrue(hud.measures > settled, "an element that is drawing again still has to be re-derived")
    }

    @Test
    fun `the editor still draws an element the user has hidden`() {
        val hud = VisibilityHud()
        hud.hidden = true
        assertFalse(hud.shouldDraw(), "an element the user hid is not drawn in game")

        HudManager.isConfigUiOpen = true
        try {
            assertTrue(hud.shouldDraw(), "and the editor is where they take it back")
        } finally {
            HudManager.isConfigUiOpen = false
        }
    }

    @Test
    fun `the debug screen and the tab list each answer for their own toggle`() {
        val hud = VisibilityHud()
        try {
            HudManager.isDebugScreenVisible = true
            hud.showInF3 = false
            assertFalse(hud.shouldDraw(), "an element kept out of f3 is not drawn while f3 is up")
            hud.showInF3 = true
            assertTrue(hud.shouldDraw(), "and one allowed in it still is")

            HudManager.isDebugScreenVisible = false
            HudManager.isTabListVisible = true
            hud.showInTab = false
            assertFalse(hud.shouldDraw(), "an element kept out of the tab list is not drawn behind it")
            hud.showInTab = true
            assertTrue(hud.shouldDraw(), "and one allowed behind it still is")
        } finally {
            HudManager.isDebugScreenVisible = false
            HudManager.isTabListVisible = false
        }
    }

    @Test
    fun `an unlocked hud measures its demo content only while the ui is up`() {
        val hud = VisibilityHud()
        hud.locked = false
        assertFalse(hud.previewing, "an unlocked hud in game has nothing to preview")

        HudManager.isConfigUiOpen = true
        try {
            assertTrue(hud.previewing, "and previews once the config ui is up")
            hud.locked = true
            assertFalse(hud.previewing, "a hud nobody has hold of has nothing to preview either")
        } finally {
            HudManager.isConfigUiOpen = false
        }
    }

    private class VisibilityHud : CornerHud("vanillahud-visibility.json", "Visibility")

    @Test
    fun `an element that measured nothing is not one oneconfig has never measured`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(EmptyMeasureHud())
        hud.reseedDefaultForScreen()
        assertEquals(1f, hud.scaledWidth, 0f, "an element that measured nothing is not one to size from staticW")
        assertEquals(1f, hud.scaledHeight, 0f)
    }

    private class EmptyMeasureHud : VanillaHud("vanillahud-empty-measure.json", "Empty Measure", Hud.Category.INFO) {
        override val naturalWidth get() = 0f
        override val naturalHeight get() = 0f
        override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = 0f
        override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = 0f
    }

    private class HiddenWalkHud : CornerHud("vanillahud-hidden-walk.json", "Hidden Walk") {
        var measures = 0
        override fun measuredWidth(): Float {
            measures++
            return naturalWidth
        }
    }

    @Test
    fun `a hud with no captured position default is still drawn on the vanilla origin`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(NoDefaultHud())
        val props = hud.tree!!
        hud.relativeX += 40f

        props.getProp("relativeX")!!.removeMetadata("default")
        assertTrue(
            hud.anchorsToVanillaOrigin(),
            "a hud with no default to compare against still belongs on the vanilla origin",
        )

        props.getProp("relativeX")!!.addMetadata("default", hud.relativeX)
        props.getProp("relativeY")!!.addMetadata("default", hud.relativeY)
        props.getProp("section")!!.removeMetadata("default")
        assertTrue(
            hud.anchorsToVanillaOrigin(),
            "and neither is a section nobody captured a default for",
        )
    }

    private class NoDefaultHud : CornerHud("vanillahud-no-default.json", "No Default")

    @Test
    fun `a screen with no size re-derives nothing`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(ZeroScreenHud())
        VanillaHud.refreshAll()
        val settled = hud.measures

        screen(0, 0)
        hud.reseedDefaultForScreen()
        assertEquals(settled, hud.measures, "a screen with no size must not cost a measure, let alone a capture")

        hud.relativeX = 40f
        hud.relativeY = 20f
        hud.queueForceDefault()
        hud.applyForceDefault()
        assertEquals(settled, hud.measures, "and neither must a reset")
        assertEquals(40f, hud.relativeX, 0.01f, "let alone place the hud against nothing")

        screen(LAPTOP_W, LAPTOP_H)
        hud.applyForceDefault()
        assertEquals(0f, hud.x, 0.5f, "a reset with no screen to derive against is owed until there is one")
        assertEquals(0f, hud.y, 0.5f)
    }

    private class ZeroScreenHud : MeasuringHud("vanillahud-zero-screen.json", "Zero Screen")

    @Test
    fun `turning an element retires the seed its default was captured under`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(TurnSeedHud())
        VanillaHud.refreshAll()
        val settled = hud.measures
        VanillaHud.refreshAll()
        assertEquals(settled, hud.measures, "a pass with nothing changed must not measure")

        hud.turns = 1
        try {
            VanillaHud.refreshAll()
            assertTrue(hud.measures > settled, "a turn has to retire the seed the default was captured under")
        } finally {
            hud.turns = 0
        }
    }

    private class TurnSeedHud : MeasuringHud("vanillahud-turn-seed.json", "Turn Seed") {
        var turns = 0
        override val naturalHeight get() = 50f
        override val quarterTurns get() = turns
    }

    private open class MeasuringHud(id: String, title: String) : CornerHud(id, title) {
        var measures = 0
        override fun measuredWidth(): Float {
            measures++
            return naturalWidth
        }

        override fun measuredHeight(): Float {
            measures++
            return naturalHeight
        }
    }

    @Test
    fun `a position write inside a system reposition oneconfig opened hands it back`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(SystemWriteHud())
        hud.cancelForceDefault()

        HudInternals.systemReposition(true)
        try {
            hud.pinToVanillaOrigin(LAPTOP_W, LAPTOP_H)
            assertTrue(
                HudInternals.systemReposition(),
                "a position write must not close a system reposition it did not open",
            )
        } finally {
            HudInternals.systemReposition(false)
        }

        hud.pinToVanillaOrigin(LAPTOP_W, LAPTOP_H)
        assertFalse(
            HudInternals.systemReposition(),
            "a position write that opened a system reposition has to close it",
        )

        val refW = HudManager.layoutRefWidth
        val refH = HudManager.layoutRefHeight
        HudManager.layoutRefWidth = 111f
        HudManager.layoutRefHeight = 222f
        try {
            hud.x = hud.x
            assertEquals(
                LAPTOP_W.toFloat(), HudManager.layoutRefWidth, 0f,
                "a plain position write still has to note the screen it was arranged on",
            )
        } finally {
            HudManager.layoutRefWidth = refW
            HudManager.layoutRefHeight = refH
        }
    }

    private class SystemWriteHud : CornerHud("vanillahud-system-write.json", "System Write")

    @Test
    fun `a reset that threw part way through placing is tried again`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(ForceThrowHud())
        hud.cancelForceDefault()
        VanillaHud.refreshAll()
        hud.relativeX = 40f
        hud.relativeY = 20f

        hud.seen = 0
        hud.failAt = 2
        hud.queueForceDefault()
        hud.applyForceDefault()
        assertEquals(40f, hud.relativeX, 0.01f, "a placement that threw leaves the position it found")

        hud.failAt = -1
        hud.applyForceDefault()
        assertEquals(0f, hud.x, 0.5f, "a reset that threw part way through has to be tried again")
        assertEquals(0f, hud.y, 0.5f)
    }

    private class ForceThrowHud : CornerHud("vanillahud-force-throw.json", "Force Throw") {
        var failAt = -1
        var seen = 0
        override val positionAnchorX: Float
            get() {
                if (++seen == failAt) throw IllegalStateException("not placeable yet")
                return super.positionAnchorX
            }
    }

    @Test
    fun `a list already read is not read off the disk again`() {
        val file = ConfigManager.active().folder.resolve("vanillahud-customized")
        val had = if (Files.isRegularFile(file)) Files.readAllLines(file) else null
        val hud = CachedListHud()
        try {
            Files.createDirectories(file.parent)
            Files.write(file, listOf(hud.hudId))
            ForceDefaultPosition.invalidate()
            assertEquals(true, ForceDefaultPosition.customized(hud), "the first query has to read the file")

            Files.delete(file)
            assertEquals(
                true, ForceDefaultPosition.customized(hud),
                "a list already read must not be read off the disk again",
            )
        } finally {
            if (had != null) Files.write(file, had) else Files.deleteIfExists(file)
            ForceDefaultPosition.invalidate()
        }
    }

    private class CachedListHud : CornerHud("vanillahud-cached-list.json", "Cached List")

    @Test
    fun `an id padded with whitespace still names the hud it was written for`() {
        val file = ConfigManager.active().folder.resolve("vanillahud-customized")
        val had = if (Files.isRegularFile(file)) Files.readAllLines(file) else null
        val hud = PaddedIdHud()
        try {
            Files.createDirectories(file.parent)
            Files.write(file, listOf("  ${hud.hudId}\t"))
            ForceDefaultPosition.invalidate()
            assertEquals(true, ForceDefaultPosition.customized(hud), "a padded id still names its hud")
        } finally {
            if (had != null) Files.write(file, had) else Files.deleteIfExists(file)
            ForceDefaultPosition.invalidate()
        }
    }

    private class PaddedIdHud : CornerHud("vanillahud-padded-id.json", "Padded Id")

    @Test
    fun `a hud is handed to the reset queue once a profile rather than once a tick`() {
        screen(LAPTOP_W, LAPTOP_H)
        val file = ConfigManager.active().folder.resolve("vanillahud-customized")
        val had = if (Files.isRegularFile(file)) Files.readAllLines(file) else null
        try {
            Files.createDirectories(file.parent)
            Files.write(file, listOf(KEPT_ID))
            ForceDefaultPosition.invalidate()

            val hud = fresh(QueuedOnceHud())
            hud.cancelForceDefault()
            ForceDefaultPosition.tick()
            VanillaHud.refreshAll()
            assertEquals(0f, hud.x, 0.5f, "the first tick has to hand the hud out")

            hud.relativeX = 40f
            hud.relativeY = 20f
            ForceDefaultPosition.tick()
            VanillaHud.refreshAll()
            assertEquals(40f, hud.relativeX, 0.01f, "a second tick must not hand the same hud out again")
            assertEquals(20f, hud.relativeY, 0.01f)
        } finally {
            if (had != null) Files.write(file, had) else Files.deleteIfExists(file)
            ForceDefaultPosition.invalidate()
        }
    }

    private class QueuedOnceHud : CornerHud("vanillahud-queued-once.json", "Queued Once")

    @Test
    fun `a hud the list already names does not cost a write a tick`() {
        screen(LAPTOP_W, LAPTOP_H)
        val file = ConfigManager.active().folder.resolve("vanillahud-customized")
        val had = if (Files.isRegularFile(file)) Files.readAllLines(file) else null
        try {
            Files.createDirectories(file.parent)
            val hud = fresh(SteadyDragHud())
            ForceDefaultPosition.invalidate()
            ForceDefaultPosition.tick()
            Files.write(file, listOf(hud.hudId))
            ForceDefaultPosition.invalidate()

            var clockReads = 0
            ForceDefaultPosition.nanos = { clockReads++; 0L }
            hud.locked = false
            ForceDefaultPosition.tick()
            hud.locked = true
            assertEquals(
                0, clockReads,
                "a hud the list already names must not reach the retry pacing, let alone the write",
            )
        } finally {
            ForceDefaultPosition.nanos = System::nanoTime
            if (had != null) Files.write(file, had) else Files.deleteIfExists(file)
            ForceDefaultPosition.invalidate()
        }
    }

    private class SteadyDragHud : CornerHud("vanillahud-steady-drag.json", "Steady Drag")

    @Test
    fun `a backlog dropped by the profile it belonged to is not written into the next one`() {
        screen(LAPTOP_W, LAPTOP_H)
        val file = ConfigManager.active().folder.resolve("vanillahud-customized")
        val had = if (Files.isRegularFile(file)) Files.readAllLines(file) else null
        val was = ConfigManager.activeProfile()
        try {
            unwritableList(file)

            val dragged = fresh(BacklogProfileHud())
            dragged.locked = false
            ForceDefaultPosition.tick()
            dragged.locked = true
            Files.delete(file)

            makeProfile(FLUSH_PROFILE)

            ForceDefaultPosition.tick()
            assertFalse(
                Files.exists(ConfigManager.active().folder.resolve("vanillahud-customized")),
                "a flush with nothing left to push must not write the file at all",
            )
        } finally {
            runCatching { ConfigManager.deleteProfile(FLUSH_PROFILE) }
            if (ConfigManager.activeProfile() != was) runCatching { ConfigManager.openProfile(was) }
            clearStoredTrees()
            runCatching { Files.delete(file) }
            if (had != null) Files.write(file, had)
            ForceDefaultPosition.invalidate()
        }
    }

    private class BacklogProfileHud : CornerHud("vanillahud-backlog-profile.json", "Backlog Profile")

    @Test
    fun `a write that found the profile switched under it is dropped`() {
        screen(LAPTOP_W, LAPTOP_H)
        val file = ConfigManager.active().folder.resolve("vanillahud-customized")
        val had = if (Files.isRegularFile(file)) Files.readAllLines(file) else null
        val was = ConfigManager.activeProfile()
        try {
            unwritableList(file)

            val dragged = fresh(WriteRaceHud())
            dragged.locked = false
            ForceDefaultPosition.tick()
            dragged.locked = true
            Files.delete(file)

            makeProfile(RACE_PROFILE)
            ConfigManager.openProfile(was)
            ForceDefaultPosition.invalidate()

            var switched = false
            ForceDefaultPosition.nanos = {
                if (!switched) {
                    switched = true
                    ConfigManager.openProfile(RACE_PROFILE)
                }
                0L
            }
            ForceDefaultPosition.tick()

            assertTrue(switched, "the scenario has to reach the clock the push is paced on")
            assertFalse(
                Files.exists(file),
                "a write that found another profile under it must not put the list back anyway",
            )
        } finally {
            ForceDefaultPosition.nanos = System::nanoTime
            runCatching { ConfigManager.deleteProfile(RACE_PROFILE) }
            if (ConfigManager.activeProfile() != was) runCatching { ConfigManager.openProfile(was) }
            clearStoredTrees()
            runCatching { Files.delete(file) }
            if (had != null) Files.write(file, had)
            ForceDefaultPosition.invalidate()
        }
    }

    private class WriteRaceHud : CornerHud("vanillahud-write-race.json", "Write Race")

    @Test
    fun `a hud the user has hold of is not drawn back on the vanilla origin`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(HeldDrawHud())
        hud.cancelForceDefault()
        hud.pinToVanillaOrigin(LAPTOP_W, LAPTOP_H)
        VanillaHud.refreshAll()
        assertTrue(hud.anchorsToVanillaOrigin(), "a hud that never moved has to read as being on its default")

        hud.locked = false
        try {
            assertFalse(hud.anchorsToVanillaOrigin(), "a hud the user has hold of is theirs to move")
        } finally {
            hud.locked = true
        }
    }

    private class HeldDrawHud : CornerHud("vanillahud-held-draw.json", "Held Draw")

    @Test
    fun `a reseed does not re-place a hud whose captured default it could not read`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(UnreadDefaultHud())
        hud.queueForceDefault()
        VanillaHud.refreshAll()

        hud.tree!!.getProp("relativeX")!!.removeMetadata("default")
        hud.relativeX += 40f
        val placed = hud.relativeX

        screen(MONITOR_W, MONITOR_H)
        hud.reseedDefaultForScreen()

        assertEquals(
            placed, hud.relativeX, 0.01f,
            "a default it could not read is no licence to re-place the hud",
        )
    }

    private class UnreadDefaultHud : CornerHud("vanillahud-unread-default.json", "Unread Default")

    @Test
    fun `a hud that grew taller without growing wider still reseeds its default`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(HeightGrowthHud())
        hud.queueForceDefault()
        VanillaHud.refreshAll()
        assertEquals(LAPTOP_H / 2f - 10f, hud.y, 0.5f)

        hud.tall = true
        hud.reseedDefaultForScreen()

        assertEquals(
            LAPTOP_H / 2f - 20f, hud.y, 0.5f,
            "a growth on the axis the other half of the key watches still moves the default",
        )
    }

    private class HeightGrowthHud : CornerHud("vanillahud-height-growth.json", "Height Growth") {
        var tall = false
        override val naturalHeight get() = if (tall) 20f else 10f
        override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight / 2f - height
    }

    @Test
    fun `turning an element swaps the box it is laid out in`() {
        val hud = TurnBoxHud()
        assertEquals(80f, hud.width, 0f)
        assertEquals(20f, hud.height, 0f)

        hud.turns = 1
        assertEquals(20f, hud.width, 0f, "a quarter turn has to swap the two")
        assertEquals(80f, hud.height, 0f)
        assertEquals(80f, hud.unrotatedWidth, 0f, "and leave the content itself on its own axes")
        assertEquals(20f, hud.unrotatedHeight, 0f)

        hud.turns = 2
        assertEquals(80f, hud.width, 0f, "a half turn leaves the box exactly as it was")
        assertEquals(20f, hud.height, 0f)
    }

    private class TurnBoxHud : VanillaHud("vanillahud-turn-box.json", "Turn Box", Hud.Category.INFO) {
        var turns = 0
        override val naturalWidth get() = 80f
        override val naturalHeight get() = 20f
        override val quarterTurns get() = turns
        override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = 0f
        override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = 0f
    }

    @Test
    fun `a hud oneconfig has not made is not this mod's to position`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = NoTreeHud()
        hud.section = Section.BottomRight
        hud.relativeX = 40f
        hud.relativeY = 20f

        HudManager.activeInstances.add(hud)
        try {
            VanillaHud.refreshAll()
            hud.pinToVanillaOrigin(LAPTOP_W, LAPTOP_H)
            assertFalse(hud.anchorsToVanillaOrigin(), "a hud with no tree anchors to nothing")
        } finally {
            HudManager.activeInstances.remove(hud)
        }

        assertEquals(Section.BottomRight, hud.section, "the walk must not convert a hud with no tree")
        assertEquals(40f, hud.relativeX, 0f, "and the draw must not place one")
        assertEquals(20f, hud.relativeY, 0f)
        assertNull(hud.tree, "and nothing may build it one behind our back")
    }

    private class NoTreeHud : CornerHud("vanillahud-no-tree.json", "No Tree")

    @Test
    fun `the frame hook re-derives every element from the screen it is drawn on`() {
        screen(LAPTOP_W, LAPTOP_H)
        val hud = fresh(FrameHookHud())
        hud.queueForceDefault()
        VanillaHud.refreshAll()
        assertEquals(LAPTOP_W / 3f, hud.x, 0.5f)

        HudManager.isConfigUiOpen = true
        try {
            screen(MONITOR_W, MONITOR_H)
            VanillaHud.beginFrame(uninitialisedGraphics())
        } finally {
            HudManager.isConfigUiOpen = false
        }

        assertEquals(
            MONITOR_W / 3f, hud.x, 0.5f,
            "a frame has to re-derive the element it is about to draw",
        )
    }

    private class FrameHookHud : ThrowingSeedHud("vanillahud-frame-hook.json", "Frame Hook")

    private fun unwritableList(file: java.nio.file.Path) {
        Files.deleteIfExists(file)
        Files.createDirectories(file)
        ForceDefaultPosition.invalidate()
    }
}

private const val KEPT_ID = "vanillahud-unreadable-kept.json"

private const val RETRY_WINDOW_NANOS = 1_000_000_000L

private const val SCOPE_PROFILE = "vanillahud-test-scope"

private const val LIST_PROFILE = "vanillahud-test-list"

private const val FLUSH_PROFILE = "vanillahud-test-flush"

private const val RACE_PROFILE = "vanillahud-test-race"
