package org.polyfrost.vanillahud

import Apec.Components.Gui.GuiIngame.ApecGuiIngameForge
import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.features.misc.compacttablist.TabListReader
import at.hannibal2.skyhanni.utils.LorenzUtils
import codes.biscuit.skyblockaddons.SkyblockAddons
import codes.biscuit.skyblockaddons.core.Feature
import codes.biscuit.skyblockaddons.features.tablist.TabListParser
import dev.deftu.omnicore.client.OmniClient
import net.hypixel.data.type.GameType
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import org.polyfrost.oneconfig.api.hypixel.v1.HypixelUtils
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

object Compatibility {
    fun isApec() = Loader.isModLoaded("apec") && OmniClient.hud is ApecGuiIngameForge

    fun isPatcher() = Loader.isModLoaded("patcher")

    fun isHytils() = Loader.isModLoaded("hytils")

    fun isSBA() = Loader.isModLoaded("skyblockaddons") || Loader.isModLoaded("sbaunofficial")

    fun isSkyHanni() = Loader.isModLoaded("skyhanni")

    fun isCompactTab(): Boolean {
        return isSBACompactTab() || isSkyHanniCompactTab()
    }

    private fun isSBACompactTab(): Boolean {
        return isSBA() && SkyblockAddons.getInstance().utils.isOnSkyblock && SkyblockAddons.getInstance()
            .configValues.isEnabled(Feature.COMPACT_TAB_LIST) && TabListParser.getRenderColumns() != null
    }

    private fun isSkyHanniCompactTab(): Boolean {
        if (!isSkyHanni()) return false
        if (forceDisableCompactTab) {
            return GameType.SKYBLOCK == HypixelUtils.getLocation().gameType.orElse(null)
        }
        if (!LorenzUtils.INSTANCE.inSkyBlock) return false
        if (skyHanniField) {
            if (!SkyHanniMod.feature.gui.compactTabList.enabled.get()) return false
        } else {
            if (!SkyHanniMod.getFeature().gui.compactTabList.enabled.get()) return false
        }
        return TabListReader.INSTANCE.renderColumns != null
    }

    fun isSkyHanniScoreboard(): Boolean {
        if (!isSkyHanni()) return false
        if (forceDisableCompactTab) {
            return GameType.SKYBLOCK == HypixelUtils.getLocation().gameType.orElse(null)
        }
        if (!LorenzUtils.INSTANCE.inSkyBlock) return false
        return if (skyHanniField) {
            SkyHanniMod.feature.gui.customScoreboard.enabled.get()
        } else {
            SkyHanniMod.getFeature().gui.customScoreboard.enabled.get()
        }
    }

    val reflectionMap: Map<String, Pair<List<String>?, List<String>?>?> = mapOf(
        "at.hannibal2.skyhanni.config.features.gui.GUIConfig" to Pair(
            listOf("compactTabList", "customScoreboard"),
            null
        ),
        "at.hannibal2.skyhanni.deps.moulconfig.observer.Property" to null,
        "at.hannibal2.skyhanni.deps.moulconfig.observer.GetSetter" to null,
        "at.hannibal2.skyhanni.config.features.misc.compacttablist.CompactTabListConfig" to Pair(
            listOf("enabled"),
            null
        ),
        "at.hannibal2.skyhanni.config.features.gui.customscoreboard.CustomScoreboardConfig" to Pair(
            listOf("enabled"),
            null
        ),
        "at.hannibal2.skyhanni.config.Features" to Pair(
            listOf("gui"),
            null
        ),
        "at.hannibal2.skyhanni.features.misc.compacttablist.RenderColumn" to null,
        "at.hannibal2.skyhanni.features.misc.compacttablist.TabListReader" to Pair(
            null,
            listOf("getRenderColumns")
        ),
        "at.hannibal2.skyhanni.utils.LorenzUtils" to Pair(
            null,
            listOf("getInSkyBlock")
        )
    )

    private var forceDisableCompactTab = false
    private var skyHanniField = false

    init {
        doBraindeadCompatChecks()
    }

    private fun doBraindeadCompatChecks() {
        if (isSkyHanni()) {
            val clazz = Class.forName("at.hannibal2.skyhanni.SkyHanniMod", false, javaClass.getClassLoader())
            try {
                clazz.getDeclaredField("feature")
                skyHanniField = true
            } catch (_: NoSuchFieldException) {
                skyHanniField = false
                try {
                    clazz.getDeclaredMethod("getFeature")
                } catch (_: NoSuchMethodException) {
                    forceDisableCompactTab = true
                    return
                }
            }
        }
        for ((clazz, values) in reflectionMap) {
            try {
                Class.forName(clazz, false, javaClass.getClassLoader())
            } catch (_: ClassNotFoundException) {
                forceDisableCompactTab = true
                return
            }

            values?.first?.forEach { field ->
                try {
                    Class.forName(clazz, false, javaClass.getClassLoader()).getDeclaredField(field)
                } catch (_: NoSuchFieldException) {
                    forceDisableCompactTab = true
                    return
                }
            }

            values?.second?.forEach { method ->
                try {
                    Class.forName(clazz, false, javaClass.getClassLoader()).getDeclaredMethod(method)
                } catch (_: NoSuchMethodException) {
                    forceDisableCompactTab = true
                    return
                }
            }
        }
    }
}