package org.polyfrost.vanillahud.compat

//? if = 1.8.9 {
/*import net.minecraft.client.Minecraft
import net.minecraft.client.render.Window
import net.minecraft.client.render.platform.GlStateManager
import org.lwjgl.opengl.GL11

/**
 * Stands in for the draw context the HUD render path hands around on the newer versions.
 *
 * 1.8.9 draws the HUD straight onto the fixed function pipeline, so there is no object to
 * thread through the mixins: the matrix operations go to [GlStateManager], the element bounds
 * come from a [Window] measured off the current framebuffer, and scissoring is a raw
 * `glScissor` call in physical pixels.
 */
object LegacyDrawContext {
    private val mc: Minecraft get() = Minecraft.getInstance()

    private fun window(): Window = Window(mc)

    fun guiWidth(): Int = window().scaledWidth.toInt()

    fun guiHeight(): Int = window().scaledHeight.toInt()

    fun pose(): LegacyPose = LegacyPose

    fun enableScissor(x0: Int, y0: Int, x1: Int, y1: Int) {
        val window = window()
        val factor = window.scale.coerceAtLeast(1)
        val height = window.scaledHeight.toInt()
        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        // glScissor works in physical pixels measured from the bottom left
        GL11.glScissor(
            x0 * factor,
            (height - y1) * factor,
            ((x1 - x0) * factor).coerceAtLeast(0),
            ((y1 - y0) * factor).coerceAtLeast(0),
        )
    }

    fun disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST)
    }
}

/**
 * The subset of the newer `PoseStack` / `Matrix3x2fStack` surface the HUD transform needs,
 * backed by the legacy GL matrix stack.
 */
object LegacyPose {
    fun pushMatrix() = GlStateManager.pushMatrix()

    fun popMatrix() = GlStateManager.popMatrix()

    fun translate(x: Float, y: Float) = GlStateManager.translatef(x, y, 0f)

    fun scale(x: Float, y: Float) = GlStateManager.scalef(x, y, 1f)

    /** [theta] is clockwise radians about the z axis, matching `Matrix3x2fStack.rotate` */
    fun rotate(theta: Float) = GlStateManager.rotatef(Math.toDegrees(theta.toDouble()).toFloat(), 0f, 0f, 1f)
}

/** lets the shared source keep one spelling for the draw context it is handed */
internal typealias GuiGraphicsExtractor = LegacyDrawContext
*///?}
