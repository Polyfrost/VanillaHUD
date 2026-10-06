package org.polyfrost.vanillahud.compat

//? if = 1.8.9 {
/*import net.minecraft.client.render.vertex.BufferBuilder
import net.minecraft.client.render.vertex.BufferUploader
import net.minecraft.client.render.vertex.DefaultVertexFormat
import org.lwjgl.opengl.GL11

/** Batches the rotated hotbar's icons, turning each back upright. Taken from Argentum. */
object LegacyIconBatch {
    private val buffer = BufferBuilder(16 * 1024 / Integer.BYTES)
    private val uploader = BufferUploader()
    private var drawing = false

    @JvmStatic
    fun quad(
        x: Float,
        y: Float,
        u: Float,
        v: Float,
        sourceWidth: Int,
        sourceHeight: Int,
        width: Int,
        height: Int,
        textureWidth: Float,
        textureHeight: Float,
        z: Float,
        turns: Int
    ) {
        if (!drawing) {
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_TEX)
            drawing = true
        }

        val u0 = u / textureWidth
        val v0 = v / textureHeight
        val u1 = (u + sourceWidth) / textureWidth
        val v1 = (v + sourceHeight) / textureHeight
        val cx = x + width / 2f
        val cy = y + height / 2f
        val steps = Math.floorMod(-turns, 4)
        corner(x, y + height, u0, v1, cx, cy, steps, z)
        corner(x + width, y + height, u1, v1, cx, cy, steps, z)
        corner(x + width, y, u1, v0, cx, cy, steps, z)
        corner(x, y, u0, v0, cx, cy, steps, z)
    }

    private fun corner(px: Float, py: Float, u: Float, v: Float, cx: Float, cy: Float, steps: Int, z: Float) {
        var dx = px - cx
        var dy = py - cy
        repeat(steps) {
            val t = dx
            dx = -dy
            dy = t
        }
        buffer.vertex((cx + dx).toDouble(), (cy + dy).toDouble(), z.toDouble()).texture(u.toDouble(), v.toDouble())
            .nextVertex()
    }

    @JvmStatic
    fun draw() {
        if (!drawing) return

        buffer.end()
        uploader.end(buffer)
        drawing = false
    }
}
*///?}
