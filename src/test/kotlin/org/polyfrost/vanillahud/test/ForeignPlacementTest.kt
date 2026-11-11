package org.polyfrost.vanillahud.test

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.polyfrost.oneconfig.api.hud.v1.Hud
import org.polyfrost.vanillahud.compat.ForeignAttribution
import org.polyfrost.vanillahud.compat.MixinOwner
import org.polyfrost.vanillahud.hud.VanillaHud
import org.polyfrost.vanillahud.render.ForeignPlacement
import org.polyfrost.vanillahud.render.ForeignTransform

class ForeignPlacementTest {

    companion object {
        private const val SCREEN_W = 640
        private const val SCREEN_H = 360
        private const val DELTA = 1e-3f
        private const val BAR_W = 182f
        private const val BAR_H = 22f
        private const val VANILLA_ORIGIN_X = 229f
        private const val VANILLA_ORIGIN_Y = 338f
    }

    private class HotbarLikeHud(private val turns: Int = 0) : VanillaHud("vanillahud-foreign-probe.json", "Foreign Probe", Hud.Category.INFO) {
        override val naturalWidth get() = BAR_W
        override val naturalHeight get() = BAR_H
        override val quarterTurns get() = turns
        override val anchorX get() = 0.5f
        override val anchorY get() = 1f
        override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = ((screenWidth - BAR_W) / 2).toFloat()
        override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = (screenHeight - BAR_H).toFloat()

        override fun defaultOriginX(screenWidth: Int, screenHeight: Int) =
            vanillaOriginX(screenWidth, screenHeight) + (naturalWidth - width) / 2f

        override fun defaultOriginY(screenWidth: Int, screenHeight: Int) =
            vanillaOriginY(screenWidth, screenHeight) + (naturalHeight - height) / 2f

        fun detect(placement: ForeignPlacement?) {
            foreignOuter = placement
            foreignLogicalX = placement?.logicalX ?: 0f
            foreignLogicalY = placement?.logicalY ?: 0f
            foreignUpdated()
        }

        fun originX(scale: Float = 1f) = scaledOriginX(SCREEN_W, SCREEN_H, scale)
        fun originY(scale: Float = 1f) = scaledOriginY(SCREEN_W, SCREEN_H, scale)
    }

    private fun noxesiumRescale(scale: Float, tx: Float = 0f, ty: Float = 0f, logicalY: Float = 0f) =
        ForeignPlacement(scale, tx, ty, (SCREEN_W / scale).toInt(), (SCREEN_H / scale).toInt(), 0f, logicalY)

    @Test
    fun `a HUD nothing touches keeps the origin vanilla gives it`() {
        val hud = HotbarLikeHud()
        assertEquals(VANILLA_ORIGIN_X, hud.originX(), DELTA)
        assertEquals(VANILLA_ORIGIN_Y, hud.originY(), DELTA)
        assertNull(hud.foreign)
    }

    @Test
    fun `an identity placement changes nothing`() {
        val hud = HotbarLikeHud()
        hud.detect(ForeignPlacement(1f, 0f, 0f, SCREEN_W, SCREEN_H, 0f, 0f))
        assertEquals(VANILLA_ORIGIN_X, hud.originX(), DELTA)
        assertEquals(VANILLA_ORIGIN_Y, hud.originY(), DELTA)
        assertEquals(1f, hud.elementScale, 0f)
        assertFalse(hud.foreign!!.moved)
        assertFalse(hud.foreign!!.scaled)
    }

    @Test
    fun `a placement that is only reported changes nothing`() {
        val hud = HotbarLikeHud()
        hud.detect(ForeignPlacement(2f, 10f, 10f, 320, 180, 0f, 0f, applied = false))
        assertEquals(VANILLA_ORIGIN_X, hud.originX(), DELTA)
        assertEquals(1f, hud.elementScale, 0f)
        assertTrue(hud.foreign!!.moved)
    }

    @Test
    fun `with both left on a pinned HUD takes the spot and size the other mod gives it`() {
        val hud = HotbarLikeHud()
        hud.detect(noxesiumRescale(2f))
        assertEquals(2f * ((SCREEN_W / 2 - BAR_W) / 2), hud.originX(), DELTA)
        assertEquals(2f, hud.elementScale, 0f)
        assertEquals(2f * BAR_W, hud.boxWidth, DELTA)
    }

    @Test
    fun `ignoring only position keeps the vanilla spot and grows the mod's size from the anchor`() {
        val hud = HotbarLikeHud()
        hud.detect(noxesiumRescale(2f))
        hud.ignoreModPosition = true
        assertEquals(VANILLA_ORIGIN_X - BAR_W / 2f, hud.originX(), DELTA)
        assertEquals(VANILLA_ORIGIN_Y - BAR_H, hud.originY(), DELTA)
        assertEquals(2f, hud.elementScale, 0f)
    }

    @Test
    fun `ignoring only scale keeps the other mod's position at the natural size`() {
        val hud = HotbarLikeHud()
        hud.detect(noxesiumRescale(2f, tx = 20f))
        hud.ignoreModScale = true
        assertEquals(VANILLA_ORIGIN_X + 20f, hud.originX(), DELTA)
        assertEquals(1f, hud.elementScale, 0f)
        assertEquals(BAR_W, hud.boxWidth, DELTA)
    }

    @Test
    fun `ignoring both is exactly vanilla`() {
        val hud = HotbarLikeHud()
        hud.detect(noxesiumRescale(2f, tx = 20f, ty = 7f, logicalY = 4f))
        hud.ignoreModPosition = true
        hud.ignoreModScale = true
        assertEquals(VANILLA_ORIGIN_X, hud.originX(), DELTA)
        assertEquals(VANILLA_ORIGIN_Y, hud.originY(), DELTA)
        assertEquals(1f, hud.elementScale, 0f)
    }

    @Test
    fun `a user scale grows the box from its anchor on top of the other mod's scale`() {
        val hud = HotbarLikeHud()
        hud.detect(noxesiumRescale(2f))
        hud.textScale = 1.5f
        val width = BAR_W * 2f * 1.5f
        assertEquals(SCREEN_W / 2f, hud.originX() + width / 2f, DELTA)
        assertEquals(3f, hud.elementScale, 0f)
    }

    @Test
    fun `a rotated HUD is placed by its turned box`() {
        val hud = HotbarLikeHud(turns = 1)
        hud.detect(noxesiumRescale(2f))
        assertEquals(SCREEN_W / 2f - BAR_H, hud.originX(), DELTA)
        assertEquals(2f * (SCREEN_H / 2 - BAR_H + BAR_H / 2) - BAR_W, hud.originY(), DELTA)
        hud.ignoreModPosition = true
        assertEquals(SCREEN_W / 2f - BAR_H, hud.originX(), DELTA)
    }

    @Test
    fun `logical shifts move the origin by the scale they were made at`() {
        val hud = HotbarLikeHud()
        hud.detect(noxesiumRescale(2f, logicalY = 4f))
        assertEquals(2f * (SCREEN_H / 2 - BAR_H + 4f), hud.originY(), DELTA)
        hud.ignoreModPosition = true
        assertEquals(VANILLA_ORIGIN_Y - BAR_H, hud.originY(), DELTA)
    }

    @Test
    fun `Noxesium's hotbar rescale lands on the same spot as the formula`() {
        val hud = HotbarLikeHud()
        hud.detect(noxesiumRescale(1.5f))
        assertEquals(1.5f * (((SCREEN_W / 1.5f).toInt() - BAR_W) / 2), hud.originX(), DELTA)
    }

    @Test
    fun `Noxesium's boss bar shift is added to the rescaled spot`() {
        val shiftInsideScale = 0.5f * (SCREEN_W / 2) / 2f
        val inner = ForeignPlacement(2f, 2f * shiftInsideScale, 0f, 320, 180, 0f, 0f)
        val hud = HotbarLikeHud()
        hud.detect(inner)
        assertEquals(2f * shiftInsideScale + 2f * ((SCREEN_W / 2 - BAR_W) / 2), hud.originX(), DELTA)
        hud.ignoreModPosition = true
        assertEquals(VANILLA_ORIGIN_X - BAR_W / 2f, hud.originX(), DELTA)
    }

    @Test
    fun `an inner placement goes inside an outer one`() {
        val outer = ForeignPlacement(2f, 5f, 0f, 320, 180, 0f, 0f)
        val inner = ForeignPlacement(0.5f, 10f, 0f, 640, 360, 0f, 0f)
        val both = outer.then(inner)
        assertEquals(1f, both.scale, DELTA)
        assertEquals(25f, both.tx, DELTA)
        assertEquals(640, both.screenW)
    }

    @Test
    fun `status bars hang off a hotbar's scaled box`() {
        val bar = ForeignTransform.anchorOffset(
            origin = 2f * ((320 - 81) / 2), extent = 81f * 2f, fraction = 0f,
            targetOrigin = 138f, targetExtent = 182f * 2f, targetFraction = 0f,
        )
        assertEquals(2f * 119f - 138f, bar, DELTA)
        val unscaled = ForeignTransform.anchorOffset(119f, 81f, 0f, 69f, 182f, 0f)
        assertEquals(50f, unscaled, DELTA)
        assertTrue(bar != unscaled)
    }

    @Test
    fun `small changes are not changes and big ones are`() {
        val base = noxesiumRescale(2f)
        assertFalse(base.differs(base.copy(tx = 0.0005f)))
        assertTrue(base.differs(base.copy(tx = 0.01f)))
        assertTrue(base.differs(base.copy(screenW = 321)))
        assertTrue(base.differs(null))
    }

    @Test
    fun `moved and scaled tell a shift from a rescale`() {
        assertTrue(noxesiumRescale(2f).scaled)
        assertTrue(noxesiumRescale(1f, tx = 3f).moved)
        assertFalse(noxesiumRescale(1f, tx = 3f).scaled)
        assertTrue(noxesiumRescale(1f, logicalY = 4f).moved)
    }

    private val owners = listOf(
        MixinOwner("com.noxcrew.noxesium.core.fabric.mixin", "noxesium"),
        MixinOwner("com.noxcrew.noxesium.core.fabric.mixin.feature.scale", "noxesium-scale"),
        MixinOwner("org.polyfrost.vanillahud.mixin", "vanillahud"),
        MixinOwner("org.polyfrost.oneconfig.mixin", "oneconfig"),
        MixinOwner("net.fabricmc.fabric.mixin.client.rendering", "fabric-rendering-v1"),
        MixinOwner("org.example.nomod", null),
    )

    @Test
    fun `a merged mixin is traced to the mod that registered its config`() {
        val mixins = listOf("com.noxcrew.noxesium.core.fabric.mixin.feature.item.HeldItemOffsetNameMixin")
        assertEquals("noxesium", ForeignAttribution.modIdOf(mixins, owners, { false }))
    }

    @Test
    fun `the closest package owns the mixin`() {
        val mixins = listOf("com.noxcrew.noxesium.core.fabric.mixin.feature.scale.BossBarMixin")
        assertEquals("noxesium-scale", ForeignAttribution.modIdOf(mixins, owners, { false }))
    }

    @Test
    fun `this mod, OneConfig and Fabric API are skipped for what is behind them`() {
        val mixins = listOf(
            "org.polyfrost.vanillahud.mixin.elements.GuiMixinHotbar",
            "org.polyfrost.oneconfig.mixin.GuiMixin",
            "net.fabricmc.fabric.mixin.client.rendering.HudMixin",
            "com.noxcrew.noxesium.core.fabric.mixin.feature.scale.WindowExt",
        )
        assertEquals("noxesium-scale", ForeignAttribution.modIdOf(mixins, owners, { false }))
    }

    @Test
    fun `a stack with no foreign mixin falls back to a known mod that is loaded`() {
        val mixins = listOf("org.polyfrost.vanillahud.mixin.elements.GuiMixinHotbar", "org.example.nomod.SomeMixin")
        assertEquals("noxesium", ForeignAttribution.modIdOf(mixins, owners, { it == "noxesium" }))
        assertNull(ForeignAttribution.modIdOf(mixins, owners, { false }))
    }
}
