package cc.woverflow.vanillahud

import cc.woverflow.onecore.utils.withScale
import cc.woverflow.onecore.utils.withTranslate
import cc.woverflow.vanillahud.config.VanillaHUDConfig
import cc.woverflow.vanillahud.mixin.GuiIngameAccessor
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.universal.UGraphics
import gg.essential.universal.UMinecraft
import gg.essential.universal.UResolution
import org.lwjgl.input.Keyboard
import java.awt.Color

object ActionBar {

    init {
        UIRoundedRectangle.initShaders()
    }

    fun renderActionBar(width: Int, height: Int, partialTicks: Float) {
        if (VanillaHUDConfig.actionBar) {
            val ingameGUI = UMinecraft.getMinecraft().ingameGUI as GuiIngameAccessor
            if (ingameGUI.recordPlayingUpFor > 0) {
                UMinecraft.getMinecraft().mcProfiler.startSection("overlayMessage")
                val hue = ingameGUI.recordPlayingUpFor.toFloat() - partialTicks
                var opacity = (hue * 256.0f / 20.0f).toInt()
                if (opacity > 255) opacity = 255
                if (opacity > 0) {
                    withTranslate((width / 2 + VanillaHUDConfig.actionBarX).toFloat(), (height - 68 + VanillaHUDConfig.actionBarY).toFloat(), 0.0f) {
                        withScale(VanillaHUDConfig.actionBarScale, VanillaHUDConfig.actionBarScale, 1.0F) {
                            UGraphics.enableBlend()
                            UGraphics.tryBlendFuncSeparate(0x302, 0x303, 1, 0)
                            val color = if (ingameGUI.recordIsPlaying) Color.HSBtoRGB(
                                hue / 50.0f,
                                0.7f,
                                0.6f
                            ) and 0xFFFFFF else 0xFFFFFF
                            val recordWidth = UMinecraft.getFontRenderer().getStringWidth(ingameGUI.recordPlaying)
                            if (VanillaHUDConfig.actionBarBackground) {
                                if (VanillaHUDConfig.actionBarRoundBackground) {
                                    drawRoundedRectangleExt((-recordWidth / 2) - VanillaHUDConfig.actionBarPadding, -4 - VanillaHUDConfig.actionBarPadding, recordWidth + VanillaHUDConfig.actionBarPadding * 2, UMinecraft.getFontRenderer().FONT_HEIGHT + VanillaHUDConfig.actionBarPadding * 2, VanillaHUDConfig.actionBarRadius.toFloat(), VanillaHUDConfig.actionBarBackgroundColor, opacity)
                                } else {
                                    drawRectButForActionBarExt((-recordWidth / 2) - VanillaHUDConfig.actionBarPadding, -4 - VanillaHUDConfig.actionBarPadding, recordWidth + VanillaHUDConfig.actionBarPadding * 2, UMinecraft.getFontRenderer().FONT_HEIGHT + VanillaHUDConfig.actionBarPadding * 2, VanillaHUDConfig.actionBarBackgroundColor.rgb, opacity)
                                }
                            }
                            UMinecraft.getFontRenderer().drawString(
                                ingameGUI.recordPlaying,
                                (-recordWidth / 2).toFloat(),
                                -4F,
                                color or (opacity shl 24), VanillaHUDConfig.actionBarShadow
                            )
                            UGraphics.disableBlend()
                        }
                    }
                }
                UMinecraft.getMinecraft().mcProfiler.endSection()
            }
        }
    }

    class ActionBarGui : PositionGui() {

        override fun updatePos(mouseX: Int, mouseY: Int, mouseButton: Int) {
            if (mouseButton == 0 && (UMinecraft.getMinecraft().ingameGUI as GuiIngameAccessor).recordPlayingUpFor > 0 && VanillaHUDConfig.actionBar) {
                VanillaHUDConfig.actionBarX = mouseX - (UResolution.scaledWidth / 2)
                VanillaHUDConfig.actionBarY = mouseY - (height - 68)
            }
        }

        override fun updatePosKeyPress(keyCode: Int) {
            if ((UMinecraft.getMinecraft().ingameGUI as GuiIngameAccessor).recordPlayingUpFor > 0 && VanillaHUDConfig.actionBar) {
                when (keyCode) {
                    Keyboard.KEY_UP -> VanillaHUDConfig.actionBarY -= 5
                    Keyboard.KEY_DOWN -> VanillaHUDConfig.actionBarY += 5
                    Keyboard.KEY_LEFT -> VanillaHUDConfig.actionBarX -= 5
                    Keyboard.KEY_RIGHT -> VanillaHUDConfig.actionBarX += 5
                }
            }
        }
    }
}