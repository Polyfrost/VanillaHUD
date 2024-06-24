package org.polyfrost.vanillahud;

import Apec.Components.Gui.GuiIngame.ApecGuiIngameForge;
import at.hannibal2.skyhanni.SkyHanniMod;
import at.hannibal2.skyhanni.features.misc.compacttablist.TabListReader;
import at.hannibal2.skyhanni.utils.LorenzUtils;
import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.utils.Notifications;
import club.sk1er.patcher.config.OldPatcherConfig;
import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.features.tablist.TabListParser;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Loader;
import org.polyfrost.vanillahud.config.ModConfig;
import org.polyfrost.vanillahud.hud.*;
import org.polyfrost.vanillahud.utils.TabListManager;

@net.minecraftforge.fml.common.Mod(modid = VanillaHUD.MODID, name = VanillaHUD.NAME, version = VanillaHUD.VERSION)
public class VanillaHUD {
    public static final String MODID = "@ID@";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VER@";

    public static ModConfig modConfig;
    private static boolean apec = false;
    public static boolean isPatcher = false;
    public static boolean isHytils = false;
    private static boolean isSBA = false;
    private static boolean isSkyHanni = false;
    private static boolean skyHanniField = false;

    @net.minecraftforge.fml.common.Mod.EventHandler
    public void onFMLInitialization(net.minecraftforge.fml.common.event.FMLInitializationEvent event) {
        modConfig = new ModConfig();
        TabListManager.asyncUpdateList();
        EventManager.INSTANCE.register(this);
    }

    @net.minecraftforge.fml.common.Mod.EventHandler
    public void onPostInit(net.minecraftforge.fml.common.event.FMLPostInitializationEvent event) {
        if (Loader.isModLoaded("bossbar_customizer")) {
            Notifications.INSTANCE.send("VanillaHUD", "Bossbar Customizer has been replaced by VanillaHUD and thus can be removed (they will also not work with each other).");
        }

        if (Loader.isModLoaded("sidebarmod")) {
            Notifications.INSTANCE.send("VanillaHUD", "Sidebar Mod Revamp has been replaced by VanillaHUD and thus can be removed (they will also not work with each other).");
        }
        apec = Loader.isModLoaded("apec");
        isPatcher = Loader.isModLoaded("patcher");
        isHytils = Loader.isModLoaded("hytils-reborn");
        isSBA = Loader.isModLoaded("skyblockaddons") || Loader.isModLoaded("sbaunofficial");
        isSkyHanni = Loader.isModLoaded("skyhanni");
        if (isSkyHanni) {
            try {
                // make sure the classes are loaded
                Class.forName("at.hannibal2.skyhanni.config.features.gui.GUIConfig", false, getClass().getClassLoader());
                Class.forName("at.hannibal2.skyhanni.config.features.misc.compacttablist.CompactTabListConfig", false, getClass().getClassLoader());
                Class.forName("at.hannibal2.skyhanni.config.Features", false, getClass().getClassLoader());
                Class.forName("at.hannibal2.skyhanni.deps.moulconfig.observer.Property", false, getClass().getClassLoader());
                Class.forName("at.hannibal2.skyhanni.deps.moulconfig.observer.GetSetter", false, getClass().getClassLoader());
                Class.forName("at.hannibal2.skyhanni.features.misc.compacttablist.RenderColumn", false, getClass().getClassLoader());
                Class.forName("at.hannibal2.skyhanni.features.misc.compacttablist.TabListReader", false, getClass().getClassLoader());
                Class.forName("at.hannibal2.skyhanni.utils.LorenzUtils", false, getClass().getClassLoader());
                Class<?> clazz = Class.forName("at.hannibal2.skyhanni.SkyHanniMod", true, getClass().getClassLoader());
                try {
                    clazz.getDeclaredField("feature");
                    skyHanniField = true;
                } catch (NoSuchFieldException e) {
                    skyHanniField = false;
                }
            } catch (ClassNotFoundException e) {
                isSkyHanni = false;
            }
        }

        if (!ModConfig.doneDebugMigration) {
            if (!ModConfig.actionBar.hud.showInDebug) {
                ModConfig.actionBar.hud.showInDebug = true;
                ModConfig.actionBar.save();
            }
            if (!Air.hud.showInDebug) {
                ModConfig.air.hud.showInDebug = true;
                ModConfig.air.save();
            }
            if (!Armor.hud.showInDebug) {
                ModConfig.armor.hud.showInDebug = true;
                ModConfig.armor.save();
            }
            if (!BossBar.hud.showInDebug) {
                BossBar.hud.showInDebug = true;
                ModConfig.bossBar.save();
            }
            if (!Experience.hud.showInDebug) {
                Experience.hud.showInDebug = true;
                ModConfig.experience.save();
            }
            if (!Health.hud.showInDebug) {
                Health.hud.showInDebug = true;
                ModConfig.health.save();
            }
            if (!Hotbar.hud.showInDebug) {
                Hotbar.hud.showInDebug = true;
                ModConfig.hotBar.save();
            }
            if (!Hunger.hud.showInDebug) {
                Hunger.hud.showInDebug = true;
                ModConfig.hunger.save();
            }
            if (!Hunger.mountHud.showInDebug) {
                Hunger.mountHud.showInDebug = true;
                ModConfig.hunger.save();
            }
            if (!ModConfig.itemTooltip.hud.showInDebug) {
                ModConfig.itemTooltip.hud.showInDebug = true;
                ModConfig.itemTooltip.save();
            }
            if (!ModConfig.scoreboard.hud.showInDebug) {
                ModConfig.scoreboard.hud.showInDebug = true;
                ModConfig.scoreboard.save();
            }
            if (!TabList.hud.showInDebug) {
                TabList.hud.showInDebug = true;
                ModConfig.tab.save();
            }
            if (TabList.TabHud.selfAtTop) {
                TabList.TabHud.selfAtTop = false;
                ModConfig.tab.save();
            }
            if (!ModConfig.title.titleHUD.showInDebug) {
                ModConfig.title.titleHUD.showInDebug = true;
                ModConfig.title.save();
            }
            if (!ModConfig.title.subtitleHUD.showInDebug) {
                ModConfig.title.subtitleHUD.showInDebug = true;
                ModConfig.title.save();
            }
            ModConfig.doneDebugMigration = true;
            modConfig.save();
        }

        if (!TabList.TabHud.updatedHeight) {
            TabList.TabHud.updatedHeight = true;
            if (TabList.hud.position.getY() == 10) {
                TabList.hud.position.setY(TabList.hud.position.getY() + 10);
                ModConfig.tab.save();
            }
        }

        if (isPatcher) {
            try {
                if (ModConfig.hasMigratedPatcher) return;
                Class.forName("club.sk1er.patcher.config.OldPatcherConfig");
                boolean saveTitle = false;
                boolean saveTab = false;
                boolean saveActionBar = false;

                if (OldPatcherConfig.disableTitles) {
                    ModConfig.title.enabled = false;
                    saveTitle = true;
                }
                if (OldPatcherConfig.titleScale != 1.0F) {
                    ModConfig.title.titleHUD.setScale(ModConfig.title.titleHUD.getScale() * OldPatcherConfig.titleScale, true);
                    ModConfig.title.subtitleHUD.setScale(ModConfig.title.subtitleHUD.getScale() * OldPatcherConfig.titleScale, true);
                    saveTitle = true;
                }
                if (OldPatcherConfig.titleOpacity != 1.0F) {
                    ModConfig.title.titleHUD.getColor().setAlpha((int) (OldPatcherConfig.titleOpacity * 255));
                    ModConfig.title.subtitleHUD.getColor().setAlpha((int) (OldPatcherConfig.titleOpacity * 255));
                    saveTitle = true;
                }

                if (OldPatcherConfig.toggleTab) {
                    TabList.TabHud.displayMode = true;
                    saveTab = true;
                }
                if (OldPatcherConfig.tabOpacity != 1.0F) {
                    TabList.hud.getBackgroundColor().setAlpha((int) (TabList.hud.getBackgroundColor().getAlpha() * OldPatcherConfig.tabOpacity));
                    TabList.TabHud.tabWidgetColor.setAlpha((int) (TabList.TabHud.tabWidgetColor.getAlpha() * OldPatcherConfig.tabOpacity));
                    saveTab = true;
                }
                if (OldPatcherConfig.tabPlayerCount != 80) {
                    TabList.TabHud.tabPlayerLimit = OldPatcherConfig.tabPlayerCount;
                    saveTab = true;
                }
                if (!OldPatcherConfig.tabHeightAllow) {
                    TabList.hud.position.setY(TabList.hud.position.getY() - 10);
                    saveTab = true;
                } else if (OldPatcherConfig.tabHeight != 10) {
                    TabList.hud.position.setY(TabList.hud.position.getY() + (OldPatcherConfig.tabHeight - 10));
                    saveTab = true;
                }

                if (OldPatcherConfig.shadowedActionbarText) {
                    ModConfig.actionBar.hud.setTextType(1);
                    saveActionBar = true;
                }
                if (OldPatcherConfig.actionbarBackground) {
                    ModConfig.actionBar.hud.setBackground(true);
                    saveActionBar = true;
                }

                if (saveTitle) {
                    ModConfig.title.save();
                }
                if (saveTab) {
                    ModConfig.tab.save();
                }
                if (saveActionBar) {
                    ModConfig.actionBar.save();
                }
                ModConfig.hasMigratedPatcher = true;
                modConfig.save();
                if (saveTitle || saveTab || saveActionBar) {
                    Notifications.INSTANCE.send("VanillaHUD", "Migrated Patcher settings replaced by VanillaHUD. Please check VanillaHUD's settings to make sure they are correct.");
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isApec() {
        return apec && Minecraft.getMinecraft().ingameGUI instanceof ApecGuiIngameForge;
    }

    public static boolean isCompactTab() {
        return (isSBA && SkyblockAddons.getInstance().getUtils().isOnSkyblock() && SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.COMPACT_TAB_LIST) && TabListParser.getRenderColumns() != null)
                || (isSkyHanni && LorenzUtils.INSTANCE.getInSkyBlock() && (skyHanniField ? SkyHanniMod.feature.gui.compactTabList.enabled.get() : SkyHanniMod.getFeature().gui.compactTabList.enabled.get()) && TabListReader.INSTANCE.getRenderColumns() != null);
    }
}
