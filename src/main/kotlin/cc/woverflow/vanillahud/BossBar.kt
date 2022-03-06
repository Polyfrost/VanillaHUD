package cc.woverflow.vanillahud

import cc.woverflow.vanillahud.config.VanillaHUDConfig
import gg.essential.universal.UGraphics
import gg.essential.universal.UMinecraft
import gg.essential.universal.UResolution
import net.minecraft.client.gui.Gui
import net.minecraft.entity.boss.BossStatus
import net.minecraftforge.client.GuiIngameForge
import org.lwjgl.input.Keyboard

object BossBar {
    fun renderBossBar() {
        if (BossStatus.bossName != null && BossStatus.statusBarTime > 0 && VanillaHUDConfig.bossBar) {
            --BossStatus.statusBarTime
            val y = 12 + VanillaHUDConfig.bossBarY

            UGraphics.GL.pushMatrix()
            UGraphics.GL.scale(VanillaHUDConfig.bossbarScale, VanillaHUDConfig.bossbarScale, 1.0F)
            if (VanillaHUDConfig.bossBarBar) {
                val width = 182
                val x = UResolution.scaledWidth / 2 - width / 2 + VanillaHUDConfig.bossBarX
                val health = (BossStatus.healthScale * (width + 1))
                UMinecraft.getMinecraft().ingameGUI.drawTexturedModalRect(x, y, 0, 74, width, 5)
                UMinecraft.getMinecraft().ingameGUI.drawTexturedModalRect(x, y, 0, 74, width, 5)
                if (health > 0) {
                    UMinecraft.getMinecraft().ingameGUI.drawTexturedModalRect(x, y, 0, 79, health.toInt(), 5)
                }
            }

            if (VanillaHUDConfig.bossBarText) {
                UMinecraft.getFontRenderer().drawString(
                    BossStatus.bossName,
                    ((UResolution.scaledWidth / 2 - UMinecraft.getFontRenderer()
                        .getStringWidth(BossStatus.bossName) / 2).toFloat()) + VanillaHUDConfig.bossBarX,
                    (y - 10).toFloat(),
                    16777215,
                    VanillaHUDConfig.bossBarShadow
                )
            }
            UGraphics.GL.popMatrix()
            UGraphics.color4f(1.0f, 1.0f, 1.0f, 1.0f)
            UMinecraft.getMinecraft().textureManager.bindTexture(Gui.icons)
        }
    }

    class BossBarGui : PositionGui() {
        override fun updatePos(mouseX: Int, mouseY: Int, mouseButton: Int) {
            if (mouseButton == 0 && GuiIngameForge.renderBossHealth && BossStatus.bossName != null && BossStatus.statusBarTime > 0 && VanillaHUDConfig.bossBar) {
                VanillaHUDConfig.bossBarX = mouseX - (UResolution.scaledWidth / 2)
                VanillaHUDConfig.bossBarY = mouseY - 12
            }
        }

        override fun updatePosKeyPress(keyCode: Int) {
            if (GuiIngameForge.renderBossHealth && BossStatus.bossName != null && BossStatus.statusBarTime > 0 && VanillaHUDConfig.bossBar) {
                when (keyCode) {
                    Keyboard.KEY_UP -> VanillaHUDConfig.bossBarY -= 5
                    Keyboard.KEY_DOWN -> VanillaHUDConfig.bossBarY += 5
                    Keyboard.KEY_LEFT -> VanillaHUDConfig.bossBarX -= 5
                    Keyboard.KEY_RIGHT -> VanillaHUDConfig.bossBarX += 5
                }
            }
        }
    }
}
