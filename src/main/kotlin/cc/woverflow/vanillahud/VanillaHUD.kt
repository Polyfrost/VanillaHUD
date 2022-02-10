package cc.woverflow.vanillahud

import cc.woverflow.wcore.utils.Updater
import cc.woverflow.wcore.utils.command
import cc.woverflow.vanillahud.config.VanillaHUDConfig
import gg.essential.api.EssentialAPI
import net.minecraft.client.Minecraft
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
