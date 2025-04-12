package org.polyfrost.vanillahud;

import Apec.Components.Gui.GuiIngame.ApecGuiIngameForge;
import at.hannibal2.skyhanni.SkyHanniMod;
import at.hannibal2.skyhanni.config.Features;
import at.hannibal2.skyhanni.config.features.gui.GUIConfig;
import at.hannibal2.skyhanni.config.features.gui.customscoreboard.CustomScoreboardConfig;
import at.hannibal2.skyhanni.config.features.misc.compacttablist.CompactTabListConfig;
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
import org.polyfrost.overflowanimations.config.OldAnimationsSettings;
import org.polyfrost.vanillahud.config.ModConfig;
import org.polyfrost.vanillahud.hud.*;
import org.polyfrost.vanillahud.utils.TabListManager;
import org.polyfrost.vanillahud.utils.Utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

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
    private static boolean isOAM = false;
    private static boolean forceDisableCompactTab = false;
    private static boolean skyHanniField = false;

    @net.minecraftforge.fml.common.Mod.EventHandler
    public void onFMLInitialization(net.minecraftforge.fml.common.event.FMLInitializationEvent event) {
        modConfig = new ModConfig();
        TabListManager.asyncUpdateList();
        EventManager.INSTANCE.register(this);
        EventManager.INSTANCE.register(new Utils());
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
        isOAM = Loader.isModLoaded("overflowanimations");

        checkForSkyHanni();
        doDebugMigration();
        updateHeight();
        doPatcherMigration();
    }

    private void checkForSkyHanni() {
        if (isSkyHanni) {
            try {
                // make sure the classes are loaded
                Class<?> guiConfig = Class.forName("at.hannibal2.skyhanni.config.features.gui.GUIConfig");
                try {
                    guiConfig.getDeclaredField("compactTabList");
                    guiConfig.getDeclaredField("customScoreboard");
                } catch (NoSuchFieldException e) {
                    forceDisableCompactTab = true;
                    System.out.println("SkyHanni: compactTabList not found");
                    return;
                }
                Class<?> property = Class.forName("at.hannibal2.skyhanni.deps.moulconfig.observer.Property");
                Class.forName("at.hannibal2.skyhanni.deps.moulconfig.observer.GetSetter");
                Class<?> compactTabListConfig = Class.forName("at.hannibal2.skyhanni.config.features.misc.compacttablist.CompactTabListConfig");
                Class<?> customScoreboardConfig = Class.forName("at.hannibal2.skyhanni.config.features.gui.customscoreboard.CustomScoreboardConfig");
                try {
                    Field enabledTab = compactTabListConfig.getDeclaredField("enabled");
                    if (enabledTab.getType() != property) {
                        forceDisableCompactTab = true;
                        System.out.println("SkyHanni: enabled not found");
                        return;
                    }
                    Field enabledScoreboard = customScoreboardConfig.getDeclaredField("enabled");
                    if (enabledScoreboard.getType() != property) {
                        forceDisableCompactTab = true;
                        System.out.println("SkyHanni: enabled not found");
                        return;
                    }
                } catch (NoSuchFieldException e) {
                    forceDisableCompactTab = true;
                    System.out.println("SkyHanni: enabled not found");
                    return;
                }
                Class<?> features = Class.forName("at.hannibal2.skyhanni.config.Features");
                try {
                    features.getDeclaredField("gui");
                } catch (NoSuchFieldException e) {
                    //skyHanniMisc = true;
                    forceDisableCompactTab = true;
                    System.out.println("SkyHanni: gui not found");
                    return;
                    //try {
                    //    features.getDeclaredField("misc");
                    //} catch (NoSuchFieldException e1) {
                    //    isSkyHanni = false;
                    //    return;
                    //}
                }
                Class<?> renderColumn = Class.forName("at.hannibal2.skyhanni.features.misc.compacttablist.RenderColumn");
                Class<?> tabListReader = Class.forName("at.hannibal2.skyhanni.features.misc.compacttablist.TabListReader");
                try {
                    Method method = tabListReader.getDeclaredMethod("getRenderColumns"); // this is a list
                    // get the type parameter of the list
                    java.lang.reflect.Type returnType = method.getGenericReturnType();
                    if (!(returnType instanceof java.lang.reflect.ParameterizedType)) {
                        System.out.println("SkyHanni: !(returnType instanceof java.lang.reflect.ParameterizedType)");
                        forceDisableCompactTab = true;
                        return;
                    }
                    java.lang.reflect.ParameterizedType parameterizedType = (java.lang.reflect.ParameterizedType) returnType;
                    java.lang.reflect.Type[] typeParameters = parameterizedType.getActualTypeArguments();
                    if (typeParameters.length != 1) {
                        System.out.println("SkyHanni: typeParameters.length != 1");
                        forceDisableCompactTab = true;
                        return;
                    } else {
                        Type renderColumnParameter = typeParameters[0];
                        if (renderColumnParameter == null || !renderColumn.getName().equals(renderColumnParameter.getTypeName())) {
                            System.out.println("SkyHanni: renderColumnParameter == null || !renderColumn.equals(renderColumnParameter.getGenericDeclaration())");
                            forceDisableCompactTab = true;
                            return;
                        }
                    }
                } catch (NoSuchMethodException e) {
                    forceDisableCompactTab = true;
                    System.out.println("SkyHanni: getRenderColumns not found");
                    return;
                }
                Class<?> lorenzUtils = Class.forName("at.hannibal2.skyhanni.utils.LorenzUtils");
                try {
                    lorenzUtils.getDeclaredMethod("getInSkyBlock");
                } catch (NoSuchMethodException e) {
                    forceDisableCompactTab = true;
                    System.out.println("SkyHanni: getInSkyBlock not found");
                    return;
                }
                Class<?> clazz = Class.forName("at.hannibal2.skyhanni.SkyHanniMod", true, getClass().getClassLoader());
                try {
                    clazz.getDeclaredField("feature");
                    skyHanniField = true;
                } catch (NoSuchFieldException e) {
                    skyHanniField = false;
                    try {
                        clazz.getDeclaredMethod("getFeature");
                    } catch (NoSuchMethodException e1) {
                        forceDisableCompactTab = true;
                        System.out.println("SkyHanni: getFeature not found");
                        return;
                    }
                }
            } catch (ClassNotFoundException e) {
                System.out.println("SkyHanni: class not found");
                forceDisableCompactTab = true;
                e.printStackTrace();
            }
        }
    }

    private void doDebugMigration() {
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
    }

    private void updateHeight() {
        if (!TabList.TabHud.updatedHeight) {
            TabList.TabHud.updatedHeight = true;
            if (TabList.hud.position.getY() == 10) {
                TabList.hud.position.setY(TabList.hud.position.getY() + 10);
                ModConfig.tab.save();
            }
        }
    }

    private void doPatcherMigration() {
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
        return isSBACompactTab() || isSkyHanniCompactTab();
    }

    public static boolean isForceDisableCompactTab() {
        return isCompactTab();
    }

    private static boolean isSBACompactTab() {
        return isSBA && SkyblockAddons.getInstance().getUtils().isOnSkyblock() && SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.COMPACT_TAB_LIST) && TabListParser.getRenderColumns() != null;
    }

    private static boolean isSkyHanniCompactTab() {
        if (!isSkyHanni) return false;
        if (forceDisableCompactTab) {
            return Utils.inSkyblock;
        }
        if (!LorenzUtils.INSTANCE.getInSkyBlock()) return false;
        try {
            if (Boolean.FALSE.equals(isSkyHanniCompactTabList())) return false;
        }
        catch (Throwable ignored) {
            //ignored, simply fall through and try the methods below
        }
        if (skyHanniField) {
            if (!SkyHanniMod.feature.gui.compactTabList.enabled.get()) return false;
        } else {
            if (!SkyHanniMod.getFeature().gui.compactTabList.enabled.get()) return false;
        }
        return TabListReader.INSTANCE.getRenderColumns() != null;
    }

    public static boolean isSkyHanniScoreboard() {
        if (!isSkyHanni) return false;
        if (forceDisableCompactTab) {
            return Utils.inSkyblock;
        }
        if (!LorenzUtils.INSTANCE.getInSkyBlock()) return false;
        try {
            if(Boolean.FALSE.equals(isSkyHanniCustomScoreboard())) return false;
        }
        catch (Throwable ignored) {
            //ignored, simply fall through and try the methods below
        }
        if (skyHanniField) {
            return SkyHanniMod.feature.gui.customScoreboard.enabled.get();
        } else {
            return SkyHanniMod.getFeature().gui.customScoreboard.enabled.get();
        }
    }

    private static Object getSkyHanniGuiFeature() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        Features features = SkyHanniMod.feature;

        MethodHandle getGuiHandle = lookup.findGetter(Features.class, "gui", Object.class);

        return getGuiHandle.invoke(features);
    }

    /**
     * This method has 3 possible return values: true, false, null
     * True = Compact Tab List is enabled
     * False = Compact Tab List is disabled
     * Null = Couldn't get the config through method handles
     *
     * @return If the Compact Tab List is enabled in the SkyHanni config
     */
    private static Boolean isSkyHanniCompactTabList() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        Object gui = getSkyHanniGuiFeature();

        MethodHandle getCompactTabListHandle = lookup.findGetter(GUIConfig.class, "compactTabList", CompactTabListConfig.class);

        Object compactTabListObj = getCompactTabListHandle.invoke(gui);

        if(compactTabListObj instanceof CompactTabListConfig) {
            return ((CompactTabListConfig) compactTabListObj).enabled.get();
        }

        // If the method returns an invalid class, simply return null, the calling method should ignore this
        return null;
    }

    /**
     * This method has 3 possible return values: true, false, null
     * True = Custom Scoreboard is enabled
     * False = Custom Scoreboard is disabled
     * Null = Couldn't get the config through method handles
     *
     * @return If the Custom Scoreboard is enabled in the SkyHanni config
     */
    private static Boolean isSkyHanniCustomScoreboard() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        Object gui = getSkyHanniGuiFeature();

        MethodHandle getCustomScoreboard = lookup.findGetter(GUIConfig.class, "customScoreboard", CustomScoreboardConfig.class);

        Object customScoreboardObj = getCustomScoreboard.invoke(gui);

        if(customScoreboardObj instanceof CustomScoreboardConfig) {
            return  ((CustomScoreboardConfig) customScoreboardObj).enabled.get();
        }

        // If the method returns an invalid class, simply return null, the calling method should ignore this
        return null;
    }

    public static boolean isLegacyTablist() {
        return isOAM && OldAnimationsSettings.INSTANCE.enabled && OldAnimationsSettings.INSTANCE.tabMode == 0;
    }
}
