package cc.woverflow.vanillahud

import cc.woverflow.vanillahud.config.VanillaHUDConfig
import cc.woverflow.wcore.utils.Updater
import cc.woverflow.wcore.utils.command
import gg.essential.api.EssentialAPI
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import java.io.File

@Mod(
    modid = VanillaHUD.ID,
    name = VanillaHUD.NAME,
    version = VanillaHUD.VER,
    modLanguageAdapter = "gg.essential.api.utils.KotlinAdapter"
)
object VanillaHUD {

    const val NAME = "@NAME@"
    const val VER = "@VER@"
    const val ID = "@ID@"

    val modDir = File(File(Minecraft.getMinecraft().mcDataDir, "W-OVERFLOW"), NAME)

    @Mod.EventHandler
    fun onFMLPreInitialization(event: FMLPreInitializationEvent) {
        if (!modDir.exists()) modDir.mkdirs()
        Updater.addToUpdater(event.sourceFile, NAME, ID, VER, "W-OVERFLOW/$ID")
    }

    @Mod.EventHandler
    fun onInitialization(event: FMLInitializationEvent) {
        VanillaHUDConfig.preload()
        command(ID) {
            main {
                EssentialAPI.getGuiUtil().openScreen(VanillaHUDConfig.gui())
            }
        }
    }
}

fun drawRectExt(x: Int, y: Int, width: Int, height: Int, color: Int) = Gui.drawRect(x, y, x + width, y + height, color)

fun drawRectButForActionBarExt(x: Int, y: Int, width: Int, height: Int, color: Int, minAlpha: Int) = drawRectButForActionBar(x, y, x + width, y + height, color, minAlpha)
fun drawRectButForActionBar(left: Int, top: Int, right: Int, bottom: Int, color: Int, minAlpha: Int) {
    var left = left
    var top = top
    var right = right
    var bottom = bottom
    if (left < right) {
        val i = left
        left = right
        right = i
    }

    if (top < bottom) {
        val j: Int = top
        top = bottom
        bottom = j
    }

    val alpha = (color shr 24 and 255).coerceAtMost(minAlpha).toFloat() / 255.0f
    val red = (color shr 16 and 255).toFloat() / 255.0f
    val green = (color shr 8 and 255).toFloat() / 255.0f
    val blue = (color and 255).toFloat() / 255.0f

    val tessellator = Tessellator.getInstance()
    val worldrenderer = tessellator.worldRenderer
    GlStateManager.enableBlend()
    GlStateManager.disableTexture2D()
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
    GlStateManager.color(red, green, blue, alpha)
    worldrenderer.begin(7, DefaultVertexFormats.POSITION)
    worldrenderer.pos(left.toDouble(), bottom.toDouble(), 0.0).endVertex()
    worldrenderer.pos(right.toDouble(), bottom.toDouble(), 0.0).endVertex()
    worldrenderer.pos(right.toDouble(), top.toDouble(), 0.0).endVertex()
    worldrenderer.pos(left.toDouble(), top.toDouble(), 0.0).endVertex()
    tessellator.draw()
    GlStateManager.enableTexture2D()
    GlStateManager.disableBlend()
}