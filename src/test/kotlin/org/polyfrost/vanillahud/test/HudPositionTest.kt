package org.polyfrost.vanillahud.test

import net.minecraft.client.gui.GuiGraphicsExtractor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.polyfrost.oneconfig.api.hud.v1.Hud
import org.polyfrost.oneconfig.api.hud.v1.HudAnchor
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.oneconfig.api.hud.v1.Section
import org.polyfrost.vanillahud.hud.VanillaHud
import org.polyfrost.vanillahud.hud.growthAnchorFor
import org.polyfrost.vanillahud.hud.sectionFor

class HudPositionTest {

    companion object {
        private const val MEASURE_SCREEN_W = 640f
        private const val MEASURE_SCREEN_H = 360f
    }

    private var hadScreenW = 0f
    private var hadScreenH = 0f

    @BeforeEach
    fun rememberScreen() {
        hadScreenW = HudManager.guiScreenWidth
        hadScreenH = HudManager.guiScreenHeight
    }

    @AfterEach
    fun releaseScreen() {
        HudManager.guiScreenWidth = hadScreenW
        HudManager.guiScreenHeight = hadScreenH
        HudManager.isConfigUiOpen = false
    }

    @Test
    fun `the screen centre buckets to the centre section and its top edge to the top growth edge`() {
        assertEquals(Section.Center, sectionFor(0.5f, 0.5f))
        assertEquals(HudAnchor.Top, growthAnchorFor(0.5f, 0f))
    }

    @Test
    fun `the buckets are cut a quarter of the way in from either edge`() {
        assertEquals(Section.CenterLeft, sectionFor(0.2499f, 0.5f))
        assertEquals(Section.Center, sectionFor(0.25f, 0.5f))
        assertEquals(Section.Center, sectionFor(0.7499f, 0.5f))
        assertEquals(Section.CenterRight, sectionFor(0.75f, 0.5f))
        assertEquals(Section.TopCenter, sectionFor(0.5f, 0.2499f))
        assertEquals(Section.Center, sectionFor(0.5f, 0.25f))
        assertEquals(Section.Center, sectionFor(0.5f, 0.7499f))
        assertEquals(Section.BottomCenter, sectionFor(0.5f, 0.75f))

        assertEquals(HudAnchor.Left, growthAnchorFor(0.2499f, 0.5f))
        assertEquals(HudAnchor.Center, growthAnchorFor(0.25f, 0.5f))
        assertEquals(HudAnchor.Center, growthAnchorFor(0.7499f, 0.5f))
        assertEquals(HudAnchor.Right, growthAnchorFor(0.75f, 0.5f))
        assertEquals(HudAnchor.Top, growthAnchorFor(0.5f, 0.2499f))
        assertEquals(HudAnchor.Center, growthAnchorFor(0.5f, 0.25f))
        assertEquals(HudAnchor.Center, growthAnchorFor(0.5f, 0.7499f))
        assertEquals(HudAnchor.Bottom, growthAnchorFor(0.5f, 0.75f))
    }

    @Test
    fun `every other bucket keeps the section and the growth edge in step`() {
        val fractions = (0..20).map { it / 20f } + listOf(0.2499f, 0.25f, 0.7499f, 0.75f)
        val hud = ProbeHud()
        for (fx in fractions) {
            for (fy in fractions) {
                hud.section = sectionFor(fx, fy)
                val anchor = growthAnchorFor(fx, fy)
                assertEquals(
                    hud.anchorPointX(HudAnchor.Auto), hud.anchorPointX(anchor), 0f,
                    "section and growth anchor disagree across the screen at ($fx, $fy)",
                )
                assertEquals(
                    hud.anchorPointY(HudAnchor.Auto), hud.anchorPointY(anchor), 0f,
                    "section and growth anchor disagree down the screen at ($fx, $fy)",
                )
            }
        }
    }

    @Test
    fun `a refresh pass retires the measure the pass before it cached`() {
        val hud = MeasureCountHud()
        HudManager.guiScreenWidth = MEASURE_SCREEN_W
        HudManager.guiScreenHeight = MEASURE_SCREEN_H

        VanillaHud.refreshAll()
        assertEquals(1f, hud.unrotatedWidth, 0f)
        assertEquals(1, hud.measures, "the first read of a pass has to measure")
        assertEquals(1f, hud.unrotatedWidth, 0f)
        assertEquals(1, hud.measures, "and a second read inside the same pass has to be the cache")

        hud.next = 2f
        VanillaHud.refreshAll()
        assertEquals(2f, hud.unrotatedWidth, 0f)
        assertEquals(2, hud.measures, "a pass the client tick drove has to retire the cached measure")
    }

    @Test
    fun `a screen width change inside one pass retires the measure too`() {
        val hud = MeasureCountHud()
        HudManager.guiScreenWidth = MEASURE_SCREEN_W
        HudManager.guiScreenHeight = MEASURE_SCREEN_H

        VanillaHud.refreshAll()
        assertEquals(1f, hud.unrotatedWidth, 0f)

        hud.next = 2f
        HudManager.guiScreenWidth = MEASURE_SCREEN_W + 1f
        assertEquals(2f, hud.unrotatedWidth, 0f)
        assertEquals(2, hud.measures, "a measure taken on another screen width is not the answer")
    }

    @Test
    fun `going into preview inside one pass retires the measure too`() {
        val hud = MeasureCountHud()
        HudManager.guiScreenWidth = MEASURE_SCREEN_W
        HudManager.guiScreenHeight = MEASURE_SCREEN_H

        VanillaHud.refreshAll()
        assertEquals(1f, hud.unrotatedWidth, 0f)

        hud.next = 2f
        hud.locked = false
        HudManager.isConfigUiOpen = true
        assertEquals(2f, hud.unrotatedWidth, 0f)
        assertEquals(2, hud.measures, "a measure taken outside a preview is not the answer inside one")
    }

    @Test
    fun `the preview flag and the screen width do not share one measure key`() {
        val hud = MeasureCountHud()
        HudManager.guiScreenWidth = 100f
        HudManager.guiScreenHeight = MEASURE_SCREEN_H
        hud.locked = false
        HudManager.isConfigUiOpen = true

        VanillaHud.refreshAll()
        assertEquals(1f, hud.unrotatedWidth, 0f)

        hud.next = 2f
        HudManager.isConfigUiOpen = false
        HudManager.guiScreenWidth = 100f + 31f
        assertEquals(2f, hud.unrotatedWidth, 0f, "a demo measure is not the answer for what the game is showing")
        assertEquals(2, hud.measures)
    }

    @Test
    fun `a frame drawn while the editor is up does not restate the screen it is drawn on`() {
        HudManager.guiScreenWidth = MEASURE_SCREEN_W
        HudManager.guiScreenHeight = MEASURE_SCREEN_H
        HudManager.isConfigUiOpen = true

        VanillaHud.beginFrame(uninitialisedGraphics())

        assertEquals(MEASURE_SCREEN_W, HudManager.guiScreenWidth, 0f, "the editor's screen width is not the frame's to restate")
        assertEquals(MEASURE_SCREEN_H, HudManager.guiScreenHeight, 0f)
    }

    private class MeasureCountHud : VanillaHud("vanillahud-measure-probe.json", "Measure Probe", Hud.Category.INFO) {
        var measures = 0
        var next = 1f

        override val naturalWidth get() = 0f
        override val naturalHeight get() = 0f
        override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = 0f
        override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = 0f

        override fun measuredWidth(): Float = measureOnce {
            measures++
            next
        } ?: 0f
    }

    private class ProbeHud : VanillaHud("vanillahud-anchor-probe.json", "Anchor Probe", Hud.Category.INFO) {
        override val naturalWidth get() = 64f
        override val naturalHeight get() = 32f
        override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = 0f
        override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = 0f
    }
}

internal fun uninitialisedGraphics(): GuiGraphicsExtractor {
    val field = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe")
    field.isAccessible = true
    val unsafe = field.get(null) as sun.misc.Unsafe
    return unsafe.allocateInstance(GuiGraphicsExtractor::class.java) as GuiGraphicsExtractor
}
