package cc.woverflow.vanillahud

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.Dropdown
import cc.polyfrost.oneconfig.config.annotations.HUD
import cc.polyfrost.oneconfig.config.annotations.Switch
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import cc.polyfrost.oneconfig.hud.BasicHud
import cc.polyfrost.oneconfig.libs.universal.UGraphics
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack
import cc.polyfrost.oneconfig.libs.universal.UMinecraft
import cc.polyfrost.oneconfig.renderer.RenderManager
import net.minecraft.entity.boss.BossStatus
import kotlin.math.max

object BossBar : Config(Mod("Boss Bar", ModType.HUD), "bossbar.json") {
    @HUD(
        name = "HUD"
    )
    var hud = BossBarHud()

    class BossBarHud : BasicHud(true, 1920f / 2 - 5f, 2f) {
        @Dropdown(name = "Text Type", options = ["No Shadow", "Shadow", "Full Shadow"])
        var textType = 0

        @Switch(
            name = "Enable Text",
            category = "Bossbar"
        )
        var bossBarText = true

        @Switch(
            name = "Enable Health Bar",
            category = "Bossbar"
        )
        var bossBarBar = true
        override fun draw(matrices: UMatrixStack?, x: Float, y: Float, scale: Float, example: Boolean) {
            --BossStatus.statusBarTime
            if (bossBarBar) {
                val width = (182f * scale).toInt()
                val health = (BossStatus.healthScale * (width + 1)).toInt()
                UMinecraft.getMinecraft().ingameGUI.drawTexturedModalRect(x, y, 0, 74, width, 5)
                UMinecraft.getMinecraft().ingameGUI.drawTexturedModalRect(x, y, 0, 74, width, 5)
                if (health > 0) {
                    UMinecraft.getMinecraft().ingameGUI.drawTexturedModalRect(x, y, 0, 79, health, 5)
                }
            }

            if (bossBarText) {
                RenderManager.drawScaledString(BossStatus.bossName,
                    x,
                    y,
                    16777215, RenderManager.TextType.toType(textType), scale)
            }
        }

        override fun getWidth(scale: Float, example: Boolean): Float {
            if (UMinecraft.getMinecraft().ingameGUI == null) return 10f * scale
            return max(UMinecraft.getFontRenderer().getStringWidth(BossStatus.bossName).toFloat() * scale, 182f * scale)
        }

        override fun getHeight(scale: Float, example: Boolean): Float {
            var height = 0
            if (bossBarBar) {
                height += 15
            }
            if (bossBarText) {
                height += 10
            }
            return height.toFloat()
        }

        override fun shouldShow(): Boolean {
            if (!super.shouldShow()) return false
            if (BossStatus.bossName == null || BossStatus.statusBarTime > 0) return false
            return true
        }

    }
}