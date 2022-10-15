package cc.woverflow.vanillahud

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.Dropdown
import cc.polyfrost.oneconfig.config.annotations.Exclude
import cc.polyfrost.oneconfig.config.annotations.HUD
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import cc.polyfrost.oneconfig.hud.BasicHud
import cc.polyfrost.oneconfig.libs.universal.UGraphics
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack
import cc.polyfrost.oneconfig.libs.universal.UMinecraft
import cc.polyfrost.oneconfig.renderer.RenderManager
import cc.polyfrost.oneconfig.utils.dsl.*
import cc.woverflow.vanillahud.mixin.GuiIngameAccessor
import cc.woverflow.vanillahud.mixin.MinecraftAccessor
import java.awt.Color

object ActionBar : Config(Mod("Action Bar", ModType.HUD), "actionbar.json") {

    @HUD(
        name = "HUD"
    )
    var hud = ActionBarHud()

    class ActionBarHud : BasicHud(true, 1920f / 2 - 5f,
        1080f - 72 - 4.5f
    ) {

        @Dropdown(name = "Text Type", options = ["No Shadow", "Shadow", "Full Shadow"])
        var textType = 0

        @Exclude
        var hue = 0f

        @Exclude
        var opacity = 0

        @Exclude
        var recordWidth = 0f

        override fun draw(matrices: UMatrixStack?, x: Float, y: Float, scale: Float, example: Boolean) {
            val ingameGUI = UMinecraft.getMinecraft().ingameGUI as GuiIngameAccessor
            UGraphics.GL.pushMatrix()
            UGraphics.enableBlend()
            UGraphics.tryBlendFuncSeparate(0x302, 0x303, 1, 0)
            val color = if (ingameGUI.recordIsPlaying) Color.HSBtoRGB(
                hue / 50.0f,
                0.7f,
                0.6f
            ) and 0xFFFFFF else 0xFFFFFF
            RenderManager.drawScaledString(ingameGUI.recordPlaying,
                x,
                y,
                color or (opacity shl 24), RenderManager.TextType.toType(textType), scale)
            UGraphics.disableBlend()
            UGraphics.GL.popMatrix()
        }

        override fun drawBackground(x: Float, y: Float, width: Float, height: Float, scale: Float) {
            nanoVG(true) {
                val bgColor = bgColor.rgb.setAlpha(bgColor.alpha.coerceAtMost(opacity))
                val borderColor = borderColor.rgb.setAlpha(borderColor.alpha.coerceAtMost(opacity))
                if (rounded) {
                    drawRoundedRect(
                        x,
                        y,
                        width,
                        height,
                        cornerRadius * scale,
                        bgColor,
                    )
                    if (border) drawHollowRoundedRect(
                        x - borderSize * scale,
                        y - borderSize * scale,
                        width + borderSize * scale,
                        height + borderSize * scale,
                        cornerRadius * scale,
                        borderColor,
                        borderSize * scale
                    )
                } else {
                    drawRect(x, y, width, height, bgColor)
                    if (border) drawHollowRoundedRect(
                        x - borderSize * scale,
                        y - borderSize * scale,
                        width + borderSize * scale,
                        height + borderSize * scale,
                        0f,
                        borderColor,
                        borderSize * scale
                    )
                }
            }
        }

        override fun shouldShow(): Boolean {
            if (!super.shouldShow()) return false
            val ingameGUI = UMinecraft.getMinecraft().ingameGUI as GuiIngameAccessor
            if (ingameGUI.recordPlayingUpFor <= 0) return false
            hue = ingameGUI.recordPlayingUpFor.toFloat() - (mc as MinecraftAccessor).timer.renderPartialTicks
            opacity = (hue * 256.0f / 20.0f).toInt()
            if (opacity > 255) opacity = 255
            return opacity > 0
        }

        override fun getWidth(scale: Float, example: Boolean): Float {
            if (UMinecraft.getMinecraft().ingameGUI == null) return 10f * scale
            val ingameGUI = UMinecraft.getMinecraft().ingameGUI as GuiIngameAccessor
            recordWidth = UMinecraft.getFontRenderer().getStringWidth(ingameGUI.recordPlaying).toFloat()
            return recordWidth * scale
        }

        override fun getHeight(scale: Float, example: Boolean) = 9F * scale

    }
}