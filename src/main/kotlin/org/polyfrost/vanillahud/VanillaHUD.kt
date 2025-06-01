package org.polyfrost.vanillahud

import Apec.Components.Gui.GuiIngame.ApecGuiIngameForge
import dev.deftu.omnicore.client.OmniClient
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import org.polyfrost.oneconfig.api.ui.v1.Notifications
import org.polyfrost.vanillahud.hud.PlayerListHud
import org.polyfrost.vanillahud.hud.bars.*
import org.polyfrost.vanillahud.utils.TabListManager

@Mod(modid = Constants.ID, name = Constants.NAME, version = Constants.VERSION)
object VanillaHUD {
    @JvmStatic
    val armor = ArmorHud()

    @JvmStatic
    val experience = ExperienceHud()

    @JvmStatic
    val health = HealthHud()

    @JvmStatic
    val hotbar = HotbarHud()

    @JvmStatic
    val hunger = HungerHud()

    @JvmStatic
    val mount = MountHealthHud()

    @JvmStatic
    val oxygen = OxygenHud()

    @JvmStatic
    val healthLinkAmount
        get() = health.healthLink

    @JvmStatic
    val mountLinkAmount
        get() = mount.mountLink

    @JvmStatic
    val playerList = PlayerListHud()

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        TabListManager.asyncUpdateList()
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        if (Loader.isModLoaded("bossbar_customizer")) {
            Notifications.enqueue(
                Notifications.Type.Warning,
                "VanillaHUD",
                "Bossbar Customizer has been replaced by VanillaHUD! (They will NOT work together)"
            )
        }

        if (Loader.isModLoaded("sidebarmod")) {
            Notifications.enqueue(
                Notifications.Type.Warning,
                "VanillaHUD",
                "Sidebar Mod has been replaced by VanillaHUD! (They will NOT work together)"
            )
        }

        Compatibility
    }
}

// FIXME: Missing SkyHanni check for 'forceDisableCompactTab'
object Compatibility {
    fun isApec() = Loader.isModLoaded("apec") && OmniClient.hud is ApecGuiIngameForge

    fun isPatcher() = Loader.isModLoaded("patcher")

    fun isHytils() = Loader.isModLoaded("hytils")

    fun isSBA() = Loader.isModLoaded("skyblockaddons") || Loader.isModLoaded("sbaunofficial")

    fun isSkyHanni() = Loader.isModLoaded("skyhanni")
}