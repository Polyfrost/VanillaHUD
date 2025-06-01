package org.polyfrost.vanillahud;

import at.hannibal2.skyhanni.SkyHanniMod;
import at.hannibal2.skyhanni.features.misc.compacttablist.TabListReader;
import at.hannibal2.skyhanni.utils.LorenzUtils;
import net.hypixel.data.type.GameType;
import org.polyfrost.oneconfig.api.hypixel.v1.HypixelUtils;
import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.features.tablist.TabListParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class VanillaHUDOld {
    private static boolean isSBA = false;
    private static boolean isSkyHanni = false;
    private static boolean forceDisableCompactTab = false;
    private static boolean skyHanniField = false;

    @net.minecraftforge.fml.common.Mod.EventHandler
    public void onPostInit(net.minecraftforge.fml.common.event.FMLPostInitializationEvent event) {
        checkForSkyHanni();
    }

    private void checkForSkyHanni() {
        if (isSkyHanni) {
            try {
                // make sure the classes are loaded
                Class<?> guiConfig = Class.forName("at.hannibal2.skyhanni.config.features.gui.GUIConfig", false, getClass().getClassLoader());
                try {
                    guiConfig.getDeclaredField("compactTabList");
                    guiConfig.getDeclaredField("customScoreboard");
                } catch (NoSuchFieldException e) {
                    forceDisableCompactTab = true;
                    System.out.println("SkyHanni: compactTabList not found");
                    return;
                }
                Class<?> property = Class.forName("at.hannibal2.skyhanni.deps.moulconfig.observer.Property", false, getClass().getClassLoader());
                Class.forName("at.hannibal2.skyhanni.deps.moulconfig.observer.GetSetter", false, getClass().getClassLoader());
                Class<?> compactTabListConfig = Class.forName("at.hannibal2.skyhanni.config.features.misc.compacttablist.CompactTabListConfig", false, getClass().getClassLoader());
                Class<?> customScoreboardConfig = Class.forName("at.hannibal2.skyhanni.config.features.gui.customscoreboard.CustomScoreboardConfig", false, getClass().getClassLoader());
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
                Class<?> features = Class.forName("at.hannibal2.skyhanni.config.Features", false, getClass().getClassLoader());
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
                Class<?> renderColumn = Class.forName("at.hannibal2.skyhanni.features.misc.compacttablist.RenderColumn", false, getClass().getClassLoader());
                Class<?> tabListReader = Class.forName("at.hannibal2.skyhanni.features.misc.compacttablist.TabListReader", false, getClass().getClassLoader());
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
                Class<?> lorenzUtils = Class.forName("at.hannibal2.skyhanni.utils.LorenzUtils", false, getClass().getClassLoader());
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
                e.printStackTrace();
                forceDisableCompactTab = true;
            }
        }
    }

    public static boolean isCompactTab() {
        return isSBACompactTab() || isSkyHanniCompactTab();
    }

    private static boolean isSBACompactTab() {
        return isSBA && SkyblockAddons.getInstance().getUtils().isOnSkyblock() && SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.COMPACT_TAB_LIST) && TabListParser.getRenderColumns() != null;
    }

    private static boolean isSkyHanniCompactTab() {
        if (!isSkyHanni) return false;
        if (forceDisableCompactTab) {
            return GameType.SKYBLOCK == HypixelUtils.getLocation().getGameType().orElse(null);
        }
        if (!LorenzUtils.INSTANCE.getInSkyBlock()) return false;
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
            return GameType.SKYBLOCK == HypixelUtils.getLocation().getGameType().orElse(null);
        }
        if (!LorenzUtils.INSTANCE.getInSkyBlock()) return false;
        if (skyHanniField) {
            return SkyHanniMod.feature.gui.customScoreboard.enabled.get();
        } else {
            return SkyHanniMod.getFeature().gui.customScoreboard.enabled.get();
        }
    }
}
