package org.polyfrost.vanillahud.test

//? if >=1.21.8 {
import org.joml.Matrix3x2f
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.polyfrost.vanillahud.render.HeldTooltipBounds

class HeldTooltipBoundsTest {
    private fun lift(x0: Float, x1: Float, top: Float) =
        HeldTooltipBounds.clearance(x0, x1, top, top + 9f, 100f, 180f, 160f, 207f)

    @Test
    fun `bar on the tooltip moves above it`() {
        assertEquals(19f, lift(110f, 150f, 190f))
    }

    @Test
    fun `bar beside or above the tooltip stays put`() {
        assertEquals(0f, lift(0f, 90f, 190f))
        assertEquals(0f, lift(110f, 150f, 171f))
    }

    @Test
    fun `only the vertical lift is dropped`() {
        val laidOut = Matrix3x2f().translate(200f, 400f)
        val pose = Matrix3x2f(laidOut).translate(5f, -30f).scale(2f)
        assertTrue(HeldTooltipBounds.dropLift(laidOut, pose))
        assertEquals(Matrix3x2f(laidOut).translate(5f, 0f).scale(2f), pose)
    }
}
//?}
